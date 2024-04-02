package com.tom.cpmcore;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.io.FastByteArrayInputStream;
import com.tom.cpm.shared.network.NetH.ServerNetH;

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
			ServerHandler.netHandler.sendPlayerData((EntityPlayerMP) myEntity, trackingPlayer);
		}
	}
}
