package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.core.Timer;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.World;
import net.minecraft.core.world.save.LevelStorage;

import com.tom.cpm.MinecraftServerObject;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.EmulNetwork;
import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.retro.MCExecutor;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.MinecraftServerAccess;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.editor.gui.EditorGui;

@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin {

	@Shadow private Timer timer;
	@Shadow public GuiScreen currentScreen;
	@Shadow public EntityPlayerSP thePlayer;
	@Shadow public abstract void displayGuiScreen(GuiScreen screen);

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/util/phys/AABB;initializePool()V"), method = "run()V")
	public void onRenderTick(CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().getAnimationEngine().update(this.timer.partialTicks);
	}

	@Inject(at = @At("HEAD"), method = "displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V", cancellable = true)
	public void onOpenScreen(GuiScreen screen, CallbackInfo cbi) {
		if(screen == null && this.currentScreen instanceof GuiImpl.Overlay) {
			cbi.cancel();
			displayGuiScreen(((GuiImpl.Overlay)this.currentScreen).getGui());
		}
		if(screen instanceof GuiMainMenu && EditorGui.doOpenEditor()) {
			cbi.cancel();
			displayGuiScreen(new GuiImpl(EditorGui::new, screen));
		}
		if(screen instanceof GuiImpl)((GuiImpl)screen).onOpened();
	}

	@Inject(at = @At(value = "NEW", target = "net/minecraft/core/world/World", shift = Shift.AFTER), method = "startWorld(Ljava/lang/String;Ljava/lang/String;J)V", locals = LocalCapture.CAPTURE_FAILHARD)
	public void onStartWorld(String string, String string2, long l, CallbackInfo cbi, int i, I18n i18n, LevelStorage sh) {
		EmulNetwork.reset();
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(sh));
	}

	@Inject(at = @At("HEAD"), method = "changeWorld(Lnet/minecraft/core/world/World;Ljava/lang/String;Lnet/minecraft/core/entity/player/EntityPlayer;)V")
	public void onSetWorld(World world, String string, EntityPlayer arg2, CallbackInfo cbi) {
		if (world == null) {
			CustomPlayerModelsClient.INSTANCE.onLogout();
			if (MinecraftServerAccess.get() != null)
				ModConfig.getWorldConfig().save();
			MinecraftObjectHolder.setServerObject(null);
		}
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;thePlayer:Lnet/minecraft/client/entity/player/EntityPlayerSP;", shift = Shift.AFTER), method = "changeWorld(Lnet/minecraft/core/world/World;Ljava/lang/String;Lnet/minecraft/core/entity/player/EntityPlayer;)V")
	public void onSetPlayer(World world, String string, EntityPlayer arg2, CallbackInfo cbi) {
		if (thePlayer != null)
			ServerHandler.netHandler.onJoin(thePlayer);
	}

	@Inject(at = @At("HEAD"), method = "runTick()V")
	public void onTick(CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.clientTickStart();
	}

	@Inject(at = @At("RETURN"), method = "runTick()V")
	public void onTickEnd(CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.clientTickEnd();
		MCExecutor.executeAll();
	}

	@Inject(at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lorg/lwjgl/opengl/GL11;glViewport(IIII)V", remap = false), method = "startGame()V")
	public void onStartGame(CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.postInit();
	}
}
