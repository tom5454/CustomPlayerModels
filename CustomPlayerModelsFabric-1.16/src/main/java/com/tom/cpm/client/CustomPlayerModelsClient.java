package com.tom.cpm.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;

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
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import com.mojang.authlib.GameProfile;

import com.tom.cpl.util.Image;
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

public class CustomPlayerModelsClient implements ClientModInitializer {
	public static MinecraftObject mc;
	private ModelDefinitionLoader loader;
	public static CustomPlayerModelsClient INSTANCE;
	public static boolean optifineLoaded;
	private RenderManager<GameProfile, PlayerEntity, Model, VertexConsumerProvider> manager;

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
		manager = new RenderManager<>(mc.getPlayerRenderManager(), loader, PlayerEntity::getGameProfile);
	}

	public void playerRenderPre(AbstractClientPlayerEntity player, VertexConsumerProvider buffer) {
		manager.bindPlayer(player, buffer);
	}

	public void playerRenderPost() {
		manager.tryUnbind();
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
		manager.bindHand(MinecraftClient.getInstance().player, buffer, PlayerRenderManager::unbindHand);
	}

	public void renderSkull(Model skullModel, GameProfile profile, VertexConsumerProvider buffer) {
		manager.bindSkull(profile, buffer, PlayerRenderManager::unbindSkull, skullModel);
	}

	public static class Button extends ButtonWidget {

		public Button(int x, int y, Runnable r) {
			super(x, y, 100, 20, new TranslatableText("button.cpm.open_editor"), b -> r.run());
		}

	}

	public void onLogout() {
		loader.clearServerData();
	}

	public void receivePacket(CustomPayloadS2CPacket packet, NetH handler) {
		Identifier rl = packet.getChannel();
		ClientPlayNetworkHandler h = (ClientPlayNetworkHandler) handler;
		if(NetworkHandler.helloPacket.equals(rl)) {
			PacketByteBuf pb = packet.getData();
			CompoundTag nbt = pb.readCompoundTag();
			MinecraftClient.getInstance().execute(() -> {
				handler.cpm$setHasMod(true);
				loader.clearServerData();
				h.sendPacket(new CustomPayloadC2SPacket(NetworkHandler.helloPacket, new PacketByteBuf(Unpooled.EMPTY_BUFFER)));
			});
		} else if(NetworkHandler.setSkin.equals(rl)) {
			PacketByteBuf pb = packet.getData();
			int entId = pb.readVarInt();
			CompoundTag data = pb.readCompoundTag();
			MinecraftClient.getInstance().execute(() -> {
				Entity ent = MinecraftClient.getInstance().world.getEntityById(entId);
				if(ent instanceof PlayerEntity) {
					loader.setModel(((PlayerEntity)ent).getGameProfile(), data.contains("data") ? data.getByteArray("data") : null, data.getBoolean("forced"));
				}
			});
		} else if(NetworkHandler.getSkin.equals(rl)) {
			sendSkinData(h);
		}
	}

	public void sendSkinData(ClientPlayNetworkHandler h) {
		String model = ModConfig.getConfig().getString(ConfigKeys.SELECTED_MODEL, null);
		if(model != null) {
			File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
			try {
				ModelFile file = ModelFile.load(new File(modelsDir, model));
				PacketByteBuf pb = new PacketByteBuf(Unpooled.buffer());
				CompoundTag data = new CompoundTag();
				data.putByteArray("data", file.getDataBlock());
				pb.writeCompoundTag(data);
				h.sendPacket(new CustomPayloadC2SPacket(NetworkHandler.setSkin, pb));
			} catch (IOException e) {
			}
		} else {
			PacketByteBuf pb = new PacketByteBuf(Unpooled.buffer());
			CompoundTag data = new CompoundTag();
			pb.writeCompoundTag(data);
			h.sendPacket(new CustomPayloadC2SPacket(NetworkHandler.setSkin, pb));
		}
	}
}
