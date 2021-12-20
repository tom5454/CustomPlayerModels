package com.tom.cpl.nbt;

import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;

public abstract class NBTTag {
	public static final int TAG_END         = 0;
	public static final int TAG_BYTE        = 1;
	public static final int TAG_SHORT       = 2;
	public static final int TAG_INT         = 3;
	public static final int TAG_LONG        = 4;
	public static final int TAG_FLOAT       = 5;
	public static final int TAG_DOUBLE      = 6;
	public static final int TAG_BYTE_ARRAY  = 7;
	public static final int TAG_STRING      = 8;
	public static final int TAG_LIST        = 9;
	public static final int TAG_COMPOUND    = 10;
	public static final int TAG_INT_ARRAY   = 11;
	public static final int TAG_ANY_NUMERIC = 99;

	public abstract byte getId();
	public abstract void write(IOHelper o) throws IOException;
	public abstract void read(IOHelper i) throws IOException;
	public abstract NBTTag copy();

	@Override
	public int hashCode() {
		return this.getId();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof NBTTag)) {
			return false;
		} else {
			NBTTag nbtbase = (NBTTag)other;
			return this.getId() == nbtbase.getId();
		}
	}

	protected String getString() {
		return this.toString();
	}

	public static NBTTag createNewByType(byte id) {
		switch (id) {
		case TAG_END:
			return new NBTTagEnd();
		case TAG_BYTE:
			return new NBTTagByte();
		case TAG_SHORT:
			return new NBTTagShort();
		case TAG_INT:
			return new NBTTagInt();
		case TAG_LONG:
			return new NBTTagLong();
		case TAG_FLOAT:
			return new NBTTagFloat();
		case TAG_DOUBLE:
			return new NBTTagDouble();
		case TAG_BYTE_ARRAY:
			return new NBTTagByteArray();
		case TAG_STRING:
			return new NBTTagString();
		case TAG_LIST:
			return new NBTTagList();
		case TAG_COMPOUND:
			return new NBTTagCompound();
		case TAG_INT_ARRAY:
			return new NBTTagIntArray();
		default:
			return null;
		}
	}

	public static abstract class NBTPrimitive extends NBTTag {
		public abstract long getLong();
		public abstract int getInt();
		public abstract short getShort();
		public abstract byte getByte();
		public abstract double getDouble();
		public abstract float getFloat();
	}
}
