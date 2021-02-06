package com.tom.cpm.shared.gui.elements;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.math.Box;

public class Spinner extends GuiElement {
	private float value;
	private int dp = 3;
	private List<Runnable> changeListeners = new ArrayList<>();
	public Spinner(IGui gui) {
		super(gui);
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x + 1, bounds.y + 1, bounds.w - 2, bounds.h - 2, enabled ? gui.getColors().button_fill : gui.getColors().button_disabled);
		Box bUp = new Box(bounds.x + bounds.w - 9, bounds.y, bounds.w, bounds.h / 2);
		Box bDown = new Box(bounds.x + bounds.w - 9, bounds.y + bounds.h / 2, bounds.w, bounds.h / 2);
		gui.drawTexture(bounds.x + bounds.w - 9, bounds.y + 1, 8, 8, enabled ? bounds.isInBounds(mouseX, mouseY) && bUp.isInBounds(mouseX, mouseY) ? 16 : 8 : 0, 0, "editor");
		gui.drawTexture(bounds.x + bounds.w - 9, bounds.y + bounds.h / 2, 8, 8, enabled ? bounds.isInBounds(mouseX, mouseY) && bDown.isInBounds(mouseX, mouseY) ? 16 : 8 : 0, 8, "editor");
		gui.drawText(bounds.x + 2, bounds.y + 2, String.format("%." + dp + "f", value), gui.getColors().button_text_color);
	}

	@Override
	public boolean mouseClick(int x, int y, int btn) {
		if(bounds.isInBounds(x, y) && enabled) {
			Box bUp = new Box(bounds.x + bounds.w - 9, bounds.y, bounds.w, bounds.h / 2);
			Box bDown = new Box(bounds.x + bounds.w - 9, bounds.y + bounds.h / 2, bounds.w, bounds.h / 2);
			float v = gui.isAltDown() && dp > 1 ? (gui.isShiftDown() && dp > 2 ? 0.001f : 0.01f) : (gui.isShiftDown() && dp > 0 ? 0.1f : (gui.isCtrlDown() ? (gui.isShiftDown() ? 100f : 10f) : 1f));
			if(bUp.isInBounds(x, y)) {
				value += v;
				changeListeners.forEach(Runnable::run);
			} else if(bDown.isInBounds(x, y)) {
				value -= v;
				changeListeners.forEach(Runnable::run);
			} else {
				//TODO
			}
			return true;
		} else {
			//TODO
			return false;
		}
	}

	public void addChangeListener(Runnable r) {
		changeListeners.add(r);
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public void setDp(int dp) {
		this.dp = dp;
	}
}
