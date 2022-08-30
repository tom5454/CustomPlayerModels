package com.tom.cpm.shared.network.packet;

import java.io.IOException;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.IS2CPacket;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

public abstract class NBTEntityS2C implements IS2CPacket {
	private int entityId;
	protected NBTTagCompound tag;

	public NBTEntityS2C() {
	}

	public NBTEntityS2C(int entityId, NBTTagCompound data) {
		this.entityId = entityId;
		this.tag = data;
	}

	@Override
	public void read(IOHelper pb) throws IOException {
		entityId = pb.readVarInt();
		tag = pb.readNBT();
	}

	@Override
	public void write(IOHelper pb) throws IOException {
		pb.writeVarInt(entityId);
		pb.writeNBT(tag);
	}

	@Override
	public void handle(NetHandler<?, ?, ?> handler, NetH from) {
		handle0(handler, from);
	}

	private <P> void handle0(NetHandler<?, P, ?> handler, NetH from) {
		P player = handler.getPlayerById(entityId);
		if(player != null)handle(handler, from, player);
	}

	protected abstract <P> void handle(NetHandler<?, P, ?> handler, NetH from, P player);
}
