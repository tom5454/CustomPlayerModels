package com.tom.cpm.web.client.render;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyCodes;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.NativeGuiComponents;
import com.tom.cpl.gui.UIColors;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.TextField.ITextField;
import com.tom.cpl.item.Stack;
import com.tom.cpl.math.Box;
import com.tom.cpl.text.IText;
import com.tom.cpm.shared.gui.panel.Panel3d;
import com.tom.cpm.shared.gui.panel.ViewportPanelBase3d;
import com.tom.cpm.web.client.EventHandler;
import com.tom.cpm.web.client.JSKeyCodes;
import com.tom.cpm.web.client.WebChooser;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.render.RenderSystem.VertexBuffer2d;
import com.tom.cpm.web.client.util.Clipboard;
import com.tom.cpm.web.client.util.I18n;

import elemental2.dom.CSSProperties.FontSizeUnionType;
import elemental2.dom.CSSProperties.HeightUnionType;
import elemental2.dom.CSSProperties.WidthUnionType;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

public class GuiImpl implements IGui, EventHandler {
	private static float fps;
	private static final NativeGuiComponents nativeComponents = new NativeGuiComponents();
	private static final KeyCodes CODES = new JSKeyCodes();
	private Frame gui;
	private CtxStack stack;
	private UIColors colors;
	private Consumer<Runnable> closeListener;
	private int width, height;
	private Set<NativeElement> nativeElements = new HashSet<>();

	static {
		nativeComponents.register(TextField.class, TxtField::new);
		nativeComponents.register(Panel3d.class, Panel3dImpl::new);
		nativeComponents.register(FileChooserPopup.class, WebChooser::new);
	}

	public GuiImpl() {
		this.colors = new UIColors();
	}

	public void setGui(Frame gui) {
		this.gui = gui;
	}

	@Override
	public void render(int mouseX, int mouseY) {
		fps = (RenderSystem.getFPS() * 9 + fps) / 10;
		nativeElements.forEach(NativeElement::preRender);
		try {
			RenderSystem.enableScissor();
			stack = new CtxStack(width, height);
			gui.draw(mouseX, mouseY, 0);
		} catch (Throwable e) {
			onGuiException("Error drawing gui", e, true);
		} finally {
			RenderSystem.disableScissor();
			String s = "CPM Web App (" + WebMC.platform + ")";
			RenderSystem.text(s, width - textWidth(s) - 4, 2, 0xff000000);
			s = "FPS: " + Java.fixed(fps, 1);
			RenderSystem.text(s, width - textWidth(s) - 4, 11, 0xff000000);
			nativeElements.forEach(NativeElement::postRender);
		}
	}

	@Override
	public void drawBox(int x, int y, int w, int h, int color) {
		x += getOffset().x;
		y += getOffset().y;
		rect(x, y, w, h, color);
	}

	@Override
	public void drawBox(float x, float y, float w, float h, int color) {
		x += getOffset().x;
		y += getOffset().y;
		rect(x, y, w, h, color);
	}

	private void rect(float x, float y, float w, float h, int color) {
		VertexBuffer2d buf = RenderSystem.getColor();
		float left = x;
		float top = y;
		float right = x + w;
		float bottom = y + h;

		float a = (color >> 24 & 255) / 255.0F;
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;

		buf.pos(right, top).color(r, g, b, a).endVertex();
		buf.pos(left, top).color(r, g, b, a).endVertex();
		buf.pos(left, bottom).color(r, g, b, a).endVertex();
		buf.pos(right, bottom).color(r, g, b, a).endVertex();

		buf.finish();

		RenderSystem.boxDrawn(x, y, w, h);
	}

	@Override
	public void init(int w, int h) {
		nativeElements.forEach(NativeElement::remove);
		nativeElements.clear();
		width = w;
		height = h;
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
		RenderSystem.text(text, x, y, color | 0xff000000);
	}

	@Override
	public boolean keyPressed(int keyCode, String key) {
		try {
			KeyboardEvent evt = new KeyboardEvent(keyCode, -1, (char) -1, key);
			gui.keyPressed(evt);
			return evt.isConsumed();
		} catch (Throwable e) {
			onGuiException("Error processing key event", e, false);
			return true;
		}
	}

