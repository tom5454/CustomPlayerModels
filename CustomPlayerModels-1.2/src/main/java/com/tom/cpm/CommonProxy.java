package com.tom.cpm;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;

import com.tom.cpm.common.ServerNetworkImpl;

public abstract class CommonProxy {
	public void init() {

	}

	public void apiInit() {
	}

	public abstract void getTracking(EntityPlayer player, Consumer<EntityPlayer> f);
	public abstract Set<EntityPlayer> getTrackingPlayers(Entity entity);
	public abstract List<EntityPlayer> getPlayersOnline();
	public abstract ServerNetworkImpl getServer(EntityPlayer pl);
	public abstract boolean runCommand(String command, String sender, Object listener);
}
