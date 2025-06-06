package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.shared.editor.gui.EditorGui;

@Mixin(Minecraft.class)
public abstract class MinecraftMixinFabric {

	@Shadow private DeltaTracker.Timer deltaTracker;
	@Shadow public abstract void setScreen(Screen screen);

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V"), method = "runTick(Z)V")
	public void onRenderTick(boolean v, CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().getAnimationEngine().update(deltaTracker.getGameTimeDeltaPartialTick(true));
	}

	@Inject(at = @At("HEAD"), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", cancellable = true)
	public void onSetScreen(Screen screen, CallbackInfo cbi) {
		if(screen instanceof TitleScreen && EditorGui.doOpenEditor()) {
			cbi.cancel();
			setScreen(new GuiImpl(EditorGui::new, screen));
		}
	}

	@Inject(at = @At("HEAD"), method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V")
	public void onDisconnect(Screen screen, boolean b, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.onLogout();
	}
}
