package com.tom.cpm.shared.network.packet;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;

public class SetSkinC2S extends NBTC2S {

	public SetSkinC2S(NBTTagCompound data) {
		super(data);
	}

	public SetSkinC2S() {
	}

	@Override
	public <P> void handle(NetHandler<?, P, ?> handler, ServerNetH net, P player) {
		PlayerData pd = net.cpm$getEncodedModelData();
		if(pd.canChangeModel()) {
			pd.setModel(tag.hasKey(NetworkUtil.DATA_TAG) ? tag.getByteArray(NetworkUtil.DATA_TAG) : null, false, false);
			handler.sendPacketToTracking(player, NetworkUtil.writeSkinData(handler, pd, player));
			pd.save(handler.getID(player));
		} else {
			handler.sendChat(player, NetworkUtil.FORCED_CHAT_MSG);
		}
	}
}
