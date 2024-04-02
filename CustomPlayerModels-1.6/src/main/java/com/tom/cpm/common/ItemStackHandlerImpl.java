package com.tom.cpm.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.oredict.OreDictionary;

import com.tom.cpl.item.ItemStackHandler;
import com.tom.cpl.item.NbtMapper;
import com.tom.cpl.item.Stack;
import com.tom.cpl.nbt.NBTTagCompound;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

@SuppressWarnings("unchecked")
public class ItemStackHandlerImpl extends ItemStackHandler<ItemStack> {
	private static final String AIR = "minecraft:air";
	public static final ItemStackHandlerImpl impl = new ItemStackHandlerImpl();
	public static final NBT nbt = new NBT();

	@Override
	public int getCount(ItemStack stack) {
		return stack == null ? 0 : stack.stackSize;
	}

	@Override
	public int getMaxCount(ItemStack stack) {
		return stack == null ? 0 : stack.getMaxStackSize();
	}

	@Override
	public int getDamage(ItemStack stack) {
		return stack == null ? 0 : stack.getItemDamage();
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return stack == null ? 0 : stack.getMaxDamage();
	}

	@Override
	public boolean itemEquals(ItemStack a, ItemStack b) {
		return a == null && b == null ? true : (a != null && b != null ? (!a.isItemStackDamageable() ? a.isItemEqual(b) : a.getItem() == b.getItem()) : false);
	}

	@Override
	public boolean itemEqualsFull(ItemStack a, ItemStack b) {
		return (a == null && b == null ? true : (a != null && b != null ? a.isItemEqual(b) : false)) && ItemStack.areItemStackTagsEqual(a, b);
	}

	@Override
	public List<String> listNativeTags() {
		return Arrays.stream(OreDictionary.getOreNames()).map(t -> "#oredict:" + t).collect(Collectors.toList());
	}

	@Override
	public List<Stack> getAllElements() {
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < Item.itemsList.length; i++) {
			Item item = Item.itemsList[i];
			if (item == null)continue;
			item.getSubItems(i, CreativeTabs.tabAllSearch, stacks);
		}
		return stacks.stream().map(this::wrap).collect(Collectors.toList());
	}

	public static interface NBTPrimitive {
		public long cpm$getLong();
		public int cpm$getInt();
		public short cpm$getShort();
		public byte cpm$getByte();
		public double cpm$getDouble();
		public float cpm$getFloat();
	}

	public static class NBT extends NbtMapper<NBTBase, net.minecraft.nbt.NBTTagCompound, NBTTagList, NBTPrimitive> {

		@Override
		public long getLong(NBTPrimitive t) {
			return t.cpm$getLong();
		}

		@Override
		public int getInt(NBTPrimitive t) {
			return t.cpm$getInt();
		}

		@Override
		public short getShort(NBTPrimitive t) {
			return t.cpm$getShort();
		}

		@Override
		public byte getByte(NBTPrimitive t) {
			return t.cpm$getByte();
		}

		@Override
		public double getDouble(NBTPrimitive t) {
			return t.cpm$getDouble();
		}

		@Override
		public float getFloat(NBTPrimitive t) {
			return t.cpm$getFloat();
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
			return t.tagAt(i);
		}

		@Override
		public boolean contains(net.minecraft.nbt.NBTTagCompound t, String name, int type) {
			if(!t.hasKey(name))return false;
			return t.getTag(name).getId() == type;
		}

		@Override
		public net.minecraft.nbt.NBTTagCompound newCompound() {
			return new net.minecraft.nbt.NBTTagCompound();
		}

		@Override
		public NBTTagList newList() {
			return new NBTTagList();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterable<String> keys(net.minecraft.nbt.NBTTagCompound t) {
			return t.tagMap.keySet();
		}

		@Override
		public int getId(NBTBase t) {
			return t.getId();
		}

		@Override
		public byte[] getByteArray(NBTBase t) {
			return t instanceof NBTTagByteArray ? ((NBTTagByteArray) t).byteArray : new byte[0];
		}

		@Override
		public int[] getIntArray(NBTBase t) {
			return t instanceof NBTTagIntArray ? ((NBTTagIntArray) t).intArray : new int[0];
		}

		@Override
		public long[] getLongArray(NBTBase t) {
			return new long[0];//1.12 and older is missing the proper implementation for long arrays
		}
	}

	@Override
	public NBTTagCompound getTag(ItemStack stack) {
		return stack != null && stack.hasTagCompound() ? nbt.wrap(stack.getTagCompound()) : null;
	}

	private static Map<String, Integer> oreIDs;
	static {
		try {
			Field oi = OreDictionary.class.getDeclaredField("oreIDs");
			oi.setAccessible(true);
			oreIDs = (Map<String, Integer>) oi.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Stack> listNativeEntries(String tag) {
		/*if(tag.charAt(0) == '#') {
			if (tag.startsWith("#oredict:")) {
				tag = tag.substring(9);
				if (oreIDs.containsKey(tag)) {
					return OreDictionary.getOres(tag).stream().map(this::wrap).collect(Collectors.toList());
				}
			}//TODO idmeta
		} else {
			ResourceLocation rl = tryParse(tag);//TODO
			Item item = (Item) GameData.getItemRegistry().getObject(rl);
			if (item != null) {
				return Collections.singletonList(wrap(new ItemStack(item)));
			}
		}*/
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
		if(tag.charAt(0) == '#') {
			if (tag.startsWith("#oredict:")) {
				tag = tag.substring(9);
				if (oreIDs.containsKey(tag)) {
					return OreDictionary.getOreID(stack) == OreDictionary.getOreID(tag);
				}
			}
			return checkIdMetaTags(tag, stack.itemID, stack.getItemDamage());
		} else if(stack == null) {
			return false;
		} else {
			return getItemId(stack).equals(tag);
		}
	}

	public static boolean checkIdMetaTags(String tag, int id, int meta) {
		if (tag.startsWith("#id:")) {
			tag = tag.substring(4);
			Integer i = parseInt(tag);
			if (i == null)return false;
			return i == id;
		} else if (tag.startsWith("#idmeta:")) {
			tag = tag.substring(8);
			String[] sp = tag.split("/");
			if (sp.length != 2)return false;
			Integer i = parseInt(sp[0]);
			Integer m = parseInt(sp[1]);
			if (i == null || m == null)return false;
			return i == id && m == meta;
		}
		return false;
	}

	private static Integer parseInt(String tag) {
		try {
			return Integer.parseInt(tag);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public List<String> listTags(ItemStack stack) {
		if(stack == null)return Collections.emptyList();
		List<String> tags = new ArrayList<>();
		tags.add("#idmeta:" + stack.itemID + "/" + stack.getItemDamage());
		tags.add("#id:" + stack.itemID);
		int id = OreDictionary.getOreID(stack);
		if (id == -1)return tags;
		tags.add("#oredict:" + OreDictionary.getOreName(id));
		return tags;
	}

	@Override
	public String getItemId(ItemStack stack) {
		if(stack == null)return AIR;
		UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());
		if (id != null)return id.modId + ":" + id.name;
		return "unloc:" + stack.getItem().getUnlocalizedName();
	}

	@Override
	public Stack emptyObject() {
		return wrap(null);
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		return stack.getDisplayName();
	}
}
