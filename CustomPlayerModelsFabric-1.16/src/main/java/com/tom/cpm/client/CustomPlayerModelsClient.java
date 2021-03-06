package com.tom.cpm.client;

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
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.mixinplugin.OFDetector;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.model.RenderManager;
import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;

public class CustomPlayerModelsClient implements ClientModInitializer {
	public static MinecraftObject mc;
	public static CustomPlayerModelsClient INSTANCE;
	public static boolean optifineLoaded;
	public RenderManager<GameProfile, PlayerEntity, Model, VertexConsumerProvider> manager;
	public NetHandler<Identifier, CompoundTag, PlayerEntity, PacketByteBuf, ClientPlayNetworkHandler> netHandler;

	@Override
	public void onInitializeClient() {
		CustomPlayerModels.LOG.info("Customizable Player Models Client Init started");
		INSTANCE = this;
		mc = new MinecraftObject(MinecraftClient.getInstance());
		optifineLoaded = OFDetector.doApply();
		ClientTickEvents.START_CLIENT_TICK.register(cl -> {
			if(!cl.isPaused())
				mc.getPlayerRenderManager().getAnimationEngine().tick();
		});
		KeyBindings.init();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null)
				return;

			if(KeyBindings.gestureMenuBinding.wasPressed()) {
				client.openScreen(new GuiImpl(GestureGui::new, null));
			}

			if(KeyBindings.renderToggleBinding.wasPressed()) {
				Player.setEnableRendering(!Player.isEnableRendering());
			}

