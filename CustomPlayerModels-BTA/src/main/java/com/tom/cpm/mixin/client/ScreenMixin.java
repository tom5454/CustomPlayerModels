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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.options.GuiOptions;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.editor.gui.EditorGui;

@Mixin(value = GuiScreen.class, remap = false)
public class ScreenMixin {
	protected @Shadow Minecraft mc;
	protected @Shadow List<GuiButton> controlList;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;buttonPressed(Lnet/minecraft/client/gui/GuiButton;)V"), method = "mouseClicked(III)V", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	protected void mouseClicked(int mouseX, int mouseY, int mb, CallbackInfo cbi, Iterator i, GuiButton button) {
		if(button instanceof CustomPlayerModelsClient.Button) {
			mc.displayGuiScreen(new GuiImpl(EditorGui::new, (GuiScreen) (Object) this));
		}
	}

	@Inject(at = @At("RETURN"), method = "setWorldAndResolution(Lnet/minecraft/client/Minecraft;II)V")
	public void init(Minecraft minecraft, int width, int height, CallbackInfo cbi) {
		GuiScreen screen = (GuiScreen) (Object) this;
		if((screen instanceof GuiMainMenu && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				screen instanceof GuiOptions) {
			controlList.add(new CustomPlayerModelsClient.Button(0, 0));
		}
	}
}
