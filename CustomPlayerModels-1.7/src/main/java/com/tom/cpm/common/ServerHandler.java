package com.tom.cpm.common;

import java.util.Set;

import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import com.tom.cpm.shared.network.NetH.ServerNetH;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class ServerHandler {
	public static NetHandlerExt<EntityPlayerMP, NetHandlerPlayServer> netHandler;

	static {
		netHandler = new NetHandlerExt<>();
		netHandler.setGetPlayerUUID(EntityPlayerMP::getUniqueID);
		netHandler.setSendPacketServer(a -> a, (c, rl, pb) -> c.sendPacket(new S3FPacketCustomPayload(rl.toString(), pb)), ent -> ((WorldServer)ent.worldObj).getEntityTracker().getTrackingPlayers(ent), e -> (EntityPlayerMP) e);
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
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
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
}
