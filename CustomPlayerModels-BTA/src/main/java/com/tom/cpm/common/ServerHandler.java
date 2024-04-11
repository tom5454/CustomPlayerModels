package com.tom.cpm.common;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import net.minecraft.core.entity.player.EntityPlayer;

import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.retro.NetHandlerExt;

public class ServerHandler {
	public static NetHandlerExt<String, EntityPlayer, ServerNetworkImpl> netHandler;

	static {
		netHandler = new NetHandlerExt<>((a, b) -> a + ":" + b);
		netHandler.setGetPlayerUUID(p -> UUID.nameUUIDFromBytes(("OfflinePlayer:" + p.username).getBytes(StandardCharsets.UTF_8)));
		netHandler.setSendPacketServer(a -> a, ServerNetworkImpl::cpm$sendPacket, ent -> CustomPlayerModels.proxy.getTrackingPlayers(ent), e -> e);
		netHandler.setFindTracking((p, f) -> CustomPlayerModels.proxy.getTracking(p, f));
		netHandler.setSendChat((p, m) -> CustomPlayerModels.proxy.getServer(p).cpm$sendChat(m.<String>remap()));
		netHandler.setExecutor(() -> Runnable::run);
		netHandler.setGetNet(CustomPlayerModels.proxy::getServer);
		netHandler.setGetPlayer(ServerNetworkImpl::cpm$getPlayer);
		netHandler.setGetPlayerId(e -> e.id);
		netHandler.setGetOnlinePlayers(CustomPlayerModels.proxy::getPlayersOnline);
		netHandler.setKickPlayer((p, m) -> CustomPlayerModels.proxy.getServer(p).cpm$kickPlayer(m.remap()));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
	}

	public static void init() {}
}
