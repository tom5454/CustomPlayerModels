package com.tom.cpm.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import net.minecraft.core.net.handler.NetHandler;
import net.minecraft.core.net.packet.Packet;

public class CustomPayload extends Packet {
	private byte[] id;
	private byte[] data;

	public CustomPayload() {
	}

	public CustomPayload(String id, byte[] data) {
		this.id = id.getBytes(StandardCharsets.UTF_8);
		this.data = data;
	}

	@Override
	public int getPacketSize() {
		return id.length + data.length + 3;
	}

	@Override
	public void processPacket(NetHandler netHandler) {
		((CPMPayloadHandler) netHandler).cpm$processCustomPayload(this);
	}

	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		int idl = dataInputStream.read();
		if (idl < 0)throw new IOException();
		id = new byte[idl];
		dataInputStream.readFully(id);
		short dl = dataInputStream.readShort();
		if (dl < 0)throw new IOException();
		data = new byte[dl];
		dataInputStream.readFully(data);
	}

	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.write(id.length);
		dataOutputStream.write(id);
		dataOutputStream.writeShort(data.length);
		dataOutputStream.write(data);
	}

	public String getId() {
		return new String(id, StandardCharsets.UTF_8);
	}

	public byte[] getData() {
		return data;
	}
}
