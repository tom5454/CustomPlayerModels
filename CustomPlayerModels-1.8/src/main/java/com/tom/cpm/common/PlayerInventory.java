package com.tom.cpm.common;

import net.minecraft.entity.player.InventoryPlayer;

import com.tom.cpl.item.Inventory;
import com.tom.cpl.item.NamedSlot;
import com.tom.cpl.item.Stack;
import com.tom.cpm.shared.animation.AnimationState;

public class PlayerInventory implements Inventory {
	private InventoryPlayer inv;

	public static void setInv(AnimationState a, InventoryPlayer inv) {
		if(!(a.playerInventory instanceof PlayerInventory))a.playerInventory = new PlayerInventory();
		((PlayerInventory) a.playerInventory).inv = inv;
	}

	@Override
	public int size() {
		return inv == null ? 0 : inv.getSizeInventory();
	}

	@Override
	public Stack getInSlot(int i) {
		return ItemStackHandlerImpl.impl.wrap(inv.getStackInSlot(i));
	}

	@Override
	public void reset() {
		inv = null;
	}

	@Override
	public int getNamedSlotId(NamedSlot slot) {
		switch (slot) {
		case MAIN_HAND: return inv.currentItem;
		case ARMOR_BOOTS: return 0 + inv.mainInventory.length;
		case ARMOR_CHESTPLATE: return 2 + inv.mainInventory.length;
		case ARMOR_HELMET: return 3 + inv.mainInventory.length;
		case ARMOR_LEGGINGS: return 1 + inv.mainInventory.length;
		case OFF_HAND: return -1;
		default: throw new IllegalArgumentException("Unexpected value: " + slot);
		}
	}
}
