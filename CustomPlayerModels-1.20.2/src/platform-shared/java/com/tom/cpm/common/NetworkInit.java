package com.tom.cpm.common;

import java.util.HashMap;
import java.util.concurrent.Executor;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;

import com.tom.cpm.shared.network.NetHandler;

public class NetworkInit {
	private static boolean initialized = false;

	public static void initNetworking(NetHandler<ResourceLocation, ?, ?> netH, Executor exec) {
		if(initialized)return;
		initialized = true;
		exec.execute(() -> {
			ClientboundCustomPayloadPacket.KNOWN_TYPES = new HashMap<>(ClientboundCustomPayloadPacket.KNOWN_TYPES);
			ServerboundCustomPayloadPacket.KNOWN_TYPES = new HashMap<>(ServerboundCustomPayloadPacket.KNOWN_TYPES);
			netH.registerIn(id -> ServerboundCustomPayloadPacket.KNOWN_TYPES.put(id, b -> new ByteArrayPayload(id, b)));
			netH.registerOut(id -> ClientboundCustomPayloadPacket.KNOWN_TYPES.put(id, b -> new ByteArrayPayload(id, b)));
		});
	}
}
