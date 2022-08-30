package com.tom.cpm.shared.network.packet;

import java.io.IOException;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.IC2SPacket;

public abstract class NBTC2S implements IC2SPacket {
	protected NBTTagCompound tag;

	public NBTC2S(NBTTagCompound data) {
		this.tag = data;
	}

	public NBTC2S() {
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
