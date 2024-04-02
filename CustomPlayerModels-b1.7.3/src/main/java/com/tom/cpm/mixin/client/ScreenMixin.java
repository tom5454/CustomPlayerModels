package com.tom.cpm.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.editor.gui.EditorGui;

@Mixin(Screen.class)
public class ScreenMixin {
	protected @Shadow Minecraft minecraft;
	protected @Shadow List buttons;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;buttonClicked(Lnet/minecraft/client/gui/widget/ButtonWidget;)V"), method = "mouseClicked(III)V", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	protected void mouseClicked(int mouseX, int mouseY, int mb, CallbackInfo cbi, int i, ButtonWidget button) {
		if(button instanceof CustomPlayerModelsClient.Button) {
			minecraft.setScreen(new GuiImpl(EditorGui::new, (Screen) (Object) this));
		}
	}

	@Inject(at = @At("RETURN"), method = "init(Lnet/minecraft/client/Minecraft;II)V")
	public void init(Minecraft minecraft, int width, int height, CallbackInfo cbi) {
		Screen screen = (Screen) (Object) this;
		if((screen instanceof TitleScreen && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				screen instanceof OptionsScreen) {
			buttons.add(new CustomPlayerModelsClient.Button(0, 0));
		}
	}
}
