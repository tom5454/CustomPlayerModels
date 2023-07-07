package com.tom.cpl.nbt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.util.Log;

public class NBTTagList extends NBTTag {
	private List<NBTTag> tagList = new ArrayList<>();
	private byte tagType = 0;

	public NBTTagList() {
	}

	@Override
	public void write(IOHelper output) throws IOException {
		if (!this.tagList.isEmpty()) {
			this.tagType = this.tagList.get(0).getId();
		} else {
			this.tagType = 0;
		}

		output.writeByte(this.tagType);
		output.writeInt(this.tagList.size());

		for (int i = 0; i < this.tagList.size(); ++i) {
			this.tagList.get(i).write(output);
		}
	}

	@Override
	public void read(IOHelper input) throws IOException {
		this.tagType = input.readByte();
		int i = input.readInt();

		if (this.tagType == 0 && i > 0) {
			throw new RuntimeException("Missing type on ListTag");
		} else {
			this.tagList = new ArrayList<>();

			for (int j = 0; j < i; ++j) {
				NBTTag nbtbase = NBTTag.createNewByType(this.tagType);
				nbtbase.read(input);
				this.tagList.add(nbtbase);
			}
		}
	}

	@Override
	public byte getId() {
		return TAG_LIST;
	}

	@Override
	public String toString() {
		StringBuilder stringbuilder = new StringBuilder("[");

		for (int i = 0; i < this.tagList.size(); ++i) {
			if (i != 0) {
				stringbuilder.append(',');
			}

			stringbuilder.append(i).append(':').append(this.tagList.get(i));
		}

		return stringbuilder.append(']').toString();
	}

	public void appendTag(NBTTag nbt)
	{
		if (nbt.getId() != 0) {
			if (this.tagType == 0) {
				this.tagType = nbt.getId();
			} else if (this.tagType != nbt.getId()) {
				Log.warn("Adding mismatching tag types to tag list");
				return;
			}

			this.tagList.add(nbt);
		}
	}

	/**
	 * Set the given index to the given tag
	 */
	public void set(int idx, NBTTag nbt) {
		if (nbt.getId() == 0) {
			Log.warn("Invalid TagEnd added to ListTag");
		} else if (idx >= 0 && idx < this.tagList.size()) {
			if (this.tagType == 0) {
				this.tagType = nbt.getId();
			} else if (this.tagType != nbt.getId()) {
				Log.warn("Adding mismatching tag types to tag list");
				return;
			}

			this.tagList.set(idx, nbt);
		} else {
			Log.warn("index out of bounds to set tag in tag list");
		}
	}

	public NBTTag removeTag(int i) {
		return this.tagList.remove(i);
	}

	public boolean hasNoTags() {
		return this.tagList.isEmpty();
	}

	public NBTTagCompound getCompoundTagAt(int i) {
		if (i >= 0 && i < this.tagList.size()) {
			NBTTag nbtbase = this.tagList.get(i);
			return nbtbase.getId() == TAG_COMPOUND ? (NBTTagCompound)nbtbase : new NBTTagCompound();
		} else {
			return new NBTTagCompound();
		}
	}

	public int[] getIntArrayAt(int i) {
		if (i >= 0 && i < this.tagList.size()) {
			NBTTag nbtbase = this.tagList.get(i);
			return nbtbase.getId() == 11 ? ((NBTTagIntArray)nbtbase).getIntArray() : new int[0];
		} else {
			return new int[0];
		}
	}

	public double getDoubleAt(int i) {
		if (i >= 0 && i < this.tagList.size()) {
			NBTTag nbtbase = this.tagList.get(i);
			return nbtbase.getId() == 6 ? ((NBTTagDouble)nbtbase).getDouble() : 0.0D;
		} else {
			return 0.0D;
		}
	}

	public float getFloatAt(int i) {
		if (i >= 0 && i < this.tagList.size()) {
			NBTTag nbtbase = this.tagList.get(i);
			return nbtbase.getId() == 5 ? ((NBTTagFloat)nbtbase).getFloat() : 0.0F;
		} else {
			return 0.0F;
		}
	}

	public String getStringTagAt(int i) {
		if (i >= 0 && i < this.tagList.size()) {
			NBTTag nbtbase = this.tagList.get(i);
			return nbtbase.getId() == 8 ? nbtbase.getString() : nbtbase.toString();
		} else {
			return "";
		}
	}

	public NBTTag get(int idx) {
		return idx >= 0 && idx < this.tagList.size() ? (NBTTag)this.tagList.get(idx) : new NBTTagEnd();
	}

	public int tagCount() {
		return this.tagList.size();
	}

	@Override
	public NBTTag copy() {
		NBTTagList nbttaglist = new NBTTagList();
		nbttaglist.tagType = this.tagType;

		for (NBTTag nbtbase : this.tagList) {
			NBTTag nbtbase1 = nbtbase.copy();
			nbttaglist.tagList.add(nbtbase1);
		}

		return nbttaglist;
	}

	@Override
	public boolean equals(Object other) {
		if (super.equals(other)) {
			NBTTagList nbttaglist = (NBTTagList)other;

			if (this.tagType == nbttaglist.tagType) {
				return this.tagList.equals(nbttaglist.tagList);
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ this.tagList.hashCode();
	}

	public int getTagType() {
		return this.tagType;
	}
}
