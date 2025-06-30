package com.tom.cpm.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.common.NetworkInit.PacketRegistry;
import com.tom.cpm.shared.io.FastByteArrayInputStream;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetH.ServerNetH;

public class PlatformCommon {

	public static final IPayloadHandler<ByteArrayPayload> PAYLOAD_HANDLER = (p, ctx) -> {
		if (ctx.flow() == PacketFlow.CLIENTBOUND)
			CustomPlayerModelsClient.INSTANCE.netHandler.receiveClient(p.id(), new FastByteArrayInputStream(p.data()), (NetH) ctx.connection().getPacketListener());
		else
			ServerHandler.netHandler.receiveServer(p.id(), new FastByteArrayInputStream(p.data()), (ServerNetH) ctx.connection().getPacketListener());
	};

	public static Biome.ClimateSettings getClimateSettings(Biome b) {
		return b.getModifiedClimateSettings();
	}

	@FunctionalInterface
	public static interface PacketRegistryP extends PacketRegistry {
		void accept(Type<ByteArrayPayload> t, StreamCodec<? super FriendlyByteBuf, ByteArrayPayload> u, IPayloadHandler<ByteArrayPayload> h);

		@Override
		default void accept(Type<ByteArrayPayload> t, StreamCodec<? super FriendlyByteBuf, ByteArrayPayload> u) {
			accept(t, u, PAYLOAD_HANDLER);
		}

		public static void register(PacketRegistryP toClient, PacketRegistryP toServer, PacketRegistryBi bidirectional) {
			NetworkInit.register(toClient, toServer, bidirectional);
		}
	}

	@FunctionalInterface
	public static interface PacketRegistryBi extends PacketRegistry {
		void accept(Type<ByteArrayPayload> t, StreamCodec<? super FriendlyByteBuf, ByteArrayPayload> u, IPayloadHandler<ByteArrayPayload> hs, IPayloadHandler<ByteArrayPayload> hc);

		@Override
		default void accept(Type<ByteArrayPayload> t, StreamCodec<? super FriendlyByteBuf, ByteArrayPayload> u) {
			accept(t, u, PAYLOAD_HANDLER, PAYLOAD_HANDLER);
		}
	}
}
