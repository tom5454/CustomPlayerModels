package com.tom.cpm.client;

import net.minecraft.network.chat.Component;

import com.tom.cpm.shared.config.Player;

public interface PlayerRenderStateAccess {
	void cpm$setPlayer(Player<net.minecraft.world.entity.player.Player> player);
	Player<net.minecraft.world.entity.player.Player> cpm$getPlayer();
	void cpm$setModelStatus(Component status);
	Component cpm$getModelStatus();
}
