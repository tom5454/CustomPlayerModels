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
	private int index;
	private long lastInc;
	private List<Stack> stacks;

	public StackTagSlot(IGui gui, TagManager<Stack> mngr, String elem) {
		super(gui);
		this.mngr = mngr;
		stacks = mngr.listStacks(Arrays.asList(elem));
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		if (stacks.isEmpty()) {
			gui.drawTexture(bounds.x, bounds.y, 16, 16, 64, 0, "editor");
			return;
		}
		long ticks = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
		if (lastInc + 1000 < ticks && stacks.size() > 1) {
			lastInc = ticks;
			index = (index + 1) % stacks.size();
		}
		Stack st = stacks.get(index);
		gui.drawStack(bounds.x, bounds.y, st);
		if (event.isHovered(bounds)) {
			new StackTooltip(gui.getFrame(), st).set();
		}
	}

	public void setStacks(String elem) {
		stacks = mngr.listStacks(Arrays.asList(elem));
		index = 0;
	}
}
