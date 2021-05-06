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
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentTranslation;
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
		netHandler.setIsDedicatedServer(p -> p.getServer().isDedicatedServer());
		netHandler.setGetPlayerUUID(EntityPlayerMP::getUniqueID);
		netHandler.setWriteCompound(PacketBuffer::writeCompoundTag, t -> {
			try {
				return t.readCompoundTag();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		netHandler.setSendPacket((c, rl, pb) -> c.sendPacket(new SPacketCustomPayload(rl.toString(), pb)), (spe, rl, pb) -> sendToAllTrackingAndSelf(spe, new SPacketCustomPayload(rl.toString(), pb), ServerHandler::hasMod));
		netHandler.setWritePlayerId((pb, pl) -> pb.writeVarInt(pl.getEntityId()));
		netHandler.setNBTSetters(NBTTagCompound::setBoolean, NBTTagCompound::setByteArray, NBTTagCompound::setFloat);
		netHandler.setNBTGetters(NBTTagCompound::getBoolean, NBTTagCompound::getByteArray, NBTTagCompound::getFloat);
		netHandler.setContains(NBTTagCompound::hasKey);
		netHandler.setFindTracking((p, f) -> {
			for(EntityTrackerEntry tr : ((WorldServer)p.world).getEntityTracker().entries) {
				if(tr.getTrackedEntity() instanceof EntityPlayer && tr.trackingPlayers.contains(p)) {
					f.accept((EntityPlayerMP) tr.getTrackedEntity());
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.connection.sendPacket(new SPacketChat(new TextComponentTranslation(m), ChatType.CHAT)));
		netHandler.setExecutor(() -> FMLCommonHandler.instance().getMinecraftServerInstance()::addScheduledTask);
		netHandler.setGetNet(spe -> spe.connection);
		netHandler.setGetPlayer(net -> net.player);
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		netHandler.onJoin((EntityPlayerMP) evt.player);
	}

	@SubscribeEvent
	public void onTrackingStart(PlayerEvent.StartTracking evt) {
		NetHandlerPlayServer handler = ((EntityPlayerMP)evt.getEntityPlayer()).connection;
		if(((ServerNetH)handler).cpm$hasMod()) {
			if(evt.getTarget() instanceof EntityPlayer) {
				netHandler.sendPlayerData((EntityPlayerMP) evt.getTarget(), (EntityPlayerMP) evt.getEntityPlayer());
			}
		}
	}

	public static boolean hasMod(EntityPlayerMP spe) {
		return ((ServerNetH)spe.connection).cpm$hasMod();
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
