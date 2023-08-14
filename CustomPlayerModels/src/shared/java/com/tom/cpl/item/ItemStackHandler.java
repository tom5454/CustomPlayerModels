package com.tom.cpl.item;

import java.util.List;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.tag.NativeTagManager;

public abstract class ItemStackHandler<S> implements NativeTagManager<Stack> {
	public abstract List<String> listTags(S stack);
	@Override
	public abstract List<String> listNativeTags();
	public abstract List<Stack> getAllItems();
	public abstract int getCount(S stack);
	public abstract int getMaxCount(S stack);
	public abstract int getDamage(S stack);
	public abstract int getMaxDamage(S stack);
	public abstract boolean itemEquals(S a, S b);
	public abstract boolean itemEqualsFull(S a, S b);
	public abstract NBTTagCompound getTag(S stack);
	public abstract boolean isInTag(String tag, S stack);
	public abstract String getItemId(S stack);

	public Stack wrap(S stack) {
		return new Stack(stack);
	}

	@SuppressWarnings("unchecked")
	public S unwrap(Stack s) {
		return (S) s.getStack();
	}

	@Override
	public List<String> listNativeTags(Stack stack) {
		return listTags(unwrap(stack));
	}

	@Override
	public boolean isInNativeTag(String tag, Stack stack) {
		return isInTag(tag, unwrap(stack));
	}
}
