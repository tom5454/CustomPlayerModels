package com.tom.cpm.client;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyCodes;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.NativeGuiComponents;
import com.tom.cpl.gui.NativeGuiComponents.NativeConstructor;
import com.tom.cpl.gui.UIColors;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.TextField.ITextField;
import com.tom.cpl.item.Stack;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.text.IText;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.common.ItemStackHandlerImpl;
import com.tom.cpm.mixin.GuiAccessor;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.gui.panel.Panel3d;

public class GuiImpl extends Screen implements IGui {
	protected static final KeyCodes CODES = new GLFWKeyCodes();
	protected static final NativeGuiComponents nativeComponents = new NativeGuiComponents();
	protected Frame gui;
	protected Screen parent;
	protected CtxStack stack;
	protected UIColors colors;
	protected Consumer<Runnable> closeListener;
	protected int keyModif;
	protected GuiGraphics graphics;
	protected boolean noScissorTest;
	protected int vanillaScale = -1;

	static {
		nativeComponents.register(TextField.class, local(GuiImpl::createTextField));
		nativeComponents.register(FileChooserPopup.class, TinyFDChooser::new);
		nativeComponents.register(Panel3d.class, Panel3dImpl::new);
	}

	public GuiImpl(Function<IGui, Frame> creator, Screen parent) {
		super(Component.literal(""));
		this.colors = new UIColors();
		this.parent = parent;
		try {
			this.gui = creator.apply(this);
		} catch (Throwable e) {
			onGuiException("Error creating gui", e, true);
		}
		noScissorTest = isCtrlDown();
	}

