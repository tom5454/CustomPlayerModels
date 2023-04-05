package com.tom.cpmoscc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class CPMOSCClientFabric implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_CLIENT_TICK.register(cl -> {
			if(!cl.isPaused())
				CPMOSC.tick(cl.player);
		});
		CPMOSC.LOGGER.info("CPM OSC initialized");
	}
}
