package com.tom.cpm.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ByteArrayPayload(ResourceLocation id, byte[] data) implements CustomPacketPayload {

	public ByteArrayPayload(ResourceLocation id, FriendlyByteBuf b) {
		this(id, b.readByteArray());
	}

	@Override
	public void write(FriendlyByteBuf b) {
		b.writeByteArray(data);
	}

}
