package com.tom.cpm.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.class_340;
import net.minecraft.class_69;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import net.modificationstation.stationapi.api.util.Identifier;

import com.tom.cpm.common.ServerNetworkImpl;
import com.tom.cpm.shared.config.PlayerData;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerNetworkHandlerMixin implements ServerNetworkImpl {

	private @Shadow class_69 field_920;
	public @Shadow abstract void method_835(Packet arg);
	public @Shadow abstract void method_833(String string);

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
		method_835(new class_340(msg));
	}

	@Override
	public PlayerEntity cpm$getPlayer() {
		return field_920;
	}

	@Override
	public void cpm$kickPlayer(String msg) {
		method_833(msg);
	}

	@Override
	public void cpm$sendPacket(Identifier id, byte[] data) {
		MessagePacket p = new MessagePacket(id);
		p.bytes = data;
		method_835(p);
	}
}
