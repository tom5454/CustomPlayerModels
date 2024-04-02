package com.tom.cpm.common;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;

import com.tom.cpm.retro.NetHandlerExt;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ServerHandler {
	public static NetHandlerExt<String, EntityPlayerMP, NetServerHandler> netHandler;

	static {
		netHandler = new NetHandlerExt<>((a, b) -> a + "|" + b);
		netHandler.setGetPlayerUUID(EntityPlayerMP::getPersistentID);
		netHandler.setSendPacketServer(a -> a, (c, rl, pb) -> c.sendPacketToPlayer(new Packet250CustomPayload(rl.toString(), pb)), ent -> getTrackingPlayers(((WorldServer)ent.worldObj).getEntityTracker(), ent), e -> (EntityPlayerMP) e);
		netHandler.setFindTracking((p, f) -> {
			for(EntityTrackerEntry tr : (Set<EntityTrackerEntry>) ((WorldServer)p.worldObj).getEntityTracker().trackedEntities) {
				if(tr.myEntity instanceof EntityPlayer && tr.trackingPlayers.contains(p)) {
					f.accept((EntityPlayerMP) tr.myEntity);
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(m.<String>remap())));
		netHandler.setExecutor(() -> Runnable::run);
		netHandler.setGetNet(spe -> spe.playerNetServerHandler);
		netHandler.setGetPlayer(net -> net.playerEntity);
		netHandler.setGetPlayerId(e -> e.entityId);
		netHandler.setGetOnlinePlayers(() -> MinecraftServer.getServer().getConfigurationManager().playerEntityList);
		netHandler.setKickPlayer((p, m) -> p.playerNetServerHandler.kickPlayerFromServer(m.toString()));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());

		TickRegistry.registerTickHandler(new ITickHandler() {

			@Override
			public EnumSet<TickType> ticks() {
				return EnumSet.of(TickType.SERVER);
			}

			@Override
			public void tickStart(EnumSet<TickType> var1, Object... var2) {
			}

			@Override
			public void tickEnd(EnumSet<TickType> var1, Object... var2) {
				if (var1.contains(TickType.SERVER))
					netHandler.tick();
			}

			@Override
			public String getLabel() {
				return "CPM";
			}
		}, Side.SERVER);

		NetworkRegistry.instance().registerConnectionHandler(new IConnectionHandler() {

			@Override
			public void playerLoggedIn(Player var1, NetHandler var2, INetworkManager var3) {
				ServerHandler.netHandler.onJoin((EntityPlayerMP) var1);
			}

			@Override public String connectionReceived(NetLoginHandler var1, INetworkManager var2) { return null; }
			@Override public void connectionOpened(NetHandler var1, String var2, int var3, INetworkManager var4) {}
			@Override public void connectionOpened(NetHandler var1, MinecraftServer var2, INetworkManager var3) {}
			@Override public void connectionClosed(INetworkManager var1) {}
			@Override public void clientLoggedIn(NetHandler var1, INetworkManager var2, Packet1Login var3) {}
		});
	}

	@ForgeSubscribe
	public void onJump(LivingJumpEvent evt) {
		if(evt.entityLiving instanceof EntityPlayerMP) {
			netHandler.onJump((EntityPlayerMP) evt.entityLiving);
		}
	}

	@SuppressWarnings("unchecked")
	public static Set<EntityPlayer> getTrackingPlayers(EntityTracker et, Entity entity) {
		EntityTrackerEntry entry = (EntityTrackerEntry) et.trackedEntityIDs.lookup(entity.entityId);
		if (entry == null)
			return Collections.emptySet();
		else
			return entry.trackingPlayers;
	}
}
