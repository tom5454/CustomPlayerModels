package com.tom.cpm.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ByteArrayPayload(CustomPacketPayload.Type<ByteArrayPayload> id, byte[] data) implements CustomPacketPayload {

	public ByteArrayPayload(CustomPacketPayload.Type<ByteArrayPayload> id, FriendlyByteBuf b) {
		this(id, toArray(b));
	}

	public void write(FriendlyByteBuf b) {
		b.writeBytes(data);
	}

	private static byte[] toArray(FriendlyByteBuf b) {
		byte[] d = new byte[b.readableBytes()];
		b.readBytes(d);
		return d;
	}

	@Override
	public CustomPacketPayload.Type<ByteArrayPayload> type() {
		return id;
	}
}
