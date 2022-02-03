package com.tom.cpm.client;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.ClientBrandRetriever;
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
import net.minecraftforge.common.MinecraftForge;

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
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.AWTChooser;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.client.RetroGL.RetroTessellator;
import com.tom.cpm.shared.gui.panel.Panel3d;

import cpw.mods.fml.common.Loader;

public class GuiImpl extends GuiScreen implements IGui {
	private static final KeyCodes CODES = new LWJGLKeyCodes();
	private static final NativeGuiComponents nativeComponents = new NativeGuiComponents();
	private Frame gui;
	private GuiScreen parent;
	private CtxStack stack;
	private UIColors colors;
	private Consumer<Runnable> closeListener;
	private int vanillaScale = -1;

	static {
		nativeComponents.register(TextField.class, local(GuiImpl::createTextField));
		nativeComponents.register(FileChooserPopup.class, AWTChooser::new);
		nativeComponents.register(Panel3d.class, Panel3dImpl::new);
	}

	public GuiImpl(Function<IGui, Frame> creator, GuiScreen parent) {
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
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		try {
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			stack = new CtxStack(width, height);
			gui.draw(mouseX, mouseY, partialTicks);
		} catch (Throwable e) {
			onGuiException("Error drawing gui", e, true);
		} finally {
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			String s = "Minecraft " + MinecraftForge.MC_VERSION + " (" + ClientBrandRetriever.getClientModName() + ") " + Loader.instance().getIndexedModList().get(CustomPlayerModels.ID).getDisplayVersion();
			fontRendererObj.drawString(s, width - fontRendererObj.getStringWidth(s) - 4, 2, 0xff000000);
			s =  Minecraft.getMinecraft().debug;
			fontRendererObj.drawString(s, width - fontRendererObj.getStringWidth(s) - 4, 11, 0xff000000);
		}
		if(mc.thePlayer != null && gui.enableChat()) {
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
		Keyboard.enableRepeatEvents(false);
		if(vanillaScale != -1 && vanillaScale != mc.gameSettings.guiScale) {
			mc.gameSettings.guiScale = vanillaScale;
		}
		if(parent != null) {
			GuiScreen p = parent;
			parent = null;
			mc.displayGuiScreen(p);
		}
	}

	@Override
	public void drawBox(int x, int y, int w, int h, int color) {
		x += getOffset().x;
		y += getOffset().y;
		drawRect(x, y, x+w, y+h, color);
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
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
			onGuiException("Error processing key event", e, false);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		try {
			gui.mouseClick(new MouseEvent(mouseX, mouseY, mouseButton));
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		try {
			gui.mouseDrag(new MouseEvent(mouseX, mouseY, clickedMouseButton));
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
		}
	}


	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
		if(state == -1)return;
		try {
			gui.mouseRelease(new MouseEvent(mouseX, mouseY, state));
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
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
				onGuiException("Error processing mouse event", e, false);
			}
		}
	}

	@Override
	public void displayError(String e) {
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
		x += getOffset().x;
		y += getOffset().y;
		GL11.glColor4f(1, 1, 1, 1);
		drawTexturedModalRect(x, y, u, v, w, h);
	}

	@Override
	public void drawTexture(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
		x += getOffset().x;
		y += getOffset().y;
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
	public void setupCut() {
		float multiplierX = mc.displayWidth / (float)width;
		float multiplierY = mc.displayHeight / (float)height;
		Box box = getContext().cutBox;
		GL11.glScissor((int) (box.x * multiplierX), mc.displayHeight - (int) ((box.y + box.h) * multiplierY),
				(int) (box.w * multiplierX), (int) (box.h * multiplierY));
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
			Vec2i off = getOffset();
			field.xPosition = bounds.x + off.x + 4;
			field.yPosition = bounds.y + off.y + 6;
			currentOff.x = off.x;
			currentOff.y = off.y;
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

		@Override
		public boolean isFocused() {
			return field.isFocused();
		}

		@Override
		public void setFocused(boolean focused) {
			field.setFocused(focused);
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
		RetroTessellator t = RetroGL.tessellator;
		t.begin(7);
		t.pos(right, top, this.zLevel).color(rtr, gtr, btr, atr).endVertex();
		t.pos(left, top, this.zLevel).color(rtl, gtl, btl, atl).endVertex();
		t.pos(left, bottom, this.zLevel).color(rbl, gbl, bbl, abl).endVertex();
		t.pos(right, bottom, this.zLevel).color(rbr, gbr, bbr, abr).endVertex();
		t.draw();
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

	@Override
	public void setScale(int value) {
		if(value != mc.gameSettings.guiScale) {
			if(vanillaScale == -1)vanillaScale = mc.gameSettings.guiScale;
			if(value == -1) {
				if(mc.gameSettings.guiScale != vanillaScale) {
					mc.gameSettings.guiScale = vanillaScale;
					vanillaScale = -1;
					ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
					int j = scaledresolution.getScaledWidth();
					int k = scaledresolution.getScaledHeight();
					this.setWorldAndResolution(this.mc, j, k);
				}
			} else {
				mc.gameSettings.guiScale = value;
				ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
				int j = scaledresolution.getScaledWidth();
				int k = scaledresolution.getScaledHeight();
				this.setWorldAndResolution(this.mc, j, k);
			}
		}
	}

	@Override
	public int getScale() {
		return mc.gameSettings.guiScale;
	}

	@Override
	public int getMaxScale() {
		return 4;
	}

	@Override
	public CtxStack getStack() {
		return stack;
	}

	@Override
	public void updateScreen() {
		try {
			gui.tick();
		} catch (Throwable e) {
			onGuiException("Error in tick gui", e, true);
		}
	}
}
