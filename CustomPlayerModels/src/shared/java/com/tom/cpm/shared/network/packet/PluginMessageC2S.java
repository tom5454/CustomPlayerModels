package com.tom.cpm.shared.network.packet;

import java.io.IOException;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;

public class PluginMessageC2S extends NBTC2S {
	private String id;
	private byte flags;

	public PluginMessageC2S() {
		super();
	}

	public PluginMessageC2S(String msgID, NBTTagCompound data, int flags) {
		super(data);
		this.id = msgID;
		this.flags = (byte) flags;
	}

	@Override
	public void write(IOHelper pb) throws IOException {
		pb.writeUTF(id);
		super.write(pb);
		pb.writeByte(flags);
	}

	@Override
	public void read(IOHelper pb) throws IOException {
		id = pb.readUTF();
		super.read(pb);
		flags = pb.readByte();
	}

	@Override
	public <P> void handle(NetHandler<?, P, ?> handler, ServerNetH from, P player) {
		if((flags & 1) != 0 || (flags & 2) != 0) {
			handler.sendPacketToTracking(player, new PluginMessageS2C(id, handler.getPlayerId(player), tag));
			if((flags & 2) != 0) {
				from.cpm$getEncodedModelData().pluginStates.put(id, tag);
			}
		} else
			MinecraftCommonAccess.get().getApi().commonApi().handlePacket(id, tag, player);
	}
}
