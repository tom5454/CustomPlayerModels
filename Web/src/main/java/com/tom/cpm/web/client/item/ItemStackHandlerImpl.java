package com.tom.cpm.web.client.item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.tom.cpl.item.ItemStackHandler;
import com.tom.cpl.item.Stack;
import com.tom.cpl.nbt.NBTTagCompound;

public class ItemStackHandlerImpl extends ItemStackHandler<ItemStack> {
	public static final ItemStackHandlerImpl impl = new ItemStackHandlerImpl();

	@Override
	public List<Stack> listNativeEntries(String tag) {
		return Collections.emptyList();
	}

	@Override
	public List<String> listTags(ItemStack stack) {
		return Collections.emptyList();
	}

	@Override
	public List<String> listNativeTags() {
		return Collections.emptyList();
	}

	@Override
	public List<Stack> getAllItems() {
		return Collections.emptyList();
	}

	@Override
	public int getCount(ItemStack stack) {
		return stack.count;
	}

	@Override
	public int getMaxCount(ItemStack stack) {
		return 64;
	}

	@Override
	public int getDamage(ItemStack stack) {
		return 0;
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return 0;
	}

	@Override
	public boolean itemEquals(ItemStack a, ItemStack b) {
		return a.item == b.item;
	}

	@Override
	public boolean itemEqualsFull(ItemStack a, ItemStack b) {
		return a.item == b.item && Objects.equals(a.tag, b.tag);
	}

	@Override
	public NBTTagCompound getTag(ItemStack stack) {
		return stack.tag;
	}

	@Override
	public boolean isInTag(String tag, ItemStack stack) {
		return false;
	}

	@Override
	public String getItemId(ItemStack stack) {
		return stack.item.id;
	}

	@Override
	public Stack emptyObject() {
		return wrap(ItemStack.EMPTY);
	}
}
