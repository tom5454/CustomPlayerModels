package com.tom.cpm.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

import javax.imageio.ImageIO;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.options.SkinOptionsScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;

import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;

public class CustomPlayerModelsClient implements ClientModInitializer {
	public static MinecraftObject mc;
	private ModelDefinitionLoader loader;

	public static CustomPlayerModelsClient INSTANCE;

	@Override
	public void onInitializeClient() {
		INSTANCE = this;
		try(InputStream is = CustomPlayerModelsClient.class.getResourceAsStream("/assets/cpm/textures/template/free_space_template.png")) {
			loader = new ModelDefinitionLoader(ImageIO.read(is), PlayerProfile::create);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load template", e);
		}
		mc = new MinecraftObject(MinecraftClient.getInstance(), loader);
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
		tryBindModel(player, buffer, null);
	}

	private void tryBindModel(PlayerEntity player, VertexConsumerProvider buffer, Predicate<Object> unbindRule) {
		PlayerProfile profile = (PlayerProfile) loader.loadPlayer(player.getGameProfile());
		ModelDefinition def = profile.getModelDefinition();
		if(def != null) {
			if(def.getResolveState() == 0)def.startResolve();
			else if(def.getResolveState() == 2) {
				if(def.doRender()) {
					this.profile = profile;
					profile.updateFromPlayer(player);
					mc.getPlayerRenderManager().bindModel(profile.getModel(), buffer, def, unbindRule);
					mc.getPlayerRenderManager().getAnimationEngine().handleAnimation(profile);
					return;
				}
			}
		}
		mc.getPlayerRenderManager().unbindModel(profile.getModel());
	}

	public void playerRenderPost() {
		if(profile != null) {
			mc.getPlayerRenderManager().unbindModel(profile.getModel());
			profile = null;
		}
	}

	public void initGui(Screen screen, List<Element> children, List<AbstractButtonWidget> buttons) {
		if(screen instanceof TitleScreen || screen instanceof SkinOptionsScreen) {
			Button btn = new Button(0, 0, () -> MinecraftClient.getInstance().openScreen(new GuiImpl(EditorGui::new, screen)));
			buttons.add(btn);
			children.add(btn);
		}
	}

	public void renderHand(VertexConsumerProvider buffer) {
		tryBindModel(MinecraftClient.getInstance().player, buffer, PlayerRenderManager::unbindHand);
		this.profile = null;
	}

	public static class Button extends ButtonWidget {

		public Button(int x, int y, Runnable r) {
			super(x, y, 100, 20, new TranslatableText("button.cpm.open_editor"), b -> r.run());
		}

	}

}
