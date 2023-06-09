package com.tom.cpm.client;

import java.lang.reflect.Method;
import java.util.function.Function;

import net.minecraft.client.gui.screen.Screen;

import net.minecraftforge.client.gui.ForgeIngameGui;

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
			RenderSystem.translated(0.0D, 0.0D, 800);
			try {
				Method m = ForgeIngameGui.class.getDeclaredMethod("renderChat", int.class, int.class);
				m.setAccessible(true);
				m.invoke(minecraft.gui, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
			} catch (Throwable e) {
			}
			RenderSystem.popMatrix();
		}
	}
}
