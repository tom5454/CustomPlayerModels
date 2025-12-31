package com.tom.cpm;

import java.util.function.Function;

import net.minecraft.client.gui.Screen;

import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.shared.gui.SettingsGui;

import io.github.prospector.modmenu.api.ModMenuApi;

public class ModMenu implements ModMenuApi {

	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return screen -> new GuiImpl(SettingsGui::new, screen);
	}
}
