package com.tom.cpm.common;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Packet250CustomPayload;

import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.retro.NetHandlerExt;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import forge.MinecraftForge;
import forge.adaptors.EntityLivingHandlerAdaptor;

public class ServerHandler {
	public static NetHandlerExt<String, EntityPlayer, ServerNetworkImpl> netHandler;

	static {
		netHandler = new NetHandlerExt<>((a, b) -> a + "|" + b);
		netHandler.setGetPlayerUUID(p -> UUID.nameUUIDFromBytes(("OfflinePlayer:" + p.username).getBytes(StandardCharsets.UTF_8)));
		netHandler.setSendPacketServer(a -> a, ServerNetworkImpl::cpm$sendPacket, ent -> CustomPlayerModels.proxy.getTrackingPlayers(ent), e -> e);
		netHandler.setFindTracking((p, f) -> CustomPlayerModels.proxy.getTracking(p, f));
		netHandler.setSendChat((p, m) -> CustomPlayerModels.proxy.getServer(p).cpm$sendChat(m.<String>remap()));
		netHandler.setExecutor(() -> Runnable::run);
		netHandler.setGetNet(CustomPlayerModels.proxy::getServer);
		netHandler.setGetPlayer(ServerNetworkImpl::cpm$getPlayer);
		netHandler.setGetPlayerId(e -> e.entityId);
		netHandler.setGetOnlinePlayers(CustomPlayerModels.proxy::getPlayersOnline);
		netHandler.setKickPlayer((p, m) -> CustomPlayerModels.proxy.getServer(p).cpm$kickPlayer(m.remap()));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());

		FMLCommonHandler.instance().registerTickHandler(new ITickHandler() {

			@Override
			public EnumSet<TickType> ticks() {
				return EnumSet.of(TickType.GAME);
			}

			@Override
			public void tickStart(EnumSet<TickType> var1, Object... var2) {
			}

			@Override
			public void tickEnd(EnumSet<TickType> var1, Object... var2) {
				if (var1.contains(TickType.GAME))
					netHandler.tick();
			}

			@Override
			public String getLabel() {
				return "CPM";
			}
		});

		MinecraftForge.registerEntityLivingHandler(new EntityLivingHandlerAdaptor() {

			@Override
			public void onEntityLivingJump(EntityLiving var1) {
				if (var1 instanceof EntityPlayer) {
					netHandler.onJump((EntityPlayer) var1);
				}
			}
		});
	}

	public static Packet250CustomPayload packet(String id, byte[] data) {
		Packet250CustomPayload p = new Packet250CustomPayload();
		p.channel = id;
		p.data = data;
		return p;
	}

	public static void init() {}
}
