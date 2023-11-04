package com.tom.cpm.shared.network.packet;

import java.io.IOException;
import java.util.UUID;

import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.IC2SPacket;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;

public class RequestPlayerC2S implements IC2SPacket {
	private UUID entityId;
	private boolean full;

	public RequestPlayerC2S() {
	}

	public RequestPlayerC2S(UUID entityId, boolean full) {
		this.entityId = entityId;
		this.full = full;
	}

	@Override
	public void read(IOHelper pb) throws IOException {
		entityId = pb.readUUID();
		full = pb.readBoolean();
	}

	@Override
	public void write(IOHelper pb) throws IOException {
		pb.writeUUID(entityId);
		pb.writeBoolean(full);
	}

	@Override
	public <P> void handle(NetHandler<?, P, ?> handler, ServerNetH from, P player) {
		P req = handler.getPlayerByUUID(entityId);
		if(req != null) {
			if (full)
				NetworkUtil.sendPlayerData(handler, req, player);
			else
				NetworkUtil.sendPlayerState(handler, req, player);
		}
	}
}
