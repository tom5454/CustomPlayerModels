package com.tom.cpm.common;

import java.util.function.Predicate;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ChunkManager.EntityTracker;
import net.minecraft.world.server.ServerWorld;

import net.minecraftforge.fml.network.ICustomPacket;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.common.NetH.ServerNetH;
import com.tom.cpm.shared.MinecraftObjectHolder;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NetworkHandler {
	public static final ResourceLocation helloPacket = new ResourceLocation(MinecraftObjectHolder.NETWORK_ID, "hello");
	public static final ResourceLocation setSkin = new ResourceLocation(MinecraftObjectHolder.NETWORK_ID, "set_skin");
	public static final ResourceLocation getSkin = new ResourceLocation(MinecraftObjectHolder.NETWORK_ID, "get_skin");

	public static void handlePacket(ICustomPacket<?> packet, NetH handler, boolean client) {
		try {
			if(!client) {
				ServerHandler.receivePacket(packet, (ServerNetH) handler);
			} else {
				ClientProxy.INSTANCE.receivePacket(packet, handler);
			}
		} catch (Throwable e) {
			System.out.println("Exception while processing cpm packet");
			e.printStackTrace();
		}
	}

	public static void sendToAllTrackingAndSelf(ServerPlayerEntity ent, IPacket<?> pckt, Predicate<ServerPlayerEntity> test, GenericFutureListener<? extends Future<? super Void>> future) {
		EntityTracker tr = ((ServerWorld)ent.world).getChunkProvider().chunkManager.entities.get(ent.getEntityId());
		for (ServerPlayerEntity p : tr.trackingPlayers) {
			if(test.test(p)) {
				p.connection.sendPacket(pckt, future);
			}
		}
		ent.connection.sendPacket(pckt, future);
	}
}
