package com.tom.cpm.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;

@Mixin(Hud.class)
public interface HudAccessor {

	@Invoker
	void callExtractChat(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
}
