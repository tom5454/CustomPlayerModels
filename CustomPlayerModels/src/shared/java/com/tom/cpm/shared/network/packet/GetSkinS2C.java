package com.tom.cpm.shared.network.packet;

import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.IS2CPacket;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;

public class GetSkinS2C implements IS2CPacket {

	@Override
	public void read(IOHelper pb) throws IOException {
		pb.readByte();
	}

	@Override
	public void write(IOHelper pb) throws IOException {
		pb.writeByte(0);
	}

	@Override
	public void handle(NetHandler<?, ?, ?> handler, NetH from) {
		NetworkUtil.sendSkinDataToServer(handler);
	}
}
