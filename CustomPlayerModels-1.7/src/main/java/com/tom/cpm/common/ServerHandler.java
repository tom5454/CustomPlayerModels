package com.tom.cpm.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import com.tom.cpm.PlayerDataExt;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class ServerHandler {
	public static NetHandler<ResourceLocation, EntityPlayerMP, NetHandlerPlayServer> netHandler;

	static {
		netHandler = new NetHandler<ResourceLocation, EntityPlayerMP, NetHandlerPlayServer>(ResourceLocation::new) {
			@Override
			protected PlayerData newData() {
				return new PlayerDataExt();
			}

			@Override
			public void receiveServer(ResourceLocation key, InputStream data, ServerNetH net) {
				EntityPlayerMP pl = getPlayer.apply((NetHandlerPlayServer) net);
				if(key.equals(setLayer)) {
					try {
						PlayerDataExt.setSkinLayer(pl, data.read());
						sendPacketToTracking(pl, setLayer, writeLayerData(pl));
					} catch (IOException e) {
					}
				} else
					super.receiveServer(key, data, net);
			}
		};
		netHandler.setGetPlayerUUID(EntityPlayerMP::getUniqueID);
		netHandler.setSendPacket((c, rl, pb) -> c.sendPacket(new S3FPacketCustomPayload(rl.toString(), pb)), (spe, rl, pb) -> sendToAllTrackingAndSelf(spe, new S3FPacketCustomPayload(rl.toString(), pb), ServerHandler::hasMod));
		netHandler.setFindTracking((p, f) -> {
			for(EntityTrackerEntry tr : (Set<EntityTrackerEntry>) ((WorldServer)p.worldObj).getEntityTracker().trackedEntities) {
				if(tr.myEntity instanceof EntityPlayer && tr.trackingPlayers.contains(p)) {
					f.accept((EntityPlayerMP) tr.myEntity);
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.playerNetServerHandler.sendPacket(new S02PacketChat(m.remap())));
		netHandler.setExecutor(() -> Runnable::run);
		netHandler.setGetNet(spe -> spe.playerNetServerHandler);
		netHandler.setGetPlayer(net -> net.playerEntity);
		netHandler.setGetPlayerId(EntityPlayerMP::getEntityId);
		netHandler.setGetOnlinePlayers(() -> MinecraftServer.getServer().getConfigurationManager().playerEntityList);
		netHandler.setKickPlayer((p, m) -> p.playerNetServerHandler.kickPlayerFromServer(m.toString()));
		netHandler.setGetPlayerAnimGetters(p -> p.fallDistance, p -> p.capabilities.isFlying);
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

	@SubscribeEvent
	public void onTick(ServerTickEvent evt) {
		if(evt.phase == Phase.END) {
			netHandler.tick();
		}
	}

	@SubscribeEvent
	public void onJump(LivingJumpEvent evt) {
		if(evt.entityLiving instanceof EntityPlayerMP) {
			netHandler.onJump((EntityPlayerMP) evt.entityLiving);
		}
	}

	private static byte[] writeLayerData(EntityPlayerMP playerEntity) {
		try {
			IOHelper h = new IOHelper();
			h.writeVarInt(playerEntity.getEntityId());
			h.writeByte(PlayerDataExt.getSkinLayer(playerEntity));
			return h.toBytes();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
