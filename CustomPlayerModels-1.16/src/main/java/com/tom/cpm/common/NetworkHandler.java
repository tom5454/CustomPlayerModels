package com.tom.cpm.common;

import java.util.function.Predicate;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.world.server.ChunkManager.EntityTracker;
import net.minecraft.world.server.ServerWorld;

import net.minecraftforge.fml.network.ICustomPacket;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.shared.network.NetH;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NetworkHandler {
	public static void handlePacket(ICustomPacket<?> packet, NetH handler, boolean client) {
		try {
			if(!client) {
				ServerHandler.netHandler.receiveServer(packet.getName(), packet.getInternalData(), (ServerPlayNetHandler) handler);
			} else {
				ClientProxy.INSTANCE.netHandler.receiveClient(packet.getName(), packet.getInternalData(), (ClientPlayNetHandler) handler);
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
