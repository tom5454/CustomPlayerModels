package com.tom.cpm.common;

import java.util.Collections;
import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ChunkManager.EntityTracker;
import net.minecraft.world.server.ServerWorld;

import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;

public class ServerHandlerBase {

	public static NetHandler<ResourceLocation, ServerPlayerEntity, ServerPlayNetHandler> init() {
		NetHandler<ResourceLocation, ServerPlayerEntity, ServerPlayNetHandler> netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setGetPlayerUUID(ServerPlayerEntity::getUUID);
		netHandler.setSendPacketServer(d -> new PacketBuffer(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.send(new SCustomPayloadPlayPacket(rl, pb)), ent -> {
			EntityTracker tr = ((ServerWorld)ent.level).getChunkSource().chunkMap.entityMap.get(ent.getId());
			if(tr != null) {
				return tr.seenBy;
			}
			return Collections.emptyList();
		}, Function.identity());
		netHandler.setFindTracking((p, f) -> {
			for(EntityTracker tr : ((ServerWorld)p.level).getChunkSource().chunkMap.entityMap.values()) {
				if(tr.entity instanceof PlayerEntity && tr.seenBy.contains(p)) {
					f.accept((ServerPlayerEntity) tr.entity);
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.displayClientMessage(m.remap(), false));
		netHandler.setGetNet(spe -> spe.connection);
		netHandler.setGetPlayer(net -> net.player);
		netHandler.setGetPlayerId(ServerPlayerEntity::getId);
		netHandler.setKickPlayer((p, m) -> p.connection.disconnect(m.remap()));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
		return netHandler;
	}
}
