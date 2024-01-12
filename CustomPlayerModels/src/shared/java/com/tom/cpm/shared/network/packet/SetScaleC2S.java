package com.tom.cpm.shared.network.packet;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.nbt.NBTTagList;
import com.tom.cpl.nbt.NBTTagString;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;
import com.tom.cpm.shared.util.ScalingOptions;

public class SetScaleC2S extends NBTC2S {

	public SetScaleC2S(NBTTagCompound data) {
		super(data);
	}

	public SetScaleC2S() {
	}

	@Override
	public <P> void handle(NetHandler<?, P, ?> handler, ServerNetH net, P pl) {
		PlayerData pd = net.cpm$getEncodedModelData();
		List<ScalingOptions> blocked = new ArrayList<>();
		pd.targetScale.clear();
		for (ScalingOptions so : ScalingOptions.VALUES) {
			pd.targetScale.put(so, tag.getFloat(so.getNetKey()));
		}
		pd.rescaleToTarget(handler, pl, blocked);
		pd.save(handler.getID(pl));
		NBTTagCompound ret = new NBTTagCompound();
		if(!blocked.isEmpty()) {
			NBTTagList list = new NBTTagList();
			blocked.forEach(sc -> list.appendTag(new NBTTagString(sc.getNetKey())));
			ret.setTag(NetworkUtil.SCALING, list);
		}
		handler.sendPacketTo(net, new ScaleInfoS2C(ret));
	}
}
