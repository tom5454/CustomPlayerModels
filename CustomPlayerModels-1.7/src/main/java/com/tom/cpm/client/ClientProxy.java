package com.tom.cpm.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.CommonProxy;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.util.Image;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;

public class ClientProxy extends CommonProxy {
	public static MinecraftObject mc;
	private ModelDefinitionLoader loader;
	private Minecraft minecraft;

	@Override
	public void init() {
		super.init();
		try(InputStream is = ClientProxy.class.getResourceAsStream("/assets/cpm/textures/template/free_space_template.png")) {
			loader = new ModelDefinitionLoader(Image.loadFrom(is), PlayerProfile::create);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load template", e);
		}
		minecraft = Minecraft.getMinecraft();
		mc = new MinecraftObject(minecraft, loader);
		MinecraftObjectHolder.setClientObject(mc);
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		KeyBindings.init();
	}

	private PlayerProfile profile;

	@SubscribeEvent
	public void playerRenderPre(RenderPlayerEvent.Pre event) {
		tryBindModel(null, event.entityPlayer, null, null);
	}

	private boolean tryBindModel(GameProfile gprofile, EntityPlayer player, Predicate<Object> unbindRule, ModelBase toBind) {
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
			mc.getPlayerRenderManager().bindModel(toBind, null, def, unbindRule);
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
	public void openGui(GuiScreenEvent.InitGuiEvent.Post evt) {
		if(evt.gui instanceof GuiMainMenu || evt.gui instanceof GuiOptions) {
			evt.buttonList.add(new Button(0, 0));
		}
	}

	@SubscribeEvent
	public void buttonPress(GuiScreenEvent.ActionPerformedEvent.Pre evt) {
		if(evt.button instanceof Button) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiImpl(EditorGui::new, evt.gui));
		}
	}

	@SubscribeEvent
	public void openGui(GuiOpenEvent openGui) {
		if(openGui.gui == null && minecraft.currentScreen instanceof GuiImpl.Overlay) {
			openGui.gui = ((GuiImpl.Overlay) minecraft.currentScreen).getGui();
		}
	}

	@SubscribeEvent
	public void renderHand(RenderHandEvent evt) {
		tryBindModel(null, Minecraft.getMinecraft().thePlayer, PlayerRenderManager::unbindHand, null);
		this.profile = null;
	}

	public void renderSkull(ModelBase skullModel, GameProfile profile) {
		tryBindModel(profile, null, PlayerRenderManager::unbindSkull, skullModel);
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

		if (minecraft.thePlayer == null || evt.phase == Phase.START)
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
	public void onRenderName(RenderLivingEvent.Specials.Pre evt) {
		if(evt.entity instanceof AbstractClientPlayer) {
			if(!Player.isEnableNames())
				evt.setCanceled(true);
		}
	}

	public static class Button extends GuiButton {

		public Button(int x, int y) {
			super(99, x, y, 100, 20, I18n.format("button.cpm.open_editor"));
		}

	}
}
