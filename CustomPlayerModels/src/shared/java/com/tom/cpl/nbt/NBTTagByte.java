package com.tom.cpl.nbt;

import java.io.IOException;

import com.tom.cpl.nbt.NBTTag.NBTPrimitive;
import com.tom.cpm.shared.io.IOHelper;

public class NBTTagByte extends NBTPrimitive {

	private byte data;

	NBTTagByte() {
	}

	public NBTTagByte(byte data) {
		this.data = data;
	}

	@Override
	public void write(IOHelper output) throws IOException {
		output.writeByte(this.data);
	}

	@Override
	public void read(IOHelper input) throws IOException {
		this.data = input.readByte();
	}

	@Override
	public byte getId() {
		return TAG_BYTE;
	}

	@Override
	public String toString() {
		return this.data + "b";
	}

	@Override
	public NBTTag copy() {
		return new NBTTagByte(this.data);
	}

	@Override
	public boolean equals(Object p_equals_1_) {
		if (super.equals(p_equals_1_))
		{
			NBTTagByte nbttagbyte = (NBTTagByte)p_equals_1_;
			return this.data == nbttagbyte.data;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ this.data;
	}

	@Override
	public long getLong() {
		return this.data;
	}

	@Override
	public int getInt() {
		return this.data;
	}

	@Override
	public short getShort() {
		return this.data;
	}

	@Override
	public byte getByte() {
		return this.data;
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
