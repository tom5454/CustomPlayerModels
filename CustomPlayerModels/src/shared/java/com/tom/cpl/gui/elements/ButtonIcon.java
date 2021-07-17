package com.tom.cpl.gui.elements;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;

public class ButtonIcon extends Button {
	private String name;
	private int u, v;
	public ButtonIcon(IGui gui, String name, int u, int v, Runnable action) {
		super(gui, "", action);
		this.name = name;
		this.u = u;
		this.v = v;
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		int bgColor = gui.getColors().button_fill;
		if(!enabled) {
			bgColor = gui.getColors().button_disabled;
		} else if(event.isHovered(bounds)) {
			bgColor = gui.getColors().button_hover;
		}
		if(event.isHovered(bounds) && tooltip != null)
			tooltip.set();
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, bgColor);
		gui.drawTexture(bounds.x+2, bounds.y+2, bounds.w-4, bounds.h-4, u, v, name);
	}

	public void setU(int u) {
		this.u = u;
	}

	public void setV(int v) {
		this.v = v;
	}
}
