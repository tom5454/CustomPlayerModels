package com.tom.cpm.common;

import java.util.Collections;

import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.ServerLifecycleHooks;

import com.mojang.brigadier.CommandDispatcher;

import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;

public class ServerHandler {
	public static NetHandler<ResourceLocation, ServerPlayer, ServerGamePacketListenerImpl> netHandler;

	static {
		netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setGetPlayerUUID(ServerPlayer::getUUID);
		netHandler.setSendPacket2(d -> new FriendlyByteBuf(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.send(new ClientboundCustomPayloadPacket(rl, pb)), ent -> {
			ChunkMap.TrackedEntity tr = ((ServerLevel)ent.level).getChunkSource().chunkMap.entityMap.get(ent.getId());
			if(tr != null) {
				return tr.seenBy;
			}
			return Collections.emptyList();
		});
		netHandler.setFindTracking((p, f) -> {
			for(ChunkMap.TrackedEntity tr : ((ServerLevel)p.level).getChunkSource().chunkMap.entityMap.values()) {
				if(tr.entity instanceof Player && tr.seenBy.contains(p.connection)) {
					f.accept((ServerPlayer) tr.entity);
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.connection.send(new ClientboundChatPacket(m.remap(), ChatType.CHAT, Util.NIL_UUID)));
		netHandler.setExecutor(ServerLifecycleHooks::getCurrentServer);
		if(ModList.get().isLoaded("pehkui")) {
			netHandler.setScaler(new PehkuiInterface());
		}
		netHandler.setGetNet(spe -> spe.connection);
		netHandler.setGetPlayer(net -> net.player);
		netHandler.setGetPlayerId(ServerPlayer::getId);
		netHandler.setGetOnlinePlayers(() -> ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers());
		netHandler.setKickPlayer((p, m) -> p.connection.disconnect(m.remap()));
		netHandler.setGetPlayerAnimGetters(p -> p.fallDistance, p -> p.getAbilities().flying);
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		netHandler.onJoin((ServerPlayer) evt.getPlayer());
	}

	@SubscribeEvent
	public void onTrackingStart(PlayerEvent.StartTracking evt) {
		ServerGamePacketListenerImpl handler = ((ServerPlayer)evt.getPlayer()).connection;
		NetH h = (NetH) handler;
		if(h.cpm$hasMod()) {
			if(evt.getTarget() instanceof Player) {
				netHandler.sendPlayerData((ServerPlayer) evt.getTarget(), (ServerPlayer) evt.getPlayer());
			}
		}
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent evt) {
		CommandDispatcher<CommandSourceStack> d = evt.getDispatcher();
		new Command(d);
	}

	@SubscribeEvent
	public void onRespawn(PlayerRespawnEvent evt) {
		if(!evt.isEndConquered()) {
			netHandler.onRespawn((ServerPlayer) evt.getPlayer());
		}
	}

	@SubscribeEvent
	public void onTick(ServerTickEvent evt) {
		if(evt.phase == Phase.END) {
			netHandler.tick();
		}
	}

	@SubscribeEvent
	public void onJump(LivingJumpEvent evt) {
		if(evt.getEntityLiving() instanceof ServerPlayer) {
			netHandler.onJump((ServerPlayer) evt.getEntityLiving());
		}
	}
}
