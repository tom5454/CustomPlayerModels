package com.tom.cpm.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CustomizeSkinScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.util.text.TranslationTextComponent;

import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;

public class CustomPlayerModelsClient extends ClientBase implements ClientModInitializer {
	public static CustomPlayerModelsClient INSTANCE;

	@Override
	public void onInitializeClient() {
		CustomPlayerModels.LOG.info("Customizable Player Models Client Init started");
		INSTANCE = this;
		init0();
		ClientTickEvents.START_CLIENT_TICK.register(cl -> {
			if(!cl.isPaused())
				mc.getPlayerRenderManager().getAnimationEngine().tick();
		});
		KeyBindings.init();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null)
				return;

			if(KeyBindings.gestureMenuBinding.consumeClick()) {
				client.setScreen(new GuiImpl(GestureGui::new, null));
			}

			if(KeyBindings.renderToggleBinding.consumeClick()) {
				Player.setEnableRendering(!Player.isEnableRendering());
			}

			mc.getPlayerRenderManager().getAnimationEngine().updateKeys(KeyBindings.quickAccess);
		});
		ScreenEvents.AFTER_INIT.register((mc, screen, sw, sh) -> {
			ScreenEvents.beforeRender(screen).register((_1, _2, _3, _4, _5) -> PlayerProfile.inGui = true);
			ScreenEvents.afterRender(screen).register((_1, _2, _3, _4, _5) -> PlayerProfile.inGui = false);
			if((screen instanceof MainMenuScreen && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
					screen instanceof CustomizeSkinScreen) {
				Screens.getButtons(screen).add(new Button(0, 0, () -> Minecraft.getInstance().setScreen(new GuiImpl(EditorGui::new, screen))));
			}
		});
		init1();
		CustomPlayerModels.LOG.info("Customizable Player Models Client Initialized");
		apiInit();
	}

	public static class Button extends net.minecraft.client.gui.widget.button.Button {

		public Button(int x, int y, Runnable r) {
			super(x, y, 100, 20, new TranslationTextComponent("button.cpm.open_editor"), b -> r.run());
		}

	}

	public void onLogout() {
		mc.onLogOut();
	}
}
