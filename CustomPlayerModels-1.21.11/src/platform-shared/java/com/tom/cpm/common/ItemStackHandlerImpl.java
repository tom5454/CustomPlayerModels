package com.tom.cpm.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.stream.Streams;

import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
		return ItemStack.isSameItem(a, b);
	}

	@Override
	public boolean itemEqualsFull(ItemStack a, ItemStack b) {
		return ItemStack.isSameItemSameComponents(a, b);
	}

	@Override
	public List<String> listNativeTags() {
		return BuiltInRegistries.ITEM.getTags().map(k -> k.key().location().toString()).toList();
	}

	@Override
	public List<Stack> getAllElements() {
		if (CreativeModeTabs.searchTab().getSearchTabDisplayItems().isEmpty()) {
			CreativeModeTabs.tryRebuildTabContents(FeatureFlagSet.of(FeatureFlags.VANILLA), false, ClientRegistryLayer.createRegistryAccess().compositeAccess());
		}
		return CreativeModeTabs.searchTab().getSearchTabDisplayItems().stream().map(this::wrap).collect(Collectors.toList());
	}

	public static class NBT extends NbtMapper<Tag, CompoundTag, ListTag, NumericTag> {

		@Override
		public long getLong(NumericTag t) {
			return t.longValue();
		}

		@Override
		public int getInt(NumericTag t) {
			return t.intValue();
		}

		@Override
		public short getShort(NumericTag t) {
			return t.shortValue();
		}

		@Override
		public byte getByte(NumericTag t) {
			return t.byteValue();
		}

		@Override
		public double getDouble(NumericTag t) {
			return t.doubleValue();
		}

		@Override
		public float getFloat(NumericTag t) {
			return t.floatValue();
		}

		@Override
		public NumericTag asNumber(Tag t) {
			return t instanceof NumericTag ? (NumericTag) t : null;
		}

		@Override
		public String getString(Tag t) {
			return t.asString().orElse("");
		}

		@Override
		public Tag getTag(CompoundTag t, String name) {
			return t.get(name);
		}

		@Override
		public ListTag asList(Tag t) {
			return t instanceof ListTag ? (ListTag) t : null;
		}

		@Override
		public CompoundTag asCompound(Tag t) {
			return t instanceof CompoundTag ? (CompoundTag) t : null;
		}

		@Override
		public int listSize(ListTag t) {
			return t.size();
		}

		@Override
		public Tag getAt(ListTag t, int i) {
			return t.get(i);
		}

		@Override
		public boolean contains(CompoundTag t, String name, int type) {
			return t.contains(name);
		}

		@Override
		public CompoundTag newCompound() {
			return new CompoundTag();
		}

		@Override
		public ListTag newList() {
			return new ListTag();
		}

		@Override
		public Iterable<String> keys(CompoundTag t) {
			return t.keySet();
		}

		@Override
		public int getId(Tag t) {
			return t.getId();
		}

		@Override
		public byte[] getByteArray(Tag t) {
			return t instanceof ByteArrayTag ? ((ByteArrayTag) t).getAsByteArray() : new byte[0];
		}

		@Override
		public int[] getIntArray(Tag t) {
			return t instanceof IntArrayTag ? ((IntArrayTag) t).getAsIntArray() : new int[0];
		}

		@Override
		public long[] getLongArray(Tag t) {
			return t instanceof LongArrayTag ? ((LongArrayTag) t).getAsLongArray() : new long[0];
		}
	}

	@Override
	public NBTTagCompound getTag(ItemStack stack) {
		return null;
	}

	@Override
	public List<Stack> listNativeEntries(String tag) {
		List<Stack> stacks = new ArrayList<>();
		if (tag.charAt(0) == '#') {
			Identifier rl = Identifier.tryParse(tag.substring(1));
			if (rl != null) {
				TagKey<Item> i = TagKey.create(Registries.ITEM, rl);
				Streams.of(BuiltInRegistries.ITEM.getTagOrEmpty(i)).map(h -> h.unwrap().right()).
				filter(Optional::isPresent).map(o -> wrap(new ItemStack(o.get()))).forEach(stacks::add);
			}
		} else {
			Identifier rl = Identifier.tryParse(tag);
			Item item = BuiltInRegistries.ITEM.getValue(rl);
			if (item != null) {
				stacks.add(wrap(new ItemStack(item)));
			}
		}
		return stacks;
	}

	@Override
	public boolean isInTag(String tag, ItemStack stack) {
		if (tag.charAt(0) == '#') {
			Identifier rl = Identifier.tryParse(tag.substring(1));
			if (rl != null) {
				TagKey<Item> i = TagKey.create(Registries.ITEM, rl);
				return stack.is(i);
			}
		} else {
			return getItemId(stack).equals(tag);
		}
		return false;
	}

	@Override
	public List<String> listTags(ItemStack stack) {
		return stack.getTags().map(k -> "#" + k.location()).toList();
	}

	@Override
	public String getItemId(ItemStack stack) {
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
	}

	@Override
	public Stack emptyObject() {
		return wrap(ItemStack.EMPTY);
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		return stack.getDisplayName().getString();
	}
}
