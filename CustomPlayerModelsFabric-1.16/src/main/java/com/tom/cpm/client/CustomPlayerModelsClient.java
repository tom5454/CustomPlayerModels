package com.tom.cpm.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.options.SkinOptionsScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.model.Model;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.mixinplugin.OFDetector;
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

public class CustomPlayerModelsClient implements ClientModInitializer {
	public static MinecraftObject mc;
	private ModelDefinitionLoader loader;

	public static CustomPlayerModelsClient INSTANCE;
	public static boolean optifineLoaded;

	@Override
	public void onInitializeClient() {
		INSTANCE = this;
		try(InputStream is = CustomPlayerModelsClient.class.getResourceAsStream("/assets/cpm/textures/template/free_space_template.png")) {
			loader = new ModelDefinitionLoader(Image.loadFrom(is), PlayerProfile::create);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load template", e);
		}
		mc = new MinecraftObject(MinecraftClient.getInstance(), loader);
		optifineLoaded = OFDetector.doApply();
		MinecraftObjectHolder.setClientObject(mc);
		ClientTickEvents.START_CLIENT_TICK.register(cl -> {
			if(!cl.isPaused())
				mc.getPlayerRenderManager().getAnimationEngine().tick();
		});
		KeyBindings.init();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null)
				return;

			if(KeyBindings.gestureMenuBinding.isPressed()) {
				client.openScreen(new GuiImpl(GestureGui::new, null));
			}

			if(KeyBindings.renderToggleBinding.isPressed()) {
				Player.setEnableRendering(!Player.isEnableRendering());
			}

			for (Entry<Integer, KeyBinding> e : KeyBindings.quickAccess.entrySet()) {
				if(e.getValue().isPressed()) {
					mc.getPlayerRenderManager().getAnimationEngine().onKeybind(e.getKey());
				}
			}
		});
	}

	private PlayerProfile profile;

	public void playerRenderPre(AbstractClientPlayerEntity player, VertexConsumerProvider buffer) {
		tryBindModel(null, player, buffer, null, null);
	}

	private boolean tryBindModel(GameProfile gprofile, PlayerEntity player, VertexConsumerProvider buffer, Predicate<Object> unbindRule, Model toBind) {
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

	public void playerRenderPost() {
		if(profile != null) {
			mc.getPlayerRenderManager().unbindModel(profile.getModel());
			profile = null;
		}
	}

	public void initGui(Screen screen, List<Element> children, List<AbstractButtonWidget> buttons) {
		if((screen instanceof TitleScreen && ModConfig.getConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				screen instanceof SkinOptionsScreen) {
			Button btn = new Button(0, 0, () -> MinecraftClient.getInstance().openScreen(new GuiImpl(EditorGui::new, screen)));
			buttons.add(btn);
			children.add(btn);
		}
	}

	public void renderHand(VertexConsumerProvider buffer) {
		tryBindModel(null, MinecraftClient.getInstance().player, buffer, PlayerRenderManager::unbindHand, null);
		this.profile = null;
	}

	public void renderSkull(Model skullModel, GameProfile profile, VertexConsumerProvider buffer) {
		PlayerProfile prev = this.profile;
		tryBindModel(profile, null, buffer, PlayerRenderManager::unbindSkull, skullModel);
		this.profile = prev;
	}

	public static class Button extends ButtonWidget {

		public Button(int x, int y, Runnable r) {
			super(x, y, 100, 20, new TranslatableText("button.cpm.open_editor"), b -> r.run());
		}

	}

}
