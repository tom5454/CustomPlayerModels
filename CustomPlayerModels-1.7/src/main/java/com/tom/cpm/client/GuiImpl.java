package com.tom.cpm.client;

import java.lang.reflect.Method;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.GuiIngameForge;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyCodes;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.NativeGuiComponents;
import com.tom.cpl.gui.NativeGuiComponents.NativeConstructor;
import com.tom.cpl.gui.UIColors;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.TextField.ITextField;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.editor.gui.ViewportPanel;

public class GuiImpl extends GuiScreen implements IGui {
	private static final KeyCodes CODES = new LWJGLKeyCodes();
	private static final NativeGuiComponents nativeComponents = new NativeGuiComponents();
	private Frame gui;
	private GuiScreen parent;
	private Stack<Ctx> posOff = new Stack<>();
	private Ctx current = new Ctx();
	private UIColors colors;
	private Consumer<Runnable> closeListener;

	static {
		nativeComponents.register(ViewportPanel.class, ViewportPanelImpl::new);
		nativeComponents.register(TextField.class, local(GuiImpl::createTextField));
	}

	public GuiImpl(Function<IGui, Frame> creator, GuiScreen parent) {
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
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		try {
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			current = new Ctx();
			gui.draw(mouseX, mouseY, partialTicks);
		} catch (Throwable e) {
			e.printStackTrace();
			displayError(e.toString());
		} finally {
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			String s =  Minecraft.getMinecraft().debug;
			fontRendererObj.drawString(s, width - fontRendererObj.getStringWidth(s) - 4, 2, 0xff000000);
		}
		if(mc.thePlayer != null) {
			try {
				ScaledResolution res = new ScaledResolution(mc, this.mc.displayWidth, this.mc.displayHeight);
				Method m = GuiIngameForge.class.getDeclaredMethod("renderChat", int.class, int.class);
				m.setAccessible(true);
				m.invoke(mc.ingameGUI, res.getScaledWidth(), res.getScaledHeight());
			} catch (Throwable e) {
			}
		}
	}

	@Override
	public void onGuiClosed() {
		if(parent != null) {
			GuiScreen p = parent;
			parent = null;
			mc.displayGuiScreen(p);
		}
	}

	@Override
	public void drawBox(int x, int y, int w, int h, int color) {
		x += current.box.x;
		y += current.box.y;
		drawRect(x, y, x+w, y+h, color);
	}

