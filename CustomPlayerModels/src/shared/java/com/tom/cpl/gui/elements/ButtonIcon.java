package com.tom.cpl.gui.elements;

import com.tom.cpl.gui.IGui;

public class ButtonIcon extends GuiElement {
	private String name;
	private Runnable action;
	private int u, v;
	public ButtonIcon(IGui gui, String name, int u, int v, Runnable action) {
		super(gui);
		this.name = name;
		this.action = action;
		this.u = u;
		this.v = v;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		int bgColor = gui.getColors().button_fill;
		if(!enabled) {
			bgColor = gui.getColors().button_disabled;
		} else if(bounds.isInBounds(mouseX, mouseY)) {
			bgColor = gui.getColors().button_hover;
		}
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, bgColor);
		gui.drawTexture(bounds.x+2, bounds.y+2, bounds.w-4, bounds.h-4, u, v, name);// + off * (bounds.w - 4)
	}

	@Override
	public boolean mouseClick(int x, int y, int btn) {
		if(enabled && bounds.isInBounds(x, y)) {
			action.run();
			return true;
		}
		return false;
	}

	public void setU(int u) {
		this.u = u;
	}

	public void setV(int v) {
		this.v = v;
	}
}
