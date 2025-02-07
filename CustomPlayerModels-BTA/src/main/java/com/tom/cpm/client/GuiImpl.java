package com.tom.cpm.client;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.ItemElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.TextFieldElement;
import net.minecraft.client.gui.chat.ScreenChat;
import net.minecraft.client.input.InputType;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.client.util.debug.DebugRender;
import net.minecraft.core.item.ItemStack;

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
import com.tom.cpm.client.MinecraftObject.Texture;
import com.tom.cpm.client.RetroGL.RetroTessellator;
import com.tom.cpm.common.ItemStackHandlerImpl;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.gui.panel.Panel3d;
import com.tom.cpm.shared.util.Log;

public class GuiImpl extends Screen implements IGui {
	private static final KeyCodes CODES = new LWJGLKeyCodes();
	private static final NativeGuiComponents nativeComponents = new NativeGuiComponents();
	protected static ItemElement itemRenderer = new ItemElement(Minecraft.INSTANCE);
	private Frame gui;
	private Screen parent;
	private CtxStack stack;
	private UIColors colors;
	private Consumer<Runnable> closeListener;
	private int vanillaScale = -1;

	static {
		nativeComponents.register(TextField.class, local(GuiImpl::createTextField));
		nativeComponents.register(FileChooserPopup.class, AWTChooser::new);
		nativeComponents.register(Panel3d.class, Panel3dImpl::new);
	}

	public GuiImpl(Function<IGui, Frame> creator, Screen parent) {
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
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.renderBackground();
		try {
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			stack = new CtxStack(width, height);
			gui.draw(mouseX, mouseY, partialTicks);
		} catch (Throwable e) {
			onGuiException("Error drawing gui", e, true);
		} finally {
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			String s = "Minecraft " + Minecraft.VERSION  + " " + MinecraftCommonAccess.get().getModVersion();
			font.drawString(s, width - font.getStringWidth(s) - 4, 2, 0xff000000);
			s = mc.debugFPS;
			font.drawString(s, width - font.getStringWidth(s) - 4, 11, 0xff000000);
		}
		/*if(mc.thePlayer != null && gui.enableChat()) {
			try {
				ScaledResolution res = new ScaledResolution(mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
				Method m = GuiIngame.class.getDeclaredMethod("renderChat", int.class, int.class);
				m.setAccessible(true);
				m.invoke(mc.ingameGUI, res.getScaledWidth(), res.getScaledHeight());
			} catch (Throwable e) {
			}
		}*/
	}

	@Override
	public void removed() {
		Keyboard.enableRepeatEvents(false);
		if(vanillaScale != -1 && vanillaScale != mc.gameSettings.guiScale.value) {
			mc.gameSettings.guiScale.set(vanillaScale);
		}
		if(parent != null) {
			Screen p = parent;
			parent = null;
			mc.displayScreen(p);
		}
	}

	@Override
	public void drawBox(int x, int y, int w, int h, int color) {
		x += getOffset().x;
		y += getOffset().y;
		drawRect(x, y, x+w, y+h, color);
	}

	@Override
	public void init() {
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
		font.drawString(text, x, y, color);
	}

	@Override
	public void keyPressed(final char typedChar, final int keyCode, final int mouseX, final int mouseY) {
		try {
			KeyboardEvent evt = new KeyboardEvent(keyCode, 0, typedChar, keyCode == -1 ? null : Keyboard.getKeyName(keyCode));
			gui.keyPressed(evt);
			if(!evt.isConsumed()) {
				if(mc.thePlayer != null && mc.gameSettings.keyChat.getKeyCode() == keyCode) {
					mc.displayScreen(new Overlay());
				}
			}
		} catch (Throwable e) {
			onGuiException("Error processing key event", e, false);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		try {
			gui.mouseClick(new MouseEvent(mouseX, mouseY, mouseButton));
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
		}
	}

	protected void func_85041_a(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		try {
			gui.mouseDrag(new MouseEvent(mouseX, mouseY, clickedMouseButton));
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
		}
	}


	protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
		if(state == -1)return;
		try {
			gui.mouseRelease(new MouseEvent(mouseX, mouseY, state));
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
		}
	}

	private int eventButton = 0;
	private long dragStart = 0L;

