package com.tom.cpl.gui.util;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.item.Stack;

public class StackSlot extends GuiElement {
	protected Stack stack;

	public StackSlot(IGui gui) {
		super(gui);
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		if (stack != null) {
			gui.drawStack(bounds.x + 1, bounds.y + 1, stack);
			if (event.isHovered(bounds)) {
				new StackTooltip(gui.getFrame(), stack).set();
			}
		}
	}

	public void setStack(Stack stack) {
		this.stack = stack;
	}
}
