package com.tom.cpm.server;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;

import com.tom.cpm.SidedHandler;
import com.tom.cpm.common.ServerNetworkImpl;

public class ServerSidedHandler implements SidedHandler {

	@Override
	public void getTracking(EntityPlayer player, Consumer<EntityPlayer> f) {
		/*for (EntityTrackerEntry tr : (Set<EntityTrackerEntry>) ((class_73) player.world).field_273.field_934) {
			if (tr.trackedEntity instanceof PlayerEntity && tr.trackedPlayers.contains(player)) {
				f.accept((PlayerEntityMP) tr.trackedEntity);
			}
		}*/
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<EntityPlayer> getTrackingPlayers(Entity entity) {
		/*class_79 et = ((class_73) entity.world).field_273;
		EntityTrackerEntry entry = (EntityTrackerEntry) et.trackedEntityHashTable.lookup(entity.entityId);
		if (entry == null)
			return Collections.emptySet();
		else
			return entry.trackedPlayers;*/
		return Collections.emptySet();
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
