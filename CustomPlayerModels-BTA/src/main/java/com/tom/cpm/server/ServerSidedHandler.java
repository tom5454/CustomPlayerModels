package com.tom.cpm.server;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.EntityTrackerEntryImpl;
import net.minecraft.server.entity.EntityTrackerImpl;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.world.WorldServer;

import com.tom.cpm.SidedHandler;
import com.tom.cpm.common.ServerNetworkImpl;

public class ServerSidedHandler implements SidedHandler {

	@Override
	public void getTracking(Player player, Consumer<Player> f) {
		MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
		WorldServer ws = (WorldServer) player.world;
		EntityTrackerImpl et = server.getEntityTracker(ws.dimension.id);
		for (EntityTrackerEntryImpl tr : et.trackedEntitySet) {
			if (tr.trackedEntity instanceof Player && tr.trackedPlayers.contains(player)) {
				f.accept((Player) tr.trackedEntity);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<Player> getTrackingPlayers(Entity entity) {
		MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
		WorldServer ws = (WorldServer) entity.world;
		EntityTrackerImpl et = server.getEntityTracker(ws.dimension.id);
		EntityTrackerEntryImpl entry = et.trackedEntityHashTable.get(entity.id);
		if (entry == null)
			return Collections.emptySet();
		else
			return (Set) entry.trackedPlayers;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public List<Player> getPlayersOnline() {
		MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
		return (List) server.playerList.playerEntities;
	}

	@Override
	public ServerNetworkImpl getServer(Player pl) {
		return (ServerNetworkImpl) ((PlayerServer) pl).playerNetServerHandler;
	}
}
