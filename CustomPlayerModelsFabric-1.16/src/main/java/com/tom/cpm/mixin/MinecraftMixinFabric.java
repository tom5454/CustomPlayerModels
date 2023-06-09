package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Timer;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.shared.editor.gui.EditorGui;

@Mixin(Minecraft.class)
public abstract class MinecraftMixinFabric {

	@Shadow private boolean pause;
	@Shadow private float pausePartialTick;
	@Shadow private Timer timer;
	@Shadow public Screen screen;
	@Shadow public abstract void setScreen(Screen screen);

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/IProfiler;popPush(Ljava/lang/String;)V"), method = "runTick(Z)V")
	public void onRenderTick(boolean v, CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().getAnimationEngine().update(this.pause ? this.pausePartialTick : this.timer.partialTick);
	}

	@Inject(at = @At("HEAD"), method = "setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", cancellable = true)
	public void onOpenScreen(Screen screen, CallbackInfo cbi) {
		if(screen == null && this.screen instanceof GuiImpl.Overlay) {
			cbi.cancel();
			setScreen(((GuiImpl.Overlay)this.screen).getGui());
		}
		if(screen instanceof MainMenuScreen && EditorGui.doOpenEditor()) {
			cbi.cancel();
			setScreen(new GuiImpl(EditorGui::new, screen));
		}
		if(screen instanceof GuiImpl)((GuiImpl)screen).onOpened();
	}

	@Inject(at = @At("HEAD"), method = "clearLevel(Lnet/minecraft/client/gui/screen/Screen;)V")
	public void onDisconnect(Screen screen, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.onLogout();
	}
}
