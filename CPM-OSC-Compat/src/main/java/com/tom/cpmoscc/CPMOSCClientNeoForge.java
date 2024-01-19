package com.tom.cpmoscc;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent.ClientTickEvent;
import net.neoforged.neoforge.event.TickEvent.Phase;

public class CPMOSCClientNeoForge {
	public static final CPMOSCClientNeoForge INSTANCE = new CPMOSCClientNeoForge();

	public void init() {
		NeoForge.EVENT_BUS.register(this);
		CPMOSC.LOGGER.info("CPM OSC initialized");
	}

	@SubscribeEvent
	public void clientTick(ClientTickEvent evt) {
		if (evt.phase == Phase.START || Minecraft.getInstance().isPaused())
			return;

		CPMOSC.tick(Minecraft.getInstance().player);
	}
}
