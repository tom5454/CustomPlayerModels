package com.tom.cpm.common;

import java.util.Collections;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage.EntityTracker;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import com.tom.cpm.MinecraftServerObject;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;

public class ServerHandler {
	public static NetHandler<Identifier, ServerPlayerEntity, ServerPlayNetworkHandler> netHandler;

	static {
		netHandler = new NetHandler<>(Identifier::new);
		netHandler.setGetPlayerUUID(ServerPlayerEntity::getUuid);
		netHandler.setSendPacket2(d -> new PacketByteBuf(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.sendPacket(new CustomPayloadS2CPacket(rl, pb)), ent -> {
			EntityTracker tr = ((ServerWorld)ent.world).getChunkManager().threadedAnvilChunkStorage.entityTrackers.get(ent.getEntityId());
			if(tr != null) {
				return tr.playersTracking;
			}
			return Collections.emptyList();
		});
		netHandler.setFindTracking((p, f) -> {
			for(EntityTracker tr : ((ServerWorld)p.world).getChunkManager().threadedAnvilChunkStorage.entityTrackers.values()) {
				if(tr.entity instanceof PlayerEntity && tr.playersTracking.contains(p)) {
					f.accept((ServerPlayerEntity) tr.entity);
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.networkHandler.sendPacket(new GameMessageS2CPacket(m.remap(), MessageType.CHAT, Util.NIL_UUID)));
		netHandler.setExecutor(n -> ((IServerNetHandler)n).cpm$getServer());
		if(FabricLoader.getInstance().isModLoaded("pehkui")) {
			netHandler.setScaler(new PehkuiInterface());
		}
		netHandler.setGetNet(spe -> spe.networkHandler);
		netHandler.setGetPlayer(net -> net.player);
		netHandler.setGetPlayerId(ServerPlayerEntity::getEntityId);
		netHandler.setGetOnlinePlayers(() -> MinecraftServerObject.getServer().getPlayerManager().getPlayerList());
		netHandler.setKickPlayer((p, m) -> p.networkHandler.disconnect(m.remap()));
		netHandler.setGetPlayerAnimGetters(p -> p.fallDistance, p -> p.abilities.flying);
	}

	public static void onPlayerJoin(ServerPlayerEntity spe) {
		netHandler.onJoin(spe);
	}

	public static void onTrackingStart(Entity target, ServerPlayerEntity spe) {
		ServerPlayNetworkHandler handler = spe.networkHandler;
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
