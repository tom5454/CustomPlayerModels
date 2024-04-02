package com.tom.cpm.common;

import com.tom.cpl.item.Inventory;
import com.tom.cpl.item.NamedSlot;
import com.tom.cpl.item.Stack;
import com.tom.cpm.shared.animation.AnimationState;

public class PlayerInventory implements Inventory {
	private net.minecraft.entity.player.PlayerInventory inv;

	public static void setInv(AnimationState a, net.minecraft.entity.player.PlayerInventory inv) {
		if(!(a.playerInventory instanceof PlayerInventory))a.playerInventory = new PlayerInventory();
		((PlayerInventory) a.playerInventory).inv = inv;
	}

	@Override
	public int size() {
		return inv == null ? 0 : inv.size();
	}

	@Override
	public Stack getInSlot(int i) {
		return ItemStackHandlerImpl.impl.wrap(inv.getStack(i));
	}

	@Override
	public void reset() {
		inv = null;
	}

	@Override
	public int getNamedSlotId(NamedSlot slot) {
		switch (slot) {
		case MAIN_HAND: return inv.selectedSlot;
		case ARMOR_BOOTS: return 0 + inv.main.length;
		case ARMOR_CHESTPLATE: return 2 + inv.main.length;
		case ARMOR_HELMET: return 3 + inv.main.length;
		case ARMOR_LEGGINGS: return 1 + inv.main.length;
		case OFF_HAND: return -1;
		default: throw new IllegalArgumentException("Unexpected value: " + slot);
		}
	}
}
