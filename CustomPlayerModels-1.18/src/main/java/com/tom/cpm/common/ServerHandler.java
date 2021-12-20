package com.tom.cpm.common;

import java.util.function.Predicate;

import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.RegisterCommandsEvent;
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
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ServerHandler {
	public static NetHandler<ResourceLocation, CompoundTag, ServerPlayer, FriendlyByteBuf, ServerGamePacketListenerImpl> netHandler;

	static {
		netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setNewNbt(CompoundTag::new);
		netHandler.setNewPacketBuffer(() -> new FriendlyByteBuf(Unpooled.buffer()));
		netHandler.setGetPlayerUUID(ServerPlayer::getUUID);
		netHandler.setWriteCompound(FriendlyByteBuf::writeNbt, FriendlyByteBuf::readNbt);
		netHandler.setSendPacket((c, rl, pb) -> c.send(new ClientboundCustomPayloadPacket(rl, pb)), (spe, rl, pb) -> sendToAllTrackingAndSelf(spe, new ClientboundCustomPayloadPacket(rl, pb), ServerHandler::hasMod, null));
		netHandler.setWritePlayerId((pb, pl) -> pb.writeVarInt(pl.getId()));
		netHandler.setNBTSetters(CompoundTag::putBoolean, CompoundTag::putByteArray, CompoundTag::putFloat);
		netHandler.setNBTGetters(CompoundTag::getBoolean, CompoundTag::getByteArray, CompoundTag::getFloat);
		netHandler.setContains(CompoundTag::contains);
		netHandler.setFindTracking((p, f) -> {
			for(ChunkMap.TrackedEntity tr : ((ServerLevel)p.level).getChunkSource().chunkMap.entityMap.values()) {
				if(tr.entity instanceof Player && tr.seenBy.contains(p.connection)) {
					f.accept((ServerPlayer) tr.entity);
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.connection.send(new ClientboundChatPacket(new TranslatableComponent(m), ChatType.CHAT, Util.NIL_UUID)));
		netHandler.setExecutor(ServerLifecycleHooks::getCurrentServer);
		netHandler.setScaleSetter((spe, sc) -> {
			if(ModList.get().isLoaded("pehkui")) {
				if(sc == 0) {
					PehkuiInterface.setScale(spe, 1);
				} else {
					PehkuiInterface.setScale(spe, sc);
				}
			}
		});
		netHandler.setGetNet(spe -> spe.connection);
		netHandler.setGetPlayer(net -> net.player);
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
		CommandCPM.register(d);
	}

	@SubscribeEvent
	public void onRespawn(PlayerRespawnEvent evt) {
		if(!evt.isEndConquered()) {
			netHandler.onRespawn((ServerPlayer) evt.getPlayer());
		}
	}

	public static boolean hasMod(ServerPlayer spe) {
		return ((NetH)spe.connection).cpm$hasMod();
	}

	public static void sendToAllTrackingAndSelf(ServerPlayer ent, Packet<?> pckt, Predicate<ServerPlayer> test, GenericFutureListener<? extends Future<? super Void>> future) {
		ChunkMap.TrackedEntity tr = ((ServerLevel)ent.level).getChunkSource().chunkMap.entityMap.get(ent.getId());
		for (ServerPlayerConnection p : tr.seenBy) {
			if(test.test(p.getPlayer())) {
				p.getPlayer().connection.send(pckt, future);
			}
		}
		ent.connection.send(pckt, future);
	}
}
