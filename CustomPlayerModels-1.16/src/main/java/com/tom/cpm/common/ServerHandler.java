package com.tom.cpm.common;

import java.util.function.Predicate;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.world.server.ChunkManager.EntityTracker;
import net.minecraft.world.server.ServerWorld;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import com.mojang.brigadier.CommandDispatcher;

import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ServerHandler {
	public static NetHandler<ResourceLocation, ServerPlayerEntity, ServerPlayNetHandler> netHandler;

	static {
		netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setGetPlayerUUID(ServerPlayerEntity::getUUID);
		netHandler.setSendPacket(d -> new PacketBuffer(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.send(new SCustomPayloadPlayPacket(rl, pb)), (spe, rl, pb) -> sendToAllTrackingAndSelf(spe, new SCustomPayloadPlayPacket(rl, pb), ServerHandler::hasMod, null));
		netHandler.setFindTracking((p, f) -> {
			for(EntityTracker tr : ((ServerWorld)p.level).getChunkSource().chunkMap.entityMap.values()) {
				if(tr.entity instanceof PlayerEntity && tr.seenBy.contains(p)) {
					f.accept((ServerPlayerEntity) tr.entity);
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.connection.send(new SChatPacket(m.remap(), ChatType.SYSTEM, Util.NIL_UUID)));
		netHandler.setExecutor(ServerLifecycleHooks::getCurrentServer);
		if(ModList.get().isLoaded("pehkui")) {
			netHandler.setScaler(new PehkuiInterface());
		}
		netHandler.setGetNet(spe -> spe.connection);
		netHandler.setGetPlayer(net -> net.player);
		netHandler.setGetPlayerId(ServerPlayerEntity::getId);
		netHandler.setGetOnlinePlayers(() -> ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers());
		netHandler.setKickPlayer((p, m) -> p.connection.disconnect(m.remap()));
		netHandler.setGetPlayerAnimGetters(p -> p.fallDistance, p -> p.abilities.flying);
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		netHandler.onJoin((ServerPlayerEntity) evt.getPlayer());
	}

	@SubscribeEvent
	public void onTrackingStart(PlayerEvent.StartTracking evt) {
		ServerPlayNetHandler handler = ((ServerPlayerEntity)evt.getPlayer()).connection;
		NetH h = (NetH) handler;
		if(h.cpm$hasMod()) {
			if(evt.getTarget() instanceof PlayerEntity) {
				netHandler.sendPlayerData((ServerPlayerEntity) evt.getTarget(), (ServerPlayerEntity) evt.getPlayer());
			}
		}
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent evt) {
		CommandDispatcher<CommandSource> d = evt.getDispatcher();
		new Command(d);
	}

	@SubscribeEvent
	public void onRespawn(PlayerRespawnEvent evt) {
		if(!evt.isEndConquered()) {
			netHandler.onRespawn((ServerPlayerEntity) evt.getPlayer());
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
		if(evt.getEntityLiving() instanceof ServerPlayerEntity) {
			netHandler.onJump((ServerPlayerEntity) evt.getEntityLiving());
		}
	}

	public static boolean hasMod(ServerPlayerEntity spe) {
		return ((NetH)spe.connection).cpm$hasMod();
	}

	public static void sendToAllTrackingAndSelf(ServerPlayerEntity ent, IPacket<?> pckt, Predicate<ServerPlayerEntity> test, GenericFutureListener<? extends Future<? super Void>> future) {
		EntityTracker tr = ((ServerWorld)ent.level).getChunkSource().chunkMap.entityMap.get(ent.getId());
		if(tr != null) {
			for (ServerPlayerEntity p : tr.seenBy) {
				if(test.test(p)) {
					p.connection.send(pckt, future);
				}
			}
		}
		ent.connection.send(pckt, future);
	}
}
