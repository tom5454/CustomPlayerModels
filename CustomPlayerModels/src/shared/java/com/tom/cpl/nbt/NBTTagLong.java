package com.tom.cpl.nbt;

import java.io.IOException;

import com.tom.cpl.nbt.NBTTag.NBTPrimitive;
import com.tom.cpm.shared.io.IOHelper;

public class NBTTagLong extends NBTPrimitive {

	private long data;

	NBTTagLong() {
	}

	public NBTTagLong(long data) {
		this.data = data;
	}

	@Override
	public void write(IOHelper output) throws IOException {
		output.writeLong(this.data);
	}

	@Override
	public void read(IOHelper input) throws IOException {
		this.data = input.readLong();
	}

	@Override
	public byte getId() {
		return TAG_LONG;
	}

	@Override
	public String toString() {
		return this.data + "L";
	}

	@Override
	public NBTTag copy() {
		return new NBTTagLong(this.data);
	}

	@Override
	public boolean equals(Object other) {
		if (super.equals(other)) {
			NBTTagLong nbttaglong = (NBTTagLong)other;
			return this.data == nbttaglong.data;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ (int)(this.data ^ this.data >>> 32);
	}

	@Override
	public long getLong() {
		return this.data;
	}

	@Override
	public int getInt() {
		return (int)(this.data & -1L);
	}

	@Override
	public short getShort() {
		return (short)((int)(this.data & 65535L));
	}

	@Override
	public byte getByte() {
		return (byte)((int)(this.data & 255L));
	}

	@Override
	public double getDouble() {
		return this.data;
	}

	@Override
	public float getFloat() {
		return this.data;
	}
}