			for (Entry<Integer, KeyBinding> e : KeyBindings.quickAccess.entrySet()) {
				if(e.getValue().wasPressed()) {
					mc.getPlayerRenderManager().getAnimationEngine().onKeybind(e.getKey());
				}
			}
		});
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), PlayerEntity::getGameProfile);
		netHandler = new NetHandler<>(Identifier::new);
		netHandler.setNewNbt(CompoundTag::new);
		netHandler.setNewPacketBuffer(() -> new PacketByteBuf(Unpooled.buffer()));
		netHandler.setWriteCompound(PacketByteBuf::writeCompoundTag, PacketByteBuf::readCompoundTag);
		netHandler.setNBTSetters(CompoundTag::putBoolean, CompoundTag::putByteArray, CompoundTag::putFloat);
		netHandler.setNBTGetters(CompoundTag::getBoolean, CompoundTag::getByteArray, CompoundTag::getFloat);
		netHandler.setContains(CompoundTag::contains);
		netHandler.setExecutor(MinecraftClient::getInstance);
		netHandler.setSendPacket((c, rl, pb) -> c.sendPacket(new CustomPayloadC2SPacket(rl, pb)), null);
		netHandler.setPlayerToLoader(PlayerEntity::getGameProfile);
		netHandler.setReadPlayerId(PacketByteBuf::readVarInt, id -> {
			Entity ent = MinecraftClient.getInstance().world.getEntityById(id);
			if(ent instanceof PlayerEntity) {
				return (PlayerEntity) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> MinecraftClient.getInstance().player);
		netHandler.setGetNet(c -> ((ClientPlayerEntity)c).networkHandler);
		CustomPlayerModels.LOG.info("Customizable Player Models Client Initialized");
	}

	public void playerRenderPre(AbstractClientPlayerEntity player, VertexConsumerProvider buffer) {
		manager.bindPlayer(player, buffer);
	}

	public void playerRenderPost() {
		manager.tryUnbind();
	}

	public void initGui(Screen screen, List<Element> children, List<AbstractButtonWidget> buttons) {
		if((screen instanceof TitleScreen && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
				screen instanceof SkinOptionsScreen) {
			Button btn = new Button(0, 0, () -> MinecraftClient.getInstance().openScreen(new GuiImpl(EditorGui::new, screen)));
			buttons.add(btn);
			children.add(btn);
		}
	}

	public void renderHand(VertexConsumerProvider buffer) {
		manager.bindHand(MinecraftClient.getInstance().player, buffer);
	}

	public void renderSkull(Model skullModel, GameProfile profile, VertexConsumerProvider buffer) {
		manager.bindSkull(profile, buffer, skullModel);
	}


	public void renderElytra(PlayerEntity player, VertexConsumerProvider buffer, ElytraEntityModel<LivingEntity> model) {
		manager.bindElytra(player, buffer, model);
	}

	public void renderArmor(BipedEntityModel<LivingEntity> modelArmor, BipedEntityModel<LivingEntity> modelLeggings,
			PlayerEntity player, VertexConsumerProvider bufferIn) {
		manager.bindArmor(player, bufferIn, modelArmor, 1);
		manager.bindArmor(player, bufferIn, modelLeggings, 2);
	}

	public void unbind(Model model) {
		manager.tryUnbind(model);
	}

	public static class Button extends ButtonWidget {

		public Button(int x, int y, Runnable r) {
			super(x, y, 100, 20, new TranslatableText("button.cpm.open_editor"), b -> r.run());
		}

	}

	public void onLogout() {
		mc.getDefinitionLoader().clearServerData();
	}

	//Copy from CapeFeatureRenderer
	public static void renderCape(MatrixStack matrixStack, VertexConsumer buffer, int packedLightIn,
			AbstractClientPlayerEntity abstractClientPlayerEntity, float partialTicks, PlayerEntityModel<AbstractClientPlayerEntity> model,
			ModelDefinition modelDefinition) {
		matrixStack.push();
		matrixStack.translate(0.0D, 0.0D, 0.125D);

		float r, q, s;

		if(abstractClientPlayerEntity != null) {
			double d = MathHelper.lerp(partialTicks, abstractClientPlayerEntity.prevCapeX,
					abstractClientPlayerEntity.capeX)
					- MathHelper.lerp(partialTicks, abstractClientPlayerEntity.prevX,
							abstractClientPlayerEntity.getX());
			double e = MathHelper.lerp(partialTicks, abstractClientPlayerEntity.prevCapeY,
					abstractClientPlayerEntity.capeY)
					- MathHelper.lerp(partialTicks, abstractClientPlayerEntity.prevY,
							abstractClientPlayerEntity.getY());
			double m = MathHelper.lerp(partialTicks, abstractClientPlayerEntity.prevCapeZ,
					abstractClientPlayerEntity.capeZ)
					- MathHelper.lerp(partialTicks, abstractClientPlayerEntity.prevZ,
							abstractClientPlayerEntity.getZ());
			float n = abstractClientPlayerEntity.prevBodyYaw
					+ (abstractClientPlayerEntity.bodyYaw - abstractClientPlayerEntity.prevBodyYaw);
			double o = MathHelper.sin(n * 0.017453292F);
			double p = (-MathHelper.cos(n * 0.017453292F));
			q = (float) e * 10.0F;
			q = MathHelper.clamp(q, -6.0F, 32.0F);
			r = (float) (d * o + m * p) * 100.0F;
			r = MathHelper.clamp(r, 0.0F, 150.0F);
			s = (float) (d * p - m * o) * 100.0F;
			s = MathHelper.clamp(s, -20.0F, 20.0F);
			if (r < 0.0F) {
				r = 0.0F;
			}

			float t = MathHelper.lerp(partialTicks, abstractClientPlayerEntity.prevStrideDistance,
					abstractClientPlayerEntity.strideDistance);
			q += MathHelper.sin(MathHelper.lerp(partialTicks, abstractClientPlayerEntity.prevHorizontalSpeed,
					abstractClientPlayerEntity.horizontalSpeed) * 6.0F) * 32.0F * t;
			if (abstractClientPlayerEntity.isInSneakingPose()) {
				q += 25.0F;
			}
		} else {
			r = 0;
			q = 0;
			s = 0;
		}

		matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(6.0F + r / 2.0F + q));
		matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(s / 2.0F));
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F - s / 2.0F));
		model.renderCape(matrixStack, buffer, packedLightIn, OverlayTexture.DEFAULT_UV);
		matrixStack.pop();
	}
}
