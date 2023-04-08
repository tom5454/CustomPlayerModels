package com.tom.cpm.client;

import java.util.function.Function;

import net.minecraft.client.gui.screen.Screen;

import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;

public class GuiImpl extends GuiBase {

	public GuiImpl(Function<IGui, Frame> creator, Screen parent) {
		super(creator, parent);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);
		if(minecraft.player != null && gui.enableChat()) {
			RenderSystem.pushMatrix();
			RenderSystem.translated(0.0D, minecraft.getWindow().getGuiScaledHeight() - 48, 800);
			minecraft.gui.getChat().render(minecraft.gui.getGuiTicks());
			RenderSystem.popMatrix();
		}
	}
}
