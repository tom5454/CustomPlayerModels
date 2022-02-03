package com.tom.cpmcore;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ResourceLocation;

import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.network.NetH.ServerNetH;

import io.netty.buffer.ByteBufInputStream;

public class CPMASMServerHooks {
	public static boolean onServerPacket(C17PacketCustomPayload pckt, NetHandlerPlayServer handler) {
		if(pckt.getChannelName().startsWith(MinecraftObjectHolder.NETWORK_ID)) {
			ServerHandler.netHandler.receiveServer(new ResourceLocation(pckt.getChannelName()), new ByteBufInputStream(pckt.getBufferData()), (ServerNetH) handler);
			return true;
		}
		return false;
	}
}
