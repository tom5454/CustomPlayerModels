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
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentTranslation;
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
		pb.writeCompoundTag(data);
		EntityPlayerMP spe = (EntityPlayerMP) evt.player;
		spe.connection.sendPacket(new SPacketCustomPayload(NetworkHandler.helloPacket.toString(), pb));
		if(evt.player.getServer().isDedicatedServer()) {
			ConfigEntry e = ModConfig.getConfig().getEntry(ConfigKeys.SERVER_SKINS).getEntry(spe.getUniqueID().toString());
			boolean forced = e.getBoolean(ConfigKeys.FORCED, false);
			String b64 = e.getString(ConfigKeys.MODEL, null);
			if(b64 != null) {
				CPMASMClientHooks.setEncodedModelData(spe.connection, new PlayerData(Base64.getDecoder().decode(b64), forced, true));
			}
		}
	}

	@SubscribeEvent
	public void onTrackingStart(PlayerEvent.StartTracking evt) {
		NetHandlerPlayServer handler = ((EntityPlayerMP)evt.getEntityPlayer()).connection;
		if(CPMASMClientHooks.hasMod(handler)) {
			if(evt.getTarget() instanceof EntityPlayer) {
				sendPlayerData((EntityPlayerMP) evt.getTarget(), handler);
			}
		}
	}

	private static void sendPlayerData(EntityPlayerMP target, NetHandlerPlayServer handler) {
		PlayerData dt = CPMASMClientHooks.getEncodedModelData(target.connection);
		handler.sendPacket(new SPacketCustomPayload(NetworkHandler.setSkin.toString(), writeSkinData(dt, target)));
	}

	public static void receivePacket(CPacketCustomPayload packet, NetHandlerPlayServer h) {
		ResourceLocation rl = new ResourceLocation(packet.getChannelName());
		if(NetworkHandler.helloPacket.equals(rl)) {
			FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
				CPMASMClientHooks.setHasMod(h, true);
				for(EntityTrackerEntry tr : ((WorldServer)h.player.world).getEntityTracker().entries) {
					if(tr.getTrackedEntity() instanceof EntityPlayer && tr.trackingPlayers.contains(h.player)) {
						sendPlayerData((EntityPlayerMP) tr.getTrackedEntity(), h);
					}
				}
				if(CPMASMClientHooks.getEncodedModelData(h) != null) {
					h.sendPacket(new SPacketCustomPayload(NetworkHandler.setSkin.toString(), writeSkinData(CPMASMClientHooks.getEncodedModelData(h), h.player)));
				} else {
					h.sendPacket(new SPacketCustomPayload(NetworkHandler.getSkin.toString(), new PacketBuffer(Unpooled.EMPTY_BUFFER)));
				}
			});
		} else if(NetworkHandler.setSkin.equals(rl)) {
			if(CPMASMClientHooks.getEncodedModelData(h) == null || !CPMASMClientHooks.getEncodedModelData(h).forced) {
				NBTTagCompound tag;
				try {
					tag = packet.getBufferData().readCompoundTag();
					FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
						CPMASMClientHooks.setEncodedModelData(h, tag.hasKey("data") ? new PlayerData(tag.getByteArray("data"), false, false) : null);
						NetworkHandler.sendToAllTrackingAndSelf(h.player, new SPacketCustomPayload(NetworkHandler.setSkin.toString(), writeSkinData(CPMASMClientHooks.getEncodedModelData(h), h.player)), ServerHandler::hasMod);
						ConfigEntry e = ModConfig.getConfig().getEntry(ConfigKeys.SERVER_SKINS);
						e.clearValue(h.player.getUniqueID().toString());
						ModConfig.getConfig().save();
					});
				} catch (IOException e1) {}
			} else {
				h.sendPacket(new SPacketChat(new TextComponentTranslation("chat.cpm.skinForced"), ChatType.CHAT));
			}
		}
	}

	public static PacketBuffer writeSkinData(PlayerData dt, Entity ent) {
		PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
		pb.writeVarInt(ent.getEntityId());
		NBTTagCompound data = new NBTTagCompound();
		if(dt != null) {
			data.setBoolean("forced", dt.forced);
			data.setByteArray("data", dt.data);
		}
		pb.writeCompoundTag(data);
		return pb;
	}

	public static boolean hasMod(EntityPlayerMP spe) {
		return CPMASMClientHooks.hasMod(spe.connection);
	}

}
