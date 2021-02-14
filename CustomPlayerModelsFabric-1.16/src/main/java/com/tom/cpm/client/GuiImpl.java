package com.tom.cpm.client;

import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.shared.editor.gui.ViewportPanel;
import com.tom.cpm.shared.gui.Frame;
import com.tom.cpm.shared.gui.Gui.KeyboardEvent;
import com.tom.cpm.shared.gui.Gui.MouseEvent;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.KeyCodes;
import com.tom.cpm.shared.gui.NativeGuiComponents;
import com.tom.cpm.shared.gui.NativeGuiComponents.NativeConstructor;
import com.tom.cpm.shared.gui.UIColors;
import com.tom.cpm.shared.gui.elements.TextField;
import com.tom.cpm.shared.gui.elements.TextField.ITextField;
import com.tom.cpm.shared.math.Box;
import com.tom.cpm.shared.math.Vec2i;

public class GuiImpl extends Screen implements IGui {
	private static final KeyCodes CODES = new GLFWKeyCodes();
	private static final NativeGuiComponents nativeComponents = new NativeGuiComponents();
	private Frame gui;
	private Screen parent;
	private Stack<Ctx> posOff = new Stack<>();
	private Ctx current = new Ctx();
	private UIColors colors;
	private Consumer<Runnable> closeListener;
	private int keyModif;
	public MatrixStack matrixStack;

	static {
		nativeComponents.register(ViewportPanel.class, ViewportPanelImpl::new);
		nativeComponents.register(TextField.class, local(GuiImpl::createTextField));
	}

	public GuiImpl(Function<IGui, Frame> creator, Screen parent) {
		super(new LiteralText(""));
		this.colors = new UIColors();
		this.parent = parent;
		try {
			this.gui = creator.apply(this);
		} catch (Throwable e) {
			e.printStackTrace();
			displayError(e.toString());
		}
	}

