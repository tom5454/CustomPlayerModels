package com.tom.cpm.shared.gui;

import java.util.function.Consumer;

import com.tom.cpm.shared.math.Box;
import com.tom.cpm.shared.math.Vec2i;

public interface IGui {
	void drawBox(int x, int y, int w, int h, int color);
	void drawGradientBox(int x, int y, int w, int h, int topLeft, int topRight, int bottomLeft, int bottomRight);
	void drawText(int x, int y, String text, int color);
	String i18nFormat(String key, Object... obj);
	int textWidth(String text);
	void drawTexture(int x, int y, int w, int h, int u, int v, String texture);
	void drawTexture(int x, int y, int width, int height, float u1, float v1, float u2, float v2);
	void close();
	void pushMatrix();
	void setPosOffset(Box box);
	void popMatrix();
	UIColors getColors();
	void setCloseListener(Consumer<Runnable> listener);
	Vec2i getOffset();
	boolean isShiftDown();
	boolean isCtrlDown();
	boolean isAltDown();
	KeyCodes getKeyCodes();
	NativeGuiComponents getNative();
	void setClipboardText(String text);
	Frame getFrame();

	default void drawBox(float x, float y, float w, float h, int color) {
		drawBox((int) x, (int) y, (int) w, (int) h, color);
	}
}
