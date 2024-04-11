package com.tom.cpm;

import java.util.function.Function;

import net.minecraft.client.gui.GuiScreen;

import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.shared.gui.SettingsGui;

import io.github.prospector.modmenu.api.ModMenuApi;

public class ModMenu implements ModMenuApi {

	@Override
	public Function<GuiScreen, ? extends GuiScreen> getConfigScreenFactory() {
		return screen -> new GuiImpl(SettingsGui::new, screen);
	}

	@Override
	public String getModId() {
		return "cpm";
	}
}
