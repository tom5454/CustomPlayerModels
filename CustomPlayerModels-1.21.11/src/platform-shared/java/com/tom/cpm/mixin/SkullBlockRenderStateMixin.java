package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.blockentity.state.SkullBlockRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;

import com.tom.cpm.client.SkullBlockRenderStateAccess;
import com.tom.cpm.shared.config.Player;

@Mixin(SkullBlockRenderState.class)
public class SkullBlockRenderStateMixin implements SkullBlockRenderStateAccess {
	private Player<Avatar> cpm$player;
	private PlayerSkin cpm$skin;

	@Override
	public void cpm$setPlayer(Player<Avatar> player) {
		cpm$player = player;
	}

	@Override
	public Player<Avatar> cpm$getPlayer() {
		return cpm$player;
	}

	@Override
	public void cpm$setSkin(PlayerSkin tex) {
		cpm$skin = tex;
	}

	@Override
	public PlayerSkin cpm$getSkin() {
		return cpm$skin;
	}
}