	@Override
	public boolean charTyped(int codePoint) {
		try {
			KeyboardEvent evt = new KeyboardEvent(-1, -1, (char) codePoint, null);
			gui.keyPressed(evt);
			return evt.isConsumed();
		} catch (Throwable e) {
			onGuiException("Error processing key event", e, false);
			return true;
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		try {
			MouseEvent evt = new MouseEvent(mouseX, mouseY, button);
			gui.mouseClick(evt);
			return evt.isConsumed();
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
			return true;
		}
	}

	@Override
	public boolean mouseDragged(int mouseX, int mouseY, int button) {
		try {
			MouseEvent evt = new MouseEvent(mouseX, mouseY, button);
			gui.mouseDrag(evt);
			return evt.isConsumed();
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
			return true;
		}
	}

	@Override
	public boolean mouseReleased(int mouseX, int mouseY, int button) {
		try {
			MouseEvent evt = new MouseEvent(mouseX, mouseY, button);
			gui.mouseRelease(evt);
			return evt.isConsumed();
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
			return true;
		}
	}

	@Override
	public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
		if(delta != 0) {
			try {
				MouseEvent evt = new MouseEvent(mouseX, mouseY, delta);
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
	public void displayError(String e) {
		nativeElements.forEach(NativeElement::remove);
		RenderSystem.remove();
		HTMLElement el = Js.uncheckedCast(RenderSystem.getDocument().getElementById("loadingBar"));
		el.style.display = "";
		el.innerHTML = e;
	}

	@Override
	public void closeGui() {
		if(closeListener != null) {
			closeListener.accept(WebMC::close);
		} else
			WebMC.close();
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture) {
		x += getOffset().x;
		y += getOffset().y;
		float left = x;
		float top = y;
		float right = x + w;
		float bottom = y + h;

		float tleft = u / 256f;
		float ttop = v / 256f;
		float tright = (u + w) / 256f;
		float tbottom = (v + h) / 256f;

		VertexBuffer2d buf = RenderSystem.getTexture("assets/cpm/textures/gui/" + texture + ".png");
		buf.pos(right, top).tex(tright, ttop).color(1, 1, 1, 1).endVertex();
		buf.pos(left, top).tex(tleft, ttop).color(1, 1, 1, 1).endVertex();
		buf.pos(left, bottom).tex(tleft, tbottom).color(1, 1, 1, 1).endVertex();
		buf.pos(right, bottom).tex(tright, tbottom).color(1, 1, 1, 1).endVertex();

		buf.finish();

		RenderSystem.boxDrawn(x, y, w, h);
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture, int color) {
		x += getOffset().x;
		y += getOffset().y;
		float left = x;
		float top = y;
		float right = x + w;
		float bottom = y + h;

		float tleft = u / 256f;
		float ttop = v / 256f;
		float tright = (u + w) / 256f;
		float tbottom = (v + h) / 256f;

		float a = (color >> 24 & 255) / 255.0F;
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;

		VertexBuffer2d buf = RenderSystem.getTexture("assets/cpm/textures/gui/" + texture + ".png");
		buf.pos(right, top).tex(tright, ttop).color(r, g, b, a).endVertex();
		buf.pos(left, top).tex(tleft, ttop).color(r, g, b, a).endVertex();
		buf.pos(left, bottom).tex(tleft, tbottom).color(r, g, b, a).endVertex();
		buf.pos(right, bottom).tex(tright, tbottom).color(r, g, b, a).endVertex();

		buf.finish();

		RenderSystem.boxDrawn(x, y, w, h);
	}

	@Override
	public void drawTexture(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
		x += getOffset().x;
		y += getOffset().y;
		VertexBuffer2d buf = RenderSystem.getTextureDyn();
		buf.pos(x, y + height).tex(u1, v2).color(1, 1, 1, 1).endVertex();
		buf.pos(x + width, y + height).tex(u2, v2).color(1, 1, 1, 1).endVertex();
		buf.pos(x + width, y).tex(u2, v1).color(1, 1, 1, 1).endVertex();
		buf.pos(x, y).tex(u1, v1).color(1, 1, 1, 1).endVertex();
		buf.finish();

		RenderSystem.boxDrawn(x, y, width, height);
	}

	@Override
	public String i18nFormat(String key, Object... obj) {
		return String.format(I18n.get(key), obj);
	}

	@Override
	public void setupCut() {
		Box box = getContext().cutBox;
		RenderSystem.scissor(box.x, box.y, box.w, box.h);
	}

	@Override
	public int textWidth(String text) {
		return RenderSystem.getTextWidth(text);
	}

	private static class TxtField extends HTMLNativeElement<HTMLInputElement> implements ITextField {
		private Runnable eventListener;
		private boolean settingText, focused, enabled;

		public TxtField(TextField fieldIn) {
			super("input", fieldIn.get());
			element.style.fontFamily = "Minecraftia";
			element.onchange = e -> {
				changed();
				return null;
			};
			element.onkeypress = e -> {
				changed();
				return null;
			};
			element.onpaste = e -> {
				changed();
				return null;
			};
			element.oninput = e -> {
				changed();
				return null;
			};
			element.style.fontSize = FontSizeUnionType.of(RenderSystem.fontSize + "px");
			element.style.color = toCSSColor(this0.colors.label_text_color);
			this.enabled = true;
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks, Box bounds) {
			setBounds(bounds, focused);
			if(!focused) {
				this0.pushMatrix();
				this0.setPosOffset(bounds);
				this0.setupCut();
				this0.drawText(2, bounds.h / 2 - 3, getText(), this0.getColors().label_text_color);
				this0.popMatrix();
				this0.setupCut();
			}
		}

		@Override
		public void keyPressed(KeyboardEvent evt) {
			if(evt.isConsumed())return;
			if(evt.keyCode == CODES.KEY_TAB)return;
			if(isFocused()) {
				passEvent();
				evt.consume();
			}
		}

		@Override
		public void mouseClick(MouseEvent evt) {
			if(evt.isHovered(bounds)) {
				focused = true;
				divScissor.style.visibility = "visible";
				element.focus();
				element.select();
				passEvent();
				evt.consume();
			} else {
				focused = false;
				element.blur();
			}
		}

		@Override
		public String getText() {
			return element.value;
		}

		@Override
		public void setText(String txt) {
			element.value = txt;
		}

		@Override
		public void setEventListener(Runnable eventListener) {
			this.eventListener = eventListener;
		}

		public void changed() {
			if(eventListener != null && !settingText && enabled)
				RenderSystem.withContext(eventListener);
		}

		@Override
		public void setEnabled(boolean enabled) {
			element.disabled = !enabled;
			this.enabled = enabled;
		}

		@Override
		public boolean isFocused() {
			return focused;
		}

		@Override
		public void setFocused(boolean focused) {
			this.focused = focused;
			if(focused) {
				element.focus();
			} else element.blur();
		}

		@Override
		protected void makeVisible() {
			element.focus();
		}

		@Override
		public int getCursorPos() {
			return element.selectionStart;
		}

		@Override
		public void setCursorPos(int pos) {
			element.setSelectionRange(pos, pos);
		}

		@Override
		public int getSelectionPos() {
			return element.selectionEnd;
		}

		@Override
		public void setSelectionPos(int pos) {
			element.setSelectionRange(Math.min(element.selectionStart, pos), Math.max(element.selectionStart, pos));
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
		return RenderSystem.isKeyDown(CODES.KEY_LEFT_SHIFT) || RenderSystem.isKeyDown(CODES.KEY_RIGHT_SHIFT);
	}

	@Override
	public boolean isCtrlDown() {
		return RenderSystem.isKeyDown(CODES.KEY_LEFT_CONTROL) || RenderSystem.isKeyDown(CODES.KEY_RIGHT_CONTROL);
	}

	@Override
	public boolean isAltDown() {
		return RenderSystem.isKeyDown(CODES.KEY_LEFT_ALT) || RenderSystem.isKeyDown(CODES.KEY_RIGHT_ALT);
	}

	@Override
	public KeyCodes getKeyCodes() {
		return CODES;
	}

	@Override
	public void drawGradientBox(int x, int y, int w, int h, int topLeft, int topRight, int bottomLeft,
			int bottomRight) {
		VertexBuffer2d buf = RenderSystem.getColor();
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
		buf.pos(right, top).color(rtr, gtr, btr, atr).endVertex();
		buf.pos(left, top).color(rtl, gtl, btl, atl).endVertex();
		buf.pos(left, bottom).color(rbl, gbl, bbl, abl).endVertex();
		buf.pos(right, bottom).color(rbr, gbr, bbr, abr).endVertex();
		buf.finish();
		RenderSystem.boxDrawn(x, y, w, h);
	}

	@Override
	public NativeGuiComponents getNative() {
		return nativeComponents;
	}

	@Override
	public void setClipboardText(String text) {
		Clipboard.writeText(text);
	}

	@Override
	public Frame getFrame() {
		return gui;
	}

	@Override
	public String getClipboardText() {
		String in = DomGlobal.window.prompt("Text to paste");
		if(in == null)return "";
		return in;
	}

	@Override
	public void setScale(int value) {
		RenderSystem.setScale(value);
	}

	@Override
	public int getScale() {
		return RenderSystem.sizeRequest;
	}

	@Override
	public int getMaxScale() {
		return RenderSystem.maxScale - 1;
	}

	@Override
	public CtxStack getStack() {
		return stack;
	}

	@Override
	public void tick() {
		ViewportPanelBase3d.manager.getAnimationEngine().tick();
		try {
			gui.tick();
		} catch (Throwable e) {
			onGuiException("Error in tick gui", e, true);
		}
	}

	public static interface NativeElement {
		void remove();
		void preRender();
		void postRender();
	}

	public static abstract class HTMLNativeElement<E extends Element> implements NativeElement {
		public boolean visible;
		protected HTMLDivElement divScissor, divInner;
		protected Box bounds = new Box(0, 0, 0, 0);
		protected E element;
		protected GuiImpl this0;

		public HTMLNativeElement(String name, IGui gui) {
			this0 = (GuiImpl) gui;
			element = Js.uncheckedCast(RenderSystem.getDocument().createElement(name));
			divScissor = Js.uncheckedCast(RenderSystem.getDocument().createElement("div"));
			divInner = Js.uncheckedCast(RenderSystem.getDocument().createElement("div"));
			divScissor.className = "scissorBox";
			divScissor.appendChild(divInner);
			divInner.className = "innerBox";
			divInner.appendChild(element);
			element.className = "innerElement";
			RenderSystem.bindEventListeners(element, false);
		}

		public void setBounds(Box bounds, boolean visible) {
			this0.addNative(this);
			int scale = RenderSystem.displayRatio;
			this.bounds.x = bounds.x;
			this.bounds.y = bounds.y;
			this.bounds.w = bounds.w;
			this.bounds.h = bounds.h;
			int w = bounds.w - 2;
			int h = bounds.h - 2;
			divInner.style.width = WidthUnionType.of((w * scale) + "px");
			divInner.style.height = HeightUnionType.of((h * scale) + "px");
			Ctx current = this0.getContext();
			Box cutBox = current.cutBox.intersect(new Box(current.off.x + bounds.x + 2, current.off.y + bounds.y + 1, w, h));
			cutBox.w = Math.max(cutBox.w, 0);
			cutBox.h = Math.max(cutBox.h, 0);
			if(visible) {
				if(cutBox.w > 0 && cutBox.h > 0) {
					divScissor.style.left = (cutBox.x * scale) + "px";
					divScissor.style.top = (cutBox.y * scale) + "px";
					divScissor.style.width = WidthUnionType.of((cutBox.w * scale) + "px");
					divScissor.style.height = HeightUnionType.of((cutBox.h * scale) + "px");
					this.visible = true;
					if(cutBox.y == current.off.y + bounds.y + 1) {
						divInner.style.bottom = null;
						divInner.style.top = "0px";
					} else {
						divInner.style.bottom = "0px";
						divInner.style.top = null;
					}
				}
			}
		}

		public void passEvent() {
			RenderSystem.passEvent();
		}

		@Override
		public void preRender() {
			visible = false;
		}

		@Override
		public void remove() {
			divScissor.remove();
		}

		@Override
		public void postRender() {
			if(visible) {
				if(divScissor.parentNode == null) {
					RenderSystem.getDocument().body.appendChild(divScissor);
					makeVisible();
				}
			} else if(divScissor.parentNode != null)
				divScissor.remove();
		}

		protected void makeVisible() {}

		public static String toCSSColor(int rgb) {
			int r = (rgb & 0x00FF0000) >>> 16;
			int g = (rgb & 0x0000FF00) >>> 8;
			int b = rgb & 0x000000FF;
			return "rgb(" + r + ", " + g + ", " + b + ")";
		}
	}

	private boolean forceClose;

	@Override
	public boolean canClose() {
		if(forceClose) {
			forceClose = false;
			return true;
		}
		int[] close = new int[] {0};
		if(closeListener != null) {
			closeListener.accept(() -> {
				forceClose = true;
				if(close[0] == 1)RenderSystem.getWindow().close();
				close[0] = 2;
			});
			if(close[0] == 0)close[0] = 1;
		} else {
			close[0] = 2;
		}
		return close[0] == 2;
	}

	public void addNative(NativeElement nat) {
		nativeElements.add(nat);
	}

	@Override
	public void filesDropped(List<File> files) {
		try {
			gui.filesDropped(files);
		} catch (Throwable e) {
			onGuiException("Error processing mouse event", e, false);
		}
	}

	@Override
	public boolean canScaleVanilla() {
		return false;
	}

	@Override
	public void drawFormattedText(float x, float y, IText text, int color, float scale) {
		x += getOffset().x;
		y += getOffset().y;
		RenderSystem.textFormatted(this, text, x, y, color | 0xff000000, scale);
	}

	@Override
	public int textWidthFormatted(IText text) {
		return RenderSystem.getTextWidthFormatted(this, text);
	}

	@Override
	public void openURL0(String url) {
		WebMC.getInstance().openURL(url);
	}

	@Override
	public void drawStack(int x, int y, Stack stack) {
	}

	@Override
	public void drawStackTooltip(int mx, int my, Stack stack) {
	}
}
