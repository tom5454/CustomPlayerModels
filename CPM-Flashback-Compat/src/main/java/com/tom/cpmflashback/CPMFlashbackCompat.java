package com.tom.cpmflashback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;

public class CPMFlashbackCompat implements ClientModInitializer {
	public static final String MOD_ID = "cpm-flashback-compat";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("CPM Flashback loaded");
	}
}