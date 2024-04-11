package com.tom.cpm.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.net.packet.Packet3Chat;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.minecraft.server.net.handler.NetServerHandler;

import com.tom.cpm.common.CustomPayload;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.common.ServerNetworkImpl;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.io.FastByteArrayInputStream;

@Mixin(value = NetServerHandler.class, remap = false)
public abstract class ServerNetworkHandlerMixin implements ServerNetworkImpl {

	private @Shadow EntityPlayerMP playerEntity;
	public @Shadow abstract void sendPacket(Packet arg);
	public @Shadow abstract void kickPlayer(String string);

	private boolean cpm$hasMod;
	private PlayerData cpm$data;

	@Override
	public boolean cpm$hasMod() {
		return cpm$hasMod;
	}

	@Override
	public void cpm$setHasMod(boolean v) {
		this.cpm$hasMod = v;
	}

	@Override
	public PlayerData cpm$getEncodedModelData() {
		return cpm$data;
	}

	@Override
	public void cpm$setEncodedModelData(PlayerData data) {
		cpm$data = data;
	}

	@Override
	public void cpm$sendChat(String msg) {
		sendPacket(new Packet3Chat(msg));
	}

	@Override
	public EntityPlayer cpm$getPlayer() {
		return playerEntity;
	}

	@Override
	public void cpm$kickPlayer(String msg) {
		kickPlayer(msg);
	}

	@Override
	public void cpm$sendPacket(String id, byte[] data) {
		sendPacket(new CustomPayload(id, data));
	}

	@Override
	public void cpm$processCustomPayload(CustomPayload p) {
		ServerHandler.netHandler.receiveServer(p.getId(), new FastByteArrayInputStream(p.getData()), this);
	}
}
