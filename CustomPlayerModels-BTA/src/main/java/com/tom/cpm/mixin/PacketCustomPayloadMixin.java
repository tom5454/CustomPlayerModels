package com.tom.cpm.mixin;

import java.io.DataInputStream;
import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.net.packet.PacketCustomPayload;

// TODO remove if BTA fixes this
@Mixin(value = PacketCustomPayload.class, remap = false)
public abstract class PacketCustomPayloadMixin extends Packet {
	public @Shadow String channel;
	public @Shadow byte[] data;

	@Override
	@Overwrite
	public void read(final DataInputStream in) throws IOException {
		this.channel = readStringUTF8(in, 128);
		final int length = in.readInt();
		if (length > 0 && length < 32768) {
			in.readFully(this.data = new byte[length]);
		}
	}
}
