package com.tom.cpm.client;

import java.util.function.Function;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPayloadHandler;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.SkinCustomizationScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

import com.tom.cpl.tag.AllTagManagers;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.common.ByteArrayPayload;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.io.FastByteArrayInputStream;
import com.tom.cpm.shared.network.NetH;

public class CustomPlayerModelsClient extends ClientBase implements ClientModInitializer {
	public static CustomPlayerModelsClient INSTANCE;

	@Override
	public void onInitializeClient() {
		CustomPlayerModels.LOG.info("Customizable Player Models Client Init started");
		INSTANCE = this;
		init0();
		mc.setTags(new AllTagManagers(ResourceManagerHelper.get(PackType.CLIENT_RESOURCES), CPMTagLoaderFabric::new));
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
			if((screen instanceof TitleScreen && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
					screen instanceof SkinCustomizationScreen) {
				Screens.getButtons(screen).add(
						Button.builder(Component.translatable("button.cpm.open_editor"), b -> Minecraft.getInstance().setScreen(new GuiImpl(EditorGui::new, screen))).
						bounds(0, 0, 100, 20).build());
			}
		});
		init1();
		ClientCommandRegistrationCallback.EVENT.register((d, r) -> new ClientCommand(d));
		PlayPayloadHandler<ByteArrayPayload> h = (p, c) -> {
			netHandler.receiveClient(p.id(), new FastByteArrayInputStream(p.data()), (NetH) c.player().connection);
		};
		CustomPlayerModels.clientPackets.forEach(p -> ClientPlayNetworking.registerGlobalReceiver(p, h));
		GuiGraphicsEx.registerPictureInPictureRenderers(this::registerPip);
		CustomPlayerModels.LOG.info("Customizable Player Models Client Initialized");
		apiInit();
	}

	private <T extends PictureInPictureRenderState> void registerPip(Class<T> stateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<T>> factory) {
		SpecialGuiElementRegistry.register(c -> factory.apply(c.vertexConsumers()));
	}

	public void onLogout() {
		mc.onLogOut();
	}
}
