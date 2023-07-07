package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.shared.editor.gui.EditorGui;

@Mixin(Minecraft.class)
public abstract class MinecraftMixinFabric {

	@Shadow private boolean pause;
	@Shadow private float pausePartialTick;
	@Shadow private Timer timer;
	@Shadow public abstract void setScreen(Screen screen);

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V"), method = "runTick(Z)V")
	public void onRenderTick(boolean v, CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().getAnimationEngine().update(this.pause ? this.pausePartialTick : this.timer.partialTick);
	}

	@Inject(at = @At("HEAD"), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", cancellable = true)
	public void onSetScreen(Screen screen, CallbackInfo cbi) {
		if(screen instanceof TitleScreen && EditorGui.doOpenEditor()) {
			cbi.cancel();
			setScreen(new GuiImpl(EditorGui::new, screen));
		}
		if(screen instanceof GuiImpl i)i.onOpened();
	}

	@Inject(at = @At("HEAD"), method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V")
	public void onDisconnect(Screen screen, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.onLogout();
	}
}
