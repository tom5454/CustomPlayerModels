package com.tom.cpm.server;

import net.fabricmc.api.DedicatedServerModInitializer;

import com.tom.cpm.CustomPlayerModels;

public class CustomPlayerModelsServer implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {
		CustomPlayerModels.proxy = new ServerSidedHandler();
	}

}
