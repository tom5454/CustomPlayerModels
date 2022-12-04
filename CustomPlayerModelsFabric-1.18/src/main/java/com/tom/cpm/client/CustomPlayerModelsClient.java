package com.tom.cpm.client;

import java.util.function.Consumer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.model.Model;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.mixinplugin.OFDetector;
import com.tom.cpm.mixinplugin.VRDetector;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.GestureGui;
import com.tom.cpm.shared.model.RenderManager;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.util.Log;

import io.netty.buffer.Unpooled;

public class CustomPlayerModelsClient implements ClientModInitializer {
	public static final Identifier DEFAULT_CAPE = new Identifier("cpm:textures/template/cape.png");
	public static boolean optifineLoaded, irisLoaded, vrLoaded;
	public static MinecraftObject mc;
	public static CustomPlayerModelsClient INSTANCE;
	public RenderManager<GameProfile, PlayerEntity, Model, VertexConsumerProvider> manager;
	public NetHandler<Identifier, PlayerEntity, ClientPlayNetworkHandler> netHandler;

	@Override
	public void onInitializeClient() {
		CustomPlayerModels.LOG.info("Customizable Player Models Client Init started");
		INSTANCE = this;
		mc = new MinecraftObject(MinecraftClient.getInstance());
		optifineLoaded = OFDetector.doApply();
		vrLoaded = VRDetector.doApply();
		irisLoaded = FabricLoader.getInstance().isModLoaded("iris");
		if(optifineLoaded)Log.info("Optifine detected, enabling optifine compatibility");
		if(vrLoaded)Log.info("ViveCraft detected, enabling ViveCraft compatibility");
		if(irisLoaded)Log.info("Iris detected, enabling iris compatibility");
		ClientTickEvents.START_CLIENT_TICK.register(cl -> {
			if(!cl.isPaused())
				mc.getPlayerRenderManager().getAnimationEngine().tick();
		});
		KeyBindings.init();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null)
				return;

			if(KeyBindings.gestureMenuBinding.wasPressed()) {
				client.setScreen(new Gui(GestureGui::new, null));
			}

			if(KeyBindings.renderToggleBinding.wasPressed()) {
				Player.setEnableRendering(!Player.isEnableRendering());
			}

