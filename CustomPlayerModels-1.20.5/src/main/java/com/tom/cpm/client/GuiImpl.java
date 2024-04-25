package com.tom.cpm.client;

import java.lang.reflect.Method;
import java.util.function.Function;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;

public class GuiImpl extends GuiBase {

	public GuiImpl(Function<IGui, Frame> creator, Screen parent) {
		super(creator, parent);
	}

	@Override
	public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if(minecraft.player != null && gui.enableChat()) {
			matrixStack.pose().pushPose();
			matrixStack.pose().translate(0, 0, 800);
			try {
				Method m = Gui.class.getDeclaredMethod("renderChat", GuiGraphics.class, float.class);
				m.setAccessible(true);
				m.invoke(minecraft.gui, matrixStack, partialTicks);
			} catch (Throwable e) {
			}
			matrixStack.pose().popPose();
		}
	}
}
