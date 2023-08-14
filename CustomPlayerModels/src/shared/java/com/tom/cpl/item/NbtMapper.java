package com.tom.cpl.item;

import com.tom.cpl.nbt.MappedNBTCompound;
import com.tom.cpl.nbt.NBTTag;
import com.tom.cpl.nbt.NBTTagCompound;

public abstract class NbtMapper<T, C, L, N> {
	public abstract long getLong(N t);
	public abstract int getInt(N t);
	public abstract short getShort(N t);
	public abstract byte getByte(N t);
	public abstract double getDouble(N t);
	public abstract float getFloat(N t);
	public abstract String getString(T t);
	public abstract T getTag(C t, String name);
	public abstract N asNumber(T t);
	public abstract L asList(T t);
	public abstract C asCompound(T t);
	public abstract int listSize(L t);
	public abstract T getAt(L t, int i);
	public abstract boolean contains(C t, String name, int type);
	public abstract C newCompound();
	public abstract L newList();
	public abstract Iterable<String> keys(C t);
	public abstract int getId(T t);
	public abstract byte[] getByteArray(T t);
	public abstract int[] getIntArray(T t);
	public abstract long[] getLongArray(T t);

	public byte getByte(C tag, String name) {
		if (contains(tag, name, NBTTag.TAG_ANY_NUMERIC)) {
			N n = asNumber(getTag(tag, name));
			return n != null ? getByte(n) : 0;
		}

		return 0;
	}

	public short getShort(C tag, String name) {
		if (contains(tag, name, NBTTag.TAG_ANY_NUMERIC)) {
			N n = asNumber(getTag(tag, name));
			return n != null ? getShort(n) : 0;
		}

		return 0;
	}

	public int getInt(C tag, String name) {
		if (contains(tag, name, NBTTag.TAG_ANY_NUMERIC)) {
			N n = asNumber(getTag(tag, name));
			return n != null ? getInt(n) : 0;
		}

		return 0;
	}

	public long getLong(C tag, String name) {
		if (contains(tag, name, NBTTag.TAG_ANY_NUMERIC)) {
			N n = asNumber(getTag(tag, name));
			return n != null ? getLong(n) : 0;
		}

		return 0;
	}

	public float getFloat(C tag, String name) {
		if (contains(tag, name, NBTTag.TAG_ANY_NUMERIC)) {
			N n = asNumber(getTag(tag, name));
			return n != null ? getFloat(n) : 0;
		}

		return 0;
	}

	public double getDouble(C tag, String name) {
		if (contains(tag, name, NBTTag.TAG_ANY_NUMERIC)) {
			N n = asNumber(getTag(tag, name));
			return n != null ? getDouble(n) : 0;
		}

		return 0;
	}

	public String getString(C tag, String name) {
		if (contains(tag, name, NBTTag.TAG_STRING)) {
			return getString(getTag(tag, name));
		}

		return "";
	}

	public C getCompoundTag(C tag, String name) {
		if (contains(tag, name, NBTTag.TAG_COMPOUND)) {
			C c = asCompound(getTag(tag, name));
			if(c != null)return c;
		}

		return newCompound();
	}

	public L getListTag(C tag, String name) {
		if (contains(tag, name, NBTTag.TAG_LIST)) {
			L c = asList(getTag(tag, name));
			if(c != null)return c;
		}

		return newList();
	}

	public byte getByteAt(L list, int i) {
		if (i >= 0 && i < listSize(list)) {
			N n = asNumber(getAt(list, i));
			return n != null ? getByte(n) : 0;
		}

		return 0;
	}

	public short getShortAt(L list, int i) {
		if (i >= 0 && i < listSize(list)) {
			N n = asNumber(getAt(list, i));
			return n != null ? getShort(n) : 0;
		}

		return 0;
	}

	public int getIntAt(L list, int i) {
		if (i >= 0 && i < listSize(list)) {
			N n = asNumber(getAt(list, i));
			return n != null ? getInt(n) : 0;
		}

		return 0;
	}

	public long getLongAt(L list, int i) {
		if (i >= 0 && i < listSize(list)) {
			N n = asNumber(getAt(list, i));
			return n != null ? getLong(n) : 0;
		}

		return 0;
	}

	public float getFloatAt(L list, int i) {
		if (i >= 0 && i < listSize(list)) {
			N n = asNumber(getAt(list, i));
			return n != null ? getFloat(n) : 0;
		}

		return 0;
	}

	public double getDoubleAt(L list, int i) {
		if (i >= 0 && i < listSize(list)) {
			N n = asNumber(getAt(list, i));
			return n != null ? getDouble(n) : 0;
		}

		return 0;
	}

	public String getStringAt(L list, int i) {
		if (i >= 0 && i < listSize(list)) {
			return getString(getAt(list, i));
		}

		return "";
	}

	public C getCompoundTagAt(L list, int i) {
		if (i >= 0 && i < listSize(list)) {
			C c = asCompound(getAt(list, i));
			if(c != null)return c;
		}

		return newCompound();
	}

	public L getListTagAt(L list, int i) {
		if (i >= 0 && i < listSize(list)) {
			L c = asList(getAt(list, i));
			if(c != null)return c;
		}

		return newList();
	}

	public NBTTagCompound wrap(C tag) {
		return new MappedNBTCompound<>(this, tag);
	}
}
