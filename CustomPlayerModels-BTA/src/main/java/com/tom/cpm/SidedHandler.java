package com.tom.cpm;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;

import com.tom.cpm.common.ServerNetworkImpl;

public interface SidedHandler {
	public void getTracking(Player player, Consumer<Player> f);
	public Set<Player> getTrackingPlayers(Entity entity);
	public List<Player> getPlayersOnline();
	public ServerNetworkImpl getServer(Player pl);
}
