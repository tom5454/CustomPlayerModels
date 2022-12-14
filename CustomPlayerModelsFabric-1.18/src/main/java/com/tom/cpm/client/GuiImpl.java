package com.tom.cpm.client;

import java.util.function.Function;

import net.minecraft.client.gui.screens.Screen;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;

public class GuiImpl extends GuiBase {

	public GuiImpl(Function<IGui, Frame> creator, Screen parent) {
		super(creator, parent);
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if(minecraft.player != null && gui.enableChat()) {
			matrixStack.pushPose();
			matrixStack.translate(0.0D, minecraft.getWindow().getGuiScaledHeight() - 48, 800);
			minecraft.gui.getChat().render(matrixStack, minecraft.gui.getGuiTicks());
			matrixStack.popPose();
		}
	}
}
