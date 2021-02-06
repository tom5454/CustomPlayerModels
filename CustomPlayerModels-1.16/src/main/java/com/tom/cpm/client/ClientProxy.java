package com.tom.cpm.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.function.Predicate;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CustomizeSkinScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.tom.cpm.CommonProxy;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;

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
			loader = new ModelDefinitionLoader(ImageIO.read(is), PlayerProfile::create);
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
		tryBindModel(event.getPlayer(), event.getBuffers(), null);
	}

	private void tryBindModel(PlayerEntity player, IRenderTypeBuffer buffer, Predicate<Object> unbindRule) {
		PlayerProfile profile = (PlayerProfile) loader.loadPlayer(player.getGameProfile());
		ModelDefinition def = profile.getModelDefinition();
		if(def != null) {
			if(def.getResolveState() == 0)def.startResolve();
			else if(def.getResolveState() == 2) {
				if(def.doRender()) {
					this.profile = profile;
					profile.updateFromPlayer(player);
					mc.getPlayerRenderManager().bindModel(profile.getModel(), buffer, def, unbindRule);
					if(unbindRule == null)
						mc.getPlayerRenderManager().getAnimationEngine().handleAnimation(profile);
					return;
				}
			}
		}
		mc.getPlayerRenderManager().unbindModel(profile.getModel());
	}

	@SubscribeEvent
	public void playerRenderPost(RenderPlayerEvent.Post event) {
		if(profile != null) {
			mc.getPlayerRenderManager().unbindModel(profile.getModel());
			profile = null;
		}
	}

	@SubscribeEvent
	public void openGui(GuiScreenEvent.InitGuiEvent.Post evt) {
		if(evt.getGui() instanceof MainMenuScreen || evt.getGui() instanceof CustomizeSkinScreen) {
			evt.addWidget(new Button(0, 0, () -> Minecraft.getInstance().displayGuiScreen(new GuiImpl(EditorGui::new, evt.getGui()))));
		}
	}

	public void renderHand(IRenderTypeBuffer buffer) {
		tryBindModel(Minecraft.getInstance().player, buffer, PlayerRenderManager::unbindHand);
		this.profile = null;
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
			Minecraft.getInstance().displayGuiScreen(new GuiImpl(GestureGui::new, null));
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
			super(x, y, 100, 20, new TranslationTextComponent("button.cpm.open_editor"), b -> r.run());
		}

	}
}
