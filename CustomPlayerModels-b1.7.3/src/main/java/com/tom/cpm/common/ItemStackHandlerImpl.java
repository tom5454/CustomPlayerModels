package com.tom.cpm.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.modificationstation.stationapi.api.nbt.NbtIntArray;
import net.modificationstation.stationapi.api.nbt.NbtLongArray;

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
		return stack == null ? 0 : stack.count;
	}

	@Override
	public int getMaxCount(ItemStack stack) {
		return stack == null ? 0 : stack.getMaxCount();
	}

	@Override
	public int getDamage(ItemStack stack) {
		return stack == null ? 0 : stack.getDamage();
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return stack == null ? 0 : stack.getMaxDamage();
	}

	@Override
	public boolean itemEquals(ItemStack a, ItemStack b) {
		return a == null && b == null ? true : (a != null && b != null ? (!a.isDamageable() ? a.isItemEqual(b) : a.getItem() == b.getItem()) : false);
	}

	@Override
	public boolean itemEqualsFull(ItemStack a, ItemStack b) {
		return (a == null && b == null ? true : (a != null && b != null ? a.isItemEqual(b) : false)) && ItemStack.areEqual(a, b);
	}

	@Override
	public List<String> listNativeTags() {
		//return Arrays.stream(OreDictionary.getOreNames()).map(t -> "#oredict:" + t).collect(Collectors.toList());
		return Collections.emptyList();
	}

	@Override
	public List<Stack> getAllElements() {
		ArrayList<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < Item.ITEMS.length; i++) {
			Item item = Item.ITEMS[i];
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

	public static class NBT extends NbtMapper<NbtElement, NbtCompound, NbtList, NBTPrimitive> {

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
		public NBTPrimitive asNumber(NbtElement t) {
			return t instanceof NBTPrimitive ? (NBTPrimitive) t : null;
		}

		@Override
		public String getString(NbtElement t) {
			return t.toString();
		}

		@Override
		public NbtElement getTag(NbtCompound t, String name) {
			return (NbtElement) t.entries.get(name);
		}

		@Override
		public NbtList asList(NbtElement t) {
			return t instanceof NbtList ? (NbtList) t : null;
		}

		@Override
		public NbtCompound asCompound(NbtElement t) {
			return t instanceof NbtCompound ? (NbtCompound) t : null;
		}

		@Override
		public int listSize(NbtList t) {
			return t.size();
		}

		@Override
		public NbtElement getAt(NbtList t, int i) {
			return t.get(i);
		}

		@Override
		public boolean contains(NbtCompound t, String name, int type) {
			if(!t.contains(name))return false;
			return ((NbtElement) t.entries.get(name)).getType() == type;
		}

		@Override
		public NbtCompound newCompound() {
			return new NbtCompound();
		}

		@Override
		public NbtList newList() {
			return new NbtList();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterable<String> keys(NbtCompound t) {
			return t.entries.keySet();
		}

		@Override
		public int getId(NbtElement t) {
			return t.getType();
		}

		@Override
		public byte[] getByteArray(NbtElement t) {
			return t instanceof NbtByteArray ? ((NbtByteArray) t).value : new byte[0];
		}

		@Override
		public int[] getIntArray(NbtElement t) {
			return t instanceof NbtIntArray ? ((NbtIntArray) t).data : new int[0];
		}

		@Override
		public long[] getLongArray(NbtElement t) {
			return t instanceof NbtLongArray ? ((NbtLongArray) t).data : new long[0];
		}
	}

	@Override
	public NBTTagCompound getTag(ItemStack stack) {
		return stack != null && stack.getStationNbt() != null ? nbt.wrap(stack.getStationNbt()) : null;
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
			return checkIdMetaTags(tag, stack.itemId, stack.getDamage());
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
		tags.add("#idmeta:" + stack.itemId + "/" + stack.getDamage());
		tags.add("#id:" + stack.itemId);
		/*int id = OreDictionary.getOreID(stack);
		if (id == -1)return tags;
		tags.add("#oredict:" + OreDictionary.getOreName(id));*/
		return tags;
	}

	@Override
	public String getItemId(ItemStack stack) {
		if(stack == null)return AIR;
		return "unloc:" + stack.getItem().getTranslationKey();
	}

	@Override
	public Stack emptyObject() {
		return wrap(null);
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		return stack.getItem().method_469();
	}
}
