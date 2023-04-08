package com.tom.cpm.common;

import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;

public class ServerHandler {

	public static NetHandler<ResourceLocation, EntityPlayerMP, NetHandlerPlayServer> netHandler;

	static {
		netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setGetPlayerUUID(EntityPlayerMP::getUniqueID);
		netHandler.setSendPacketServer(d -> new PacketBuffer(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.sendPacket(new SPacketCustomPayload(rl.toString(), pb)), ent -> ((WorldServer)ent.worldObj).getEntityTracker().getTrackingPlayers(ent), e -> (EntityPlayerMP) e);
		netHandler.setFindTracking((p, f) -> {
			for(EntityTrackerEntry tr : ((WorldServer)p.worldObj).getEntityTracker().trackedEntities) {
				if(tr.getTrackedEntity() instanceof EntityPlayer && tr.trackingPlayers.contains(p)) {
					f.accept((EntityPlayerMP) tr.getTrackedEntity());
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.connection.sendPacket(new SPacketChat(m.remap())));
		netHandler.setExecutor(() -> FMLCommonHandler.instance().getMinecraftServerInstance()::addScheduledTask);
		netHandler.setGetNet(spe -> spe.connection);
		netHandler.setGetPlayer(net -> net.playerEntity);
		netHandler.setGetPlayerId(EntityPlayerMP::getEntityId);
		netHandler.setGetOnlinePlayers(() -> FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerList());
		netHandler.setKickPlayer((p, m) -> p.connection.kickPlayerFromServer(m.toString()));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
		if(Loader.isModLoaded("chiseled_me")) {
			netHandler.setScaler(new ChiseledMeScaler());
		}
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

	@SubscribeEvent
	public void onTick(ServerTickEvent evt) {
		if(evt.phase == Phase.END) {
			netHandler.tick();
		}
	}

	@SubscribeEvent
	public void onJump(LivingJumpEvent evt) {
		if(evt.getEntityLiving() instanceof EntityPlayerMP) {
			netHandler.onJump((EntityPlayerMP) evt.getEntityLiving());
		}
	}
}
