package com.tom.cpm.common;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.ServerLifecycleHooks;

import com.mojang.brigadier.CommandDispatcher;

import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

public class ServerHandler extends ServerHandlerBase {
	public static NetHandler<ResourceLocation, ServerPlayer, ServerGamePacketListenerImpl> netHandler;

	static {
		netHandler = init();
		netHandler.setGetOnlinePlayers(() -> ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers());
		netHandler.setExecutor(ServerLifecycleHooks::getCurrentServer);
		if(ModList.get().isLoaded("pehkui")) {
			//netHandler.setScaler(new PehkuiInterface());
		}
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerLoggedInEvent evt) {
		netHandler.onJoin((ServerPlayer) evt.getEntity());
	}

	@SubscribeEvent
	public void onTrackingStart(PlayerEvent.StartTracking evt) {
		ServerGamePacketListenerImpl handler = ((ServerPlayer)evt.getEntity()).connection;
		NetH h = (NetH) handler;
		if(h.cpm$hasMod()) {
			if(evt.getTarget() instanceof Player) {
				netHandler.sendPlayerData((ServerPlayer) evt.getTarget(), (ServerPlayer) evt.getEntity());
			}
		}
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent evt) {
		CommandDispatcher<CommandSourceStack> d = evt.getDispatcher();
		new Command(d, false);
	}

	@SubscribeEvent
	public void onRespawn(PlayerRespawnEvent evt) {
		if(!evt.isEndConquered()) {
			netHandler.onRespawn((ServerPlayer) evt.getEntity());
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
		if(evt.getEntity() instanceof ServerPlayer) {
			netHandler.onJump((ServerPlayer) evt.getEntity());
		}
	}
}
