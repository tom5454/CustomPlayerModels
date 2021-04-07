package com.tom.cpm.common;

import java.util.Base64;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ChunkManager.EntityTracker;
import net.minecraft.world.server.ServerWorld;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.ICustomPacket;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import com.mojang.brigadier.CommandDispatcher;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.common.NetH.ServerNetH;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerData;

import io.netty.buffer.Unpooled;

public class ServerHandler {

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
		CompoundNBT data = new CompoundNBT();
		pb.writeCompoundTag(data);
		ServerPlayerEntity spe = (ServerPlayerEntity) evt.getPlayer();
		spe.connection.sendPacket(new SCustomPayloadPlayPacket(NetworkHandler.helloPacket, pb));
		if(evt.getPlayer().getServer().isDedicatedServer()) {
			ServerNetH snh = (ServerNetH) spe.connection;
			ConfigEntry e = ModConfig.getConfig().getEntry(ConfigKeys.SERVER_SKINS).getEntry(spe.getUniqueID().toString());
			boolean forced = e.getBoolean(ConfigKeys.FORCED, false);
			String b64 = e.getString(ConfigKeys.MODEL, null);
			if(b64 != null) {
				snh.cpm$setEncodedModelData(new PlayerData(Base64.getDecoder().decode(b64), forced, true));
			}
		}
	}

	@SubscribeEvent
	public void onTrackingStart(PlayerEvent.StartTracking evt) {
		ServerPlayNetHandler handler = ((ServerPlayerEntity)evt.getPlayer()).connection;
		NetH h = (NetH) handler;
		if(h.cpm$hasMod()) {
			if(evt.getTarget() instanceof PlayerEntity) {
				sendPlayerData((ServerPlayerEntity) evt.getTarget(), handler);
			}
		}
	}

	public static void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
		CommandCPM.register(dispatcher);
	}

	private static void sendPlayerData(ServerPlayerEntity target, ServerPlayNetHandler handler) {
		PlayerData dt = ((ServerNetH)target.connection).cpm$getEncodedModelData();
		handler.sendPacket(new SCustomPayloadPlayPacket(NetworkHandler.setSkin, writeSkinData(dt, target)));
	}

	public static void receivePacket(ICustomPacket<?> packet, ServerNetH handler) {
		ResourceLocation rl = packet.getName();
		ServerPlayNetHandler h = (ServerPlayNetHandler) handler;
		if(NetworkHandler.helloPacket.equals(rl)) {
			ServerLifecycleHooks.getCurrentServer().execute(() -> {
				handler.cpm$setHasMod(true);
				for(EntityTracker tr : ((ServerWorld)h.player.world).getChunkProvider().chunkManager.entities.values()) {
					if(tr.entity instanceof PlayerEntity && tr.trackingPlayers.contains(h.player)) {
						sendPlayerData((ServerPlayerEntity) tr.entity, h);
					}
				}
				if(handler.cpm$getEncodedModelData() != null) {
					h.sendPacket(new SCustomPayloadPlayPacket(NetworkHandler.setSkin, writeSkinData(handler.cpm$getEncodedModelData(), h.player)));
				} else {
					h.sendPacket(new SCustomPayloadPlayPacket(NetworkHandler.getSkin, new PacketBuffer(Unpooled.EMPTY_BUFFER)));
				}
			});
		} else if(NetworkHandler.setSkin.equals(rl)) {
			if(handler.cpm$getEncodedModelData() == null || !handler.cpm$getEncodedModelData().forced) {
				CompoundNBT tag = packet.getInternalData().readCompoundTag();
				ServerLifecycleHooks.getCurrentServer().execute(() -> {
					handler.cpm$setEncodedModelData(tag.contains("data") ? new PlayerData(tag.getByteArray("data"), false, false) : null);
					NetworkHandler.sendToAllTrackingAndSelf(h.player, new SCustomPayloadPlayPacket(NetworkHandler.setSkin, writeSkinData(handler.cpm$getEncodedModelData(), h.player)), ServerHandler::hasMod, null);
					ConfigEntry e = ModConfig.getConfig().getEntry(ConfigKeys.SERVER_SKINS);
					e.clearValue(h.player.getUniqueID().toString());
					ModConfig.getConfig().save();
				});
			} else {
				h.sendPacket(new SChatPacket(new TranslationTextComponent("chat.cpm.skinForced"), ChatType.CHAT));
			}
		}
	}

	public static PacketBuffer writeSkinData(PlayerData dt, Entity ent) {
		PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
		pb.writeVarInt(ent.getEntityId());
		CompoundNBT data = new CompoundNBT();
		if(dt != null) {
			data.putBoolean("forced", dt.forced);
			data.putByteArray("data", dt.data);
		}
		pb.writeCompoundTag(data);
		return pb;
	}

	public static boolean hasMod(ServerPlayerEntity spe) {
		return ((NetH)spe.connection).cpm$hasMod();
	}

}
