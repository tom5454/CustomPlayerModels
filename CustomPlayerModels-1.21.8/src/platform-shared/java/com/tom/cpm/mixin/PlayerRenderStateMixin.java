package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.network.chat.Component;

import com.tom.cpm.client.PlayerRenderStateAccess;
import com.tom.cpm.shared.config.Player;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements PlayerRenderStateAccess {
	private @Unique Player<net.minecraft.world.entity.player.Player> cpm$player;
	private @Unique Component cpm$modelStatus;

	@Override
	public void cpm$setPlayer(Player<net.minecraft.world.entity.player.Player> player) {
		this.cpm$player = player;
	}

	@Override
	public Player<net.minecraft.world.entity.player.Player> cpm$getPlayer() {
		return cpm$player;
	}

	@Override
	public void cpm$setModelStatus(Component status) {
		cpm$modelStatus = status;
	}

	@Override
	public Component cpm$getModelStatus() {
		return cpm$modelStatus;
	}
}
