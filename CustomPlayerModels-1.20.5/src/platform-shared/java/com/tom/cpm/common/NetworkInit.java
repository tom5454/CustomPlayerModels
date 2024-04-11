package com.tom.cpm.common;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.tom.cpm.shared.network.NetHandler;

public class NetworkInit {
	private static boolean initialized = false;

	public static void initNetworking(NetHandler<CustomPacketPayload.Type<ByteArrayPayload>, ?, ?> netH, Executor exec) {
		if(initialized)return;
		initialized = true;
		exec.execute(() -> {
			netH.registerIn(id -> makeCodec(id, PlatformCommon::registerC2SPacket));
			netH.registerOut(id -> makeCodec(id, PlatformCommon::registerS2CPacket));
		});
	}

	private static void makeCodec(CustomPacketPayload.Type<ByteArrayPayload> type, BiConsumer<CustomPacketPayload.Type<ByteArrayPayload>, StreamCodec<FriendlyByteBuf, ByteArrayPayload>> c) {
		StreamCodec<FriendlyByteBuf, ByteArrayPayload> codec = CustomPacketPayload.codec(ByteArrayPayload::write, d -> new ByteArrayPayload(type, d));
		c.accept(type, codec);
	}
}
