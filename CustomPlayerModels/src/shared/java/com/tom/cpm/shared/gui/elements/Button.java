package com.tom.cpm.shared.gui.elements;

import com.tom.cpm.shared.gui.IGui;

public class Button extends GuiElement {
	protected String name;
	protected Runnable action;
	protected Tooltip tooltip;
	public Button(IGui gui, String name, Runnable action) {
		super(gui);
		this.name = name;
		this.action = action;
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
			if(tooltip != null)tooltip.set();
		}
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, bgColor);
		gui.drawText(bounds.x + bounds.w / 2 - w / 2, bounds.y + bounds.h / 2 - 4, name, color);
	}

	@Override
	public boolean mouseClick(int x, int y, int btn) {
		if(enabled && bounds.isInBounds(x, y)) {
			if(action != null)
				action.run();
			return true;
		}
		return false;
	}

	public void setText(String name) {
		this.name = name;
	}

	public String getText() {
		return name;
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	public void setTooltip(Tooltip tooltip) {
		this.tooltip = tooltip;
	}
}
