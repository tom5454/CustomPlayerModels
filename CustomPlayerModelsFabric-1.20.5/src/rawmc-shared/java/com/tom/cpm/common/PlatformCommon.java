package com.tom.cpm.common;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.biome.Biome;

public class PlatformCommon {

	public static Biome.ClimateSettings getClimateSettings(Biome b) {
		return b.climateSettings;
	}

	public static void registerS2CPacket(CustomPacketPayload.Type<ByteArrayPayload> id, StreamCodec<? super FriendlyByteBuf, ByteArrayPayload> codec) {
		PayloadTypeRegistry.playS2C().register(id, codec);
	}

	public static void registerC2SPacket(CustomPacketPayload.Type<ByteArrayPayload> id, StreamCodec<? super FriendlyByteBuf, ByteArrayPayload> codec) {
		PayloadTypeRegistry.playC2S().register(id, codec);
	}
}