	@Override
	public void initGui() {
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
		fontRendererObj.drawString(text, x, y, color);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		try {
			KeyboardEvent evt = new KeyboardEvent(keyCode, 0, typedChar, Keyboard.getKeyName(keyCode));
			gui.keyPressed(evt);
			if(!evt.isConsumed()) {
				if(mc.thePlayer != null && mc.gameSettings.keyBindChat.getKeyCode() == keyCode && mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
					mc.displayGuiScreen(new Overlay());
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			logError(e.toString());
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		try {
			gui.mouseClick(new MouseEvent(mouseX, mouseY, mouseButton));
		} catch (Throwable e) {
			e.printStackTrace();
			logError(e.toString());
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		try {
			gui.mouseDrag(new MouseEvent(mouseX, mouseY, clickedMouseButton));
		} catch (Throwable e) {
			e.printStackTrace();
			logError(e.toString());
		}
	}


	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
		if(state == -1)return;
		try {
			gui.mouseRelease(new MouseEvent(mouseX, mouseY, state));
		} catch (Throwable e) {
			e.printStackTrace();
			logError(e.toString());
		}
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		int i = Mouse.getEventDWheel();
		if (i > 0) {
			i = 1;
		}

		if (i < 0) {
			i = -1;
		}

		if(i != 0) {
			try {
				int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
				int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
				gui.mouseWheel(new MouseEvent(x, y, i));
			} catch (Throwable e) {
				e.printStackTrace();
				logError(e.toString());
			}
		}
	}

	private void logError(String e) {
		if(gui != null) {
			try {
				gui.logMessage(e);
			} catch (Throwable ex) {
				ex.printStackTrace();
				ex.addSuppressed(new RuntimeException(e));
				displayError(ex.toString());
			}
		}
	}

	private void displayError(String e) {
		GuiScreen p = parent;
		parent = null;
		Minecraft.getMinecraft().displayGuiScreen(new GuiErrorScreen("Custom Player Models", I18n.format("error.cpm.crash", e)) {
			private GuiScreen parent = p;

			@Override
			public void onGuiClosed() {
				if(parent != null) {
					GuiScreen p = parent;
					parent = null;
					mc.displayGuiScreen(p);
				}
			}
		});
	}

	@Override
	public void close() {
		if(closeListener != null) {
			closeListener.accept(() -> this.mc.displayGuiScreen((GuiScreen)null));
		} else
			this.mc.displayGuiScreen((GuiScreen)null);
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture) {
		mc.getTextureManager().bindTexture(new ResourceLocation("cpm", "textures/gui/" + texture + ".png"));
		x += current.box.x;
		y += current.box.y;
		GL11.glColor4f(1, 1, 1, 1);
		drawTexturedModalRect(x, y, u, v, w, h);
	}

	@Override
	public void drawTexture(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
		x += current.box.x;
		y += current.box.y;
		GL11.glColor4f(1, 1, 1, 1);
		Tessellator t = Tessellator.instance;
		t.startDrawingQuads();
		t.addVertexWithUV(x, y + height, 0.0D, u1, v2);
		t.addVertexWithUV(x + width, y + height, 0.0D, u2, v2);
		t.addVertexWithUV(x + width, y, 0.0D, u2, v1);
		t.addVertexWithUV(x, y, 0.0D, u1, v1);
		t.draw();
	}

	@Override
	public String i18nFormat(String key, Object... obj) {
		return I18n.format(key, obj);
	}

	@Override
	public void pushMatrix() {
		posOff.push(current);
		current = new Ctx(current);
	}

	@Override
	public void setPosOffset(Box box) {
		current.box = new Box(current.box.x + box.x, current.box.y + box.y, box.w, box.h);
	}

	@Override
	public void setupCut() {
		float multiplierX = mc.displayWidth / (float)width;
		float multiplierY = mc.displayHeight / (float)height;
		GL11.glScissor((int) (current.box.x * multiplierX), mc.displayHeight - (int) ((current.box.y + current.box.h) * multiplierY),
				(int) (current.box.w * multiplierX), (int) (current.box.h * multiplierY));
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
		return fontRendererObj.getStringWidth(text);
	}

	private ITextField createTextField() {
		return new TxtField();
	}

	private class TxtField implements ITextField {
		private GuiTextField field;
		private Runnable eventListener;
		private Vec2i currentOff = new Vec2i(0, 0);
		private Box bounds = new Box(0, 0, 0, 0);
		private boolean refreshTextBox;
		public TxtField() {
			this.field = new GuiTextField(fontRendererObj, 0, 0, 0, 0);
			this.field.setMaxStringLength(1024*1024);
			this.field.setEnableBackgroundDrawing(false);
			this.field.setVisible(true);
			this.field.setTextColor(16777215);
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks, Box bounds) {
			field.xPosition = bounds.x + current.box.x + 4;
			field.yPosition = bounds.y + current.box.y + 6;
			currentOff.x = current.box.x;
			currentOff.y = current.box.y;
			this.bounds.x = bounds.x;
			this.bounds.y = bounds.y;
			this.bounds.w = bounds.w;
			this.bounds.h = bounds.h;
			field.width = bounds.w - 5;
			field.height = bounds.h - 12;
			if(refreshTextBox) {
				field.setCursorPositionEnd();
				refreshTextBox = false;
			}
			field.drawTextBox();
		}

		@Override
		public void keyPressed(KeyboardEvent e) {
			if(e.isConsumed())return;
			String text = field.getText();
			boolean b = field.textboxKeyTyped(e.charTyped, e.keyCode);
			if(b)e.consume();
			if(b && eventListener != null) {
				if(!text.equals(field.getText()))
					eventListener.run();
			}
		}

		@Override
		public void mouseClick(MouseEvent e) {
			if(e.isConsumed()) {
				field.mouseClicked(Integer.MIN_VALUE, Integer.MIN_VALUE, e.btn);
				return;
			}
			field.xPosition = bounds.x + currentOff.x;
			field.yPosition = bounds.y + currentOff.y;
			field.width = bounds.w;
			field.height = bounds.h;
			field.mouseClicked(e.x + currentOff.x, e.y + currentOff.y, e.btn);
			if(bounds.isInBounds(e.x, e.y))e.consume();
		}

		@Override
		public String getText() {
			return field.getText();
		}

		@Override
		public void setText(String txt) {
			field.setText(txt);
			refreshTextBox = true;
		}

		@Override
		public void setEventListener(Runnable eventListener) {
			this.eventListener = eventListener;
		}

		@Override
		public void setEnabled(boolean enabled) {
			field.setEnabled(enabled);
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
		return isShiftKeyDown();
	}

	@Override
	public boolean isCtrlDown() {
		return isCtrlKeyDown();
	}

	@Override
	public boolean isAltDown() {
		return false;
	}

	public class Overlay extends GuiChat {

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			GuiImpl.this.drawScreen(Integer.MIN_VALUE, Integer.MIN_VALUE, partialTicks);
			super.drawScreen(mouseX, mouseY, partialTicks);
		}

		public GuiScreen getGui() {
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
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		Render.t.startDrawing(7);
		Render.t.pos(right, top, this.zLevel).color(rtr, gtr, btr, atr).endVertex();
		Render.t.pos(left, top, this.zLevel).color(rtl, gtl, btl, atl).endVertex();
		Render.t.pos(left, bottom, this.zLevel).color(rbl, gbl, bbl, abl).endVertex();
		Render.t.pos(right, bottom, this.zLevel).color(rbr, gbr, bbr, abr).endVertex();
		Render.t.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Override
	public NativeGuiComponents getNative() {
		return nativeComponents;
	}

	@Override
	public void setClipboardText(String text) {
		setClipboardString(text);
	}

	@Override
	public Frame getFrame() {
		return gui;
	}

	@Override
	public String getClipboardText() {
		return getClipboardString();
	}
}
