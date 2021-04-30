package com.tom.cpm.common;

import java.util.function.Predicate;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

import com.tom.cpm.client.ClientProxy;

public class NetworkHandler {

	public static void handlePacket(Packet<?> packet, Object handler, boolean client) {
		try {
			if(!client) {
				CPacketCustomPayload pckt = (CPacketCustomPayload) packet;
				ServerHandler.netHandler.receiveServer(new ResourceLocation(pckt.getChannelName()), pckt.getBufferData(), (NetHandlerPlayServer) handler);
			} else {
				SPacketCustomPayload pckt = (SPacketCustomPayload) packet;
				ClientProxy.INSTANCE.netHandler.receiveClient(new ResourceLocation(pckt.getChannelName()), pckt.getBufferData(), (NetHandlerPlayClient) handler);
			}
		} catch (Throwable e) {
			System.out.println("Exception while processing cpm packet");
			e.printStackTrace();
		}
	}

	public static void sendToAllTrackingAndSelf(EntityPlayerMP ent, Packet<?> pckt, Predicate<EntityPlayerMP> test) {
		for (EntityPlayer pl : ((WorldServer)ent.world).getEntityTracker().getTrackingPlayers(ent)) {
			EntityPlayerMP p = (EntityPlayerMP) pl;
			if(test.test(p)) {
				p.connection.sendPacket(pckt);
			}
		}
		ent.connection.sendPacket(pckt);
	}
}
