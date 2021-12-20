package com.tom.cpl.nbt;

import java.io.IOException;
import java.util.Arrays;

import com.tom.cpm.shared.io.IOHelper;

public class NBTTagIntArray extends NBTTag {

	private int[] data;

	NBTTagIntArray() {
	}

	public NBTTagIntArray(int[] dataIn) {
		this.data = dataIn;
	}

	@Override
	public void write(IOHelper output) throws IOException {
		output.writeInt(this.data.length);

		for (int i = 0; i < this.data.length; ++i)
		{
			output.writeInt(this.data[i]);
		}
	}

	@Override
	public void read(IOHelper input) throws IOException {
		int i = input.readInt();
		this.data = new int[i];

		for (int j = 0; j < i; ++j)
		{
			this.data[j] = input.readInt();
		}
	}

	@Override
	public byte getId() {
		return TAG_INT_ARRAY;
	}

	@Override
	public String toString() {
		String s = "[";

		for (int i : this.data) {
			s = s + i + ",";
		}

		return s + "]";
	}

	@Override
	public NBTTag copy() {
		int[] aint = new int[this.data.length];
		System.arraycopy(this.data, 0, aint, 0, this.data.length);
		return new NBTTagIntArray(aint);
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other) ? Arrays.equals(this.data, ((NBTTagIntArray)other).data) : false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ Arrays.hashCode(this.data);
	}

	public int[] getIntArray() {
		return this.data;
	}
}
