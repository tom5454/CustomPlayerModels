package com.tom.cpm.client;

import net.minecraft.client.option.GameSettings;

import turniplabs.halplibe.util.OptionsInitEntrypoint;

public class KeybindingsSetup implements OptionsInitEntrypoint {

	@Override
	public void initOptions(GameSettings settings) {
		KeyBindings.preInit();
	}
}
