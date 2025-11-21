package com.tom.cpm.client;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.platform.cursor.CursorType;

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
import com.tom.cpm.client.GuiGraphicsEx.StateFactory;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.common.ItemStackHandlerImpl;
import com.tom.cpm.mixin.access.GuiAccessor;
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
	protected ModifierUtil keyModif = new ModifierUtil();
	protected GuiGraphics graphics;
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
		try {
			this.graphics = graphics;
			graphics.pose().pushMatrix();
			stack = new CtxStack(width, height);
			float pt = minecraft.getDeltaTracker().getRealtimeDeltaTicks();
			graphics.enableScissor(0, 0, width, height);
			this.gui.draw(mouseX, mouseY, pt);
		} catch (Throwable e) {
			onGuiException("Error drawing gui", e, true);
		} finally {
			String modVer = MinecraftCommonAccess.get().getModVersion();
			String s = "Minecraft " + SharedConstants.getCurrentVersion().name() + " (" + minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(minecraft.getVersionType()) ? "" : "/" + minecraft.getVersionType()) + ") " + modVer;
			graphics.drawString(font, s, width - font.width(s) - 4, 2, 0xff000000, false);
			s = minecraft.getFps() + " FPS";
			graphics.drawString(font, s, width - font.width(s) - 4, 11, 0xff000000, false);
			this.graphics = null;
			graphics.pose().popMatrix();
			graphics.requestCursor(CursorType.DEFAULT);
		}
		if(minecraft.player != null && gui.enableChat()) {
			((GuiAccessor) minecraft.gui).callRenderChat(graphics, minecraft.getDeltaTracker());
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
		graphics.fill(x, y, x+w, y+h, color);
	}

	@Override
	public void drawBox(float xI, float yI, float w, float h, int color) {
		float x = xI + getOffset().x;
		float y = yI + getOffset().y;
		((GuiGraphicsEx) graphics).cpm$fill(x, y, x + w, y + h, color);
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
		graphics.drawString(font, text, x, y, color | 0xFF000000, false);
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		try {
			int keyCode = keyEvent.key();
			int scanCode = keyEvent.scancode();
			this.keyModif.setModifier(keyEvent.modifiers());
			KeyboardEvent evt = new KeyboardEvent(keyCode, scanCode, (char) -1, GLFW.glfwGetKeyName(keyCode, scanCode));
			gui.keyPressed(evt);
			if(!evt.isConsumed()) {
				if(minecraft.player != null && minecraft.options.keyChat.matches(keyEvent) && minecraft.options.chatVisibility().get() != ChatVisiblity.HIDDEN) {
					Minecraft.getInstance().schedule(() -> {
						int scale = vanillaScale;
						vanillaScale = -1;
						minecraft.gui.getChat().openScreen(ChatComponent.ChatMethod.MESSAGE, (a, b) -> new Overlay(scale, a, b));
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
	public boolean keyReleased(KeyEvent keyEvent) {
		this.keyModif.setModifier(keyEvent.modifiers());
		return true;
	}

	@Override
	public boolean charTyped(CharacterEvent characterEvent) {
		try {
			this.keyModif.setModifier(characterEvent.modifiers());
			KeyboardEvent evt = new KeyboardEvent(-1, -1, characterEvent.codepointAsString().charAt(0), null);
			gui.keyPressed(evt);
			return evt.isConsumed();
		} catch (Throwable e) {
			onGuiException("Error processing key event", e, false);
			return true;
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
		try {
			this.keyModif.setModifier(mouseButtonEvent.modifiers());
			MouseEvent evt = new MouseEvent((int) mouseButtonEvent.x(), (int) mouseButtonEvent.y(), mouseButtonEvent.button());
			gui.mouseClick(evt);
			return evt.isConsumed();
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
			return true;
		}
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double dx, double dy) {
		try {
			this.keyModif.setModifier(mouseButtonEvent.modifiers());
			MouseEvent evt = new MouseEvent((int) mouseButtonEvent.x(), (int) mouseButtonEvent.y(), mouseButtonEvent.button());
			gui.mouseDrag(evt);
			return evt.isConsumed();
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
			return true;
		}
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
		try {
			this.keyModif.setModifier(mouseButtonEvent.modifiers());
			MouseEvent evt = new MouseEvent((int) mouseButtonEvent.x(), (int) mouseButtonEvent.y(), mouseButtonEvent.button());
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
				graphics.drawCenteredString(this.font, txt[i], this.width / 2, 15 + i * 10, 0xFFFFFFFF);
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
		graphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.tryBuild("cpm", "textures/gui/" + texture + ".png"), x, y, u, v, w, h, 256, 256);
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture, int color) {
		x += getOffset().x;
		y += getOffset().y;
		graphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.tryBuild("cpm", "textures/gui/" + texture + ".png"), x, y, u, v, w, h, 256, 256, color);
	}

	@Override
	public void drawTexture(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
		x = x + getOffset().x;
		y = y + getOffset().y;
		graphics.blit(DynTexture.getBoundLoc(), x, y, x + width, y + height, u1, u2, v1, v2);
	}

	@Override
	public String i18nFormat(String key, Object... obj) {
		return I18n.get(key, obj);
	}

	@Override
	public void setupCut() {
		Box box = getContext().cutBox;
		graphics.disableScissor();
		graphics.enableScissor(box.x, box.y, box.x + box.w, box.y + box.h);
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
			this.field.setTextColor(colors.label_text_color);
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
			field.setHeight(bounds.h - 12);
			if(updateField) {
				settingText = true;
				field.moveCursorToEnd(false);
				settingText = false;
				updateField = false;
			}
			field.render(graphics, mouseX, mouseY, partialTicks);
		}

		@Override
		public void keyPressed(KeyboardEvent evt) {
			if(evt.isConsumed())return;
			if(evt.keyCode == -1) {
				if(field.charTyped(new CharacterEvent(evt.charTyped, keyModif.modifiers())))
					evt.consume();
			} else {
				if(field.keyPressed(new KeyEvent(evt.keyCode, evt.scancode, keyModif.modifiers())) || field.canConsumeInput())
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
			field.setHeight(bounds.h);
			if(field.mouseClicked(new MouseButtonEvent(evt.x + currentOff.x, evt.y + currentOff.y, new MouseButtonInfo(evt.btn, keyModif.modifiers())), false)) {
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
		return keyModif.hasShiftDown();
	}

	@Override
	public boolean isCtrlDown() {
		return keyModif.hasControlDown();
	}

	@Override
	public boolean isAltDown() {
		return keyModif.hasAltDown();
	}

	public class Overlay extends ChatScreen {
		private int vanillaScaleReset;

		public Overlay(int vanillaScaleReset, String string, boolean bl) {
			super(string, bl);
			this.vanillaScaleReset = vanillaScaleReset;
		}

		@Override
		public void render(GuiGraphics gr, int mouseX, int mouseY, float partialTicks) {
			GuiImpl.this.render(gr, Integer.MIN_VALUE, Integer.MIN_VALUE, partialTicks);
			super.render(gr, mouseX, mouseY, partialTicks);
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
		int x = xI + getOffset().x;
		int y = yI + getOffset().y;
		((GuiGraphicsEx) graphics).cpm$fillGradient(x, y, x + w, y + h, topLeft, topRight, bottomLeft, bottomRight);
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
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(scale, scale);
		graphics.drawString(font, text.<Component>remap(), 0, 0, color | 0xFF000000, false);
		graphics.pose().popMatrix();
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
		ItemStack s = ItemStackHandlerImpl.impl.unwrap(stack);
		graphics.setTooltipForNextFrame(font, s, mx, my);
	}

	public <I, S extends PictureInPictureRenderState> void drawPip(I instance, int w, int h, StateFactory<I, S> sf) {
		int x = getOffset().x;
		int y = getOffset().y;
		((GuiGraphicsEx) graphics).cpm$drawPip(instance, x, y, x + w, y + h, sf);
	}

	private class ModifierUtil implements InputWithModifiers {
		private int modifier;

		@Override
		public int input() {
			return 0;
		}

		@Override
		public int modifiers() {
			return modifier;
		}

		public void setModifier(int modifier) {
			this.modifier = modifier;
		}
	}
}