package com.tom.cpm.mixin.server;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;

import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.network.NetH.ServerNetH;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin implements ServerNetH {
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
}