	private static <G extends Supplier<IGui>, N> NativeConstructor<G, N> local(Function<GuiImpl, N> fac) {
		return f -> fac.apply((GuiImpl) f.get());
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics, mouseX, mouseY, partialTicks);
		try {
			this.graphics = graphics;
			graphics.pose().pushPose();
			stack = new CtxStack(width, height);
			float pt = minecraft.getDeltaTracker().getRealtimeDeltaTicks();
			this.gui.draw(mouseX, mouseY, pt);
		} catch (Throwable e) {
			onGuiException("Error drawing gui", e, true);
		} finally {
			if(!noScissorTest)
				RenderSystem.disableScissor();
			String modVer = MinecraftCommonAccess.get().getModVersion();
			String s = "Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(minecraft.getVersionType()) ? "" : "/" + minecraft.getVersionType()) + ") " + modVer;
			graphics.drawString(font, s, width - font.width(s) - 4, 2, 0xff000000, false);
			s = minecraft.fpsString;
			if(noScissorTest)s += " No Scissor";
			graphics.drawString(font, s, width - font.width(s) - 4, 11, 0xff000000, false);
			this.graphics = null;
			graphics.pose().popPose();
		}
		if(minecraft.player != null && gui.enableChat()) {
			graphics.pose().pushPose();
			graphics.pose().translate(0, 0, 800);
			((GuiAccessor) minecraft.gui).callRenderChat(graphics, minecraft.getDeltaTracker());
			graphics.pose().popPose();
		}
	}

	@Override
	public void removed() {
		if(vanillaScale >= 0 && vanillaScale != minecraft.options.guiScale().get()) {
			int v = vanillaScale;
			vanillaScale = -999;
			minecraft.options.guiScale().set(v);
		}
	}

	@Override
	public void onClose() {
		Screen p = parent;
		parent = null;
		minecraft.setScreen(p);
	}

	@Override
	public void drawBox(int x, int y, int w, int h, int color) {
		x += getOffset().x;
		y += getOffset().y;
		graphics.fill(RenderType.guiOverlay(), x, y, x+w, y+h, 0, color);
	}

	@Override
	public void drawBox(float xI, float yI, float w, float h, int color) {
		graphics.drawSpecial(b -> {
			float x = xI + getOffset().x;
			float y = yI + getOffset().y;

			float minX = x;
			float minY = y;
			float maxX = x+w;
			float maxY = y+h;

			if (minX < maxX) {
				float i = minX;
				minX = maxX;
				maxX = i;
			}

			if (minY < maxY) {
				float j = minY;
				minY = maxY;
				maxY = j;
			}
			Matrix4f matrix = graphics.pose().last().pose();
			float f3 = (color >> 24 & 255) / 255.0F;
			float f = (color >> 16 & 255) / 255.0F;
			float f1 = (color >> 8 & 255) / 255.0F;
			float f2 = (color & 255) / 255.0F;
			VertexConsumer bufferbuilder = b.getBuffer(RenderType.guiOverlay());
			bufferbuilder.addVertex(matrix, minX, maxY, 0.0F).setColor(f, f1, f2, f3);
			bufferbuilder.addVertex(matrix, maxX, maxY, 0.0F).setColor(f, f1, f2, f3);
			bufferbuilder.addVertex(matrix, maxX, minY, 0.0F).setColor(f, f1, f2, f3);
			bufferbuilder.addVertex(matrix, minX, minY, 0.0F).setColor(f, f1, f2, f3);
			graphics.flush();
		});
	}

	@Override
	protected void init() {
		try {
			gui.init(width, height);
		} catch (Throwable e) {
			onGuiException("Error in init gui", e, true);
		}
	}
	@Override
	public void drawText(int x, int y, String text, int color) {
		x += getOffset().x;
		y += getOffset().y;
		graphics.pose().pushPose();
		graphics.pose().translate(0, 0, 500);
		graphics.drawString(font, text, x, y, color, false);
		graphics.pose().popPose();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		try {
			this.keyModif = modifiers;
			KeyboardEvent evt = new KeyboardEvent(keyCode, scanCode, (char) -1, GLFW.glfwGetKeyName(keyCode, scanCode));
			gui.keyPressed(evt);
			if(!evt.isConsumed()) {
				if(minecraft.player != null && minecraft.options.keyChat.matches(keyCode, scanCode) && minecraft.options.chatVisibility().get() != ChatVisiblity.HIDDEN) {
					Minecraft.getInstance().schedule(() -> {
						int scale = vanillaScale;
						vanillaScale = -1;
						minecraft.setScreen(new Overlay(scale));
						vanillaScale = scale;
					});
					return true;
				}
			}
			return true;
		} catch (Throwable e) {
			onGuiException("Error processing key event", e, false);
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
			onGuiException("Error processing key event", e, false);
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
			onGuiException("Error processing mouse event", e, false);
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
			onGuiException("Error processing mouse event", e, false);
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
			onGuiException("Error processing mouse event", e, false);
			return true;
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xdelta, double ydelta) {
		if(ydelta != 0) {
			try {
				MouseEvent evt = new MouseEvent((int) mouseX, (int) mouseY, (int) ydelta);
				gui.mouseWheel(evt);
				return evt.isConsumed();
			} catch (Throwable e) {
				onGuiException("Error processing mouse event", e, false);
				return true;
			}
		}
		return false;
	}

	@Override
	public void onFilesDrop(List<Path> filesIn) {
		try {
			gui.filesDropped(filesIn.stream().map(Path::toFile).filter(File::exists).collect(Collectors.toList()));
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
		}
	}

	@Override
	public void displayError(String e) {
		Screen p = parent;
		parent = null;
		Minecraft.getInstance().setScreen(new CrashScreen(e, p));
	}

	private static class CrashScreen extends Screen {
		private String error;
		private Screen parent;

		public CrashScreen(String error, Screen p) {
			super(Component.literal("Error"));
			this.error = error;
			parent = p;
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			super.render(graphics, mouseX, mouseY, partialTicks);
			String[] txt = I18n.get("error.cpm.crash", error).split("\\\\");
			for (int i = 0; i < txt.length; i++) {
				graphics.drawCenteredString(this.font, txt[i], this.width / 2, 15 + i * 10, 16777215);
			}
		}

		@Override
		protected void init() {
			super.init();
			this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (p_213034_1_) -> {
				this.minecraft.setScreen((Screen)null);
			}).bounds(this.width / 2 - 100, 140, 200, 20).build());
		}

		@Override
		public void onClose() {
			if(parent != null) {
				Screen p = parent;
				parent = null;
				minecraft.setScreen(p);
			}
		}
	}

	@Override
	public void closeGui() {
		if(closeListener != null) {
			closeListener.accept(this::onClose);
		} else
			onClose();
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture) {
		x += getOffset().x;
		y += getOffset().y;
		graphics.blit(RenderType::guiTexturedOverlay, ResourceLocation.tryBuild("cpm", "textures/gui/" + texture + ".png"), x, y, u, v, w, h, 256, 256);
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture, int color) {
		x += getOffset().x;
		y += getOffset().y;
		graphics.blit(RenderType::guiTexturedOverlay, ResourceLocation.tryBuild("cpm", "textures/gui/" + texture + ".png"), x, y, u, v, w, h, 256, 256, color);
	}

	@Override
	public void drawTexture(int xI, int yI, int width, int height, float u1, float v1, float u2, float v2) {
		graphics.drawSpecial(b -> {
			int x = xI + getOffset().x;
			int y = yI + getOffset().y;
			ResourceLocation rl = DynTexture.getBoundLoc();
			final VertexConsumer vertexConsumer = b.getBuffer(RenderType.guiTexturedOverlay(rl));
			float bo = 0;
			Matrix4f matrix = graphics.pose().last().pose();
			vertexConsumer.addVertex(matrix, x, y + height, bo).setUv(u1, v2).setColor(-1);
			vertexConsumer.addVertex(matrix, x + width, y + height, bo).setUv(u2, v2).setColor(-1);
			vertexConsumer.addVertex(matrix, x + width, y, bo).setUv(u2, v1).setColor(-1);
			vertexConsumer.addVertex(matrix, x, y, bo).setUv(u1, v1).setColor(-1);
		});
	}

	@Override
	public String i18nFormat(String key, Object... obj) {
		return I18n.get(key, obj);
	}

	@Override
	public void setupCut() {
		if(!noScissorTest) {
			graphics.flush();
			int dw = minecraft.getWindow().getWidth();
			int dh = minecraft.getWindow().getHeight();
			float multiplierX = dw / (float)width;
			float multiplierY = dh / (float)height;
			Box box = getContext().cutBox;
			RenderSystem.enableScissor((int) (box.x * multiplierX), dh - (int) ((box.y + box.h) * multiplierY),
					(int) (box.w * multiplierX), (int) (box.h * multiplierY));
		}
	}

	@Override
	public int textWidth(String text) {
		return font.width(text);
	}

	private ITextField createTextField() {
		return new TxtField();
	}

	private class TxtField implements ITextField, Consumer<String> {
		private EditBox field;
		private Runnable eventListener;
		private Vec2i currentOff = new Vec2i(0, 0);
		private Box bounds = new Box(0, 0, 0, 0);
		private boolean settingText, updateField, enabled;
		public TxtField() {
			this.field = new EditBox(font, 0, 0, 0, 0, Component.translatable("narrator.cpm.field"));
			this.field.setMaxLength(1024*1024);
			this.field.setBordered(false);
			this.field.setVisible(true);
			this.field.setTextColor(16777215);
			this.field.setResponder(this);
			this.enabled = true;
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks, Box bounds) {
			Vec2i off = getOffset();
			field.setX(bounds.x + off.x + 4);
			field.setY(bounds.y + off.y + 6);
			currentOff.x = off.x;
			currentOff.y = off.y;
			this.bounds.x = bounds.x;
			this.bounds.y = bounds.y;
			this.bounds.w = bounds.w;
			this.bounds.h = bounds.h;
			field.setWidth(bounds.w - 5);
			Platform.setHeight(field, bounds.h - 12);
			if(updateField) {
				settingText = true;
				field.moveCursorToEnd(false);
				settingText = false;
				updateField = false;
			}
			graphics.pose().pushPose();
			graphics.pose().translate(0, 0, 400);
			field.render(graphics, mouseX, mouseY, partialTicks);
			graphics.pose().popPose();
		}

		@Override
		public void keyPressed(KeyboardEvent evt) {
			if(evt.isConsumed())return;
			if(evt.keyCode == -1) {
				if(field.charTyped(evt.charTyped, keyModif))
					evt.consume();
			} else {
				if(field.keyPressed(evt.keyCode, evt.scancode, keyModif) || field.canConsumeInput())
					evt.consume();
			}
		}

		@Override
		public void mouseClick(MouseEvent evt) {
			if(evt.isConsumed() || !enabled) {
				field.setFocused(false);
				field.setHighlightPos(field.getCursorPosition());
				return;
			}
			field.setX(bounds.x + currentOff.x);
			field.setY(bounds.y + currentOff.y);
			field.setWidth(bounds.w);
			Platform.setHeight(field, bounds.h);
			if(field.mouseClicked(evt.x + currentOff.x, evt.y + currentOff.y, evt.btn)) {
				field.setFocused(true);
				evt.consume();
			} else {
				field.setFocused(false);
				field.setHighlightPos(field.getCursorPosition());
			}
		}

		@Override
		public String getText() {
			return field.getValue();
		}

		@Override
		public void setText(String txt) {
			this.settingText = true;
			field.setValue(txt);
			this.settingText = false;
			this.updateField = true;
		}

		@Override
		public void setEventListener(Runnable eventListener) {
			this.eventListener = eventListener;
		}

		@Override
		public void accept(String value) {
			if(eventListener != null && !settingText && enabled)eventListener.run();
		}

		@Override
		public void setEnabled(boolean enabled) {
			field.setEditable(enabled);
			this.enabled = enabled;
		}

		@Override
		public boolean isFocused() {
			return field.isFocused();
		}

		@Override
		public void setFocused(boolean focused) {
			field.setFocused(focused);
		}

		@Override
		public int getCursorPos() {
			return field.getCursorPosition();
		}

		@Override
		public void setCursorPos(int pos) {
			field.setCursorPosition(pos);
		}

		@Override
		public void setSelectionPos(int pos) {
			field.setHighlightPos(pos);
		}

		@Override
		public int getSelectionPos() {
			return field.highlightPos;
		}
	}

	@Override
	public UIColors getColors() {
		return colors;
	}

	@Override
	public void setCloseListener(Consumer<Runnable> listener) {
		this.closeListener = listener;
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

	public class Overlay extends ChatScreen {
		private int vanillaScaleReset;

		public Overlay(int vanillaScaleReset) {
			super("");
			this.vanillaScaleReset = vanillaScaleReset;
		}

		@Override
		public void render(GuiGraphics gr, int mouseX, int mouseY, float partialTicks) {
			gr.pose().pushPose();
			gr.pose().translate(0, 0, -50);
			GuiImpl.this.render(gr, Integer.MIN_VALUE, Integer.MIN_VALUE, partialTicks);
			gr.pose().popPose();
			gr.pose().pushPose();
			gr.pose().translate(0, 0, 500);
			super.render(gr, mouseX, mouseY, partialTicks);
			gr.pose().popPose();
		}

		@Override
		public void removed() {
			super.removed();
			minecraft.schedule(() -> {
				minecraft.setScreen(GuiImpl.this);
				vanillaScale = vanillaScaleReset;
			});
		}
	}

	@Override
	public KeyCodes getKeyCodes() {
		return CODES;
	}

	@Override
	public void drawGradientBox(int xI, int yI, int w, int h, int topLeft, int topRight, int bottomLeft,
			int bottomRight) {
		graphics.drawSpecial(b -> {
			int x = xI + getOffset().x;
			int y = yI + getOffset().y;
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
			VertexConsumer bufferbuilder = b.getBuffer(RenderType.guiOverlay());
			Matrix4f mat = graphics.pose().last().pose();
			float bo = 0;
			bufferbuilder.addVertex(mat, right, top, bo).setColor(rtr, gtr, btr, atr);
			bufferbuilder.addVertex(mat, left, top, bo).setColor(rtl, gtl, btl, atl);
			bufferbuilder.addVertex(mat, left, bottom, bo).setColor(rbl, gbl, bbl, abl);
			bufferbuilder.addVertex(mat, right, bottom, bo).setColor(rbr, gbr, bbr, abr);
		});
	}

	@Override
	public NativeGuiComponents getNative() {
		return nativeComponents;
	}

	@Override
	public void setClipboardText(String text) {
		minecraft.keyboardHandler.setClipboard(text);
	}

	@Override
	public Frame getFrame() {
		return gui;
	}

	@Override
	public String getClipboardText() {
		return minecraft.keyboardHandler.getClipboard();
	}

	@Override
	public void setScale(int value) {
		if(vanillaScale == -999)return;
		if(value != minecraft.options.guiScale().get()) {
			if(vanillaScale == -1)vanillaScale = minecraft.options.guiScale().get();
			if(value == -1) {
				if(minecraft.options.guiScale().get() != vanillaScale) {
					minecraft.options.guiScale().set(vanillaScale);
					vanillaScale = -1;
				}
			} else {
				minecraft.options.guiScale().set(value);
			}
		}
	}

	@Override
	public int getScale() {
		return minecraft.options.guiScale().get();
	}

	@Override
	public int getMaxScale() {
		return minecraft.getWindow().calculateScale(0, minecraft.isEnforceUnicode()) + 1;
	}

	@Override
	public CtxStack getStack() {
		return stack;
	}

	@Override
	public void tick() {
		try {
			gui.tick();
		} catch (Throwable e) {
			onGuiException("Error in tick gui", e, true);
		}
	}

	@Override
	public void drawFormattedText(float x, float y, IText text, int color, float scale) {
		x += getOffset().x;
		y += getOffset().y;
		graphics.pose().pushPose();
		graphics.pose().translate(x, y, 500);
		graphics.pose().scale(scale, scale, scale);
		graphics.drawString(font, text.<Component>remap(), 0, 0, color, false);
		graphics.pose().popPose();
	}

	@Override
	public int textWidthFormatted(IText text) {
		return font.width(text.<Component>remap().getVisualOrderText());
	}

	@Override
	public void openURL0(String url) {
		Util.getPlatform().openUri(url);
	}

	public GuiGraphics getMCGraphics() {
		return graphics;
	}

	@Override
	public void added() {
		vanillaScale = -1;
	}

	@Override
	public void drawStack(int x, int y, Stack stack) {
		x += getOffset().x;
		y += getOffset().y;
		ItemStack s = ItemStackHandlerImpl.impl.unwrap(stack);
		graphics.renderItem(s, x, y);
		graphics.renderItemDecorations(font, s, x, y);
	}

	@Override
	public void drawStackTooltip(int mx, int my, Stack stack) {
		graphics.pose().pushPose();
		graphics.pose().translate(0, 0, 100);
		ItemStack s = ItemStackHandlerImpl.impl.unwrap(stack);
		graphics.renderTooltip(font, s, mx, my);
		graphics.pose().popPose();
	}
}