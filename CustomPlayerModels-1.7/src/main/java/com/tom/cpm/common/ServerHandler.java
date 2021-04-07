package com.tom.cpm.common;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;

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

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.PlayerDataExt;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpmcore.CPMASMClientHooks;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import io.netty.buffer.Unpooled;

public class ServerHandler {

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
		NBTTagCompound data = new NBTTagCompound();
		try {
			pb.writeNBTTagCompoundToBuffer(data);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		EntityPlayerMP spe = (EntityPlayerMP) evt.player;
		spe.playerNetServerHandler.sendPacket(new S3FPacketCustomPayload(NetworkHandler.helloPacket.toString(), pb));
		if(spe.mcServer.isDedicatedServer()) {
			ConfigEntry e = ModConfig.getConfig().getEntry(ConfigKeys.SERVER_SKINS).getEntry(spe.getUniqueID().toString());
			boolean forced = e.getBoolean(ConfigKeys.FORCED, false);
			String b64 = e.getString(ConfigKeys.MODEL, null);
			if(b64 != null) {
				CPMASMClientHooks.setEncodedModelData(spe.playerNetServerHandler, new PlayerDataExt(Base64.getDecoder().decode(b64), forced, true, 0));
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
		handler.sendPacket(new S3FPacketCustomPayload(NetworkHandler.setLayer.toString(), writeLayerData(target)));
	}

	@SuppressWarnings("unchecked")
	public static void receivePacket(C17PacketCustomPayload packet, NetHandlerPlayServer h) {
		ResourceLocation rl = new ResourceLocation(packet.func_149559_c());
		if(NetworkHandler.helloPacket.equals(rl)) {
			CPMASMClientHooks.setHasMod(h, true);
			for(EntityTrackerEntry tr : (Set<EntityTrackerEntry>) ((WorldServer)h.playerEntity.worldObj).getEntityTracker().trackedEntities) {
				if(tr.myEntity instanceof EntityPlayer && tr.trackingPlayers.contains(h.playerEntity)) {
					sendPlayerData((EntityPlayerMP) tr.myEntity, h);
				}
			}
			if(CPMASMClientHooks.getEncodedModelData(h) != null) {
				h.sendPacket(new S3FPacketCustomPayload(NetworkHandler.setSkin.toString(), writeSkinData(CPMASMClientHooks.getEncodedModelData(h), h.playerEntity)));
			} else {
				h.sendPacket(new S3FPacketCustomPayload(NetworkHandler.getSkin.toString(), new PacketBuffer(Unpooled.EMPTY_BUFFER)));
			}
		} else if(NetworkHandler.setSkin.equals(rl)) {
			if(CPMASMClientHooks.getEncodedModelData(h) == null || !CPMASMClientHooks.getEncodedModelData(h).forced) {
				NBTTagCompound tag;
				try {
					PacketBuffer pb = new PacketBuffer(Unpooled.wrappedBuffer(packet.func_149558_e()));
					tag = pb.readNBTTagCompoundFromBuffer();
					CPMASMClientHooks.setEncodedModelData(h, new PlayerDataExt(tag.hasKey("data") ? tag.getByteArray("data") : null, false, false, PlayerDataExt.getSkinLayer(h.playerEntity)));
					NetworkHandler.sendToAllTrackingAndSelf(h.playerEntity, new S3FPacketCustomPayload(NetworkHandler.setSkin.toString(), writeSkinData(CPMASMClientHooks.getEncodedModelData(h), h.playerEntity)), ServerHandler::hasMod);
					ConfigEntry e = ModConfig.getConfig().getEntry(ConfigKeys.SERVER_SKINS);
					e.clearValue(h.playerEntity.getUniqueID().toString());
					ModConfig.getConfig().save();
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
			} else {
				h.sendPacket(new S02PacketChat(new ChatComponentTranslation("chat.cpm.skinForced")));
			}
		} else if(NetworkHandler.setLayer.equals(rl)) {
			PlayerDataExt.setSkinLayer(h.playerEntity, packet.func_149558_e()[0]);
			NetworkHandler.sendToAllTrackingAndSelf(h.playerEntity, new S3FPacketCustomPayload(NetworkHandler.setLayer.toString(), writeLayerData(h.playerEntity)), ServerHandler::hasMod);
		}
	}

	private static PacketBuffer writeLayerData(EntityPlayerMP playerEntity) {
		PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
		pb.writeVarIntToBuffer(playerEntity.getEntityId());
		pb.writeByte(PlayerDataExt.getSkinLayer(playerEntity));
		return pb;
	}

	public static PacketBuffer writeSkinData(PlayerData dt, Entity ent) {
		PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
		pb.writeVarIntToBuffer(ent.getEntityId());
		NBTTagCompound data = new NBTTagCompound();
		if(dt != null) {
			data.setBoolean("forced", dt.forced);
			data.setByteArray("data", dt.data);
		}
		try {
			pb.writeNBTTagCompoundToBuffer(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return pb;
	}

	public static boolean hasMod(EntityPlayerMP spe) {
		return CPMASMClientHooks.hasMod(spe.playerNetServerHandler);
	}

}
