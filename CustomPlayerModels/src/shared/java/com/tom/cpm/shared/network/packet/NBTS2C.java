package com.tom.cpm.shared.network.packet;

import java.io.IOException;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.IS2CPacket;

public abstract class NBTS2C implements IS2CPacket {
	protected NBTTagCompound tag;

	public NBTS2C(NBTTagCompound data) {
		this.tag = data;
	}

	public NBTS2C() {
	}

	@Override
	public void read(IOHelper pb) throws IOException {
		tag = pb.readNBT();
	}

	@Override
	public void write(IOHelper pb) throws IOException {
		pb.writeNBT(tag);
	}
}
