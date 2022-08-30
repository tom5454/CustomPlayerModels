package com.tom.cpm.shared.network.packet;

import com.tom.cpl.nbt.NBTTag;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.nbt.NBTTagList;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.network.ModelEventType;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;

public class SubEventC2S extends NBTC2S {

	public SubEventC2S(NBTTagCompound data) {
		super(data);
	}

	public SubEventC2S() {
	}

	@Override
	public <P> void handle(NetHandler<?, P, ?> handler, ServerNetH net, P player) {
		PlayerData pd = net.cpm$getEncodedModelData();
		pd.eventSubs.clear();
		NBTTagList list = tag.getTagList(NetworkUtil.EVENT_LIST, NBTTag.TAG_STRING);
		for (int i = 0;i<list.tagCount();i++) {
			ModelEventType type = ModelEventType.of(list.getStringTagAt(i));
			if(type != null)pd.eventSubs.add(type);
		}
	}
}
