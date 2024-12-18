package com.tom.cpmoscc;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public class CPMOSCClientNeoForgeNew {
	public static final CPMOSCClientNeoForgeNew INSTANCE = new CPMOSCClientNeoForgeNew();

	public void init() {
		NeoForge.EVENT_BUS.register(this);
		CPMOSC.LOGGER.info("CPM OSC initialized");
	}

	@SubscribeEvent
	public void clientTick(ClientTickEvent.Post evt) {
		if (Minecraft.getInstance().isPaused())
			return;

		CPMOSC.tick(Minecraft.getInstance().player);
	}
}
