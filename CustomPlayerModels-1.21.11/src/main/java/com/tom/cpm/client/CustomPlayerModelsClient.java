package com.tom.cpm.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

import com.tom.cpl.tag.AllTagManagers;
import com.tom.cpm.common.Command;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.gui.SettingsGui;

public class CustomPlayerModelsClient extends ClientBase {
	public static final CustomPlayerModelsClient INSTANCE = new CustomPlayerModelsClient();

	public void preInit(IEventBus bus) {
		init0();
		bus.addListener(KeyBindings::init);
		bus.addListener(INSTANCE::registerReloadListeners);
		bus.addListener(INSTANCE::registerPipelines);
		bus.addListener(INSTANCE::registerPip);
	}

	public void init() {
		NeoForge.EVENT_BUS.register(this);
		init1();
		ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (mc, scr) -> new GuiImpl(SettingsGui::new, scr));
	}

	@SubscribeEvent
	public void playerRenderPre(RenderPlayerEvent.Pre<?> event) {
		PlayerRenderStateAccess sa = (PlayerRenderStateAccess) event.getRenderState();
		if (sa.cpm$getPlayer() != null) {
			playerRenderPre(sa, event.getRenderer().getModel(), event.getRenderState());
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void playerRenderPreC(RenderPlayerEvent.Pre<?> event) {
		if (event.isCanceled())
			playerRenderPost(event.getRenderer().getModel());
	}

	@SubscribeEvent
	public void playerRenderPost(RenderPlayerEvent.Post<?> event) {
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
	public void renderTick(RenderFrameEvent.Pre evt) {
		mc.getPlayerRenderManager().getAnimationEngine().update(evt.getPartialTick().getGameTimeDeltaPartialTick(true));
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

	private void registerReloadListeners(AddClientReloadListenersEvent event) {
		mc.setTags(new AllTagManagers(l -> event.addListener(l.id, l), CPMTagLoader::new));
	}

	private void registerPipelines(RegisterRenderPipelinesEvent event) {
		CustomRenderTypes.linesNoDepth();//Class init
		Platform.pipelines.forEach(event::registerPipeline);
	}

	private void registerPip(RegisterPictureInPictureRenderersEvent event) {
		GuiGraphicsEx.registerPictureInPictureRenderers(event::register);
	}
}
