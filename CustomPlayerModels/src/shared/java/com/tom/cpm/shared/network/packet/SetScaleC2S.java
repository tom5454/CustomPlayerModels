package com.tom.cpm.shared.network.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.nbt.NBTTagList;
import com.tom.cpl.nbt.NBTTagString;
import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;
import com.tom.cpm.shared.util.Log;
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
		for(Entry<ScalingOptions, BiConsumer<P, Float>> e : handler.getScaleSetters().entrySet()) {
			float oldV = pd.scale.getOrDefault(e.getKey(), 1F);
			float selNewV = tag.getFloat(e.getKey().getNetKey());
			float newV = selNewV;
			Pair<Float, Float> l = NetworkUtil.getScalingLimits(e.getKey(), handler.getID(pl));
			newV = newV == 0 || l == null ? 1F : MathHelper.clamp(newV, l.getKey(), l.getValue());
			Log.debug("Scaling " + e.getKey() + " " + oldV + " -> " + newV);
			if(newV != oldV) {
				e.getValue().accept(pl, newV);
				pd.scale.put(e.getKey(), newV);
			}
			if(newV != selNewV && selNewV != 0) {
				blocked.add(e.getKey());
			}
		}
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
