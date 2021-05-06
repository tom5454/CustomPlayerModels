package com.tom.cpmcore;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ResourceLocation;

import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.network.NetH.ServerNetH;

import io.netty.buffer.Unpooled;

public class CPMASMServerHooks {
	public static boolean onServerPacket(C17PacketCustomPayload pckt, NetHandlerPlayServer handler) {
		if(pckt.func_149559_c().startsWith(MinecraftObjectHolder.NETWORK_ID)) {
			ServerHandler.netHandler.receiveServer(new ResourceLocation(pckt.func_149559_c()), new PacketBuffer(Unpooled.wrappedBuffer(pckt.func_149558_e())), (ServerNetH) handler);
			return true;
		}
		return false;
	}
}
