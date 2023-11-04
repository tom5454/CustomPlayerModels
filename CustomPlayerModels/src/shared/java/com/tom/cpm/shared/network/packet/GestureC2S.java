package com.tom.cpm.shared.network.packet;

import java.io.IOException;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.IC2SPacket;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;

public class GestureC2S implements IC2SPacket {
	private byte[] gestureData;

	public GestureC2S() {
	}

	public GestureC2S(byte[] gestureData) {
		this.gestureData = gestureData;
	}

	@Override
	public void read(IOHelper pb) throws IOException {
		gestureData = pb.readByteArray();
	}

	@Override
	public void write(IOHelper pb) throws IOException {
		pb.writeByteArray(gestureData);
	}

	@Override
	public <P> void handle(NetHandler<?, P, ?> handler, ServerNetH net, P player) {
		if(gestureData.length > 256)return;
		PlayerData pd = net.cpm$getEncodedModelData();
		pd.gestureData = gestureData;
		NBTTagCompound evt = new NBTTagCompound();
		evt.setByteArray(NetworkUtil.GESTURE, gestureData);
		handler.sendPacketToTracking(player, new ReceiveEventS2C(handler.getPlayerId(player), evt));
	}
}
