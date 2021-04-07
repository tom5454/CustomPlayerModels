package com.tom.cpm.common;

import java.io.IOException;
import java.util.Base64;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpmcore.CPMASMClientHooks;

import io.netty.buffer.Unpooled;

public class ServerHandler {

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
		NBTTagCompound data = new NBTTagCompound();
		pb.writeNBTTagCompoundToBuffer(data);
		EntityPlayerMP spe = (EntityPlayerMP) evt.player;
		spe.playerNetServerHandler.sendPacket(new S3FPacketCustomPayload(NetworkHandler.helloPacket.toString(), pb));
		if(spe.mcServer.isDedicatedServer()) {
			ConfigEntry e = ModConfig.getConfig().getEntry(ConfigKeys.SERVER_SKINS).getEntry(spe.getUniqueID().toString());
			boolean forced = e.getBoolean(ConfigKeys.FORCED, false);
			String b64 = e.getString(ConfigKeys.MODEL, null);
			if(b64 != null) {
				CPMASMClientHooks.setEncodedModelData(spe.playerNetServerHandler, new PlayerData(Base64.getDecoder().decode(b64), forced, true));
			}
		}
	}

	@SubscribeEvent
	public void onTrackingStart(PlayerEvent.StartTracking evt) {
		NetHandlerPlayServer handler = ((EntityPlayerMP)evt.entityPlayer).playerNetServerHandler;
		if(CPMASMClientHooks.hasMod(handler)) {
			if(evt.target instanceof EntityPlayer) {
				sendPlayerData((EntityPlayerMP) evt.target, handler);
			}
		}
	}

	private static void sendPlayerData(EntityPlayerMP target, NetHandlerPlayServer handler) {
		PlayerData dt = CPMASMClientHooks.getEncodedModelData(target.playerNetServerHandler);
		handler.sendPacket(new S3FPacketCustomPayload(NetworkHandler.setSkin.toString(), writeSkinData(dt, target)));
	}

	public static void receivePacket(C17PacketCustomPayload packet, NetHandlerPlayServer h) {
		ResourceLocation rl = new ResourceLocation(packet.getChannelName());
		if(NetworkHandler.helloPacket.equals(rl)) {
			FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
				CPMASMClientHooks.setHasMod(h, true);
				for(EntityTrackerEntry tr : ((WorldServer)h.playerEntity.worldObj).getEntityTracker().trackedEntities) {
					if(tr.trackedEntity instanceof EntityPlayer && tr.trackingPlayers.contains(h.playerEntity)) {
						sendPlayerData((EntityPlayerMP) tr.trackedEntity, h);
					}
				}
				if(CPMASMClientHooks.getEncodedModelData(h) != null) {
					h.sendPacket(new S3FPacketCustomPayload(NetworkHandler.setSkin.toString(), writeSkinData(CPMASMClientHooks.getEncodedModelData(h), h.playerEntity)));
				} else {
					h.sendPacket(new S3FPacketCustomPayload(NetworkHandler.getSkin.toString(), new PacketBuffer(Unpooled.EMPTY_BUFFER)));
				}
			});
		} else if(NetworkHandler.setSkin.equals(rl)) {
			if(CPMASMClientHooks.getEncodedModelData(h) == null || !CPMASMClientHooks.getEncodedModelData(h).forced) {
				NBTTagCompound tag;
				try {
					tag = packet.getBufferData().readNBTTagCompoundFromBuffer();
					FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
						CPMASMClientHooks.setEncodedModelData(h, tag.hasKey("data") ? new PlayerData(tag.getByteArray("data"), false, false) : null);
						NetworkHandler.sendToAllTrackingAndSelf(h.playerEntity, new S3FPacketCustomPayload(NetworkHandler.setSkin.toString(), writeSkinData(CPMASMClientHooks.getEncodedModelData(h), h.playerEntity)), ServerHandler::hasMod);
						ConfigEntry e = ModConfig.getConfig().getEntry(ConfigKeys.SERVER_SKINS);
						e.clearValue(h.playerEntity.getUniqueID().toString());
						ModConfig.getConfig().save();
					});
				} catch (IOException e1) {}
			} else {
				h.sendPacket(new S02PacketChat(new ChatComponentTranslation("chat.cpm.skinForced")));
			}
		}
	}

	public static PacketBuffer writeSkinData(PlayerData dt, Entity ent) {
		PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
		pb.writeVarIntToBuffer(ent.getEntityId());
		NBTTagCompound data = new NBTTagCompound();
		if(dt != null) {
			data.setBoolean("forced", dt.forced);
			data.setByteArray("data", dt.data);
		}
		pb.writeNBTTagCompoundToBuffer(data);
		return pb;
	}

	public static boolean hasMod(EntityPlayerMP spe) {
		return CPMASMClientHooks.hasMod(spe.playerNetServerHandler);
	}

}
