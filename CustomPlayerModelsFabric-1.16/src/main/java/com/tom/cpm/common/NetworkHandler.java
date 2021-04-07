package com.tom.cpm.common;

import java.util.function.Predicate;

import net.fabricmc.fabric.impl.networking.CustomPayloadC2SPacketAccessor;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage.EntityTracker;
import net.minecraft.util.Identifier;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.common.NetH.ServerNetH;
import com.tom.cpm.shared.MinecraftObjectHolder;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NetworkHandler {
	public static final Identifier helloPacket = new Identifier(MinecraftObjectHolder.NETWORK_ID, "hello");
	public static final Identifier setSkin = new Identifier(MinecraftObjectHolder.NETWORK_ID, "set_skin");
	public static final Identifier getSkin = new Identifier(MinecraftObjectHolder.NETWORK_ID, "get_skin");

	public static void handlePacket(Packet<?> packet, NetH handler, boolean client) {
		try {
			if(!client) {
				ServerHandler.receivePacket((CustomPayloadC2SPacketAccessor) packet, (ServerNetH) handler);
			} else {
				CustomPlayerModelsClient.INSTANCE.receivePacket((CustomPayloadS2CPacket) packet, handler);
			}
		} catch (Throwable e) {
			System.out.println("Exception while processing cpm packet");
			e.printStackTrace();
		}
	}

	public static void sendToAllTrackingAndSelf(ServerPlayerEntity ent, Packet<?> pckt, Predicate<ServerPlayerEntity> test, GenericFutureListener<? extends Future<? super Void>> future) {
		EntityTracker tr = ((ServerWorld)ent.world).getChunkManager().threadedAnvilChunkStorage.entityTrackers.get(ent.getEntityId());
		for (ServerPlayerEntity p : tr.playersTracking) {
			if(test.test(p)) {
				p.networkHandler.sendPacket(pckt, future);
			}
		}
		ent.networkHandler.sendPacket(pckt, future);
	}
}
