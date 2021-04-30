package com.tom.cpmcore;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

import com.tom.cpm.common.NetworkHandler;
import com.tom.cpm.shared.MinecraftObjectHolder;

public class CPMASMServerHooks {
	public static boolean onServerPacket(C17PacketCustomPayload pckt, NetHandlerPlayServer handler) {
		if(pckt.getChannelName().startsWith(MinecraftObjectHolder.NETWORK_ID)) {
			NetworkHandler.handlePacket(pckt, handler, false);
			return true;
		}
		return false;
	}
}
