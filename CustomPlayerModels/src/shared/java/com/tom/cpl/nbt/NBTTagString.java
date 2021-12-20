package com.tom.cpl.nbt;

import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;

public class NBTTagString extends NBTTag {

	private String data;

	public NBTTagString() {
		this.data = "";
	}

	public NBTTagString(String data) {
		this.data = data;

		if (data == null) {
			throw new IllegalArgumentException("Empty string not allowed");
		}
	}

	@Override
	public void write(IOHelper output) throws IOException {
		output.writeJUTF(this.data);
	}

	@Override
	public void read(IOHelper input) throws IOException {
		this.data = input.readJUTF();
	}

	@Override
	public byte getId() {
		return TAG_STRING;
	}

	@Override
	public String toString() {
		return "\"" + this.data.replace("\"", "\\\"") + "\"";
	}

	@Override
	public NBTTag copy() {
		return new NBTTagString(this.data);
	}

	@Override
	public boolean equals(Object other) {
		if (!super.equals(other)) {
			return false;
		} else {
			NBTTagString nbttagstring = (NBTTagString)other;
			return this.data == null && nbttagstring.data == null || this.data != null && this.data.equals(nbttagstring.data);
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ this.data.hashCode();
	}

	@Override
	public String getString() {
		return this.data;
	}
}
