package com.tom.cpm.server;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_39;
import net.minecraft.class_69;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import com.tom.cpl.text.IText;
import com.tom.cpm.SidedHandler;
import com.tom.cpm.common.Command;
import com.tom.cpm.common.ServerNetworkImpl;
import com.tom.cpm.shared.util.Log;

public class ServerSidedHandler implements SidedHandler {

	@Override
	public void getTracking(PlayerEntity player, Consumer<PlayerEntity> f) {
		/*for (EntityTrackerEntry tr : (Set<EntityTrackerEntry>) ((class_73) player.world).field_273.field_934) {
			if (tr.trackedEntity instanceof PlayerEntity && tr.trackedPlayers.contains(player)) {
				f.accept((PlayerEntityMP) tr.trackedEntity);
			}
		}*/
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<PlayerEntity> getTrackingPlayers(Entity entity) {
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
	public List<PlayerEntity> getPlayersOnline() {
		MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
		return server.field_2842.field_578;
	}

	@Override
	public ServerNetworkImpl getServer(PlayerEntity pl) {
		return (ServerNetworkImpl) ((class_69) pl).field_255;
	}

	public static final Command.CommandHandlerBase<class_39> cpm = new Command.CommandHandlerBase<>() {

		@Override
		protected void sendMessage(class_39 sender, String string) {
			sender.method_1409(string);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void sendSuccess(class_39 sender, IText text) {
			String t = text.remap();
			sender.method_1409(t);
			String r = sender.method_1410();
			t = "[" + r + ": " + t + "]";
			Log.info(t);
			MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
			server.field_2842.method_588("\u00A77\u00A7o" + t);
		}
	};
}
