package com.tom.cpm.shared.network.packet;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

public class ReceiveEventS2C extends NBTEntityS2C {

	public ReceiveEventS2C() {
		super();
	}

	public ReceiveEventS2C(int entityId, NBTTagCompound data) {
		super(entityId, data);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <P> void handle(NetHandler<?, P, ?> handler, NetH from, P player) {
		Player<?> pl = MinecraftClientAccess.get().getDefinitionLoader().loadPlayer(handler.getLoaderId(player), ModelDefinitionLoader.PLAYER_UNIQUE);
		pl.animState.receiveEvent(tag, pl.isClientPlayer());
	}
}
