package com.tom.cpm.server;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.EntityTracker;
import net.minecraft.server.entity.EntityTrackerEntry;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.minecraft.server.world.WorldServer;

import com.tom.cpm.SidedHandler;
import com.tom.cpm.common.ServerNetworkImpl;

public class ServerSidedHandler implements SidedHandler {

	@Override
	public void getTracking(EntityPlayer player, Consumer<EntityPlayer> f) {
		MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
		WorldServer ws = (WorldServer) player.world;
		EntityTracker et = server.getEntityTracker(ws.dimension.id);
		for (EntityTrackerEntry tr : et.trackedEntitySet) {
			if (tr.trackedEntity instanceof EntityPlayer && tr.trackedPlayers.contains(player)) {
				f.accept((EntityPlayer) tr.trackedEntity);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<EntityPlayer> getTrackingPlayers(Entity entity) {
		MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
		WorldServer ws = (WorldServer) entity.world;
		EntityTracker et = server.getEntityTracker(ws.dimension.id);
		EntityTrackerEntry entry = et.trackedEntityHashTable.get(entity.id);
		if (entry == null)
			return Collections.emptySet();
		else
			return (Set) entry.trackedPlayers;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public List<EntityPlayer> getPlayersOnline() {
		MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
		return (List) server.playerList.playerEntities;
	}

	@Override
	public ServerNetworkImpl getServer(EntityPlayer pl) {
		return (ServerNetworkImpl) ((EntityPlayerMP) pl).playerNetServerHandler;
	}
}
