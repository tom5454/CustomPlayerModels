package com.tom.cpm.client;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

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
import com.tom.cpl.util.AWTChooser;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.client.RetroGL.RetroTessellator;
import com.tom.cpm.common.ItemStackHandlerImpl;
import com.tom.cpm.shared.gui.panel.Panel3d;
import com.tom.cpm.shared.util.Log;

import cpw.mods.fml.common.Loader;

public class GuiImpl extends GuiScreen implements IGui {
	private static final KeyCodes CODES = new LWJGLKeyCodes();
	private static final NativeGuiComponents nativeComponents = new NativeGuiComponents();
	protected static RenderItem itemRenderer = new RenderItem();
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
			String s = "Minecraft 1.4.7 (" + ClientBrandRetriever.getClientModName() + ") " + Loader.instance().getIndexedModList().get(CustomPlayerModels.ID).getDisplayVersion();
			fontRenderer.drawString(s, width - fontRenderer.getStringWidth(s) - 4, 2, 0xff000000);
			s =  Minecraft.getMinecraft().debug;
			fontRenderer.drawString(s, width - fontRenderer.getStringWidth(s) - 4, 11, 0xff000000);
		}
		if(mc.thePlayer != null && gui.enableChat()) {
			try {
				ScaledResolution res = new ScaledResolution(mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
				Method m = GuiIngame.class.getDeclaredMethod("renderChat", int.class, int.class);
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
		fontRenderer.drawString(text, x, y, color);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		try {
			KeyboardEvent evt = new KeyboardEvent(keyCode, 0, typedChar, Keyboard.getKeyName(keyCode));
			gui.keyPressed(evt);
			if(!evt.isConsumed()) {
				if(mc.thePlayer != null && mc.gameSettings.keyBindChat.keyCode == keyCode && mc.gameSettings.chatVisibility != 2) {
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
	protected void func_85041_a(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
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

	private int eventButton = 0;
	private long field_85043_c = 0L;
	private int field_92018_d = 0;

	@Override
	public void handleMouseInput() {
		int var1 = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int var2 = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		if (Mouse.getEventButtonState()) {
			if (this.mc.gameSettings.touchscreen && this.field_92018_d++ > 0) {
				return;
			}

			this.eventButton = Mouse.getEventButton();
			this.field_85043_c = Minecraft.getSystemTime();
			this.mouseClicked(var1, var2, this.eventButton);
		} else if (Mouse.getEventButton() != -1) {
			if (this.mc.gameSettings.touchscreen && --this.field_92018_d > 0) {
				return;
			}

			this.eventButton = -1;
			this.mouseMovedOrUp(var1, var2, Mouse.getEventButton());
		} else if (this.eventButton != -1 && this.field_85043_c > 0L) {
			long var3 = Minecraft.getSystemTime() - this.field_85043_c;
			this.func_85041_a(var1, var2, this.eventButton, var3);
		}

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
		Minecraft.getMinecraft().displayGuiScreen(new GuiScreen() {
			private GuiScreen parent = p;
			private String message1 = "Custom Player Models";
			private String message2 = Lang.format("error.cpm.crash", e);

			@SuppressWarnings("unchecked")
			@Override
			public void initGui() {
				super.initGui();
				this.controlList.add(new GuiButton(0, this.width / 2 - 100, 140, Lang.format("gui.cancel", new Object[0])));
			}

			@Override
			protected void keyTyped(char typedChar, int keyCode) {
			}

			@Override
			public void drawScreen(int par1, int par2, float par3) {
				this.drawGradientRect(0, 0, this.width, this.height, -12574688, -11530224);
				this.drawCenteredString(this.fontRenderer, this.message1, this.width / 2, 90, 16777215);
				this.drawCenteredString(this.fontRenderer, this.message2, this.width / 2, 110, 16777215);
				super.drawScreen(par1, par2, par3);
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
			protected void actionPerformed(GuiButton button) {
				this.mc.displayGuiScreen((GuiScreen)null);
			}
		});
	}

	@Override
	public void closeGui() {
		if(closeListener != null) {
			closeListener.accept(() -> this.mc.displayGuiScreen((GuiScreen)null));
		} else
			this.mc.displayGuiScreen((GuiScreen)null);
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture) {
		mc.renderEngine.bindTexture(this.mc.renderEngine.getTexture("/assets/cpm/textures/gui/" + texture + ".png"));
		x += getOffset().x;
		y += getOffset().y;
		GL11.glColor4f(1, 1, 1, 1);
		drawTexturedModalRect(x, y, u, v, w, h);
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture, int color) {
		mc.renderEngine.bindTexture(this.mc.renderEngine.getTexture("/assets/cpm/textures/gui/" + texture + ".png"));
		x += getOffset().x;
		y += getOffset().y;
		float a = (color >> 24 & 255) / 255.0F;
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		GL11.glColor4f(r, g, b, a);
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
		return Lang.format(key, obj);
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
		return fontRenderer.getStringWidth(text);
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
			this.field = new GuiTextField(fontRenderer, 0, 0, 0, 0);
			this.field.setMaxStringLength(1024*1024);
			this.field.setEnableBackgroundDrawing(false);
			this.field.setVisible(true);
			this.field.setTextColor(colors.label_text_color);
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks, Box bounds) {
			Vec2i off = getOffset();
			field.xPos = bounds.x + off.x + 4;
			field.yPos = bounds.y + off.y + 6;
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
			field.xPos = bounds.x + currentOff.x;
			field.yPos = bounds.y + currentOff.y;
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
			field.func_82265_c(enabled);
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
			field.setSelectionPos(pos);
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
		RetroGL.glBlendFunc(770, 771, 1, 0);
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
					ScaledResolution scaledresolution = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
					int j = scaledresolution.getScaledWidth();
					int k = scaledresolution.getScaledHeight();
					this.setWorldAndResolution(this.mc, j, k);
				}
			} else {
				mc.gameSettings.guiScale = value;
				ScaledResolution scaledresolution = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
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

	@Override
	public void drawFormattedText(float x, float y, IText text, int color, float scale) {
		x += getOffset().x;
		y += getOffset().y;
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		GL11.glScalef(scale, scale, scale);
		fontRenderer.drawString(text.<String>remap(), 0, 0, color);
		GL11.glPopMatrix();
	}

	@Override
	public int textWidthFormatted(IText text) {
		return fontRenderer.getStringWidth(text.<String>remap());
	}

	@Override
	public void openURL0(String url) {
		try {
			Class<?> oclass = Class.forName("java.awt.Desktop");
			Object object = oclass.getMethod("getDesktop").invoke((Object)null);
			oclass.getMethod("browse", URI.class).invoke(object, new URI(url));
		} catch (Throwable throwable1) {
			Throwable throwable = throwable1.getCause();
			Log.error("Couldn't open link: " + (throwable == null ? "<UNKNOWN>" : throwable.getMessage()));
		}
	}

	public void onOpened() {
		vanillaScale = -1;
	}

	@Override
	public void drawStack(int x, int y, Stack stack) {
		x += getOffset().x;
		y += getOffset().y;
		ItemStack s = ItemStackHandlerImpl.impl.unwrap(stack);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glPushMatrix();
		GL11.glTranslatef(0, 0, 100);
		RenderHelper.enableGUIStandardItemLighting();
		itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.renderEngine, s, x, y);
		itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, s, x, y);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
	}

	@Override
	public void drawStackTooltip(int mx, int my, Stack stack) {
		ItemStack s = ItemStackHandlerImpl.impl.unwrap(stack);
		drawItemStackTooltip(s, mx, my);
	}

	@SuppressWarnings("unchecked")
	protected void drawItemStackTooltip(ItemStack par1ItemStack, int par2, int par3) {
		List<String> list = par1ItemStack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, "\u00A7" + Integer.toHexString(par1ItemStack.getRarity().rarityColor) + list.get(k));
			} else {
				list.set(k, "\u00A77" + list.get(k));
			}
		}

		this.drawHoveringText(list, par2, par3, this.fontRenderer);
	}

	protected void drawHoveringText(List<String> par1List, int par2, int par3, FontRenderer font) {
		if (!par1List.isEmpty()) {
			GL11.glDisable(32826);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(2896);
			GL11.glDisable(2929);
			int k = 0;
			Iterator<String> iterator = par1List.iterator();

			int j1;
			while (iterator.hasNext()) {
				String s = iterator.next();
				j1 = font.getStringWidth(s);
				if (j1 > k) {
					k = j1;
				}
			}

			int i1 = par2 + 12;
			j1 = par3 - 12;
			int k1 = 8;
			if (par1List.size() > 1) {
				k1 += 2 + (par1List.size() - 1) * 10;
			}

			if (i1 + k > this.width) {
				i1 -= 28 + k;
			}

			if (j1 + k1 + 6 > this.height) {
				j1 = this.height - k1 - 6;
			}

			this.zLevel = 300.0F;
			itemRenderer.zLevel = 300.0F;
			int l1 = -267386864;
			this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
			this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
			int i2 = 1347420415;
			int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
			this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
			this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
			this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

			for (int k2 = 0; k2 < par1List.size(); ++k2) {
				String s1 = par1List.get(k2);
				font.drawStringWithShadow(s1, i1, j1, -1);
				if (k2 == 0) {
					j1 += 2;
				}

				j1 += 10;
			}

			this.zLevel = 0.0F;
			itemRenderer.zLevel = 0.0F;
			GL11.glEnable(2896);
			GL11.glEnable(2929);
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable(32826);
		}

	}
}
