package com.tom.cpm.common;

import java.io.IOException;
import java.util.function.Predicate;

import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;

public class ServerHandler {
	public static NetHandler<ResourceLocation, NBTTagCompound, EntityPlayerMP, PacketBuffer, NetHandlerPlayServer> netHandler;

	static {
		netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setNewNbt(NBTTagCompound::new);
		netHandler.setNewPacketBuffer(() -> new PacketBuffer(Unpooled.buffer()));
		netHandler.setIsDedicatedServer(p -> p.mcServer.isDedicatedServer());
		netHandler.setGetPlayerUUID(EntityPlayerMP::getUniqueID);
		netHandler.setWriteCompound(PacketBuffer::writeNBTTagCompoundToBuffer, t -> {
			try {
				return t.readNBTTagCompoundFromBuffer();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		netHandler.setSendPacket((c, rl, pb) -> c.sendPacket(new S3FPacketCustomPayload(rl.toString(), pb)), (spe, rl, pb) -> sendToAllTrackingAndSelf(spe, new S3FPacketCustomPayload(rl.toString(), pb), ServerHandler::hasMod));
		netHandler.setWritePlayerId((pb, pl) -> pb.writeVarIntToBuffer(pl.getEntityId()));
		netHandler.setNBTSetters(NBTTagCompound::setBoolean, NBTTagCompound::setByteArray, NBTTagCompound::setFloat);
		netHandler.setNBTGetters(NBTTagCompound::getBoolean, NBTTagCompound::getByteArray, NBTTagCompound::getFloat);
		netHandler.setContains(NBTTagCompound::hasKey);
		netHandler.setFindTracking((p, f) -> {
			for(EntityTrackerEntry tr : ((WorldServer)p.worldObj).getEntityTracker().trackedEntities) {
				if(tr.trackedEntity instanceof EntityPlayer && tr.trackingPlayers.contains(p)) {
					f.accept((EntityPlayerMP) tr.trackedEntity);
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.playerNetServerHandler.sendPacket(new S02PacketChat(new ChatComponentTranslation(m))));
		netHandler.setExecutor(() -> FMLCommonHandler.instance().getMinecraftServerInstance()::addScheduledTask);
		netHandler.setGetNet(spe -> spe.playerNetServerHandler);
		netHandler.setGetPlayer(net -> net.playerEntity);
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		netHandler.onJoin((EntityPlayerMP) evt.player);
	}

	@SubscribeEvent
	public void onTrackingStart(PlayerEvent.StartTracking evt) {
		NetHandlerPlayServer handler = ((EntityPlayerMP)evt.entityPlayer).playerNetServerHandler;
		if(((ServerNetH)handler).cpm$hasMod()) {
			if(evt.target instanceof EntityPlayer) {
				netHandler.sendPlayerData((EntityPlayerMP) evt.target, (EntityPlayerMP) evt.entityPlayer);
			}
		}
	}

	public static boolean hasMod(EntityPlayerMP spe) {
		return ((ServerNetH)spe.playerNetServerHandler).cpm$hasMod();
	}

	public static void sendToAllTrackingAndSelf(EntityPlayerMP ent, Packet<?> pckt, Predicate<EntityPlayerMP> test) {
		for (EntityPlayer pl : ((WorldServer)ent.worldObj).getEntityTracker().getTrackingPlayers(ent)) {
			EntityPlayerMP p = (EntityPlayerMP) pl;
			if(test.test(p)) {
				p.playerNetServerHandler.sendPacket(pckt);
			}
		}
		ent.playerNetServerHandler.sendPacket(pckt);
	}
}
