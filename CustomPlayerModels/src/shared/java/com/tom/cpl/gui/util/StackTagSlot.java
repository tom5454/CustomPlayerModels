package com.tom.cpl.gui.util;

import java.util.Arrays;
import java.util.List;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.item.Stack;
import com.tom.cpl.tag.TagManager;
import com.tom.cpm.shared.MinecraftClientAccess;

public class StackTagSlot extends GuiElement {
	private TagManager<Stack> mngr;
	private String elem;
	private int size, index;
	private long lastInc;
	public StackTagSlot(IGui gui, TagManager<Stack> mngr, String elem) {
		super(gui);
		this.mngr = mngr;
		this.elem = elem;
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		List<Stack> stacks = mngr.listStacks(Arrays.asList(elem));
		if (size != stacks.size()) {
			index = 0;
			size = stacks.size();
		}
		if (stacks.isEmpty()) {
			gui.drawBox(bounds.x, bounds.y, 16, 16, 0xFFFF0000);
			return;
		}
		long ticks = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
		if (lastInc + 1000 < ticks && size > 1) {
			lastInc = ticks;
			index = (index + 1) % size;
		}
		Stack st = stacks.get(index);
		gui.drawStack(bounds.x, bounds.y, st);
		if (event.isHovered(bounds)) {
			new StackTooltip(gui.getFrame(), st).set();
		}
	}
}
