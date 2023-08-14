package com.tom.cpl.item;

import java.util.List;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.tag.Tag;
import com.tom.cpl.tag.TagManager;
import com.tom.cpm.shared.MinecraftCommonAccess;

public class Stack {
	@SuppressWarnings("unchecked")
	public static final ItemStackHandler<Object> handler = (ItemStackHandler<Object>) MinecraftCommonAccess.get().getItemStackHandler();
	public static final Stack EMPTY = handler.emptyObject();
	private final Object stack;

	protected Stack(Object stack) {
		this.stack = stack;
	}

	public List<Tag<Stack>> getStackTags(TagManager<Stack> m) {
		return m.listStackTags(this);
	}

	public int getCount() {
		return handler.getCount(stack);
	}

	public int getMaxCount() {
		return handler.getMaxCount(stack);
	}

	public int getDamage() {
		return handler.getDamage(stack);
	}

	public int getMaxDamage() {
		return handler.getMaxDamage(stack);
	}

	public NBTTagCompound getNbtTag() {
		return handler.getTag(stack);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Stack other = (Stack) obj;
		return handler.itemEquals(stack, other.stack);
	}

	public boolean isInNativeTag(String e) {
		return handler.isInTag(e, this);
	}

	public boolean is(Tag<Stack> e) {
		return e.is(this);
	}

	public List<String> listNativeTags() {
		return handler.listTags(stack);
	}

	public String getItemId() {
		return handler.getItemId(stack);
	}

	protected Object getStack() {
		return stack;
	}
}
