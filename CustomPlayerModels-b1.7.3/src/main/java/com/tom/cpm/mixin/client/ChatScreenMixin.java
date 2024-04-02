package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {

	@Override
	public boolean shouldPause() {
		return false;
	}
}
