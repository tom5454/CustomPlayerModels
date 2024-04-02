package com.tom.cpm.server;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.class_69;
import net.modificationstation.stationapi.api.event.registry.MessageListenerRegistryEvent;
import net.modificationstation.stationapi.api.registry.Registry;

import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.io.FastByteArrayInputStream;
import com.tom.cpm.shared.network.NetH.ServerNetH;

public class CPMServer {

	@EventListener
	public static void registerServerListeners(MessageListenerRegistryEvent ev) {
		ServerHandler.netHandler.registerIn(r -> {
			Registry.register(ev.registry, r, (p, m) -> {
				ServerHandler.netHandler.receiveServer(r, new FastByteArrayInputStream(m.bytes), (ServerNetH) ((class_69) p).field_255);
			});
		});
	}
}
