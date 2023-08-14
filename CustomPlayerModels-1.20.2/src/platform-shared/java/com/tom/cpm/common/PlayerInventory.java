package com.tom.cpm.common;

import com.tom.cpl.item.Inventory;
import com.tom.cpl.item.Stack;
import com.tom.cpm.shared.animation.AnimationState;

public class PlayerInventory implements Inventory {
	private net.minecraft.world.entity.player.Inventory inv;

	public static void setInv(AnimationState a, net.minecraft.world.entity.player.Inventory inv) {
		if(!(a.playerInventory instanceof PlayerInventory))a.playerInventory = new PlayerInventory();
		((PlayerInventory) a.playerInventory).inv = inv;
		a.heldSlot = inv.selected;
	}

	@Override
	public int size() {
		return inv == null ? 0 : inv.getContainerSize();
	}

	@Override
	public Stack getInSlot(int i) {
		return ItemStackHandlerImpl.impl.wrap(inv.getItem(i));
	}

	@Override
	public void reset() {
		inv = null;
	}
}
