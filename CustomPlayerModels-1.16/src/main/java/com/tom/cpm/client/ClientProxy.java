package com.tom.cpm.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CustomizeSkinScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.ICustomPacket;

import com.mojang.authlib.GameProfile;

import com.tom.cpl.util.Image;
import com.tom.cpm.CommonProxy;
import com.tom.cpm.common.NetH;
import com.tom.cpm.common.NetworkHandler;
import com.tom.cpm.mixinplugin.OFDetector;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.model.RenderManager;

import io.netty.buffer.Unpooled;

public class ClientProxy extends CommonProxy {
	public static boolean optifineLoaded;
	public static ClientProxy INSTANCE = null;
	public static MinecraftObject mc;
	private ModelDefinitionLoader loader;
	private Minecraft minecraft;
	private RenderManager<GameProfile, PlayerEntity, Model, IRenderTypeBuffer> manager;

	@Override
	public void init() {
		super.init();
		INSTANCE = this;
		try(InputStream is = ClientProxy.class.getResourceAsStream("/assets/cpm/textures/template/free_space_template.png")) {
			loader = new ModelDefinitionLoader(Image.loadFrom(is), PlayerProfile::create);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load template", e);
		}
		optifineLoaded = OFDetector.doApply();
		if(optifineLoaded)System.out.println("Optifine detected, enabling optifine compatibility");
		minecraft = Minecraft.getInstance();
		mc = new MinecraftObject(minecraft, loader);
		MinecraftObjectHolder.setClientObject(mc);
		MinecraftForge.EVENT_BUS.register(this);
		KeyBindings.init();
		manager = new RenderManager<>(mc.getPlayerRenderManager(), loader, PlayerEntity::getGameProfile);
	}

	@SubscribeEvent
	public void playerRenderPre(RenderPlayerEvent.Pre event) {
		manager.bindPlayer(event.getPlayer(), event.getBuffers());
	}

	@SubscribeEvent
	public void playerRenderPost(RenderPlayerEvent.Post event) {
		manager.tryUnbind();
	}

	@SubscribeEvent
	public void initGui(GuiScreenEvent.InitGuiEvent.Post evt) {
		if((evt.getGui() instanceof MainMenuScreen && ModConfig.getConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				evt.getGui() instanceof CustomizeSkinScreen) {
			evt.addWidget(new Button(0, 0, () -> Minecraft.getInstance().displayGuiScreen(new GuiImpl(EditorGui::new, evt.getGui()))));
		}
	}

	public void renderHand(IRenderTypeBuffer buffer) {
		manager.bindHand(Minecraft.getInstance().player, buffer, PlayerRenderManager::unbindHand);
	}

	public void renderSkull(Model skullModel, GameProfile profile, IRenderTypeBuffer buffer) {
		manager.bindSkull(profile, buffer, PlayerRenderManager::unbindSkull, skullModel);
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
		if(openGui.getGui() instanceof MainMenuScreen && EditorGui.doOpenEditor()) {
			openGui.setGui(new GuiImpl(EditorGui::new, openGui.getGui()));
		}
	}

	public static class Button extends net.minecraft.client.gui.widget.button.Button {

		public Button(int x, int y, Runnable r) {
			super(x, y, 100, 20, new TranslationTextComponent("button.cpm.open_editor"), b -> r.run());
		}

	}

	@SubscribeEvent
	public void onLogout(ClientPlayerNetworkEvent.LoggedOutEvent evt) {
		loader.clearServerData();
	}

	public void receivePacket(ICustomPacket<?> packet, NetH handler) {
		ResourceLocation rl = packet.getName();
		ClientPlayNetHandler h = (ClientPlayNetHandler) handler;
		if(NetworkHandler.helloPacket.equals(rl)) {
			PacketBuffer pb = packet.getInternalData();
			CompoundNBT nbt = pb.readCompoundTag();
			Minecraft.getInstance().execute(() -> {
				handler.cpm$setHasMod(true);
				loader.clearServerData();
				h.sendPacket(new CCustomPayloadPacket(NetworkHandler.helloPacket, new PacketBuffer(Unpooled.EMPTY_BUFFER)));
			});
		} else if(NetworkHandler.setSkin.equals(rl)) {
			PacketBuffer pb = packet.getInternalData();
			int entId = pb.readVarInt();
			CompoundNBT data = pb.readCompoundTag();
			Minecraft.getInstance().execute(() -> {
				Entity ent = Minecraft.getInstance().world.getEntityByID(entId);
				if(ent instanceof PlayerEntity) {
					loader.setModel(((PlayerEntity)ent).getGameProfile(), data.contains("data") ? data.getByteArray("data") : null, data.getBoolean("forced"));
				}
			});
		} else if(NetworkHandler.getSkin.equals(rl)) {
			sendSkinData(h);
		}
	}

	public void sendSkinData(ClientPlayNetHandler h) {
		String model = ModConfig.getConfig().getString(ConfigKeys.SELECTED_MODEL, null);
		if(model != null) {
			File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
			try {
				ModelFile file = ModelFile.load(new File(modelsDir, model));
				file.registerLocalCache(loader);
				PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
				CompoundNBT data = new CompoundNBT();
				data.putByteArray("data", file.getDataBlock());
				pb.writeCompoundTag(data);
				h.sendPacket(new CCustomPayloadPacket(NetworkHandler.setSkin, pb));
			} catch (IOException e) {
			}
		} else {
			PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
			CompoundNBT data = new CompoundNBT();
			pb.writeCompoundTag(data);
			h.sendPacket(new CCustomPayloadPacket(NetworkHandler.setSkin, pb));
		}
	}
}
