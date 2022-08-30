package com.tom.cpm.shared.network.packet;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;

public class SetSkinS2C extends NBTEntityS2C {

	public SetSkinS2C() {
		super();
	}

	public SetSkinS2C(int entityId, NBTTagCompound data) {
		super(entityId, data);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <P> void handle(NetHandler<?, P, ?> handler, NetH from, P player) {
		MinecraftClientAccess.get().getDefinitionLoader().setModel(handler.getLoaderId(player), tag.hasKey(NetworkUtil.DATA_TAG) ? tag.getByteArray(NetworkUtil.DATA_TAG) : null, tag.getBoolean(NetworkUtil.FORCED_TAG));
	}

}
