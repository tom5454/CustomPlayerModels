package com.tom.cpm.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

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
		return stack.getItemDamage();
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return stack.getMaxDamage();
	}

	@Override
	public boolean itemEquals(ItemStack a, ItemStack b) {
		return ItemStack.areItemsEqualIgnoreDurability(a, b);
	}

	@Override
	public boolean itemEqualsFull(ItemStack a, ItemStack b) {
		return ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b);
	}

	@Override
	public List<String> listNativeTags() {
		return Arrays.stream(OreDictionary.getOreNames()).map(t -> "#oredict:" + t).collect(Collectors.toList());
	}

	@Override
	public List<Stack> getAllElements() {
		NonNullList<ItemStack> stacks = NonNullList.create();
		ForgeRegistries.ITEMS.forEach(e -> e.getSubItems(CreativeTabs.SEARCH, stacks));
		return stacks.stream().map(this::wrap).collect(Collectors.toList());
	}

	public static class NBT extends NbtMapper<NBTBase, net.minecraft.nbt.NBTTagCompound, NBTTagList, NBTPrimitive> {

		@Override
		public long getLong(NBTPrimitive t) {
			return t.getLong();
		}

		@Override
		public int getInt(NBTPrimitive t) {
			return t.getInt();
		}

		@Override
		public short getShort(NBTPrimitive t) {
			return t.getShort();
		}

		@Override
		public byte getByte(NBTPrimitive t) {
			return t.getByte();
		}

		@Override
		public double getDouble(NBTPrimitive t) {
			return t.getDouble();
		}

		@Override
		public float getFloat(NBTPrimitive t) {
			return t.getFloat();
		}

		@Override
		public NBTPrimitive asNumber(NBTBase t) {
			return t instanceof NBTPrimitive ? (NBTPrimitive) t : null;
		}

		@Override
		public String getString(NBTBase t) {
			return t.toString();
		}

		@Override
		public NBTBase getTag(net.minecraft.nbt.NBTTagCompound t, String name) {
			return t.getTag(name);
		}

		@Override
		public NBTTagList asList(NBTBase t) {
			return t instanceof NBTTagList ? (NBTTagList) t : null;
		}

		@Override
		public net.minecraft.nbt.NBTTagCompound asCompound(NBTBase t) {
			return t instanceof net.minecraft.nbt.NBTTagCompound ? (net.minecraft.nbt.NBTTagCompound) t : null;
		}

		@Override
		public int listSize(NBTTagList t) {
			return t.tagCount();
		}

		@Override
		public NBTBase getAt(NBTTagList t, int i) {
			return t.get(i);
		}

		@Override
		public boolean contains(net.minecraft.nbt.NBTTagCompound t, String name, int type) {
			return t.hasKey(name, type);
		}

		@Override
		public net.minecraft.nbt.NBTTagCompound newCompound() {
			return new net.minecraft.nbt.NBTTagCompound();
		}

		@Override
		public NBTTagList newList() {
			return new NBTTagList();
		}

		@Override
		public Iterable<String> keys(net.minecraft.nbt.NBTTagCompound t) {
			return t.getKeySet();
		}

		@Override
		public int getId(NBTBase t) {
			return t.getId();
		}

		@Override
		public byte[] getByteArray(NBTBase t) {
			return t instanceof NBTTagByteArray ? ((NBTTagByteArray) t).getByteArray() : new byte[0];
		}

		@Override
		public int[] getIntArray(NBTBase t) {
			return t instanceof NBTTagIntArray ? ((NBTTagIntArray) t).getIntArray() : new int[0];
		}

		@Override
		public long[] getLongArray(NBTBase t) {
			return new long[0];//1.12 and older is missing the proper implementation for long arrays
		}
	}

	@Override
	public NBTTagCompound getTag(ItemStack stack) {
		return stack.hasTagCompound() ? nbt.wrap(stack.getTagCompound()) : null;
	}

	@Override
	public List<Stack> listNativeEntries(String tag) {
		if(tag.charAt(0) == '#') {
			if (tag.startsWith("#oredict:")) {
				tag = tag.substring(9);
				if (OreDictionary.doesOreNameExist(tag)) {
					return OreDictionary.getOres(tag).stream().map(this::wrap).collect(Collectors.toList());
				}
			}
		} else {
			ResourceLocation rl = tryParse(tag);
			Item item = ForgeRegistries.ITEMS.getValue(rl);
			if (item != null) {
				return Collections.singletonList(wrap(new ItemStack(item)));
			}
		}
		return Collections.emptyList();
	}

	public static ResourceLocation tryParse(String tag) {
		try {
			return new ResourceLocation(tag);
		} catch (Exception var2) {
			return null;
		}
	}

	@Override
	public boolean isInTag(String tag, ItemStack stack) {
		if (stack.isEmpty())return false;
		if(tag.charAt(0) == '#') {
			if (tag.startsWith("#oredict:")) {
				tag = tag.substring(9);
				if (OreDictionary.doesOreNameExist(tag)) {
					return ArrayUtils.contains(OreDictionary.getOreIDs(stack), OreDictionary.getOreID(tag));
				}
			}
			return false;
		} else {
			return getItemId(stack).equals(tag);
		}
	}

	@Override
	public List<String> listTags(ItemStack stack) {
		if (stack.isEmpty())return Collections.emptyList();
		return IntStream.of(OreDictionary.getOreIDs(stack)).mapToObj(i -> "#oredict:" + OreDictionary.getOreName(i)).collect(Collectors.toList());
	}

	@Override
	public String getItemId(ItemStack stack) {
		return stack.getItem().delegate.name().toString();
	}

	@Override
	public Stack emptyObject() {
		return wrap(ItemStack.EMPTY);
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		return stack.getDisplayName();
	}
}
