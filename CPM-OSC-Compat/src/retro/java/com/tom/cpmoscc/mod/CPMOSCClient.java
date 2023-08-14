package com.tom.cpmoscc.mod;

import net.minecraft.client.Minecraft;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import com.tom.cpmoscc.CPMOSC;

public class CPMOSCClient {
	public static final CPMOSCClient INSTANCE = new CPMOSCClient();

	public void init() {
		MinecraftForge.EVENT_BUS.register(this);
		CPMOSC.LOGGER.info("CPM OSC initialized");
	}

	@SubscribeEvent
	public void clientTick(ClientTickEvent evt) {
		if (evt.phase == Phase.START || Minecraft.getMinecraft().isGamePaused())
			return;

		CPMOSC.tick(Minecraft.getMinecraft().player);
	}
}
