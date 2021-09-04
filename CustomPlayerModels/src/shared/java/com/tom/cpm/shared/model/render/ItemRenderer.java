package com.tom.cpm.shared.model.render;

import com.tom.cpl.util.ItemSlot;

public class ItemRenderer {
	public ItemSlot slot;
	public int slotID;

	public ItemRenderer(ItemSlot slot, int slotID) {
		this.slot = slot;
		this.slotID = slotID;
	}
}
