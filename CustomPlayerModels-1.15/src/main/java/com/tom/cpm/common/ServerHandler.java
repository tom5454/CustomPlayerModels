package com.tom.cpm.common;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import com.mojang.brigadier.CommandDispatcher;

import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

public class ServerHandler extends ServerHandlerBase {
	public static NetHandler<ResourceLocation, ServerPlayerEntity, ServerPlayNetHandler> netHandler;

	static {
		netHandler = init();
		netHandler.setExecutor(ServerLifecycleHooks::getCurrentServer);
		netHandler.setGetOnlinePlayers(() -> ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers());
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
				netHandler.sendPlayerData((ServerPlayerEntity) evt.getTarget(), (ServerPlayerEntity) evt.getPlayer());
			}
		}
	}

	public static void registerCommands(CommandDispatcher<CommandSource> d) {
		new Command(d, false);
	}

	@SubscribeEvent
	public void onRespawn(PlayerRespawnEvent evt) {
		if(!evt.isEndConquered()) {
			netHandler.onRespawn((ServerPlayerEntity) evt.getPlayer());
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
		if(evt.getEntityLiving() instanceof ServerPlayerEntity) {
			netHandler.onJump((ServerPlayerEntity) evt.getEntityLiving());
		}
	}
}
