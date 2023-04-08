package com.tom.cpm.client;

import java.lang.reflect.Method;
import java.util.function.Function;

import net.minecraft.client.gui.screen.Screen;

import net.minecraftforge.client.gui.ForgeIngameGui;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;

public class GuiImpl extends GuiBase {

	public GuiImpl(Function<IGui, Frame> creator, Screen parent) {
		super(creator, parent);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if(minecraft.player != null && gui.enableChat()) {
			matrixStack.pushPose();
			matrixStack.translate(0, 0, 800);
			try {
				Method m = ForgeIngameGui.class.getDeclaredMethod("renderChat", int.class, int.class, MatrixStack.class);
				m.setAccessible(true);
				m.invoke(minecraft.gui, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), matrixStack);
			} catch (Throwable e) {
			}
			matrixStack.popPose();
		}
	}
}
