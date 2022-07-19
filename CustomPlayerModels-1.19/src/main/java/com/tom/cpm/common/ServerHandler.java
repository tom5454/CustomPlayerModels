package com.tom.cpm.common;

import java.util.function.Predicate;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
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

import com.tom.cpl.text.IText;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ServerHandler {
	public static NetHandler<ResourceLocation, ServerPlayer, ServerGamePacketListenerImpl> netHandler;

	static {
		netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setGetPlayerUUID(ServerPlayer::getUUID);
		netHandler.setSendPacket(d -> new FriendlyByteBuf(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.send(new ClientboundCustomPayloadPacket(rl, pb)), (spe, rl, pb) -> sendToAllTrackingAndSelf(spe, new ClientboundCustomPayloadPacket(rl, pb), ServerHandler::hasMod, null));
		netHandler.setFindTracking((p, f) -> {
			for(ChunkMap.TrackedEntity tr : ((ServerLevel)p.level).getChunkSource().chunkMap.entityMap.values()) {
				if(tr.entity instanceof Player && tr.seenBy.contains(p.connection)) {
					f.accept((ServerPlayer) tr.entity);
				}
			}
		});
		netHandler.setSendChat(ServerHandler::sendMessage);
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

	private static void sendMessage(ServerPlayer p, IText m) {
		Registry<ChatType> registry = p.level.registryAccess().registryOrThrow(Registry.CHAT_TYPE_REGISTRY);
		int id = registry.getId(registry.get(ChatType.SYSTEM));
		p.connection.send(new ClientboundSystemChatPacket(m.remap(), id));
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		netHandler.onJoin((ServerPlayer) evt.getEntity());
	}

	@SubscribeEvent
	public void onTrackingStart(PlayerEvent.StartTracking evt) {
		ServerGamePacketListenerImpl handler = ((ServerPlayer)evt.getEntity()).connection;
		NetH h = (NetH) handler;
		if(h.cpm$hasMod()) {
			if(evt.getTarget() instanceof Player) {
				netHandler.sendPlayerData((ServerPlayer) evt.getTarget(), (ServerPlayer) evt.getEntity());
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
			netHandler.onRespawn((ServerPlayer) evt.getEntity());
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
		if(evt.getEntity() instanceof ServerPlayer) {
			netHandler.onJump((ServerPlayer) evt.getEntity());
		}
	}

	public static boolean hasMod(ServerPlayer spe) {
		return ((NetH)spe.connection).cpm$hasMod();
	}

	public static void sendToAllTrackingAndSelf(ServerPlayer ent, Packet<?> pckt, Predicate<ServerPlayer> test, GenericFutureListener<? extends Future<? super Void>> future) {
		ChunkMap.TrackedEntity tr = ((ServerLevel)ent.level).getChunkSource().chunkMap.entityMap.get(ent.getId());
		if(tr != null) {
			for (ServerPlayerConnection p : tr.seenBy) {
				if(test.test(p.getPlayer())) {
					p.getPlayer().connection.send(pckt, future);
				}
			}
		}
		ent.connection.send(pckt, future);
	}
}
