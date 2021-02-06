package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderTickCounter;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.GuiImpl;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

	@Shadow private boolean paused;
	@Shadow private float pausedTickDelta;
	@Shadow private RenderTickCounter renderTickCounter;
	@Shadow public Screen currentScreen;
	@Shadow public abstract void openScreen(Screen screen);

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V"), method = "render(Z)V")
	public void onRenderTick(boolean v, CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().getAnimationEngine().update(this.paused ? this.pausedTickDelta : this.renderTickCounter.tickDelta);
	}

	@Inject(at = @At("HEAD"), method = "openScreen(Lnet/minecraft/client/gui/screen/Screen;)V", cancellable = true)
	public void onOpenScreen(Screen screen, CallbackInfo cbi) {
		if(screen == null && currentScreen instanceof GuiImpl.Overlay) {
			cbi.cancel();
			openScreen(((GuiImpl.Overlay)currentScreen).getGui());
		}
	}
}
