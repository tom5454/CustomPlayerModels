package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenMainMenu;
import net.minecraft.client.world.WorldClient;
import net.minecraft.core.Timer;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.save.LevelStorage;
import net.minecraft.core.world.type.WorldTypeGroups;

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
	@Shadow public Screen currentScreen;
	@Shadow public PlayerLocal thePlayer;
	@Shadow public abstract void displayScreen(Screen screen);
	@Shadow public abstract boolean isMultiplayerWorld();

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/util/phys/AABB;initializePool()V"), method = "run()V")
	public void onRenderTick(CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().getAnimationEngine().update(this.timer.partialTicks);
	}

	@Inject(at = @At("HEAD"), method = "displayScreen(Lnet/minecraft/client/gui/Screen;)V", cancellable = true)
	public void onOpenScreen(Screen screen, CallbackInfo cbi) {
		if(screen == null && this.currentScreen instanceof GuiImpl.Overlay) {
			cbi.cancel();
			displayScreen(((GuiImpl.Overlay)this.currentScreen).getGui());
		}
		if(screen instanceof ScreenMainMenu && EditorGui.doOpenEditor()) {
			cbi.cancel();
			displayScreen(new GuiImpl(EditorGui::new, screen));
		}
		if(screen instanceof GuiImpl)((GuiImpl)screen).onOpened();
	}

	@Inject(at = @At(value = "NEW", target = "net/minecraft/client/world/WorldClient"), method = "startWorld(Ljava/lang/String;Ljava/lang/String;JLnet/minecraft/core/world/type/WorldTypeGroups$Group;)V", locals = LocalCapture.CAPTURE_FAILHARD)
	public void onStartWorld(String string, String string2, long l, final WorldTypeGroups.Group worldTypeGroup, CallbackInfo cbi, int worldSaveVersion, I18n i18n, LevelStorage sh) {
		EmulNetwork.reset();
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(sh));
	}

	@Inject(at = @At("HEAD"), method = "changeWorld(Lnet/minecraft/client/world/WorldClient;Ljava/lang/String;Lnet/minecraft/core/entity/player/Player;)V")
	public void onSetWorld(WorldClient world, String string, Player arg2, CallbackInfo cbi) {
		if (world == null) {
			CustomPlayerModelsClient.INSTANCE.onLogout();
			if (MinecraftServerAccess.get() != null)
				ModConfig.getWorldConfig().save();
			MinecraftObjectHolder.setServerObject(null);
		}
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;thePlayer:Lnet/minecraft/client/entity/player/PlayerLocal;", shift = Shift.AFTER), method = "changeWorld(Lnet/minecraft/client/world/WorldClient;Ljava/lang/String;Lnet/minecraft/core/entity/player/Player;)V")
	public void onSetPlayer(WorldClient world, String string, Player arg2, CallbackInfo cbi) {
		if (thePlayer != null && !isMultiplayerWorld())
			ServerHandler.netHandler.onJoin(thePlayer);
	}

	@Inject(at = @At("HEAD"), method = "runTick()V")
	public void onTick(CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.clientTickStart();
		CustomPlayerModelsClient.mc.getPlayerRenderManager().getAnimationEngine().update(this.timer.partialTicks);
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
