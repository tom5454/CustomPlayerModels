package com.tom.cpm.common;

import java.util.function.Predicate;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage.EntityTracker;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import com.tom.cpl.text.IText;
import com.tom.cpm.MinecraftServerObject;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ServerHandler {
	public static NetHandler<Identifier, ServerPlayerEntity, ServerPlayNetworkHandler> netHandler;

	static {
		netHandler = new NetHandler<>(Identifier::new);
		netHandler.setGetPlayerUUID(ServerPlayerEntity::getUuid);
		netHandler.setSendPacket(d -> new PacketByteBuf(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.sendPacket(new CustomPayloadS2CPacket(rl, pb)), (spe, rl, pb) -> sendToAllTrackingAndSelf(spe, new CustomPayloadS2CPacket(rl, pb), ServerHandler::hasMod, null));
		netHandler.setFindTracking((p, f) -> {
			for(EntityTracker tr : ((ServerWorld)p.world).getChunkManager().threadedAnvilChunkStorage.entityTrackers.values()) {
				if(tr.entity instanceof PlayerEntity && tr.listeners.contains(p.networkHandler)) {
					f.accept((ServerPlayerEntity) tr.entity);
				}
			}
		});
		netHandler.setSendChat(ServerHandler::sendMessage);
		netHandler.setExecutor(n -> ((IServerNetHandler)n).cpm$getServer());
		if(FabricLoader.getInstance().isModLoaded("pehkui")) {
			netHandler.setScaler(new PehkuiInterface());
		}
		netHandler.setGetNet(spe -> spe.networkHandler);
		netHandler.setGetPlayer(net -> net.player);
		netHandler.setGetPlayerId(ServerPlayerEntity::getId);
		netHandler.setGetOnlinePlayers(() -> MinecraftServerObject.getServer().getPlayerManager().getPlayerList());
		netHandler.setKickPlayer((p, m) -> p.networkHandler.disconnect(m.remap()));
		netHandler.setGetPlayerAnimGetters(p -> p.fallDistance, p -> p.getAbilities().flying);
	}

	private static void sendMessage(ServerPlayerEntity p, IText m) {
		Registry<MessageType> registry = p.world.getRegistryManager().get(Registry.MESSAGE_TYPE_KEY);
		int id = registry.getRawId(registry.get(MessageType.CHAT));
		p.networkHandler.sendPacket(new GameMessageS2CPacket(m.remap(), id));
	}

	public static void onPlayerJoin(ServerPlayerEntity spe) {
		netHandler.onJoin(spe);
	}

	public static void onTrackingStart(Entity target, ServerPlayerEntity spe) {
		ServerPlayNetworkHandler handler = spe.networkHandler;
		NetH h = (NetH) handler;
		if(h.cpm$hasMod()) {
			if(target instanceof PlayerEntity) {
				netHandler.sendPlayerData((ServerPlayerEntity) target, spe);
			}
		}
	}

	public static boolean hasMod(ServerPlayerEntity spe) {
		return ((NetH)spe.networkHandler).cpm$hasMod();
	}


	public static void sendToAllTrackingAndSelf(ServerPlayerEntity ent, Packet<?> pckt, Predicate<ServerPlayerEntity> test, GenericFutureListener<? extends Future<? super Void>> future) {
		EntityTracker tr = ((ServerWorld)ent.world).getChunkManager().threadedAnvilChunkStorage.entityTrackers.get(ent.getId());
		if(tr != null) {
			for (EntityTrackingListener p : tr.listeners) {
				if(test.test(p.getPlayer())) {
					p.getPlayer().networkHandler.sendPacket(pckt, future);
				}
			}
		}
		ent.networkHandler.sendPacket(pckt, future);
	}

	public static void jump(Object player) {
		if(player instanceof ServerPlayerEntity) {
			netHandler.onJump((ServerPlayerEntity) player);
		}
	}
}
