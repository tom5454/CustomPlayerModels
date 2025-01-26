package com.tom.cpm.mixin.client;

import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenMainMenu;
import net.minecraft.client.gui.options.ScreenOptions;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.editor.gui.EditorGui;

@Mixin(value = Screen.class, remap = false)
public class ScreenMixin {
	protected @Shadow Minecraft mc;
	protected @Shadow List<ButtonElement> buttons;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Screen;buttonClicked(Lnet/minecraft/client/gui/ButtonElement;)V"), method = "mouseClicked(III)V", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	protected void mouseClicked(int mouseX, int mouseY, int mb, CallbackInfo cbi, Iterator i, ButtonElement button) {
		if(button instanceof CustomPlayerModelsClient.Button) {
			mc.displayScreen(new GuiImpl(EditorGui::new, (Screen) (Object) this));
		}
	}

	@Inject(at = @At("RETURN"), method = "opened(Lnet/minecraft/client/Minecraft;II)V")
	public void init(Minecraft minecraft, int width, int height, CallbackInfo cbi) {
		Screen screen = (Screen) (Object) this;
		if((screen instanceof ScreenMainMenu && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				screen instanceof ScreenOptions) {
			buttons.add(new CustomPlayerModelsClient.Button(0, 0));
		}
	}
}
