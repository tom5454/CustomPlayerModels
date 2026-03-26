package com.tom.cpm.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Mixin(Gui.class)
public interface GuiAccessor {

	@Invoker
	void callExtractChat(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
}
