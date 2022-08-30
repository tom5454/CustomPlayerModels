package com.tom.cpm.shared.network;

import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;

public interface IPacket {
	void read(IOHelper pb) throws IOException;
	void write(IOHelper pb) throws IOException;
	void handle(NetHandler<?, ?, ?> handler, NetH from);

	default void handleRaw(NetHandler<?, ?, ?> handler, NetH from) {
		handler.execute(from, () -> handle(handler, from));
	}
}