	@Override
	public void updateEvents() {
		final int mouseX = Mouse.getEventX() * this.width / this.mc.resolution.getWidthScreenCoords();
		final int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.resolution.getHeightScreenCoords() - 1;
		while (Mouse.next()) {
			if (this.mc.inputType == InputType.CONTROLLER) {
				break;
			}
			if (Mouse.getEventButtonState()) {
				this.eventButton = Mouse.getEventButton();
				this.dragStart = System.currentTimeMillis();
				this.mouseClicked(mouseX, mouseY, Mouse.getEventButton());
			} else if (Mouse.getEventButton() != -1) {
				this.eventButton = -1;
				this.mouseMovedOrUp(mouseX, mouseY, Mouse.getEventButton());
			} else if (this.eventButton != -1 && this.dragStart > 0L) {
				long var3 = System.currentTimeMillis() - this.dragStart;
				this.func_85041_a(mouseX, mouseY, this.eventButton, var3);
			}
		}
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState() && this.mc.gameSettings.showFrameTimes.value
					&& DebugRender.keyPressed(Keyboard.getEventKey())) {
				continue;
			}
			final int eventKey = Keyboard.getEventKey();
			final char eventChar = Keyboard.getEventCharacter();
			if (eventKey == 0 && Character.isDefined(eventChar)) {
				this.keyPressed(eventChar, eventKey, mouseX, mouseY);
			}
			if (!Keyboard.getEventKeyState()) {
				continue;
			}
			if (eventKey == 87) {
				this.mc.gameWindow.toggleFullscreen();
				return;
			}
			this.keyPressed(eventChar, eventKey, mouseX, mouseY);
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
				gui.mouseWheel(new MouseEvent(mouseX, mouseY, i));
			} catch (Throwable e) {
				onGuiException("Error processing mouse event", e, false);
			}
		}
	}

	@Override
	public void displayError(String e) {
		Screen p = parent;
		parent = null;
		mc.displayScreen(new Screen() {
			private Screen parent = p;
			private String message1 = "Custom Player Models";
			private String message2 = Lang.format("error.cpm.crash", e);

			@SuppressWarnings("unchecked")
			@Override
			public void init() {
				super.init();
				this.buttons.add(new ButtonElement(0, this.width / 2 - 100, 140, Lang.format("gui.cancel", new Object[0])));
			}

			@Override
			public void keyPressed(final char c, final int key, final int mouseX, final int mouseY) {
			}

			@Override
			public void render(int par1, int par2, float par3) {
				GuiImpl.drawGradientRect(0, 0, this.width, this.height, -12574688, -11530224, 0);
				this.drawStringCentered(this.font, this.message1, this.width / 2, 90, 16777215);
				this.drawStringCentered(this.font, this.message2, this.width / 2, 110, 16777215);
				super.render(par1, par2, par3);
			}

			@Override
			public void removed() {
				if(parent != null) {
					Screen p = parent;
					parent = null;
					mc.displayScreen(p);
				}
			}

			@Override
			protected void buttonClicked(ButtonElement button) {
				this.mc.displayScreen((Screen)null);
			}
		});
	}

	@Override
	public void closeGui() {
		if(closeListener != null) {
			closeListener.accept(() -> this.mc.displayScreen((Screen)null));
		} else
			this.mc.displayScreen((Screen)null);
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture) {
		mc.textureManager.bindTexture(this.mc.textureManager.loadTexture("/assets/cpm/textures/gui/" + texture + ".png"));
		x += getOffset().x;
		y += getOffset().y;
		GL11.glColor4f(1, 1, 1, 1);
		drawTexturedModalRect(x, y, u, v, w, h);
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture, int color) {
		mc.textureManager.bindTexture(this.mc.textureManager.loadTexture("/assets/cpm/textures/gui/" + texture + ".png"));
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
		if (Texture.bound != null)
			Texture.bound.getMcTex().bind();
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
		float multiplierX = this.mc.resolution.getWidthScreenCoords() / (float)width;
		float multiplierY = this.mc.resolution.getHeightScreenCoords() / (float)height;
		Box box = getContext().cutBox;
		GL11.glScissor((int) (box.x * multiplierX), this.mc.resolution.getHeightScreenCoords() - (int) ((box.y + box.h) * multiplierY),
				(int) (box.w * multiplierX), (int) (box.h * multiplierY));
	}

	@Override
	public int textWidth(String text) {
		return font.getStringWidth(text);
	}

	private ITextField createTextField() {
		return new TxtField();
	}

	private class TxtField implements ITextField {
		private TextFieldElement field;
		private Runnable eventListener;
		private Vec2i currentOff = new Vec2i(0, 0);
		private Box bounds = new Box(0, 0, 0, 0);
		private boolean refreshTextBox;
		public TxtField() {
			this.field = new TextFieldElement(GuiImpl.this, font, 0, 0, 0, 0, "", "");
			this.field.setMaxStringLength(1024*1024);
			this.field.drawBackground = false;
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
				//field.func_50038_e();
				refreshTextBox = false;
			}
			field.drawTextBox();
		}

		@Override
		public void keyPressed(KeyboardEvent e) {
			if(e.isConsumed())return;
			String text = field.getText();
			if (field.isEnabled && field.isFocused) {
				if (e.charTyped == '\t') {
					return;
				}
				field.textboxKeyTyped(e.charTyped, e.keyCode);
				e.consume();
				if(eventListener != null) {
					if(!text.equals(field.getText()))
						eventListener.run();
				}
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
			field.isEnabled = enabled;
		}

		@Override
		public boolean isFocused() {
			return field.isFocused;
		}

		@Override
		public void setFocused(boolean focused) {
			field.setFocused(focused);
		}

		@Override
		public int getCursorPos() {
			return 0;//field.func_50035_h();
		}

		@Override
		public void setCursorPos(int pos) {
			//field.func_50030_e(pos);
		}

		@Override
		public void setSelectionPos(int pos) {
			//field.func_50030_e(pos);
		}

		@Override
		public int getSelectionPos() {
			return 0;//field.field_50048_p;
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
		return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
	}

	@Override
	public boolean isCtrlDown() {
		return Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
	}

	@Override
	public boolean isAltDown() {
		return false;
	}

	public class Overlay extends ScreenChat {

		@Override
		public void render(int mouseX, int mouseY, float partialTicks) {
			GuiImpl.this.render(Integer.MIN_VALUE, Integer.MIN_VALUE, partialTicks);
			super.render(mouseX, mouseY, partialTicks);
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
		t.pos(right, top, this.zLevel).color(rtr, gtr, btr, atr, false).endVertex();
		t.pos(left, top, this.zLevel).color(rtl, gtl, btl, atl, false).endVertex();
		t.pos(left, bottom, this.zLevel).color(rbl, gbl, bbl, abl, false).endVertex();
		t.pos(right, bottom, this.zLevel).color(rbr, gbr, bbr, abr, false).endVertex();
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
		try {
			StringSelection var1 = new StringSelection(text);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(var1, (ClipboardOwner) null);
		} catch (Exception var2) {
			;
		}
	}

	@Override
	public Frame getFrame() {
		return gui;
	}

	@Override
	public String getClipboardText() {
		try {
			final Transferable trans = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			if (trans != null && trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return (String) trans.getTransferData(DataFlavor.stringFlavor);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setScale(int value) {
		if(vanillaScale == -999)return;
		if(value != mc.gameSettings.guiScale.value) {
			if(vanillaScale == -1)vanillaScale = mc.gameSettings.guiScale.value;
			if(value == -1) {
				if(mc.gameSettings.guiScale.value != vanillaScale) {
					mc.gameSettings.guiScale.set(vanillaScale);
					vanillaScale = -1;
					mc.gameSettings.guiScale.onUpdate();
				}
			} else {
				mc.gameSettings.guiScale.set(value);
				mc.gameSettings.guiScale.onUpdate();
			}
		}
	}

	@Override
	public int getScale() {
		return mc.gameSettings.guiScale.value;
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
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		GL11.glScalef(scale, scale, scale);
		font.drawString(text.<String>remap(), 0, 0, color);
		GL11.glPopMatrix();
	}

	@Override
	public int textWidthFormatted(IText text) {
		return font.getStringWidth(text.<String>remap());
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
		itemRenderer.render(s, x, y);
	}

	@Override
	public void drawStackTooltip(int mx, int my, Stack stack) {
		ItemStack s = ItemStackHandlerImpl.impl.unwrap(stack);
		drawItemStackTooltip(s, mx, my);
	}

	@SuppressWarnings("unchecked")
	protected void drawItemStackTooltip(ItemStack par1ItemStack, int par2, int par3) {
		/*List<String> list = new ArrayList<>();
		list.add(class_629.method_2049(par1ItemStack.getTranslationKey() + ".name"));

		this.drawHoveringText(list, par2, par3, this.font);*/
	}

	/*protected void drawHoveringText(List<String> par1List, int par2, int par3, TextRenderer font) {
		if (!par1List.isEmpty()) {
			GL11.glDisable(32826);
			class_583.method_1927();
			GL11.glDisable(2896);
			GL11.glDisable(2929);
			int k = 0;
			Iterator<String> iterator = par1List.iterator();

			int j1;
			while (iterator.hasNext()) {
				String s = iterator.next();
				j1 = font.getWidth(s);
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
			//itemRenderer.zOffset = 300.0F;
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
				font.drawWithShadow(s1, i1, j1, -1);
				if (k2 == 0) {
					j1 += 2;
				}

				j1 += 10;
			}

			this.zLevel = 0.0F;
			//itemRenderer.zOffset = 0.0F;
			GL11.glEnable(2896);
			GL11.glEnable(2929);
			class_583.method_1930();
			GL11.glEnable(32826);
		}

	}

	public static void drawRect(int var0, int var1, int var2, int var3, int var4) {
		int var5;
		if (var0 < var2) {
			var5 = var0;
			var0 = var2;
			var2 = var5;
		}

		if (var1 < var3) {
			var5 = var1;
			var1 = var3;
			var3 = var5;
		}

		float var10 = (var4 >> 24 & 255) / 255.0F;
		float var6 = (var4 >> 16 & 255) / 255.0F;
		float var7 = (var4 >> 8 & 255) / 255.0F;
		float var8 = (var4 & 255) / 255.0F;
		Tessellator var9 = Tessellator.instance;
		GL11.glEnable(3042);
		GL11.glDisable(3553);
		GL11.glBlendFunc(770, 771);
		GL11.glColor4f(var6, var7, var8, var10);
		var9.startDrawingQuads();
		var9.addVertex(var0, var3, 0.0D);
		var9.addVertex(var2, var3, 0.0D);
		var9.addVertex(var2, var1, 0.0D);
		var9.addVertex(var0, var1, 0.0D);
		var9.draw();
		GL11.glEnable(3553);
		GL11.glDisable(3042);
	}*/

	@Override
	protected void drawGradientRect(int var1, int var2, int var3, int var4, int var5, int var6) {
		drawGradientRect(var1, var2, var3, var4, var5, var6, this.zLevel);
	}

	protected static void drawGradientRect(int var1, int var2, int var3, int var4, int var5, int var6, double z) {
		float var7 = (var5 >> 24 & 255) / 255.0F;
		float var8 = (var5 >> 16 & 255) / 255.0F;
		float var9 = (var5 >> 8 & 255) / 255.0F;
		float var10 = (var5 & 255) / 255.0F;
		float var11 = (var6 >> 24 & 255) / 255.0F;
		float var12 = (var6 >> 16 & 255) / 255.0F;
		float var13 = (var6 >> 8 & 255) / 255.0F;
		float var14 = (var6 & 255) / 255.0F;
		GL11.glDisable(3553);
		GL11.glEnable(3042);
		GL11.glDisable(3008);
		GL11.glBlendFunc(770, 771);
		GL11.glShadeModel(7425);
		Tessellator var15 = Tessellator.instance;
		var15.startDrawingQuads();
		var15.setColorRGBA_F(var8, var9, var10, var7);
		var15.addVertex(var3, var2, z);
		var15.addVertex(var1, var2, z);
		var15.setColorRGBA_F(var12, var13, var14, var11);
		var15.addVertex(var1, var4, z);
		var15.addVertex(var3, var4, z);
		var15.draw();
		GL11.glShadeModel(7424);
		GL11.glDisable(3042);
		GL11.glEnable(3008);
		GL11.glEnable(3553);
	}
}
