package com.tom.cpm.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import com.tom.cpl.item.ItemStackHandler;
import com.tom.cpl.item.NbtMapper;
import com.tom.cpl.item.Stack;
import com.tom.cpl.nbt.NBTTagCompound;

public class ItemStackHandlerImpl extends ItemStackHandler<ItemStack> {
	public static final ItemStackHandlerImpl impl = new ItemStackHandlerImpl();
	public static final NBT nbt = new NBT();

	@Override
	public int getCount(ItemStack stack) {
		return stack.getCount();
	}

	@Override
	public int getMaxCount(ItemStack stack) {
		return stack.getMaxStackSize();
	}

	@Override
	public int getDamage(ItemStack stack) {
		return stack.getDamageValue();
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return stack.getMaxDamage();
	}

	@Override
	public boolean itemEquals(ItemStack a, ItemStack b) {
		return ItemStack.isSame(a, b);
	}

	@Override
	public boolean itemEqualsFull(ItemStack a, ItemStack b) {
		return ItemStack.isSame(a, b) && ItemStack.tagMatches(a, b);
	}

	@Override
	public List<String> listNativeTags() {
		return ItemTags.getAllTags().getAvailableTags().stream().map(ResourceLocation::toString).collect(Collectors.toList());
	}

	@Override
	public List<Stack> getAllItems() {
		NonNullList<ItemStack> stacks = NonNullList.create();
		Registry.ITEM.entrySet().forEach(e -> e.getValue().fillItemCategory(ItemGroup.TAB_SEARCH, stacks));
		return stacks.stream().map(this::wrap).collect(Collectors.toList());
	}

	public static class NBT extends NbtMapper<INBT, CompoundNBT, ListNBT, NumberNBT> {

		@Override
		public long getLong(NumberNBT t) {
			return t.getAsLong();
		}

		@Override
		public int getInt(NumberNBT t) {
			return t.getAsInt();
		}

		@Override
		public short getShort(NumberNBT t) {
			return t.getAsShort();
		}

		@Override
		public byte getByte(NumberNBT t) {
			return t.getAsByte();
		}

		@Override
		public double getDouble(NumberNBT t) {
			return t.getAsDouble();
		}

		@Override
		public float getFloat(NumberNBT t) {
			return t.getAsFloat();
		}

		@Override
		public NumberNBT asNumber(INBT t) {
			return t instanceof NumberNBT ? (NumberNBT) t : null;
		}

		@Override
		public String getString(INBT t) {
			return t.getAsString();
		}

		@Override
		public INBT getTag(CompoundNBT t, String name) {
			return t.get(name);
		}

		@Override
		public ListNBT asList(INBT t) {
			return t instanceof ListNBT ? (ListNBT) t : null;
		}

		@Override
		public CompoundNBT asCompound(INBT t) {
			return t instanceof CompoundNBT ? (CompoundNBT) t : null;
		}

		@Override
		public int listSize(ListNBT t) {
			return t.size();
		}

		@Override
		public INBT getAt(ListNBT t, int i) {
			return t.get(i);
		}

		@Override
		public boolean contains(CompoundNBT t, String name, int type) {
			return t.contains(name, type);
		}

		@Override
		public CompoundNBT newCompound() {
			return new CompoundNBT();
		}

		@Override
		public ListNBT newList() {
			return new ListNBT();
		}

		@Override
		public Iterable<String> keys(CompoundNBT t) {
			return t.getAllKeys();
		}

		@Override
		public int getId(INBT t) {
			return t.getId();
		}

		@Override
		public byte[] getByteArray(INBT t) {
			return t instanceof ByteArrayNBT ? ((ByteArrayNBT) t).getAsByteArray() : new byte[0];
		}

		@Override
		public int[] getIntArray(INBT t) {
			return t instanceof IntArrayNBT ? ((IntArrayNBT) t).getAsIntArray() : new int[0];
		}

		@Override
		public long[] getLongArray(INBT t) {
			return t instanceof LongArrayNBT ? ((LongArrayNBT) t).getAsLongArray() : new long[0];
		}
	}

	@Override
	public NBTTagCompound getTag(ItemStack stack) {
		return stack.hasTag() ? nbt.wrap(stack.getTag()) : null;
	}

	@Override
	public List<Stack> listNativeEntries(String tag) {
		List<Stack> stacks = new ArrayList<>();
		if(tag.charAt(0) == '#') {
			ResourceLocation rl = ResourceLocation.tryParse(tag.substring(1));
			if (rl != null) {
				ITag<Item> t = ItemTags.getAllTags().getTag(rl);
				if (t != null) {
					t.getValues().stream().map(i -> wrap(new ItemStack(i))).forEach(stacks::add);
				}
			}
		} else {
			ResourceLocation rl = ResourceLocation.tryParse(tag);
			Item item = Registry.ITEM.get(rl);
			if (item != null) {
				stacks.add(wrap(new ItemStack(item)));
			}
		}
		return stacks;
	}

	@Override
	public boolean isInTag(String tag, ItemStack stack) {
		if(tag.charAt(0) == '#') {
			ResourceLocation rl = ResourceLocation.tryParse(tag.substring(1));
			if (rl != null) {
				ITag<Item> t = ItemTags.getAllTags().getTag(rl);
				if (t != null) {
					return t.contains(stack.getItem());
				}
			}
		} else {
			return getItemId(stack).equals(tag);
		}
		return false;
	}

	@Override
	public List<String> listTags(ItemStack stack) {
		return ItemTags.getAllTags().getMatchingTags(stack.getItem()).stream().map(e -> "#" + e).collect(Collectors.toList());
	}

	@Override
	public String getItemId(ItemStack stack) {
		return Registry.ITEM.getKey(stack.getItem()).toString();
	}

	@Override
	public Stack emptyObject() {
		return wrap(ItemStack.EMPTY);
	}
}
