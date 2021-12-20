package com.tom.cpl.nbt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.tom.cpm.shared.io.IOHelper;

public class NBTTagCompound extends NBTTag {
	private Map<String, NBTTag> tagMap = new HashMap<>();

	@Override
	public byte getId() {
		return TAG_COMPOUND;
	}

	@Override
	public void write(IOHelper output) throws IOException {
		for (String s : this.tagMap.keySet()) {
			NBTTag nbtbase = this.tagMap.get(s);
			writeEntry(s, nbtbase, output);
		}

		output.writeByte(0);
	}

	@Override
	public void read(IOHelper input) throws IOException {
		this.tagMap.clear();
		byte type;
		while ((type = readType(input)) != 0) {
			String s = readKey(input);
			NBTTag nbtbase = readNBT(type, s, input);

			this.tagMap.put(s, nbtbase);
		}
	}

	public Set<String> getKeySet() {
		return this.tagMap.keySet();
	}

	public void setTag(String key, NBTTag value) {
		this.tagMap.put(key, value);
	}

	public void setByte(String key, byte value) {
		this.tagMap.put(key, new NBTTagByte(value));
	}

	public void setShort(String key, short value) {
		this.tagMap.put(key, new NBTTagShort(value));
	}

	public void setInteger(String key, int value) {
		this.tagMap.put(key, new NBTTagInt(value));
	}

	public void setLong(String key, long value)
	{
		this.tagMap.put(key, new NBTTagLong(value));
	}

	public void setFloat(String key, float value) {
		this.tagMap.put(key, new NBTTagFloat(value));
	}

	public void setDouble(String key, double value) {
		this.tagMap.put(key, new NBTTagDouble(value));
	}

	public void setString(String key, String value) {
		this.tagMap.put(key, new NBTTagString(value));
	}

	public void setByteArray(String key, byte[] value) {
		this.tagMap.put(key, new NBTTagByteArray(value));
	}

	public void setIntArray(String key, int[] value) {
		this.tagMap.put(key, new NBTTagIntArray(value));
	}

	public void setBoolean(String key, boolean value) {
		this.setByte(key, (byte)(value ? 1 : 0));
	}

	public NBTTag getTag(String key) {
		return this.tagMap.get(key);
	}

	public byte getTagId(String key) {
		NBTTag nbtbase = this.tagMap.get(key);
		return nbtbase != null ? nbtbase.getId() : 0;
	}

	public boolean hasKey(String key) {
		return this.tagMap.containsKey(key);
	}

	public boolean hasKey(String key, int type) {
		int i = this.getTagId(key);

		if (i == type) {
			return true;
		} else if (type != TAG_ANY_NUMERIC) {
			if (i > 0) {
				;
			}

			return false;
		} else {
			return i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6;
		}
	}

	public byte getByte(String key) {
		try {
			return !this.hasKey(key, TAG_ANY_NUMERIC) ? 0 : ((NBTTag.NBTPrimitive)this.tagMap.get(key)).getByte();
		} catch (ClassCastException ex) {
			return (byte)0;
		}
	}

	public short getShort(String key) {
		try {
			return !this.hasKey(key, TAG_ANY_NUMERIC) ? 0 : ((NBTTag.NBTPrimitive)this.tagMap.get(key)).getShort();
		} catch (ClassCastException ex) {
			return (short)0;
		}
	}

	public int getInteger(String key) {
		try {
			return !this.hasKey(key, TAG_ANY_NUMERIC) ? 0 : ((NBTTag.NBTPrimitive)this.tagMap.get(key)).getInt();
		} catch (ClassCastException ex) {
			return 0;
		}
	}

	public long getLong(String key) {
		try {
			return !this.hasKey(key, TAG_ANY_NUMERIC) ? 0L : ((NBTTag.NBTPrimitive)this.tagMap.get(key)).getLong();
		} catch (ClassCastException ex) {
			return 0L;
		}
	}

	public float getFloat(String key) {
		try {
			return !this.hasKey(key, TAG_ANY_NUMERIC) ? 0.0F : ((NBTTag.NBTPrimitive)this.tagMap.get(key)).getFloat();
		} catch (ClassCastException ex) {
			return 0.0F;
		}
	}