			mc.getPlayerRenderManager().getAnimationEngine().updateKeys(KeyBindings.quickAccess);
		});
		ScreenEvents.AFTER_INIT.register((mc, screen, sw, sh) -> {
			ScreenEvents.beforeRender(screen).register((_1, _2, _3, _4, _5) -> PlayerProfile.inGui = true);
			ScreenEvents.afterRender(screen).register((_1, _2, _3, _4, _5) -> PlayerProfile.inGui = false);
			if((screen instanceof TitleScreen && ModConfig.getCommonConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true)) ||
					screen instanceof SkinOptionsScreen) {
				Screens.getButtons(screen).add(new Button(0, 0, () -> MinecraftClient.getInstance().setScreen(new Gui(EditorGui::new, screen))));
			}
		});
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), PlayerEntity::getGameProfile);
		manager.setGPGetters(GameProfile::getProperties, Property::getValue);
		netHandler = new NetHandler<>(Identifier::new);
		netHandler.setExecutor(MinecraftClient::getInstance);
		netHandler.setSendPacketClient(d -> new PacketByteBuf(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.sendPacket(new CustomPayloadC2SPacket(rl, pb)));
		netHandler.setPlayerToLoader(PlayerEntity::getGameProfile);
		netHandler.setGetPlayerById(id -> {
			Entity ent = MinecraftClient.getInstance().world.getEntityById(id);
			if(ent instanceof PlayerEntity) {
				return (PlayerEntity) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> MinecraftClient.getInstance().player);
		netHandler.setGetNet(c -> ((ClientPlayerEntity)c).networkHandler);
		netHandler.setDisplayText(t -> MinecraftClient.getInstance().player.sendMessage(t.remap(), false));
		CustomPlayerModels.LOG.info("Customizable Player Models Client Initialized");
		CustomPlayerModels.api.buildClient().voicePlayer(PlayerEntity.class, PlayerEntity::getUuid).localModelApi(GameProfile::new).
		renderApi(Model.class, Identifier.class, RenderLayer.class, VertexConsumerProvider.class, GameProfile.class, ModelTexture::new).init();
	}

	public void playerRenderPre(AbstractClientPlayerEntity player, VertexConsumerProvider buffer, PlayerEntityModel model) {
		manager.bindPlayer(player, buffer, model);
	}

	public void playerRenderPost(VertexConsumerProvider buffer, PlayerEntityModel model) {
		if(buffer instanceof Immediate i)i.draw();
		manager.unbindClear(model);
	}

	public void renderHand(VertexConsumerProvider buffer, PlayerEntityModel model) {
		manager.bindHand(MinecraftClient.getInstance().player, buffer, model);
	}

	public void renderHandPost(VertexConsumerProvider vertexConsumers, PlayerEntityModel model) {
		if(vertexConsumers instanceof Immediate i)i.draw();
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(model);
	}

	public void renderSkull(Model skullModel, GameProfile profile, VertexConsumerProvider buffer) {
		manager.bindSkull(profile, buffer, skullModel);
	}

	public void renderSkullPost(VertexConsumerProvider vertexConsumers, Model model) {
		if(vertexConsumers instanceof Immediate i)i.draw();
		CustomPlayerModelsClient.INSTANCE.manager.unbindFlush(model);
	}

	public void renderElytra(BipedEntityModel<LivingEntity> player, ElytraEntityModel<LivingEntity> model) {
		manager.bindElytra(player, model);
	}

	public void renderArmor(BipedEntityModel<LivingEntity> modelArmor, BipedEntityModel<LivingEntity> modelLeggings,
			BipedEntityModel<LivingEntity> player) {
		manager.bindArmor(player, modelArmor, 1);
		manager.bindArmor(player, modelLeggings, 2);
	}

	public static class Button extends ButtonWidget {

		public Button(int x, int y, Runnable r) {
			super(x, y, 100, 20, new TranslatableText("button.cpm.open_editor"), b -> r.run());
		}

	}

	public void onLogout() {
		mc.onLogOut();
	}

	public void updateJump() {
		MinecraftClient minecraft = MinecraftClient.getInstance();
		if(minecraft.player.isOnGround() && minecraft.player.input.jumping) {
			manager.jump(minecraft.player);
		}
	}

	//Copy from CapeFeatureRenderer
	public static void renderCape(MatrixStack matrixStack, VertexConsumer buffer, int packedLightIn,
			AbstractClientPlayerEntity abstractClientPlayerEntity, float partialTicks, PlayerEntityModel<AbstractClientPlayerEntity> model,
			ModelDefinition modelDefinition) {
		matrixStack.push();

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
			if (abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) {
				if (abstractClientPlayerEntity.isSneaking()) {
					model.cloak.pivotZ = 1.4F + 0.125F * 3;
					model.cloak.pivotY = 1.85F + 1 - 0.125F * 4;
				} else {
					model.cloak.pivotZ = 0.0F + 0.125F * 16f;
					model.cloak.pivotY = 0.0F;
				}
			} else if (abstractClientPlayerEntity.isSneaking()) {
				model.cloak.pivotZ = 0.3F + 0.125F * 16f;
				model.cloak.pivotY = 0.8F + 0.3f;
			} else {
				model.cloak.pivotZ = -1.1F + 0.125F * 32f;
				model.cloak.pivotY = -0.85F + 1;
			}
		} else {
			r = 0;
			q = 0;
			s = 0;
		}

		model.cloak.pitch = (float) -Math.toRadians(6.0F + q / 2.0F + r);
		model.cloak.yaw = (float) Math.toRadians(180.0F - s / 2.0F);
		model.cloak.roll = (float) Math.toRadians(s / 2.0F);
		mc.getPlayerRenderManager().setModelPose(model);
		model.cloak.pitch = 0;
		model.cloak.yaw = 0;
		model.cloak.roll = 0;
		model.renderCape(matrixStack, buffer, packedLightIn, OverlayTexture.DEFAULT_UV);
		matrixStack.pop();
	}

	public static interface ShaderLoader {
		void cpm$registerShader(String name, VertexFormat vertexFormat, Consumer<Shader> finish);
	}

	public void registerShaders(ShaderLoader loader) {
	}

	public static interface PlayerNameTagRenderer<E extends Entity> {
		void cpm$renderNameTag(E entityIn, Text displayNameIn, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn);
		EntityRenderDispatcher cpm$entityRenderDispatcher();
	}

	public static <E extends Entity> void renderNameTag(PlayerNameTagRenderer<E> r, E entityIn, GameProfile gprofile, String unique, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn) {
		double d0 = r.cpm$entityRenderDispatcher().getSquaredDistanceToCamera(entityIn);
		if (d0 < 100.0D) {
			FormatText st = INSTANCE.manager.getStatus(gprofile, unique);
			if(st != null) {
				matrixStackIn.push();
				matrixStackIn.translate(0.0D, 1.3F, 0.0D);
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				r.cpm$renderNameTag(entityIn, st.remap(), matrixStackIn, bufferIn, packedLightIn);
				matrixStackIn.pop();
			}
		}
	}
}
