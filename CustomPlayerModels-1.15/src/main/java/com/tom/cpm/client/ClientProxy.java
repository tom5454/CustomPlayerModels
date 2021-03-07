package com.tom.cpm.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CustomizeSkinScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.CommonProxy;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.ConfigEntry.ModConfig;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.util.Image;

public class ClientProxy extends CommonProxy {
	public static ClientProxy INSTANCE = null;
	public static MinecraftObject mc;
	private ModelDefinitionLoader loader;
	private Minecraft minecraft;

	@Override
	public void init() {
		super.init();
		INSTANCE = this;
		try(InputStream is = ClientProxy.class.getResourceAsStream("/assets/cpm/textures/template/free_space_template.png")) {
			loader = new ModelDefinitionLoader(Image.loadFrom(is), PlayerProfile::create);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load template", e);
		}
		minecraft = Minecraft.getInstance();
		mc = new MinecraftObject(minecraft, loader);
		MinecraftObjectHolder.setClientObject(mc);
		MinecraftForge.EVENT_BUS.register(this);
		KeyBindings.init();
	}

	private PlayerProfile profile;

	@SubscribeEvent
	public void playerRenderPre(RenderPlayerEvent.Pre event) {
		tryBindModel(null, event.getPlayer(), event.getBuffers(), null, null);
	}

	private boolean tryBindModel(GameProfile gprofile, PlayerEntity player, IRenderTypeBuffer buffer, Predicate<Object> unbindRule, Model toBind) {
		if(gprofile == null)gprofile = player.getGameProfile();
		PlayerProfile profile = (PlayerProfile) loader.loadPlayer(gprofile);
		if(toBind == null)toBind = profile.getModel();
		ModelDefinition def = profile.getAndResolveDefinition();
		if(def != null) {
			this.profile = profile;
			if(player != null)
				profile.updateFromPlayer(player);
			else
				profile.setRenderPose(VanillaPose.SKULL_RENDER);
			mc.getPlayerRenderManager().bindModel(toBind, buffer, def, unbindRule, profile);
			if(unbindRule == null || player == null)
				mc.getPlayerRenderManager().getAnimationEngine().handleAnimation(profile);
			return true;
		}
		mc.getPlayerRenderManager().unbindModel(toBind);
		return false;
	}

	@SubscribeEvent
	public void playerRenderPost(RenderPlayerEvent.Post event) {
		if(profile != null) {
			mc.getPlayerRenderManager().unbindModel(profile.getModel());
			profile = null;
		}
	}

	@SubscribeEvent
	public void initGui(GuiScreenEvent.InitGuiEvent.Post evt) {
		if((evt.getGui() instanceof MainMenuScreen && ModConfig.getConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				evt.getGui() instanceof CustomizeSkinScreen) {
			evt.addWidget(new Button(0, 0, () -> Minecraft.getInstance().displayGuiScreen(new GuiImpl(EditorGui::new, evt.getGui()))));
		}
	}

	public void renderHand(IRenderTypeBuffer buffer) {
		tryBindModel(null, Minecraft.getInstance().player, buffer, PlayerRenderManager::unbindHand, null);
		this.profile = null;
	}

	public void renderSkull(Model skullModel, GameProfile profile, IRenderTypeBuffer buffer) {
		PlayerProfile prev = this.profile;
		tryBindModel(profile, null, buffer, PlayerRenderManager::unbindSkull, skullModel);
		this.profile = prev;
	}

	@SubscribeEvent
	public void renderTick(RenderTickEvent evt) {
		if(evt.phase == Phase.START) {
			mc.getPlayerRenderManager().getAnimationEngine().update(evt.renderTickTime);
		}
	}

	@SubscribeEvent
	public void clientTick(ClientTickEvent evt) {
		if(evt.phase == Phase.START && !minecraft.isGamePaused()) {
			mc.getPlayerRenderManager().getAnimationEngine().tick();
		}

		if (minecraft.player == null || evt.phase == Phase.START)
			return;

		if(KeyBindings.gestureMenuBinding.isPressed()) {
			minecraft.displayGuiScreen(new GuiImpl(GestureGui::new, null));
		}

		if(KeyBindings.renderToggleBinding.isPressed()) {
			Player.setEnableRendering(!Player.isEnableRendering());
		}

		for (Entry<Integer, KeyBinding> e : KeyBindings.quickAccess.entrySet()) {
			if(e.getValue().isPressed()) {
				mc.getPlayerRenderManager().getAnimationEngine().onKeybind(e.getKey());
			}
		}
	}

	@SubscribeEvent
	public void openGui(GuiOpenEvent openGui) {
		if(openGui.getGui() == null && minecraft.currentScreen instanceof GuiImpl.Overlay) {
			openGui.setGui(((GuiImpl.Overlay) minecraft.currentScreen).getGui());
		}
	}

	public static class Button extends net.minecraft.client.gui.widget.button.Button {

		public Button(int x, int y, Runnable r) {
			super(x, y, 100, 20, I18n.format("button.cpm.open_editor"), b -> r.run());
		}

	}
}
