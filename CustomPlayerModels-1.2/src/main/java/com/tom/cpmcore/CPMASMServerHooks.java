package com.tom.cpmcore;

import net.minecraft.src.AnvilSaveHandler;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.Packet3Chat;

import com.tom.cpm.MinecraftServerObject;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.io.FastByteArrayInputStream;
import com.tom.cpm.shared.network.NetH.ServerNetH;

import cpw.mods.fml.server.FMLServerHandler;

public class CPMASMServerHooks {
	public static boolean onServerPacket(Packet250CustomPayload pckt, NetServerHandler handler) {
		if(pckt.channel.startsWith(MinecraftObjectHolder.NETWORK_ID)) {
			byte[] dt = pckt.data;
			ServerHandler.netHandler.receiveServer(pckt.channel, new FastByteArrayInputStream(dt != null ? dt : new byte[0]), (ServerNetH) handler);
			return true;
		}
		return false;
	}

	public static void startTracking(EntityPlayer myEntity, EntityPlayerMP trackingPlayer) {
		NetServerHandler handler = trackingPlayer.playerNetServerHandler;
		if (((ServerNetH)handler).cpm$hasMod()) {
			ServerHandler.netHandler.sendPlayerData(myEntity, trackingPlayer);
		}
	}

	public static void inj_sendChat(NetServerHandler handler, String msg) {
		handler.sendPacket(new Packet3Chat(msg));
	}

	public static EntityPlayer inj_getPlayer(NetServerHandler handler) {
		return handler.getPlayerEntity();
	}

	public static void inj_kickPlayer(NetServerHandler handler, String msg) {
		handler.kickPlayer(msg);
	}

	public static void inj_sendPacket(NetServerHandler handler, String id, byte[] data) {
		handler.sendPacket(ServerHandler.packet(id, data));
	}

	public static void onStarting(AnvilSaveHandler sh) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(sh));
	}

	public static void onStartingBukkit() {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(FMLServerHandler.instance().getServer().getWorldManager(0).getSaveHandler()));
	}

	public static void onStopped() {
		ModConfig.getWorldConfig().save();
		MinecraftObjectHolder.setServerObject(null);
	}
}
