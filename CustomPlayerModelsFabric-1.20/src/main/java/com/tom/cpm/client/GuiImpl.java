package com.tom.cpm.client;

import java.util.function.Function;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;

public class GuiImpl extends GuiBase {

	public GuiImpl(Function<IGui, Frame> creator, Screen parent) {
		super(creator, parent);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		if(minecraft.player != null && gui.enableChat()) {
			graphics.pose().pushPose();
			graphics.pose().translate(0, 0, 100);
			minecraft.gui.getChat().render(graphics, minecraft.gui.getGuiTicks(), mouseX, mouseY);
			graphics.pose().popPose();
		}
	}
}
