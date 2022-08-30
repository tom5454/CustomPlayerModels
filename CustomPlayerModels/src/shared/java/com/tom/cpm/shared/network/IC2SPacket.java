package com.tom.cpm.shared.network;

import com.tom.cpm.shared.network.NetH.ServerNetH;

public interface IC2SPacket extends IPacket {

	@Override
	default void handle(NetHandler<?, ?, ?> handler, NetH from) {
		handle0(handler, from);
	}

	default <P> void handle0(NetHandler<?, P, ?> handler, NetH from) {
		ServerNetH h = (ServerNetH) from;
		handle(handler, h, handler.getPlayer(h));
	}

	<P> void handle(NetHandler<?, P, ?> handler, ServerNetH from, P player);
}