	public double getDouble(String key) {
		try {
			return !this.hasKey(key, TAG_ANY_NUMERIC) ? 0.0D : ((NBTTag.NBTPrimitive)this.tagMap.get(key)).getDouble();
		} catch (ClassCastException ex) {
			return 0.0D;
		}
	}

	public String getString(String key) {
		try {
			return !this.hasKey(key, TAG_STRING) ? "" : this.tagMap.get(key).getString();
		} catch (ClassCastException ex) {
			return "";
		}
	}

	public byte[] getByteArray(String key) {
		try {
			return !this.hasKey(key, TAG_BYTE_ARRAY) ? new byte[0] : ((NBTTagByteArray)this.tagMap.get(key)).getByteArray();
		} catch (ClassCastException ex) {
			return new byte[0];
		}
	}

	public int[] getIntArray(String key) {
		try {
			return !this.hasKey(key, TAG_INT_ARRAY) ? new int[0] : ((NBTTagIntArray)this.tagMap.get(key)).getIntArray();
		} catch (ClassCastException ex) {
			return new int[0];
		}
	}

	public NBTTagCompound getCompoundTag(String key) {
		try {
			return !this.hasKey(key, TAG_COMPOUND) ? new NBTTagCompound() : (NBTTagCompound)this.tagMap.get(key);
		} catch (ClassCastException ex) {
			return new NBTTagCompound();
		}
	}

	public NBTTagList getTagList(String key, int type) {
		try {
			if (this.getTagId(key) != TAG_LIST) {
				return new NBTTagList();
			} else {
				NBTTagList nbttaglist = (NBTTagList)this.tagMap.get(key);
				return nbttaglist.tagCount() > 0 && nbttaglist.getTagType() != type ? new NBTTagList() : nbttaglist;
			}
		} catch (ClassCastException ex) {
			return new NBTTagList();
		}
	}

	public boolean getBoolean(String key) {
		return this.getByte(key) != 0;
	}

	public void removeTag(String key) {
		this.tagMap.remove(key);
	}

	@Override
	public String toString() {
		StringBuilder stringbuilder = new StringBuilder("{");

		for (Entry<String, NBTTag> entry : this.tagMap.entrySet()) {
			if (stringbuilder.length() != 1) {
				stringbuilder.append(',');
			}

			stringbuilder.append(entry.getKey()).append(':').append(entry.getValue());
		}

		return stringbuilder.append('}').toString();
	}

	@Override
	public NBTTag copy() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();

		for (String s : this.tagMap.keySet()) {
			nbttagcompound.setTag(s, this.tagMap.get(s).copy());
		}

		return nbttagcompound;
	}

	@Override
	public boolean equals(Object other) {
		if (super.equals(other)) {
			NBTTagCompound nbttagcompound = (NBTTagCompound)other;
			return this.tagMap.entrySet().equals(nbttagcompound.tagMap.entrySet());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ this.tagMap.hashCode();
	}

	private static void writeEntry(String name, NBTTag data, IOHelper output) throws IOException {
		output.writeByte(data.getId());

		if (data.getId() != TAG_END)
		{
			output.writeJUTF(name);
			data.write(output);
		}
	}

	private static byte readType(IOHelper input) throws IOException {
		return input.readByte();
	}

	private static String readKey(IOHelper input) throws IOException {
		return input.readJUTF();
	}

	static NBTTag readNBT(byte id, String key, IOHelper input) throws IOException {
		NBTTag nbtbase = NBTTag.createNewByType(id);
		nbtbase.read(input);
		return nbtbase;
	}

	public void merge(NBTTagCompound other) {
		for (String s : other.tagMap.keySet()) {
			NBTTag nbtbase = other.tagMap.get(s);

			if (nbtbase.getId() == TAG_COMPOUND) {
				if (this.hasKey(s, 10)) {
					NBTTagCompound nbttagcompound = this.getCompoundTag(s);
					nbttagcompound.merge((NBTTagCompound)nbtbase);
				} else {
					this.setTag(s, nbtbase.copy());
				}
			} else {
				this.setTag(s, nbtbase.copy());
			}
		}
	}
}
