package com.tom.cpm;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.shared.gui.SettingsGui;

import cpw.mods.fml.client.IModGuiFactory;

public class Config implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance) {
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return ConfigGui.class;
	}

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}

	public static class ConfigGui extends GuiImpl {

		public ConfigGui(GuiScreen parent) {
			super(SettingsGui::new, parent);
		}

	}
}
