package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.class_52;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Timer;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import com.tom.cpm.MinecraftServerObject;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.EmulNetwork;
import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.client.IGameOptions;
import com.tom.cpm.client.SinglePlayerCommands;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.retro.MCExecutor;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.MinecraftServerAccess;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.editor.gui.EditorGui;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Shadow private Timer timer;
	@Shadow public Screen currentScreen;
	@Shadow public ClientPlayerEntity player;
	@Shadow public InGameHud inGameHud;
	@Shadow public GameOptions options;
	@Shadow public abstract void setScreen(Screen screen);
	@Shadow public abstract boolean isWorldRemote();

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;resetCacheCount()V"), method = "run()V")
	public void onRenderTick(CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().getAnimationEngine().update(this.timer.field_2370);
	}

	@Inject(at = @At("HEAD"), method = "setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", cancellable = true)
	public void onOpenScreen(Screen screen, CallbackInfo cbi) {
		if(screen == null && this.currentScreen instanceof GuiImpl.Overlay) {
			cbi.cancel();
			setScreen(((GuiImpl.Overlay)this.currentScreen).getGui());
		}
		if(screen instanceof TitleScreen && EditorGui.doOpenEditor()) {
			cbi.cancel();
			setScreen(new GuiImpl(EditorGui::new, screen));
		}
		if(screen instanceof GuiImpl)((GuiImpl)screen).onOpened();
	}

	@Inject(at = @At(value = "NEW", target = "net/minecraft/world/World", shift = Shift.AFTER), method = "method_2120(Ljava/lang/String;Ljava/lang/String;J)V", locals = LocalCapture.CAPTURE_FAILHARD)
	public void onStartWorld(String string, String string2, long l, CallbackInfo cbi, class_52 sh) {
		EmulNetwork.reset();
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(sh));
	}

	@Inject(at = @At("HEAD"), method = "method_2115(Lnet/minecraft/world/World;Ljava/lang/String;Lnet/minecraft/entity/player/PlayerEntity;)V")
	public void onSetWorld(World world, String string, PlayerEntity arg2, CallbackInfo cbi) {
		if (world == null) {
			CustomPlayerModelsClient.INSTANCE.onLogout();
			if (MinecraftServerAccess.get() != null)
				ModConfig.getWorldConfig().save();
			MinecraftObjectHolder.setServerObject(null);
		}
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/entity/player/ClientPlayerEntity;", shift = Shift.AFTER), method = "method_2115(Lnet/minecraft/world/World;Ljava/lang/String;Lnet/minecraft/entity/player/PlayerEntity;)V")
	public void onSetPlayer(World world, String string, PlayerEntity arg2, CallbackInfo cbi) {
		if (player != null && !isWorldRemote())
			ServerHandler.netHandler.onJoin(player);
	}

	@Inject(at = @At("HEAD"), method = "tick()V")
	public void onTick(CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.clientTickStart();
		CustomPlayerModelsClient.mc.getPlayerRenderManager().getAnimationEngine().update(this.timer.field_2370);
	}

	@Inject(at = @At("RETURN"), method = "tick()V")
	public void onTickEnd(CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.clientTickEnd();
		MCExecutor.executeAll();
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;thirdPerson:Z", shift = Shift.AFTER, ordinal = 1), method = "tick()V")
	public void setThirdPerson(CallbackInfo cbi) {
		((IGameOptions) options).cpm$onToggleThirdPerson();
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWorldRemote()Z"), method = "tick()V")
	public boolean enableChat(Minecraft mc) {
		return true;
	}

	@Inject(at = @At("HEAD"), method = "isCommand(Ljava/lang/String;)Z", cancellable = true)
	public void isCommand(String command, CallbackInfoReturnable<Boolean> cbi) {
		if (!isWorldRemote())cbi.setReturnValue(true);
		if (command.startsWith("/")) {
			SinglePlayerCommands.executeCommand((Minecraft) (Object) this, command);
		} else {
			inGameHud.addChatMessage("<" + player.name + "> " + command);
		}
	}
}
