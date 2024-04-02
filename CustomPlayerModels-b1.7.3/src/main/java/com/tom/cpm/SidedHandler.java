package com.tom.cpm;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import com.tom.cpm.common.ServerNetworkImpl;

public interface SidedHandler {
	public void getTracking(PlayerEntity player, Consumer<PlayerEntity> f);
	public Set<PlayerEntity> getTrackingPlayers(Entity entity);
	public List<PlayerEntity> getPlayersOnline();
	public ServerNetworkImpl getServer(PlayerEntity pl);
}
