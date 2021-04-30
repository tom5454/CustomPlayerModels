package com.tom.cpm.common;

import java.util.function.Predicate;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage.EntityTracker;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.shared.network.NetH;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NetworkHandler {

	public static void handlePacket(Packet<?> packet, NetH handler, boolean client) {
		try {
			if(!client) {
				CustomPayloadC2SPacket p = (CustomPayloadC2SPacket) packet;
				ServerHandler.netHandler.receiveServer(p.channel, p.data, (ServerPlayNetworkHandler) handler);
			} else {
				CustomPayloadS2CPacket p = (CustomPayloadS2CPacket) packet;
				CustomPlayerModelsClient.INSTANCE.netHandler.receiveClient(p.getChannel(), p.getData(), (ClientPlayNetworkHandler) handler);
			}
		} catch (Throwable e) {
			System.out.println("Exception while processing cpm packet");
			e.printStackTrace();
		}
	}

	public static void sendToAllTrackingAndSelf(ServerPlayerEntity ent, Packet<?> pckt, Predicate<ServerPlayerEntity> test, GenericFutureListener<? extends Future<? super Void>> future) {
		EntityTracker tr = ((ServerWorld)ent.world).getChunkManager().threadedAnvilChunkStorage.entityTrackers.get(ent.getId());
		for (EntityTrackingListener p : tr.listeners) {
			if(test.test(p.getPlayer())) {
				p.getPlayer().networkHandler.sendPacket(pckt, future);
			}
		}
		ent.networkHandler.sendPacket(pckt, future);
	}
}
