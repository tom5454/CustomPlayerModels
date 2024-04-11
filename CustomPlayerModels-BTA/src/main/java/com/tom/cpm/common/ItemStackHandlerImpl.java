package com.tom.cpm.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;

import com.mojang.nbt.ByteArrayTag;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.Tag;

import com.tom.cpl.item.ItemStackHandler;
import com.tom.cpl.item.NbtMapper;
import com.tom.cpl.item.Stack;
import com.tom.cpl.nbt.NBTTagCompound;

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
		return stack == null ? 0 : stack.getMetadata();
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
		return (a == null && b == null ? true : (a != null && b != null ? a.isItemEqual(b) : false)) && ItemStack.areItemStacksEqual(a, b);
	}

	@Override
	public List<String> listNativeTags() {
		//return Arrays.stream(OreDictionary.getOreNames()).map(t -> "#oredict:" + t).collect(Collectors.toList());
		return Collections.emptyList();
	}

	@Override
	public List<Stack> getAllElements() {
		ArrayList<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < Item.itemsList.length; i++) {
			Item item = Item.itemsList[i];
			if (item == null)continue;
			stacks.add(new ItemStack(item));
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

	public static class NBT extends NbtMapper<Tag<?>, CompoundTag, ListTag, NBTPrimitive> {

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
		public NBTPrimitive asNumber(Tag<?> t) {
			return t instanceof NBTPrimitive ? (NBTPrimitive) t : null;
		}

		@Override
		public String getString(Tag<?> t) {
			return t.toString();
		}

		@Override
		public Tag<?> getTag(CompoundTag t, String name) {
			return t.getTag(name);
		}

		@Override
		public ListTag asList(Tag<?> t) {
			return t instanceof ListTag ? (ListTag) t : null;
		}

		@Override
		public CompoundTag asCompound(Tag<?> t) {
			return t instanceof CompoundTag ? (CompoundTag) t : null;
		}

		@Override
		public int listSize(ListTag t) {
			return t.tagCount();
		}

		@Override
		public Tag<?> getAt(ListTag t, int i) {
			return t.tagAt(i);
		}

		@Override
		public boolean contains(CompoundTag t, String name, int type) {
			if(!t.containsKey(name))return false;
			return t.getTag(name).getId() == type;
		}

		@Override
		public CompoundTag newCompound() {
			return new CompoundTag();
		}

		@Override
		public ListTag newList() {
			return new ListTag();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterable<String> keys(CompoundTag t) {
			return t.getValue().keySet();
		}

		@Override
		public int getId(Tag<?> t) {
			return t.getId();
		}

		@Override
		public byte[] getByteArray(Tag<?> t) {
			return t instanceof ByteArrayTag ? ((ByteArrayTag) t).getValue() : new byte[0];
		}

		@Override
		public int[] getIntArray(Tag<?> t) {
			//return t instanceof NbtIntArray ? ((NbtIntArray) t).data : new int[0];
			return new int[0];
		}

		@Override
		public long[] getLongArray(Tag<?> t) {
			//return t instanceof NbtLongArray ? ((NbtLongArray) t).data : new long[0];
			return new long[0];
		}
	}

	@Override
	public NBTTagCompound getTag(ItemStack stack) {
		return stack != null && stack.tag != null ? nbt.wrap(stack.tag) : null;
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

	@Override
	public boolean isInTag(String tag, ItemStack stack) {
		if(tag.charAt(0) == '#') {
			/*if (tag.startsWith("#oredict:")) {
				tag = tag.substring(9);
				if (oreIDs.containsKey(tag)) {
					return OreDictionary.getOreID(stack) == OreDictionary.getOreID(tag);
				}
			}*/
			return checkIdMetaTags(tag, stack.itemID, stack.getMetadata());
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
		tags.add("#idmeta:" + stack.itemID + "/" + stack.getMetadata());
		tags.add("#id:" + stack.itemID);
		/*int id = OreDictionary.getOreID(stack);
		if (id == -1)return tags;
		tags.add("#oredict:" + OreDictionary.getOreName(id));*/
		return tags;
	}

	@Override
	public String getItemId(ItemStack stack) {
		if(stack == null)return AIR;
		return "unloc:" + stack.getItem().getKey();
	}

	@Override
	public Stack emptyObject() {
		return wrap(null);
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		return stack.getItem().getTranslatedName(stack);
	}
}
