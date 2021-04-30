package com.tom.cpm.common;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ChunkManager.EntityTracker;
import net.minecraft.world.server.ServerWorld;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import com.mojang.brigadier.CommandDispatcher;

import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;

public class ServerHandler {
	public static NetHandler<ResourceLocation, CompoundNBT, ServerPlayerEntity, PacketBuffer, ServerPlayNetHandler> netHandler;

	static {
		netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setNewNbt(CompoundNBT::new);
		netHandler.setNewPacketBuffer(() -> new PacketBuffer(Unpooled.buffer()));
		netHandler.setIsDedicatedServer(p -> p.getServer().isDedicatedServer());
		netHandler.setGetPlayerUUID(ServerPlayerEntity::getUniqueID);
		netHandler.setWriteCompound(PacketBuffer::writeCompoundTag, PacketBuffer::readCompoundTag);
		netHandler.setSendPacket((c, rl, pb) -> c.sendPacket(new SCustomPayloadPlayPacket(rl, pb)), (spe, rl, pb) -> NetworkHandler.sendToAllTrackingAndSelf(spe, new SCustomPayloadPlayPacket(rl, pb), ServerHandler::hasMod, null));
		netHandler.setWritePlayerId((pb, pl) -> pb.writeVarInt(pl.getEntityId()));
		netHandler.setNBTSetters(CompoundNBT::putBoolean, CompoundNBT::putByteArray, CompoundNBT::putFloat);
		netHandler.setNBTGetters(CompoundNBT::getBoolean, CompoundNBT::getByteArray, CompoundNBT::getFloat);
		netHandler.setContains(CompoundNBT::contains);
		netHandler.setFindTracking((p, f) -> {
			for(EntityTracker tr : ((ServerWorld)p.world).getChunkProvider().chunkManager.entities.values()) {
				if(tr.entity instanceof PlayerEntity && tr.trackingPlayers.contains(p)) {
					f.accept((ServerPlayerEntity) tr.entity);
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.connection.sendPacket(new SChatPacket(new TranslationTextComponent(m), ChatType.CHAT)));
		netHandler.setExecutor(ServerLifecycleHooks::getCurrentServer);
		netHandler.setGetNet(spe -> spe.connection);
		netHandler.setGetPlayer(net -> net.player);
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		netHandler.onJoin((ServerPlayerEntity) evt.getPlayer());
	}

	@SubscribeEvent
	public void onTrackingStart(PlayerEvent.StartTracking evt) {
		ServerPlayNetHandler handler = ((ServerPlayerEntity)evt.getPlayer()).connection;
		NetH h = (NetH) handler;
		if(h.cpm$hasMod()) {
			if(evt.getTarget() instanceof PlayerEntity) {
				netHandler.sendPlayerData((ServerPlayerEntity) evt.getTarget(), (ServerPlayerEntity)evt.getPlayer());
			}
		}
	}

	public static void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
		CommandCPM.register(dispatcher);
	}

	public static boolean hasMod(ServerPlayerEntity spe) {
		return ((NetH)spe.connection).cpm$hasMod();
	}

}
