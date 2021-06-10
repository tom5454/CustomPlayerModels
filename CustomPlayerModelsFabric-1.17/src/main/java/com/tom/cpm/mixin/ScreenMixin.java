package com.tom.cpm.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(Screen.class)
public class ScreenMixin {

	@Shadow
	protected List<Selectable> selectables;

	@Shadow
	protected List<Element> children;

	@Inject(at = @At("RETURN"), method = "init(Lnet/minecraft/client/MinecraftClient;II)V")
	public void onInit(MinecraftClient client, int width, int height, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.initGui((Screen)(Object)this, children, selectables);
	}
}
