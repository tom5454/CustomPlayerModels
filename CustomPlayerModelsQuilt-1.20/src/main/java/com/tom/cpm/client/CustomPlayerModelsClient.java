package com.tom.cpm.client;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.screen.api.client.QuiltScreen;
import org.quiltmc.qsl.screen.api.client.ScreenEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;

public class CustomPlayerModelsClient extends ClientBase implements ClientModInitializer {
	public static CustomPlayerModelsClient INSTANCE;

	@Override
	public void onInitializeClient(ModContainer container) {
		CustomPlayerModels.LOG.info("Customizable Player Models Client Init started");
		INSTANCE = this;
		init0();
		ClientTickEvents.START.register(cl -> {
			if(!cl.isPaused())
				mc.getPlayerRenderManager().getAnimationEngine().tick();
		});
		KeyBindings.init();
		ClientTickEvents.END.register(client -> {
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
		ScreenEvents.AFTER_INIT.register((screen, client, firstInit) -> {
			if((screen instanceof TitleScreen && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
					screen instanceof SkinCustomizationScreen) {
				((QuiltScreen)screen).getButtons().add(
						Button.builder(Component.translatable("button.cpm.open_editor"), b -> Minecraft.getInstance().setScreen(new GuiImpl(EditorGui::new, screen))).
						bounds(0, 0, 100, 20).build());
			}
		});
		ScreenEvents.BEFORE_RENDER.register((_1, _2, _3, _4, _5) -> PlayerProfile.inGui = true);
		ScreenEvents.AFTER_RENDER.register((_1, _2, _3, _4, _5) -> PlayerProfile.inGui = false);
		init1();
		ClientCommandRegistrationCallback.EVENT.register((d, b, e) -> new ClientCommand(d));
		CustomPlayerModels.LOG.info("Customizable Player Models Client Initialized");
		apiInit();
	}

	public void onLogout() {
		mc.onLogOut();
	}
}
