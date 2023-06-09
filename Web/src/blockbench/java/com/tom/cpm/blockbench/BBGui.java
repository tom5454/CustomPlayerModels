package com.tom.cpm.blockbench;

import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyCodes;
import com.tom.cpl.gui.NativeGuiComponents;
import com.tom.cpl.gui.UIColors;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.text.IText;
import com.tom.cpm.web.client.util.I18n;

import elemental2.dom.DomGlobal;

public class BBGui implements IGui {
	private IGui gui;
	private UIColors colors;
	private Frame frm;

	private class BBFrame extends Frame {

		private BBFrame() {
			super(BBGui.this);
		}

		@Override
		public void initFrame(int width, int height) {
		}

		@Override
		public void openPopup(PopupPanel popup) {
			if(gui instanceof BBGui) {
				//make iframe
			}
			super.openPopup(popup);
		}
	}

	public static Frame makeFrame() {
		return new BBGui().frm;
	}

	private BBGui() {
		colors = new UIColors();
		frm = new BBFrame();
		frm.init(600, 400);
	}

	@Override
	public void drawBox(int x, int y, int w, int h, int color) {
		if(gui == null)return;
		gui.drawBox(x, y, w, h, color);
	}

	@Override
	public void drawGradientBox(int x, int y, int w, int h, int topLeft, int topRight, int bottomLeft,
			int bottomRight) {
		if(gui == null)return;
		gui.drawGradientBox(x, y, w, h, topLeft, topRight, bottomLeft, bottomRight);
	}

	@Override
	public void drawText(int x, int y, String text, int color) {
		if(gui == null)return;
		gui.drawText(x, y, text, color);
	}

	@Override
	public String i18nFormat(String key, Object... obj) {
		if(gui == null)return String.format(I18n.get(key), obj);
		return gui.i18nFormat(key, obj);
	}

	@Override
	public int textWidth(String text) {
		if(gui == null)return 1;
		return gui.textWidth(text);
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture) {
		if(gui == null)return;
		gui.drawTexture(x, y, w, h, u, v, texture);
	}

	@Override
	public void drawTexture(int x, int y, int w, int h, int u, int v, String texture, int color) {
		if(gui == null)return;
		gui.drawTexture(x, y, w, h, u, v, texture, color);
	}

	@Override
	public void drawTexture(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
		if(gui == null)return;
		gui.drawTexture(x, y, width, height, u1, v1, u2, v2);
	}

	@Override
	public void closeGui() {
		if(gui == null)return;
		gui.closeGui();
	}

	@Override
	public UIColors getColors() {
		if(gui == null)return colors;
		return gui.getColors();
	}

	@Override
	public void setCloseListener(Consumer<Runnable> listener) {
		if(gui == null)return;
		gui.setCloseListener(listener);
	}

	@Override
	public boolean isShiftDown() {
		if(gui == null)return false;
		return gui.isShiftDown();
	}

	@Override
	public boolean isCtrlDown() {
		if(gui == null)return false;
		return gui.isCtrlDown();
	}

	@Override
	public boolean isAltDown() {
		if(gui == null)return false;
		return gui.isAltDown();
	}

	@Override
	public KeyCodes getKeyCodes() {
		if(gui == null)return null;
		return gui.getKeyCodes();
	}

	@Override
	public NativeGuiComponents getNative() {
		if(gui == null)return null;
		return gui.getNative();
	}

	@Override
	public void setClipboardText(String text) {
		if(gui == null)return;
		gui.setClipboardText(text);
	}

	@Override
	public String getClipboardText() {
		if(gui == null)return "";
		return gui.getClipboardText();
	}

	@Override
	public Frame getFrame() {
		if(gui == null)return frm;
		return gui.getFrame();
	}

	@Override
	public void setScale(int value) {
		if(gui == null)return;
		gui.setScale(value);
	}

	@Override
	public int getScale() {
		return gui.getScale();
	}

	@Override
	public int getMaxScale() {
		return gui.getMaxScale();
	}

	@Override
	public CtxStack getStack() {
		return gui.getStack();
	}

	@Override
	public void displayError(String msg) {
		if(gui == null)return;
		gui.displayError(msg);
	}

	@Override
	public void setupCut() {
		if(gui == null)return;
		gui.setupCut();
	}

	@Override
	public void drawFormattedText(float x, float y, IText text, int color, float scale) {
		if(gui == null)return;
		gui.drawFormattedText(x, y, text, color, scale);
	}

	@Override
	public int textWidthFormatted(IText text) {
		if(gui == null)return 1;
		return gui.textWidthFormatted(text);
	}

	@Override
	public void openURL0(String uri) {
		DomGlobal.window.open(uri, "_blank");
	}
}
