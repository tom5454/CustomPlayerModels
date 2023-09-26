package com.tom.cpl.gui.util;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.item.Stack;
import com.tom.cpl.math.Box;

public class StackTooltip extends Tooltip {
	private Stack stack;

	public StackTooltip(Frame frm, Stack stack) {
		super(frm);
		this.stack = stack;
		setBounds(new Box(0, 0, 0, 0));
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		gui.pushMatrix();
		gui.setupCut();
		gui.drawStackTooltip(bounds.x, bounds.y, stack);
		gui.popMatrix();
	}
}
