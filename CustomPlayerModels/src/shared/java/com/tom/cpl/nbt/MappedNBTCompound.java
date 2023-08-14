package com.tom.cpl.nbt;

import com.tom.cpl.item.NbtMapper;

public class MappedNBTCompound<C> extends NBTTagCompound {
	private final NbtMapper<?, C, ?, ?> mapper;
	private final C compoundIn;

	public MappedNBTCompound(NbtMapper<?, C, ?, ?> mapper, C compoundIn) {
		this.mapper = mapper;
		this.compoundIn = compoundIn;
	}

	@Override
	protected void loadLazy() {
		load0();
	}

	@SuppressWarnings("unchecked")
	private <T, L, N> void load0() {
		NbtMapper<T, C, L, N> mapper = (NbtMapper<T, C, L, N>) this.mapper;
		for (String key : mapper.keys(compoundIn)) {
			T tag = mapper.getTag(compoundIn, key);
			NBTTag m = mapTag(mapper, tag);
			if (m != null)setTag(key, m);
		}
	}

	private static <T, C, L, N> NBTTag mapTag(NbtMapper<T, C, L, N> mapper, T tag) {
		int id = mapper.getId(tag);
		N n = mapper.asNumber(tag);
		if (n != null) {
			switch (id) {
			case TAG_BYTE: return new NBTTagByte(mapper.getByte(n));
			case TAG_SHORT: return new NBTTagShort(mapper.getShort(n));
			case TAG_INT: return new NBTTagInt(mapper.getInt(n));
			case TAG_LONG: return new NBTTagLong(mapper.getLong(n));
			case TAG_FLOAT: return new NBTTagFloat(mapper.getFloat(n));
			case TAG_DOUBLE: return new NBTTagDouble(mapper.getDouble(n));
			default: return null;
			}
		} else {
			switch (id) {
			case TAG_BYTE_ARRAY: return new NBTTagByteArray(mapper.getByteArray(tag));
			case TAG_STRING: return new NBTTagString(mapper.getString(tag));
			case TAG_LIST: return new MappedNBTList<>(mapper, mapper.asList(tag));
			case TAG_COMPOUND: return new MappedNBTCompound<>(mapper, mapper.asCompound(tag));
			case TAG_INT_ARRAY: return new NBTTagIntArray(mapper.getIntArray(tag));
			case TAG_LONG_ARRAY: return new NBTTagLongArray(mapper.getLongArray(tag));
			default: return null;
			}
		}
	}

	public static class MappedNBTList<L> extends NBTTagList {
		private final NbtMapper<?, ?, L, ?> mapper;
		private final L listIn;

		public MappedNBTList(NbtMapper<?, ?, L, ?> mapper, L listIn) {
			this.mapper = mapper;
			this.listIn = listIn;
		}

		@Override
		protected void loadLazy() {
			load0();
		}

		@SuppressWarnings("unchecked")
		private <T, C, N> void load0() {
			NbtMapper<T, C, L, N> mapper = (NbtMapper<T, C, L, N>) this.mapper;
			int len = mapper.listSize(listIn);
			for (int i = 0; i<len; i++) {
				T tag = mapper.getAt(listIn, i);
				NBTTag m = mapTag(mapper, tag);
				if (m != null)appendTag(m);
			}
		}
	}
}
