package com.tom.cpm.client;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IGui.CtxStack;
import com.tom.cpl.gui.KeyCodes;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.NativeGuiComponents;
import com.tom.cpl.gui.NativeGuiComponents.NativeConstructor;
import com.tom.cpl.gui.UIColors;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.TextField.ITextField;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.text.IText;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.shared.gui.panel.Panel3d;

public class Gui extends Screen {
	private static final KeyCodes CODES = new GLFWKeyCodes();
	private static final NativeGuiComponents nativeComponents = new NativeGuiComponents();
	private Impl impl;
	private Frame gui;
	private Screen parent;
	private CtxStack stack;
	private UIColors colors;
	private Consumer<Runnable> closeListener;
	private int keyModif;
	public MatrixStack matrixStack;
	private int vanillaScale = -1;

	static {
		nativeComponents.register(TextField.class, local(Impl::createTextField));
		nativeComponents.register(FileChooserPopup.class, TinyFDChooser::new);
		nativeComponents.register(Panel3d.class, Panel3dImpl::new);
	}

	public Gui(Function<IGui, Frame> creator, Screen parent) {
		super(Text.literal(""));
		this.colors = new UIColors();
		this.parent = parent;
		new Impl(creator);
	}

	private static <G extends Supplier<IGui>, N> NativeConstructor<G, N> local(Function<Impl, N> fac) {
		return f -> fac.apply((Impl) f.get());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack, 0);
		try {
			this.matrixStack = matrixStack;
			matrixStack.push();
			matrixStack.translate(0, 0, 800);
			stack = new CtxStack(width, height);
			RenderSystem.runAsFancy(() -> gui.draw(mouseX, mouseY, partialTicks));
		} catch (Throwable e) {
			impl.onGuiException("Error drawing gui", e, true);
		} finally {
			RenderSystem.disableScissor();
			String modVer = FabricLoader.getInstance().getModContainer("cpm").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("?UNKNOWN?");
			String s = "Minecraft " + SharedConstants.getGameVersion().getName() + " (" + this.client.getGameVersion() + "/" + ClientBrandRetriever.getClientModName() + ") " + modVer;
			textRenderer.draw(matrixStack, s, width - textRenderer.getWidth(s) - 4, 2, 0xff000000);
			s = this.client.fpsDebugString;
			textRenderer.draw(matrixStack, s, width - textRenderer.getWidth(s) - 4, 11, 0xff000000);
			this.matrixStack = null;
			matrixStack.pop();
		}
		if(client.player != null && gui.enableChat()) {
			matrixStack.push();
			matrixStack.translate(0.0D, client.getWindow().getScaledHeight() - 48, 800);
			client.inGameHud.getChatHud().render(matrixStack, client.inGameHud.getTicks());
			matrixStack.pop();
		}
	}

	@Override
	public void removed() {
		this.client.keyboard.setRepeatEvents(false);
		if(vanillaScale >= 0 && vanillaScale != client.options.getGuiScale().getValue()) {
			client.options.getGuiScale().setValue(vanillaScale);
			vanillaScale = -999;
			client.onResolutionChanged();
		}
	}

	@Override
	public void close() {
		Screen p = parent;
		parent = null;
		client.setScreen(p);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	protected void init() {
		this.client.keyboard.setRepeatEvents(true);
		try {
			gui.init(width, height);
		} catch (Throwable e) {
			impl.onGuiException("Error in init gui", e, true);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		try {
			this.keyModif = modifiers;
			KeyboardEvent evt = new KeyboardEvent(keyCode, scanCode, (char) -1, GLFW.glfwGetKeyName(keyCode, scanCode));
			gui.keyPressed(evt);
			if(!evt.isConsumed()) {
				if(client.player != null && client.options.chatKey.matchesKey(keyCode, scanCode) && client.options.getChatVisibility().getValue() != ChatVisibility.HIDDEN) {
					RenderSystem.recordRenderCall(() -> {
						int scale = vanillaScale;
						vanillaScale = -1;
						client.setScreen(new Overlay());
						vanillaScale = scale;
					});
					return true;
				}
			}
			return true;
		} catch (Throwable e) {
			impl.onGuiException("Error processing key event", e, false);
			return true;
		}
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		try {
			this.keyModif = modifiers;
			KeyboardEvent evt = new KeyboardEvent(-1, -1, codePoint, null);
			gui.keyPressed(evt);
			return evt.isConsumed();
		} catch (Throwable e) {
			impl.onGuiException("Error processing key event", e, false);
			return true;
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		try {
			MouseEvent evt = new MouseEvent((int) mouseX, (int) mouseY, button);
			gui.mouseClick(evt);
			return evt.isConsumed();
		} catch (Throwable e) {
			impl.onGuiException("Error processing mouse event", e, false);
			return true;
		}
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		try {
			MouseEvent evt = new MouseEvent((int) mouseX, (int) mouseY, button);
			gui.mouseDrag(evt);
			return evt.isConsumed();
		} catch (Throwable e) {
			impl.onGuiException("Error processing mouse event", e, false);
			return true;
		}
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		try {
			MouseEvent evt = new MouseEvent((int) mouseX, (int) mouseY, button);
			gui.mouseRelease(evt);
			return evt.isConsumed();
		} catch (Throwable e) {
			impl.onGuiException("Error processing mouse event", e, false);
			return true;
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if(delta != 0) {
			try {
				MouseEvent evt = new MouseEvent((int) mouseX, (int) mouseY, (int) delta);
				gui.mouseWheel(evt);
				return evt.isConsumed();
			} catch (Throwable e) {
				impl.onGuiException("Error processing mouse event", e, false);
				return true;
			}
		}
		return false;
	}

	@Override
	public void filesDragged(List<Path> filesIn) {
		try {
			gui.filesDropped(filesIn.stream().map(Path::toFile).filter(File::exists).collect(Collectors.toList()));
		} catch (Throwable e) {
			impl.onGuiException("Error processing mouse event", e, false);
		}
	}

	public class Overlay extends ChatScreen {

		public Overlay() {
			super("");
		}

		@Override
		public void render(MatrixStack st, int mouseX, int mouseY, float partialTicks) {
			Gui.this.render(st, Integer.MIN_VALUE, Integer.MIN_VALUE, partialTicks);
			super.render(st, mouseX, mouseY, partialTicks);
		}

		public Screen getGui() {
			return Gui.this;
		}
	}

	private class Impl implements IGui {

		public Impl(Function<IGui, Frame> creator) {
			impl = this;
			gui = creator.apply(this);
		}

		@Override
		public void drawBox(int x, int y, int w, int h, int color) {
			x += getOffset().x;
			y += getOffset().y;
			fill(matrixStack, x, y, x+w, y+h, color);
		}

		@Override
		public void drawText(int x, int y, String text, int color) {
			x += getOffset().x;
			y += getOffset().y;
			matrixStack.push();
			matrixStack.translate(0, 0, 50);
			textRenderer.draw(matrixStack, text, x, y, color);
			matrixStack.pop();
		}

		@Override
		public void displayError(String e) {
			Screen p = parent;
			parent = null;
			MinecraftClient.getInstance().setScreen(new CrashScreen(e, p));
		}

		private static class CrashScreen extends Screen {
			private String error;
			private Screen parent;

			public CrashScreen(String error, Screen p) {
				super(Text.literal("Error"));
				this.error = error;
				parent = p;
			}

			@Override
			public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
				this.renderBackground(matrixStack);
				String[] txt = I18n.translate("error.cpm.crash", error).split("\\\\");
				for (int i = 0; i < txt.length; i++) {
					drawCenteredText(matrixStack, textRenderer, txt[i], this.width / 2, 15 + i * 10, 16777215);
				}
				super.render(matrixStack, mouseX, mouseY, partialTicks);
			}

			@Override
			protected void init() {
				super.init();
				this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, 140, 200, 20, ScreenTexts.BACK, (p_213034_1_) -> {
					this.client.setScreen((Screen)null);
				}));
			}

			@Override
			public void close() {
				if(parent != null) {
					Screen p = parent;
					parent = null;
					client.setScreen(p);
				}
			}
		}

		@Override
		public void drawTexture(int x, int y, int w, int h, int u, int v, String texture) {
			x += getOffset().x;
			y += getOffset().y;
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, new Identifier("cpm", "textures/gui/" + texture + ".png"));
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			Gui.this.drawTexture(matrixStack, x, y, u, v, w, h);
		}

		@Override
		public void drawTexture(int x, int y, int w, int h, int u, int v, String texture, int color) {
			x += getOffset().x;
			y += getOffset().y;
			float a = (color >> 24 & 255) / 255.0F;
			float r = (color >> 16 & 255) / 255.0F;
			float g = (color >> 8 & 255) / 255.0F;
			float b = (color & 255) / 255.0F;
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(r, g, b, a);
			RenderSystem.setShaderTexture(0, new Identifier("cpm", "textures/gui/" + texture + ".png"));
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			Gui.this.drawTexture(matrixStack, x, y, u, v, w, h);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}

		@Override
		public void drawTexture(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
			x += getOffset().x;
			y += getOffset().y;
			RenderSystem.setShaderTexture(0, DynTexture.getBoundLoc());
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			Matrix4f mat = matrixStack.peek().getPositionMatrix();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			float bo = getZOffset();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			bufferbuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bufferbuilder.vertex(mat, x, y + height, bo).texture(u1, v2).next();
			bufferbuilder.vertex(mat, x + width, y + height, bo).texture(u2, v2).next();
			bufferbuilder.vertex(mat, x + width, y, bo).texture(u2, v1).next();
			bufferbuilder.vertex(mat, x, y, bo).texture(u1, v1).next();
			BufferRenderer.drawWithShader(bufferbuilder.end());
			RenderSystem.disableBlend();
		}

		@Override
		public String i18nFormat(String key, Object... obj) {
			return I18n.translate(key, obj);
		}

		@Override
		public void setupCut() {
			int dw = client.getWindow().getFramebufferWidth();
			int dh = client.getWindow().getFramebufferHeight();
			float multiplierX = dw / (float)width;
			float multiplierY = dh / (float)height;
			Box box = getContext().cutBox;
			RenderSystem.enableScissor((int) (box.x * multiplierX), dh - (int) ((box.y + box.h) * multiplierY),
					(int) (box.w * multiplierX), (int) (box.h * multiplierY));
		}

		@Override
		public int textWidth(String text) {
			return textRenderer.getWidth(text);
		}

		private ITextField createTextField() {
			return new TxtField();
		}

		private class TxtField implements ITextField, Consumer<String> {
			private TextFieldWidget field;
			private Runnable eventListener;
			private Vec2i currentOff = new Vec2i(0, 0);
			private Box bounds = new Box(0, 0, 0, 0);
			private boolean settingText, updateField;
			public TxtField() {
				this.field = new TextFieldWidget(textRenderer, 0, 0, 0, 0, Text.translatable("narrator.cpm.field"));
				this.field.setMaxLength(1024*1024);
				this.field.setDrawsBackground(false);
				this.field.setVisible(true);
				this.field.setEditableColor(16777215);
				this.field.setChangedListener(this);
			}

			@Override
			public void draw(int mouseX, int mouseY, float partialTicks, Box bounds) {
				Vec2i off = getOffset();
				field.x = bounds.x + off.x + 4;
				field.y = bounds.y + off.y + 6;
				currentOff.x = off.x;
				currentOff.y = off.y;
				this.bounds.x = bounds.x;
				this.bounds.y = bounds.y;
				this.bounds.w = bounds.w;
				this.bounds.h = bounds.h;
				field.setWidth(bounds.w - 5);
				field.height = bounds.h - 12;
				if(updateField) {
					settingText = true;
					field.setCursorToEnd();
					updateField = false;
					settingText = false;
				}
				field.render(matrixStack, mouseX, mouseY, partialTicks);
			}

			@Override
			public void keyPressed(KeyboardEvent evt) {
				if(evt.isConsumed())return;
				if(evt.keyCode == -1) {
					if(field.charTyped(evt.charTyped, keyModif))
						evt.consume();
				} else {
					if(field.keyPressed(evt.keyCode, evt.scancode, keyModif) || field.isActive())
						evt.consume();
				}
			}

			@Override
			public void mouseClick(MouseEvent evt) {
				if(evt.isConsumed()) {
					field.mouseClicked(Integer.MIN_VALUE, Integer.MIN_VALUE, evt.btn);
					return;
				}
				field.x = bounds.x + currentOff.x;
				field.y = bounds.y + currentOff.y;
				field.setWidth(bounds.w);
				field.height = bounds.h;
				if(field.mouseClicked(evt.x + currentOff.x, evt.y + currentOff.y, evt.btn))
					evt.consume();
			}

			@Override
			public String getText() {
				return field.getText();
			}

			@Override
			public void setText(String txt) {
				this.settingText = true;
				field.setText(txt);
				this.settingText = false;
				this.updateField = true;
			}

			@Override
			public void setEventListener(Runnable eventListener) {
				this.eventListener = eventListener;
			}

			@Override
			public void accept(String value) {
				if(eventListener != null && !settingText)eventListener.run();
			}

			@Override
			public void setEnabled(boolean enabled) {
				field.setEditable(enabled);
			}

			@Override
			public boolean isFocused() {
				return field.isFocused();
			}

			@Override
			public void setFocused(boolean focused) {
				field.setTextFieldFocused(focused);
			}

			@Override
			public int getCursorPos() {
				return field.getCursor();
			}

			@Override
			public void setCursorPos(int pos) {
				field.setCursor(pos);
			}

			@Override
			public void setSelectionPos(int pos) {
				field.setSelectionEnd(pos);
			}

			@Override
			public int getSelectionPos() {
				return field.selectionEnd;
			}
		}

		@Override
		public UIColors getColors() {
			return colors;
		}

		@Override
		public void setCloseListener(Consumer<Runnable> listener) {
			closeListener = listener;
		}

		@Override
		public boolean isShiftDown() {
			return hasShiftDown();
		}

		@Override
		public boolean isCtrlDown() {
			return hasControlDown();
		}

		@Override
		public boolean isAltDown() {
			return hasAltDown();
		}

		@Override
		public KeyCodes getKeyCodes() {
			return CODES;
		}

		@Override
		public void drawGradientBox(int x, int y, int w, int h, int topLeft, int topRight, int bottomLeft,
				int bottomRight) {
			x += getOffset().x;
			y += getOffset().y;
			int left = x;
			int top = y;
			int right = x + w;
			int bottom = y + h;
			float atr = (topRight >> 24 & 255) / 255.0F;
			float rtr = (topRight >> 16 & 255) / 255.0F;
			float gtr = (topRight >> 8 & 255) / 255.0F;
			float btr = (topRight & 255) / 255.0F;
			float atl = (topLeft >> 24 & 255) / 255.0F;
			float rtl = (topLeft >> 16 & 255) / 255.0F;
			float gtl = (topLeft >> 8 & 255) / 255.0F;
			float btl = (topLeft & 255) / 255.0F;
			float abl = (bottomLeft >> 24 & 255) / 255.0F;
			float rbl = (bottomLeft >> 16 & 255) / 255.0F;
			float gbl = (bottomLeft >> 8 & 255) / 255.0F;
			float bbl = (bottomLeft & 255) / 255.0F;
			float abr = (bottomRight >> 24 & 255) / 255.0F;
			float rbr = (bottomRight >> 16 & 255) / 255.0F;
			float gbr = (bottomRight >> 8 & 255) / 255.0F;
			float bbr = (bottomRight & 255) / 255.0F;
			RenderSystem.disableTexture();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			Matrix4f mat = matrixStack.peek().getPositionMatrix();
			bufferbuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
			bufferbuilder.vertex(mat, right, top, getZOffset()).color(rtr, gtr, btr, atr).next();
			bufferbuilder.vertex(mat, left, top, getZOffset()).color(rtl, gtl, btl, atl).next();
			bufferbuilder.vertex(mat, left, bottom, getZOffset()).color(rbl, gbl, bbl, abl).next();
			bufferbuilder.vertex(mat, right, bottom, getZOffset()).color(rbr, gbr, bbr, abr).next();
			tessellator.draw();
			RenderSystem.disableBlend();
			RenderSystem.enableTexture();
		}

		@Override
		public NativeGuiComponents getNative() {
			return nativeComponents;
		}

		@Override
		public void setClipboardText(String text) {
			client.keyboard.setClipboard(text);
		}

		@Override
		public Frame getFrame() {
			return gui;
		}

		@Override
		public String getClipboardText() {
			return client.keyboard.getClipboard();
		}

		@Override
		public void setScale(int value) {
			if(vanillaScale == -999)return;
			if(value != client.options.getGuiScale().getValue()) {
				if(vanillaScale == -1)vanillaScale = client.options.getGuiScale().getValue();
				if(value == -1) {
					if(client.options.getGuiScale().getValue() != vanillaScale) {
						client.options.getGuiScale().setValue(vanillaScale);
						vanillaScale = -1;
						client.onResolutionChanged();
					}
				} else {
					client.options.getGuiScale().setValue(value);
					client.onResolutionChanged();
				}
			}
		}

		@Override
		public int getScale() {
			return client.options.getGuiScale().getValue();
		}

		@Override
		public int getMaxScale() {
			return client.getWindow().calculateScaleFactor(0, client.forcesUnicodeFont()) + 1;
		}

		@Override
		public CtxStack getStack() {
			return stack;
		}

		@Override
		public void close() {
			if(closeListener != null) {
				closeListener.accept(Gui.this::close);
			} else
				Gui.this.close();
		}

		@Override
		public void drawFormattedText(float x, float y, IText text, int color, float scale) {
			x += getOffset().x;
			y += getOffset().y;
			matrixStack.push();
			matrixStack.translate(x, y, 50);
			matrixStack.scale(scale, scale, scale);
			textRenderer.draw(matrixStack, text.<Text>remap(), 0, 0, color);
			matrixStack.pop();
		}

		@Override
		public int textWidthFormatted(IText text) {
			return textRenderer.getWidth(text.<Text>remap().asOrderedText());
		}

		@Override
		public void openURL0(String url) {
			Util.getOperatingSystem().open(url);
		}
	}

	@Override
	public void tick() {
		try {
			gui.tick();
		} catch (Throwable e) {
			impl.onGuiException("Error in tick gui", e, true);
		}
	}
}
