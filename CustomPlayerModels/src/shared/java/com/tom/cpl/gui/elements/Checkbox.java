package com.tom.cpl.gui.elements;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;

public class Checkbox extends GuiElement {
	private String name;
	protected Runnable action;
	private boolean selected;
	private Tooltip tooltip;
	public Checkbox(IGui gui, String name) {
		super(gui);
		this.name = name;
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		int bgColor = gui.getColors().button_fill;
		int color = gui.getColors().button_text_color;
		if(!enabled) {
			color = gui.getColors().button_text_disabled;
			bgColor = gui.getColors().button_disabled;
		} else if(event.isHovered(bounds)) {
			color = gui.getColors().button_text_hover;
			bgColor = gui.getColors().button_hover;
		}
		if(event.isHovered(bounds) && tooltip != null)tooltip.set();
		gui.drawBox(bounds.x, bounds.y, bounds.h, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x+1, bounds.y+1, bounds.h-2, bounds.h-2, bgColor);
		gui.drawText(bounds.x + bounds.h + 3, bounds.y + bounds.h / 2 - 4, name, color);
		if(selected) {
			gui.drawText(bounds.x + bounds.h / 2 - 4, bounds.y + bounds.h / 2 - 4, "X", color);
		}
	}

	@Override
	public boolean mouseClick(int x, int y, int btn) {
		if(enabled && bounds.isInBounds(x, y)) {
			if(action != null)action.run();
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

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setTooltip(Tooltip tooltip) {
		this.tooltip = tooltip;
	}
}
