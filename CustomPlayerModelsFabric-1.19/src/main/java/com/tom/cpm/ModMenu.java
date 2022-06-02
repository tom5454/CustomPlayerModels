package com.tom.cpm;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import com.tom.cpm.client.Gui;
import com.tom.cpm.shared.gui.SettingsGui;

public class ModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> new Gui(SettingsGui::new, screen);
	}
}
