package com.tom.cpm.shared.network.packet;

import java.io.IOException;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.IS2CPacket;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

public class ServerAnimationS2C implements IS2CPacket {
	private String id;
	private int value;

	public ServerAnimationS2C() {
	}

	public ServerAnimationS2C(String id, int value) {
		this.id = id;
		this.value = value;
	}

	@Override
	public void read(IOHelper pb) throws IOException {
		id = pb.readUTF();
		value = pb.readSignedVarInt();
	}

	@Override
	public void write(IOHelper pb) throws IOException {
		pb.writeUTF(id);
		pb.writeSignedVarInt(value);
	}

	@Override
	public void handle(NetHandler<?, ?, ?> handler, NetH from) {
		MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().applyCommand(id, value);
	}
}
