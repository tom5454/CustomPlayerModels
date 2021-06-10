package com.tom.cpm.common;

import java.io.IOException;
import java.util.Set;
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

import com.tom.cpm.PlayerDataExt;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import io.netty.buffer.Unpooled;

public class ServerHandler {
	public static NetHandler<ResourceLocation, NBTTagCompound, EntityPlayerMP, PacketBuffer, NetHandlerPlayServer> netHandler;

	static {
		netHandler = new NetHandler<ResourceLocation, NBTTagCompound, EntityPlayerMP, PacketBuffer, NetHandlerPlayServer>(ResourceLocation::new) {
			@Override
			protected PlayerData newData() {
				return new PlayerDataExt();
			}

			@Override
			public void receiveServer(ResourceLocation key, PacketBuffer data, ServerNetH net) {
				EntityPlayerMP pl = getPlayer.apply((NetHandlerPlayServer) net);
				if(key.equals(setLayer)) {
					PlayerDataExt.setSkinLayer(pl, data.readByte());
					sendToAllTracking.accept(pl, setLayer, writeLayerData(pl));
				} else
					super.receiveServer(key, data, net);
			}
		};
		netHandler.setNewNbt(NBTTagCompound::new);
		netHandler.setNewPacketBuffer(() -> new PacketBuffer(Unpooled.buffer()));
		netHandler.setGetPlayerUUID(EntityPlayerMP::getUniqueID);
		netHandler.setWriteCompound((t, u) -> {
			try {
				t.writeNBTTagCompoundToBuffer(u);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, t -> {
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
			for(EntityTrackerEntry tr : (Set<EntityTrackerEntry>) ((WorldServer)p.worldObj).getEntityTracker().trackedEntities) {
				if(tr.myEntity instanceof EntityPlayer && tr.trackingPlayers.contains(p)) {
					f.accept((EntityPlayerMP) tr.myEntity);
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.playerNetServerHandler.sendPacket(new S02PacketChat(new ChatComponentTranslation(m))));
		netHandler.setExecutor(() -> Runnable::run);
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

	private static PacketBuffer writeLayerData(EntityPlayerMP playerEntity) {
		PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
		pb.writeVarIntToBuffer(playerEntity.getEntityId());
		pb.writeByte(PlayerDataExt.getSkinLayer(playerEntity));
		return pb;
	}

	public static boolean hasMod(EntityPlayerMP spe) {
		return ((ServerNetH)spe.playerNetServerHandler).cpm$hasMod();
	}

	public static void sendToAllTrackingAndSelf(EntityPlayerMP ent, Packet pckt, Predicate<EntityPlayerMP> test) {
		for (EntityPlayer pl : ((WorldServer)ent.worldObj).getEntityTracker().getTrackingPlayers(ent)) {
			EntityPlayerMP p = (EntityPlayerMP) pl;
			if(test.test(p)) {
				p.playerNetServerHandler.sendPacket(pckt);
			}
		}
		ent.playerNetServerHandler.sendPacket(pckt);
	}
}
