package com.tom.cpm.shared.network.packet;

import java.io.IOException;

import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.IC2SPacket;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;

public class HelloC2S implements IC2SPacket {
	private byte status;

	public HelloC2S() {
	}

	public HelloC2S(int status) {
		this.status = (byte) status;
	}

	@Override
	public void read(IOHelper pb) throws IOException {
		status = pb.readByte();
	}

	@Override
	public void write(IOHelper pb) throws IOException {
		pb.writeByte(status);
	}

	@Override
	public <P> void handle(NetHandler<?, P, ?> handler, ServerNetH net, P pl) {
		net.cpm$setHasMod(true);
		handler.forEachTracking(pl, p -> NetworkUtil.sendPlayerData(handler, p, pl));
		PlayerData pd = net.cpm$getEncodedModelData();
		if(pd.canChangeModel()) {
			handler.sendPacketTo(net, new GetSkinS2C());
		} else {
			handler.sendPacketTo(net, NetworkUtil.writeSkinData(handler, pd, pl));
		}
		if(ModConfig.getWorldConfig().getBoolean(ConfigKeys.RECOMMEND_SAFETY_SETTINGS, false)) {
			NetworkUtil.sendSafetySettings(handler, net);
		}
	}
}
