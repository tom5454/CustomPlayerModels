package com.tom.cpm.common;

import java.util.Collections;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.player.Player;

import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;

public class ServerHandlerBase {

	public static NetHandler<ResourceLocation, ServerPlayer, ServerGamePacketListenerImpl> init() {
		NetHandler<ResourceLocation, ServerPlayer, ServerGamePacketListenerImpl> netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setGetPlayerUUID(ServerPlayer::getUUID);
		netHandler.setSendPacketServer(d -> new FriendlyByteBuf(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.send(new ClientboundCustomPayloadPacket(rl, pb)), ent -> {
			ChunkMap.TrackedEntity tr = ((ServerLevel)ent.level).getChunkSource().chunkMap.entityMap.get(ent.getId());
			if(tr != null) {
				return tr.seenBy;
			}
			return Collections.emptyList();
		}, ServerPlayerConnection::getPlayer);
		netHandler.setFindTracking((p, f) -> {
			for(ChunkMap.TrackedEntity tr : ((ServerLevel)p.level).getChunkSource().chunkMap.entityMap.values()) {
				if(tr.entity instanceof Player && tr.seenBy.contains(p.connection)) {
					f.accept((ServerPlayer) tr.entity);
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.displayClientMessage(m.remap(), false));
		netHandler.setGetNet(spe -> spe.connection);
		netHandler.setGetPlayer(net -> net.player);
		netHandler.setGetPlayerId(ServerPlayer::getId);
		netHandler.setKickPlayer((p, m) -> p.connection.disconnect(m.remap()));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
		netHandler.addScaler(new AttributeScaler());
		return netHandler;
	}
}
