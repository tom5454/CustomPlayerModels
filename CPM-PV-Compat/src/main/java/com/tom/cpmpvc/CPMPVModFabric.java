package com.tom.cpmpvc;

import net.fabricmc.api.ClientModInitializer;

public class CPMPVModFabric implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		CPMAddon.init();
	}
}
