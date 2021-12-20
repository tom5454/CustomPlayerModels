package com.tom.cpl.nbt;

import java.io.IOException;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.nbt.NBTTag.NBTPrimitive;
import com.tom.cpm.shared.io.IOHelper;

public class NBTTagFloat extends NBTPrimitive {

	private float data;

	NBTTagFloat() {
	}

	public NBTTagFloat(float data) {
		this.data = data;
	}

	@Override
	public void write(IOHelper output) throws IOException {
		output.writeFloat(this.data);
	}

	@Override
	public void read(IOHelper input) throws IOException {
		this.data = input.readFloat();
	}

	@Override
	public byte getId() {
		return TAG_FLOAT;
	}

	@Override
	public String toString() {
		return this.data + "f";
	}

	@Override
	public NBTTag copy() {
		return new NBTTagFloat(this.data);
	}

	@Override
	public boolean equals(Object other) {
		if (super.equals(other)) {
			NBTTagFloat nbttagfloat = (NBTTagFloat)other;
			return this.data == nbttagfloat.data;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ Float.floatToIntBits(this.data);
	}

	@Override
	public long getLong() {
		return (long)this.data;
	}

	@Override
	public int getInt() {
		return MathHelper.floor(this.data);
	}

	@Override
	public short getShort() {
		return (short)(MathHelper.floor(this.data) & 65535);
	}

	@Override
	public byte getByte() {
		return (byte)(MathHelper.floor(this.data) & 255);
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
