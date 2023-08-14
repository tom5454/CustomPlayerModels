package com.tom.cpm.web.client.item;

import com.tom.cpl.nbt.NBTTagCompound;

public class ItemStack {
	public static final ItemStack EMPTY = new ItemStack(Item.AIR, 0);
	public final Item item;
	public int count;
	public NBTTagCompound tag;

	public ItemStack(Item item, int count) {
		this.item = item;
		this.count = count;
	}

}