	private static <G extends Supplier<IGui>, N> NativeConstructor<G, N> local(Function<GuiImpl, N> fac) {
		return f -> fac.apply((GuiImpl) f.get());
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack, 0);
		try {
			this.matrixStack = matrixStack;
			matrixStack.push();
			matrixStack.translate(0, 0, 1000);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			current = new Ctx();
			gui.draw(mouseX, mouseY, partialTicks);
		} catch (Throwable e) {
			e.printStackTrace();
			displayError(e.toString());
		} finally {
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			String s = "Minecraft " + SharedConstants.getGameVersion().getName() + " (" + this.client.getGameVersion() + "/" + ClientBrandRetriever.getClientModName() + ")";
			textRenderer.draw(matrixStack, s, width - textRenderer.getWidth(s) - 4, 2, 0xff000000);
			s = this.client.fpsDebugString;
			textRenderer.draw(matrixStack, s, width - textRenderer.getWidth(s) - 4, 11, 0xff000000);
			this.matrixStack = null;
			matrixStack.pop();
		}
		client.inGameHud.getChatHud().render(matrixStack, client.inGameHud.getTicks());
	}

	@Override
	public void onClose() {
		if(parent != null) {
			Screen p = parent;
			parent = null;
			client.openScreen(p);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void drawBox(int x, int y, int w, int h, int color) {
		x += current.box.x;
		y += current.box.y;
		fill(matrixStack, x, y, x+w, y+h, color);
	}

	@Override
	protected void init() {
		try {
			gui.init(width, height);
		} catch (Throwable e) {
			e.printStackTrace();
			displayError(e.toString());
		}
	}

	@Override
	public void drawText(int x, int y, String text, int color) {
		x += current.box.x;
		y += current.box.y;
		textRenderer.draw(matrixStack, text, x, y, color);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		try {
			this.keyModif = modifiers;
			KeyboardEvent evt = new KeyboardEvent(keyCode, scanCode, (char) -1, GLFW.glfwGetKeyName(keyCode, scanCode));
			gui.keyPressed(evt);
			if(!evt.isConsumed()) {
				if(client.player != null && client.options.keyChat.matchesKey(keyCode, scanCode) && client.options.chatVisibility != ChatVisibility.HIDDEN) {
					client.openScreen(new Overlay());
					return true;
				}
			}
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			logError(e.toString());
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
			e.printStackTrace();
			logError(e.toString());
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
			e.printStackTrace();
			logError(e.toString());
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
			e.printStackTrace();
			logError(e.toString());
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
			e.printStackTrace();
			logError(e.toString());
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
				e.printStackTrace();
				logError(e.toString());
				return true;
			}
		}
		return false;
	}

	private void logError(String e) {

	}

	private void displayError(String e) {
		Screen p = parent;
		parent = null;
		MinecraftClient.getInstance().openScreen(new DisconnectedScreen(p, new LiteralText("Custom Player Models"), new TranslatableText("error.cpm.crash", e)));
	}

	@Override
	public void close() {
		if(closeListener != null) {
			closeListener.accept(() -> this.client.openScreen((Screen)null));
		} else
			this.client.openScreen((Screen)null);
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture) {
		client.getTextureManager().bindTexture(new Identifier("cpm", "textures/gui/" + texture + ".png"));
		x += current.box.x;
		y += current.box.y;
		RenderSystem.color4f(1, 1, 1, 1);
		drawTexture(matrixStack, x, y, u, v, w, h);
	}

	@Override
	public void drawTexture(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
		x += current.box.x;
		y += current.box.y;
		client.getTextureManager().bindTexture(DynTexture.getBoundLoc());
		RenderSystem.color4f(1, 1, 1, 1);
		Matrix4f mat = matrixStack.peek().getModel();
		float bo = getZOffset();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, VertexFormats.POSITION_TEXTURE);
		bufferbuilder.vertex(mat, x, y + height, bo).texture(u1, v2).next();
		bufferbuilder.vertex(mat, x + width, y + height, bo).texture(u2, v2).next();
		bufferbuilder.vertex(mat, x + width, y, bo).texture(u2, v1).next();
		bufferbuilder.vertex(mat, x, y, bo).texture(u1, v1).next();
		RenderSystem.enableAlphaTest();
		tessellator.draw();
	}

	@Override
	public String i18nFormat(String key, Object... obj) {
		return I18n.translate(key, obj);
	}

	@Override
	public void pushMatrix() {
		posOff.push(current);
		current = new Ctx(current);
	}

	@Override
	public void setPosOffset(Box box) {
		int dw = client.getWindow().getWidth();
		int dh = client.getWindow().getHeight();
		float multiplierX = dw / (float)width;
		float multiplierY = dh / (float)height;
		int x = current.box.x + box.x;
		int y = current.box.y + box.y;
		GL11.glScissor((int) (x * multiplierX), dh - (int) ((y + box.h) * multiplierY),
				(int) (box.w * multiplierX), (int) (box.h * multiplierY));
		current.box = new Box(x, y, box.w, box.h);
	}

	@Override
	public void popMatrix() {
		current = posOff.pop();
	}

	private class Ctx {
		private Box box;
		public Ctx(Ctx old) {
			box = new Box(old.box.x, old.box.y, old.box.w, old.box.h);
		}

		public Ctx() {
			box = new Box(0, 0, width, height);
		}
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
			this.field = new TextFieldWidget(textRenderer, 0, 0, 0, 0, null);
			this.field.setMaxLength(1024*1024);
			this.field.setHasBorder(false);
			this.field.setVisible(true);
			this.field.setEditableColor(16777215);
			this.field.setChangedListener(this);
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks, Box bounds) {
			field.x = bounds.x + current.box.x + 4;
			field.y = bounds.y + current.box.y + 6;
			currentOff.x = current.box.x;
			currentOff.y = current.box.y;
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
				if(field.keyPressed(evt.keyCode, evt.scancode, keyModif))
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
	public Vec2i getOffset() {
		return new Vec2i(current.box.x, current.box.y);
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

		public Overlay() {
			super("");
		}

		@Override
		public void render(MatrixStack st, int mouseX, int mouseY, float partialTicks) {
			GuiImpl.this.render(st, Integer.MIN_VALUE, Integer.MIN_VALUE, partialTicks);
			super.render(st, mouseX, mouseY, partialTicks);
		}

		public Screen getGui() {
			return GuiImpl.this;
		}
	}

	@Override
	public KeyCodes getKeyCodes() {
		return CODES;
	}

	@Override
	public void drawGradientBox(int x, int y, int w, int h, int topLeft, int topRight, int bottomLeft,
			int bottomRight) {
		x += current.box.x;
		y += current.box.y;
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
		RenderSystem.disableAlphaTest();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(7425);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		Matrix4f mat = matrixStack.peek().getModel();
		bufferbuilder.begin(7, VertexFormats.POSITION_COLOR);
		bufferbuilder.vertex(mat, right, top, this.getZOffset()).color(rtr, gtr, btr, atr).next();
		bufferbuilder.vertex(mat, left, top, this.getZOffset()).color(rtl, gtl, btl, atl).next();
		bufferbuilder.vertex(mat, left, bottom, this.getZOffset()).color(rbl, gbl, bbl, abl).next();
		bufferbuilder.vertex(mat, right, bottom, this.getZOffset()).color(rbr, gbr, bbr, abr).next();
		tessellator.draw();
		RenderSystem.shadeModel(7424);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
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
}
