package com.tom.cpm.shared.model.render;

import com.tom.cpl.util.ItemSlot;

public class ItemRenderer {
	public ItemSlot slot;
	public int slotID;

	public ItemRenderer(ItemSlot slot, int slotID) {
		this.slot = slot;
		this.slotID = slotID;
	}

	public ItemRenderer(ItemRenderer itemRenderer) {
		this.slot = itemRenderer.slot;
		this.slotID = itemRenderer.slotID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((slot == null) ? 0 : slot.hashCode());
		result = prime * result + slotID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ItemRenderer other = (ItemRenderer) obj;
		if (slot != other.slot) return false;
		if (slotID != other.slotID) return false;
		return true;
	}
}
