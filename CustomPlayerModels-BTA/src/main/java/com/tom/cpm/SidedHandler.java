package com.tom.cpm;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;

import com.tom.cpm.common.ServerNetworkImpl;

public interface SidedHandler {
	public void getTracking(EntityPlayer player, Consumer<EntityPlayer> f);
	public Set<EntityPlayer> getTrackingPlayers(Entity entity);
	public List<EntityPlayer> getPlayersOnline();
	public ServerNetworkImpl getServer(EntityPlayer pl);
}
