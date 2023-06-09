package com.tom.cpm.client;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.gui.SettingsGui;
import com.tom.cpm.shared.util.Log;

public class CustomPlayerModelsClient extends ClientBase {
	public static final CustomPlayerModelsClient INSTANCE = new CustomPlayerModelsClient();

	public static void preInit() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(KeyBindings::init);
	}

	public void init() {
		init0();
		MinecraftForge.EVENT_BUS.register(this);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerShaders0);
		init1();
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((mc, scr) -> new GuiImpl(SettingsGui::new, scr)));
	}

	@SubscribeEvent
	public void playerRenderPre(RenderPlayerEvent.Pre event) {
		playerRenderPre(event.getEntity(), event.getMultiBufferSource(), event.getRenderer().getModel());
	}

	@SubscribeEvent
	public void playerRenderPost(RenderPlayerEvent.Post event) {
		playerRenderPost(event.getMultiBufferSource(), event.getRenderer().getModel());
	}

	@SubscribeEvent
	public void initGui(ScreenEvent.Init.Post evt) {
		if((evt.getScreen() instanceof TitleScreen && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				evt.getScreen() instanceof SkinCustomizationScreen) {
			Screen screen = evt.getScreen();
			Button btn = Button.builder(Component.translatable("button.cpm.open_editor"),
					b -> Minecraft.getInstance().setScreen(new GuiImpl(EditorGui::new, screen))
					).bounds(0, 0, 100, 20).build();
			evt.addListener(btn);
			((List) evt.getScreen().children()).add(btn);
		}
	}

	@SubscribeEvent
	public void renderTick(RenderTickEvent evt) {
		if(evt.phase == Phase.START) {
			mc.getPlayerRenderManager().getAnimationEngine().update(evt.renderTickTime);
		}
	}

	@SubscribeEvent
	public void clientTick(ClientTickEvent evt) {
		if(evt.phase == Phase.START && !minecraft.isPaused()) {
			mc.getPlayerRenderManager().getAnimationEngine().tick();
		}
		if (minecraft.player == null || evt.phase == Phase.START)
			return;

		if(KeyBindings.gestureMenuBinding.consumeClick()) {
			Minecraft.getInstance().setScreen(new GuiImpl(GestureGui::new, null));
		}

		if(KeyBindings.renderToggleBinding.consumeClick()) {
			Player.setEnableRendering(!Player.isEnableRendering());
		}

		mc.getPlayerRenderManager().getAnimationEngine().updateKeys(KeyBindings.quickAccess);
	}

	@SubscribeEvent
	public void openGui(ScreenEvent.Opening openGui) {
		if(openGui.getScreen() == null && minecraft.screen instanceof GuiImpl.Overlay) {
			openGui.setNewScreen(((GuiImpl.Overlay) minecraft.screen).getGui());
		}
		if(openGui.getScreen() instanceof TitleScreen && EditorGui.doOpenEditor()) {
			openGui.setNewScreen(new GuiImpl(EditorGui::new, openGui.getScreen()));
		}
	}

	@SubscribeEvent
	public void drawGuiPre(ScreenEvent.Render.Pre evt) {
		PlayerProfile.inGui = true;
	}

	@SubscribeEvent
	public void drawGuiPost(ScreenEvent.Render.Post evt) {
		PlayerProfile.inGui = false;
	}

	private void registerShaders0(RegisterShadersEvent evt) {
		registerShaders((a, b, c) -> registerShader(evt, a, b, c));
	}

	private void registerShader(RegisterShadersEvent evt, String name, VertexFormat vertexFormat, Consumer<ShaderInstance> finish) {
		try {
			evt.registerShader(new ShaderInstance(evt.getResourceProvider(), new ResourceLocation("cpm", name), vertexFormat), finish);
		} catch (IOException e) {
			Log.error("Failed to load cpm '" + name + "' shader", e);
		}
	}

	@SubscribeEvent
	public void onLogout(ClientPlayerNetworkEvent.LoggingOut evt) {
		mc.onLogOut();
	}
}
