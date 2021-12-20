package com.tom.cpl.nbt;

import java.io.IOException;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.nbt.NBTTag.NBTPrimitive;
import com.tom.cpm.shared.io.IOHelper;

public class NBTTagDouble extends NBTPrimitive {

	private double data;

	NBTTagDouble() {
	}

	public NBTTagDouble(double data) {
		this.data = data;
	}

	@Override
	public void write(IOHelper output) throws IOException {
		output.writeDouble(this.data);
	}

	@Override
	public void read(IOHelper input) throws IOException {
		this.data = input.readDouble();
	}

	@Override
	public byte getId() {
		return TAG_DOUBLE;
	}

	@Override
	public String toString() {
		return this.data + "d";
	}

	@Override
	public NBTTag copy() {
		return new NBTTagDouble(this.data);
	}

	@Override
	public boolean equals(Object p_equals_1_) {
		if (super.equals(p_equals_1_))
		{
			NBTTagDouble nbttagdouble = (NBTTagDouble)p_equals_1_;
			return this.data == nbttagdouble.data;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode() {
		long i = Double.doubleToLongBits(this.data);
		return super.hashCode() ^ (int)(i ^ i >>> 32);
	}

	@Override
	public long getLong() {
		return (long)Math.floor(this.data);
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
		return (float)this.data;
	}
}
