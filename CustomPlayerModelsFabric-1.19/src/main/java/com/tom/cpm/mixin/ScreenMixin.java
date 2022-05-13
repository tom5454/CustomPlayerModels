package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.CustomPlayerModelsClient.IScreen;

@Mixin(Screen.class)
public abstract class ScreenMixin implements IScreen {

	protected @Shadow abstract Element addDrawableChild(Element drawableElement);

	@Inject(at = @At("RETURN"), method = "init(Lnet/minecraft/client/MinecraftClient;II)V")
	public void onInit(MinecraftClient client, int width, int height, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.initGui((Screen)(Object)this);
	}

	@Override
	public void cpm$addDrawableChild(Element drawableElement) {
		addDrawableChild(drawableElement);
	}
}
