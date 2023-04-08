package com.tom.cpm.common;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

import com.tom.cpm.MinecraftServerObject;
import com.tom.cpm.shared.MinecraftServerAccess;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

public class ServerHandler extends ServerHandlerBase {
	public static NetHandler<ResourceLocation, ServerPlayerEntity, ServerPlayNetHandler> netHandler;

	public static MinecraftServer getServer() {
		return ((MinecraftServerObject)MinecraftServerAccess.get()).getServer();
	}

	static {
		netHandler = init();
		netHandler.setGetOnlinePlayers(() -> getServer().getPlayerList().getPlayers());
		netHandler.setExecutor(n -> n.server);
		if(FabricLoader.getInstance().isModLoaded("pehkui")) {
			netHandler.setScaler(new PehkuiInterface());
		}
	}

	public static void onPlayerJoin(ServerPlayerEntity spe) {
		netHandler.onJoin(spe);
	}

	public static void onTrackingStart(Entity target, ServerPlayerEntity spe) {
		ServerPlayNetHandler handler = spe.connection;
		NetH h = (NetH) handler;
		if(h.cpm$hasMod()) {
			if(target instanceof PlayerEntity) {
				netHandler.sendPlayerData((ServerPlayerEntity) target, spe);
			}
		}
	}

	public static void jump(Object player) {
		if(player instanceof ServerPlayerEntity) {
			netHandler.onJump((ServerPlayerEntity) player);
		}
	}
}
