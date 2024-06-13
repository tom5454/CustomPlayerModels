package com.tom.cpm.common;

import net.minecraft.world.entity.EquipmentSlot;

import com.tom.cpl.item.Inventory;
import com.tom.cpl.item.NamedSlot;
import com.tom.cpl.item.Stack;
import com.tom.cpm.shared.animation.AnimationState;

public class PlayerInventory implements Inventory {
	private net.minecraft.world.entity.player.Inventory inv;

	public static void setInv(AnimationState a, net.minecraft.world.entity.player.Inventory inv) {
		if(!(a.playerInventory instanceof PlayerInventory))a.playerInventory = new PlayerInventory();
		((PlayerInventory) a.playerInventory).inv = inv;
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

	@Override
	public int getNamedSlotId(NamedSlot slot) {
		return switch (slot) {
		case MAIN_HAND -> inv.selected;
		case ARMOR_BOOTS -> EquipmentSlot.FEET.getIndex(inv.items.size());
		case ARMOR_CHESTPLATE -> EquipmentSlot.CHEST.getIndex(inv.items.size());
		case ARMOR_HELMET -> EquipmentSlot.HEAD.getIndex(inv.items.size());
		case ARMOR_LEGGINGS -> EquipmentSlot.LEGS.getIndex(inv.items.size());
		case OFF_HAND -> inv.items.size() + inv.armor.size();
		default -> throw new IllegalArgumentException("Unexpected value: " + slot);
		};
	}
}
