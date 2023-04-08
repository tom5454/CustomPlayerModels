package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.IScreen;

@Mixin(Screen.class)
public abstract class ScreenMixinFabric implements IScreen {

	protected @Shadow abstract Widget addButton(Widget button);

	@Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("TAIL"))
	private void afterInitScreen(Minecraft client, int width, int height, CallbackInfo ci) {
		CustomPlayerModelsClient.guiInitPost((Screen) (Object) this);
	}

	@Override
	public void cpm$addWidget(Widget w) {
		addButton(w);
	}
}
