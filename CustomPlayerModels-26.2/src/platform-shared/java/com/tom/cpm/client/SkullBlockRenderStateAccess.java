package com.tom.cpm.client;

import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;

import com.tom.cpm.shared.config.Player;

public interface SkullBlockRenderStateAccess {
	void cpm$setPlayer(Player<Avatar> player);
	Player<Avatar> cpm$getPlayer();
	void cpm$setSkin(PlayerSkin tex);
	PlayerSkin cpm$getSkin();
}
