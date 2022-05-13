package com.tom.cpm;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.fml.client.IModGuiFactory;

import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.shared.gui.SettingsGui;

public class Config implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance) {
	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new GuiImpl(SettingsGui::new, parentScreen);
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

}
