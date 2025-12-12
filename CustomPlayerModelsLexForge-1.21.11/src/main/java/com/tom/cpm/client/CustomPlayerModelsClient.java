package com.tom.cpm.client;

import java.util.List;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.SkinCustomizationScreen;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterPictureInPictureRendererEvent;
import net.minecraftforge.client.event.RenderAvatarEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.tom.cpl.tag.AllTagManagers;
import com.tom.cpm.client.GuiGraphicsEx.RegistrationHandler;
import com.tom.cpm.common.Command;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.gui.SettingsGui;

public class CustomPlayerModelsClient extends ClientBase {
	public static final CustomPlayerModelsClient INSTANCE = new CustomPlayerModelsClient();

	public void preInit(FMLJavaModLoadingContext ctx) {
		init0();
		RegisterKeyMappingsEvent.BUS.addListener(KeyBindings::init);
		RegisterClientReloadListenersEvent.BUS.addListener(INSTANCE::registerReloadListeners);
		RegisterPictureInPictureRendererEvent.BUS.addListener(INSTANCE::registerPip);
		ctx.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((mc, scr) -> new GuiImpl(SettingsGui::new, scr)));
	}

	public void init() {
		MinecraftForge.EVENT_BUS.register(this);
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerShaders0);
		init1();
	}

	@SubscribeEvent
	public void playerRenderPre(RenderAvatarEvent.Pre event) {
		PlayerRenderStateAccess sa = (PlayerRenderStateAccess) event.getState();
		if (sa.cpm$getPlayer() != null) {
			playerRenderPre(sa, event.getRenderer().getModel(), event.getState());
		}
	}

	@SubscribeEvent(priority = Priority.MONITOR)
	public void playerRenderPreC(RenderAvatarEvent.Pre event, boolean cancelled) {
		if(cancelled)playerRenderPost(event.getRenderer().getModel());
	}

	@SubscribeEvent
	public void playerRenderPost(RenderAvatarEvent.Post event) {
		playerRenderPost(event.getRenderer().getModel());
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
	public void renderTick(RenderTickEvent.Pre evt) {
		mc.getPlayerRenderManager().getAnimationEngine().update(evt.timer().getGameTimeDeltaPartialTick(true));
	}

	@SubscribeEvent
	public void clientTickPre(ClientTickEvent.Pre evt) {
		if(!Minecraft.getInstance().isPaused()) {
			mc.getPlayerRenderManager().getAnimationEngine().tick();
		}
	}

	@SubscribeEvent
	public void clientTickPost(ClientTickEvent.Post evt) {
		if (Minecraft.getInstance().player == null)
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

	/*private void registerShaders0(RegisterShadersEvent evt) {
		registerShaders((a, b, c) -> registerShader(evt, a, b, c));
	}

	private void registerShader(RegisterShadersEvent evt, String name, VertexFormat vertexFormat, Consumer<ShaderInstance> finish) {
		try {
			evt.registerShader(new ShaderInstance(evt.getResourceProvider(), ResourceLocation.tryBuild("cpm", name), vertexFormat), finish);
		} catch (IOException e) {
			Log.error("Failed to load cpm '" + name + "' shader", e);
		}
	}*/

	@SubscribeEvent
	public void onLogout(ClientPlayerNetworkEvent.LoggingOut evt) {
		mc.onLogOut();
	}

	@SubscribeEvent
	public void registerClientCommands(RegisterClientCommandsEvent event) {
		new Command(event.getDispatcher(), true);
	}

	private void registerReloadListeners(RegisterClientReloadListenersEvent event) {
		mc.setTags(new AllTagManagers(event::registerReloadListener, CPMTagLoader::new));
	}

	private void registerPip(RegisterPictureInPictureRendererEvent event) {
		GuiGraphicsEx.registerPictureInPictureRenderers(new RegistrationHandler() {

			@Override
			public <T extends PictureInPictureRenderState> void register(Class<T> stateClass,
					Function<BufferSource, PictureInPictureRenderer<T>> factory) {
				event.register(factory.apply(event.getBufferSource()));
			}
		});
	}
}
