package com.tom.cpm.common;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.MinecraftServerObject;
import com.tom.cpm.shared.MinecraftServerAccess;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

public class ServerHandler extends ServerHandlerBase {
	public static NetHandler<CustomPacketPayload.Type<ByteArrayPayload>, ServerPlayer, ServerGamePacketListenerImpl> netHandler;

	public static MinecraftServer getServer() {
		return ((MinecraftServerObject)MinecraftServerAccess.get()).getServer();
	}

	static {
		netHandler = init();
		netHandler.setGetOnlinePlayers(() -> getServer().getPlayerList().getPlayers());
		netHandler.setExecutor(n -> n.server);
		if(CustomPlayerModels.isModLoaded("pehkui")) {
			//netHandler.setScaler(new PehkuiInterface());
		}
	}

	public static void onPlayerJoin(ServerPlayer spe) {
		netHandler.onJoin(spe);
	}

	public static void onTrackingStart(Entity target, ServerPlayer spe) {
		ServerGamePacketListenerImpl handler = spe.connection;
		NetH h = (NetH) handler;
		if(h.cpm$hasMod()) {
			if(target instanceof Player) {
				netHandler.sendPlayerData((ServerPlayer) target, spe);
			}
		}
	}

	public static void jump(Object player) {
		if(player instanceof ServerPlayer) {
			netHandler.onJump((ServerPlayer) player);
		}
	}
}
