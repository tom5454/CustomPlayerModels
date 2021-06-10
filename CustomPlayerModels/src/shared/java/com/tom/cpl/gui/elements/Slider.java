package com.tom.cpl.gui.elements;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.math.MathHelper;

public class Slider extends GuiElement {
	protected String name;
	private Runnable action;
	private boolean enableDrag;
	protected float v;
	public Slider(IGui gui, String name) {
		super(gui);
		this.name = name;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		int w = gui.textWidth(name);
		int bgColor = gui.getColors().button_fill;
		int color = gui.getColors().button_text_color;
		if(!enabled) {
			color = gui.getColors().button_text_disabled;
			bgColor = gui.getColors().button_disabled;
		} else if(bounds.isInBounds(mouseX, mouseY)) {
			color = gui.getColors().button_text_hover;
			bgColor = gui.getColors().button_hover;
		}
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, bgColor);
		gui.drawBox((int) (bounds.x+1 + v * (bounds.w - 5)), bounds.y+1, 3, bounds.h-2, gui.getColors().slider_bar);
		gui.drawText(bounds.x + bounds.w / 2 - w / 2, bounds.y + bounds.h / 2 - 4, name, color);
	}

	@Override
	public boolean mouseClick(int x, int y, int btn) {
		if(bounds.isInBounds(x, y)) {
			this.enableDrag = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseRelease(int x, int y, int btn) {
		enableDrag = false;
		return false;
	}

	@Override
	public boolean mouseDrag(int x, int y, int btn) {
		if(enableDrag) {
			v = (float) MathHelper.clamp((x - bounds.x) / (float) bounds.w, 0, 1);
			if(action != null)action.run();
			return true;
		}
		return false;
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	public float getValue() {
		return v;
	}

	public void setValue(float v) {
		this.v = v;
	}

	public void setText(String name) {
		this.name = name;
	}
}
