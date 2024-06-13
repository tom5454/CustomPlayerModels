package com.tom.cpm.common;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

public class NetworkInit {

	public static void register(PacketRegistry toClient, PacketRegistry toServer) {
		ServerHandler.netHandler.registerIn(id -> makeCodec(id, toServer));
		ServerHandler.netHandler.registerOut(id -> makeCodec(id, toClient));
	}

	public static void register(PacketRegistry toClient, PacketRegistry toServer, PacketRegistry bidirectional) {
		Map<CustomPacketPayload.Type<ByteArrayPayload>, StreamCodec<? super FriendlyByteBuf, ByteArrayPayload>> cl = new HashMap<>();
		Map<CustomPacketPayload.Type<ByteArrayPayload>, StreamCodec<? super FriendlyByteBuf, ByteArrayPayload>> se = new HashMap<>();
		Map<CustomPacketPayload.Type<ByteArrayPayload>, StreamCodec<? super FriendlyByteBuf, ByteArrayPayload>> bi = new HashMap<>();

		register((a, b) -> {
			if (se.containsKey(a)) {
				se.remove(a);
				bi.put(a, b);
			} else {
				cl.put(a, b);
			}
		}, (a, b) -> {
			if (cl.containsKey(a)) {
				cl.remove(a);
				bi.put(a, b);
			} else {
				se.put(a, b);
			}
		});
		cl.forEach(toClient);
		se.forEach(toServer);
		bi.forEach(bidirectional);
	}

	private static void makeCodec(CustomPacketPayload.Type<ByteArrayPayload> type, PacketRegistry c) {
		StreamCodec<FriendlyByteBuf, ByteArrayPayload> codec = CustomPacketPayload.codec(ByteArrayPayload::write, d -> new ByteArrayPayload(type, d));
		c.accept(type, codec);
	}

	@FunctionalInterface
	public static interface PacketRegistry extends BiConsumer<CustomPacketPayload.Type<ByteArrayPayload>, StreamCodec<? super FriendlyByteBuf, ByteArrayPayload>> {
		@Override
		void accept(Type<ByteArrayPayload> t, StreamCodec<? super FriendlyByteBuf, ByteArrayPayload> u);
	}
}
