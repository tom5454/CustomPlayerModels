package com.tom.cpm.common;

import java.util.Base64;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.MessageType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage.EntityTracker;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.common.NetH.ServerNetH;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerData;

import io.netty.buffer.Unpooled;

public class ServerHandler {

	public static void onPlayerJoin(ServerPlayerEntity spe) {
		PacketByteBuf pb = new PacketByteBuf(Unpooled.buffer());
		NbtCompound data = new NbtCompound();
		pb.writeCompound(data);
		spe.networkHandler.sendPacket(new CustomPayloadS2CPacket(NetworkHandler.helloPacket, pb));
		if(spe.getServer().isDedicated()) {
			ServerNetH snh = (ServerNetH) spe.networkHandler;
			ConfigEntry e = ModConfig.getConfig().getEntry(ConfigKeys.SERVER_SKINS).getEntry(spe.getUuid().toString());
			boolean forced = e.getBoolean(ConfigKeys.FORCED, false);
			String b64 = e.getString(ConfigKeys.MODEL, null);
			if(b64 != null) {
				snh.cpm$setEncodedModelData(new PlayerData(Base64.getDecoder().decode(b64), forced, true));
			}
		}
	}

	public static void onTrackingStart(ServerPlayerEntity spe, Entity target) {
		ServerPlayNetworkHandler handler = spe.networkHandler;
		NetH h = (NetH) handler;
		if(h.cpm$hasMod()) {
			if(target instanceof PlayerEntity) {
				sendPlayerData((ServerPlayerEntity) target, handler);
			}
		}
	}

	private static void sendPlayerData(ServerPlayerEntity target, ServerPlayNetworkHandler handler) {
		PlayerData dt = ((ServerNetH)target.networkHandler).cpm$getEncodedModelData();
		handler.sendPacket(new CustomPayloadS2CPacket(NetworkHandler.setSkin, writeSkinData(dt, target)));
	}

	public static void receivePacket(CustomPayloadC2SPacket packet, ServerNetH handler) {
		Identifier rl = packet.channel;
		ServerPlayNetworkHandler h = (ServerPlayNetworkHandler) handler;
		if(NetworkHandler.helloPacket.equals(rl)) {
			handler.cpm$getServer().execute(() -> {
				handler.cpm$setHasMod(true);
				for(EntityTracker tr : ((ServerWorld)h.player.world).getChunkManager().threadedAnvilChunkStorage.entityTrackers.values()) {
					if(tr.entity instanceof PlayerEntity && tr.listeners.contains(h.player.networkHandler)) {
						sendPlayerData((ServerPlayerEntity) tr.entity, h);
					}
				}
				if(handler.cpm$getEncodedModelData() != null) {
					h.sendPacket(new CustomPayloadS2CPacket(NetworkHandler.setSkin, writeSkinData(handler.cpm$getEncodedModelData(), h.player)));
				} else {
					h.sendPacket(new CustomPayloadS2CPacket(NetworkHandler.getSkin, new PacketByteBuf(Unpooled.EMPTY_BUFFER)));
				}
			});
		} else if(NetworkHandler.setSkin.equals(rl)) {
			if(handler.cpm$getEncodedModelData() == null || !handler.cpm$getEncodedModelData().forced) {
				NbtCompound tag = packet.data.readCompound();
				handler.cpm$getServer().execute(() -> {
					handler.cpm$setEncodedModelData(tag.contains("data") ? new PlayerData(tag.getByteArray("data"), false, false) : null);
					NetworkHandler.sendToAllTrackingAndSelf(h.player, new CustomPayloadS2CPacket(NetworkHandler.setSkin, writeSkinData(handler.cpm$getEncodedModelData(), h.player)), ServerHandler::hasMod, null);
					ConfigEntry e = ModConfig.getConfig().getEntry(ConfigKeys.SERVER_SKINS);
					e.clearValue(h.player.getUuid().toString());
					ModConfig.getConfig().save();
				});
			} else {
				h.sendPacket(new GameMessageS2CPacket(new TranslatableText("chat.cpm.skinForced"), MessageType.CHAT, Util.NIL_UUID));
			}
		}
	}

	public static PacketByteBuf writeSkinData(PlayerData dt, Entity ent) {
		PacketByteBuf pb = new PacketByteBuf(Unpooled.buffer());
		pb.writeVarInt(ent.getId());
		NbtCompound data = new NbtCompound();
		if(dt != null) {
			data.putBoolean("forced", dt.forced);
			data.putByteArray("data", dt.data);
		}
		pb.writeCompound(data);
		return pb;
	}

	public static boolean hasMod(ServerPlayerEntity spe) {
		return ((NetH)spe.networkHandler).cpm$hasMod();
	}

}
