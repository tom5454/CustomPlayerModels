package com.tom.cpm.client;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.common.PlayerAnimUpdater;
import com.tom.cpm.mixinplugin.IrisDetector;
import com.tom.cpm.mixinplugin.OFDetector;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RenderManager;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.util.Log;

import io.netty.buffer.Unpooled;

public abstract class ClientBase {
	public static final ResourceLocation DEFAULT_CAPE = new ResourceLocation("cpm:textures/template/cape.png");
	public static boolean optifineLoaded, irisLoaded, vrLoaded;
	public static MinecraftObject mc;
	protected Minecraft minecraft;
	public RenderManager<GameProfile, net.minecraft.world.entity.player.Player, Model, MultiBufferSource> manager;
	public NetHandler<ResourceLocation, net.minecraft.world.entity.player.Player, ClientPacketListener> netHandler;

	public void init0() {
		minecraft = Minecraft.getInstance();
		mc = new MinecraftObject(minecraft);
		optifineLoaded = OFDetector.doApply();
		vrLoaded = Platform.isModLoaded("vivecraft");
		irisLoaded = IrisDetector.doApply();
		if(optifineLoaded)Log.info("Optifine detected, enabling optifine compatibility");
		if(vrLoaded)Log.info("ViveCraft detected, enabling ViveCraft compatibility");
		if(irisLoaded)Log.info("Iris detected, enabling iris compatibility");
	}

	public void init1() {
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), PlayerProfile::getPlayerProfile);
		manager.setGPGetters(GameProfile::getProperties, Property::getValue);
		netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setExecutor(() -> minecraft);
		netHandler.setSendPacketClient(d -> new FriendlyByteBuf(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.send(new ServerboundCustomPayloadPacket(rl, pb)));
		netHandler.setPlayerToLoader(net.minecraft.world.entity.player.Player::getGameProfile);
		netHandler.setGetPlayerById(id -> {
			Entity ent = Minecraft.getInstance().level.getEntity(id);
			if(ent instanceof net.minecraft.world.entity.player.Player) {
				return (net.minecraft.world.entity.player.Player) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> minecraft.player);
		netHandler.setGetNet(c -> ((LocalPlayer)c).connection);
		netHandler.setDisplayText(t -> minecraft.player.displayClientMessage(t.remap(), false));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
	}

	public static void apiInit() {
		CustomPlayerModels.api.buildClient().voicePlayer(net.minecraft.world.entity.player.Player.class, net.minecraft.world.entity.player.Player::getUUID).
		renderApi(Model.class, ResourceLocation.class, RenderType.class, MultiBufferSource.class, GameProfile.class, ModelTexture::new).
		localModelApi(GameProfile::new).init();
	}

	//Copy from CapeFeatureRenderer
	public static void renderCape(PoseStack matrixStack, VertexConsumer buffer, int packedLightIn,
			AbstractClientPlayer abstractClientPlayerEntity, float partialTicks, PlayerModel<AbstractClientPlayer> model,
			ModelDefinition modelDefinition) {
		matrixStack.pushPose();

		float r, q, s;

		if(abstractClientPlayerEntity != null) {
			double d = Mth.lerp(partialTicks, abstractClientPlayerEntity.xCloakO,
					abstractClientPlayerEntity.xCloak)
					- Mth.lerp(partialTicks, abstractClientPlayerEntity.xo,
							abstractClientPlayerEntity.getX());
			double e = Mth.lerp(partialTicks, abstractClientPlayerEntity.yCloakO,
					abstractClientPlayerEntity.yCloak)
					- Mth.lerp(partialTicks, abstractClientPlayerEntity.yo,
							abstractClientPlayerEntity.getY());
			double m = Mth.lerp(partialTicks, abstractClientPlayerEntity.zCloakO,
					abstractClientPlayerEntity.zCloak)
					- Mth.lerp(partialTicks, abstractClientPlayerEntity.zo,
							abstractClientPlayerEntity.getZ());
			float n = abstractClientPlayerEntity.yBodyRotO
					+ (abstractClientPlayerEntity.yBodyRot - abstractClientPlayerEntity.yBodyRotO);
			double o = Mth.sin(n * 0.017453292F);
			double p = (-Mth.cos(n * 0.017453292F));
			q = (float) e * 10.0F;
			q = Mth.clamp(q, -6.0F, 32.0F);
			r = (float) (d * o + m * p) * 100.0F;
			r = Mth.clamp(r, 0.0F, 150.0F);
			s = (float) (d * p - m * o) * 100.0F;
			s = Mth.clamp(s, -20.0F, 20.0F);
			if (r < 0.0F) {
				r = 0.0F;
			}

			float t = Mth.lerp(partialTicks, abstractClientPlayerEntity.oBob,
					abstractClientPlayerEntity.bob);
			q += Mth.sin(Mth.lerp(partialTicks, abstractClientPlayerEntity.walkDistO,
					abstractClientPlayerEntity.walkDist) * 6.0F) * 32.0F * t;
			if (abstractClientPlayerEntity.isCrouching()) {
				q += 50.0F;
			}
			if (abstractClientPlayerEntity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
				if (abstractClientPlayerEntity.isCrouching()) {
					model.cloak.z = 1.4F + 0.125F * 3;
					model.cloak.y = 1.85F + 1 - 0.125F * 4;
				} else {
					model.cloak.z = 0.0F + 0.125F * 16f;
					model.cloak.y = 0.0F;
				}
			} else if (abstractClientPlayerEntity.isCrouching()) {
				model.cloak.z = 0.3F + 0.125F * 16f;
				model.cloak.y = 0.8F + 0.3f;
			} else {
				model.cloak.z = -1.1F + 0.125F * 32f;
				model.cloak.y = -0.85F + 1;
			}
		} else {
			r = 0;
			q = 0;
			s = 0;
		}

		model.cloak.xRot = (float) -Math.toRadians(6.0F + q / 2.0F + r);
		model.cloak.yRot = (float) Math.toRadians(180.0F - s / 2.0F);
		model.cloak.zRot = (float) Math.toRadians(s / 2.0F);
		mc.getPlayerRenderManager().setModelPose(model);
		model.cloak.xRot = 0;
		model.cloak.yRot = 0;
		model.cloak.zRot = 0;
		model.renderCloak(matrixStack, buffer, packedLightIn, OverlayTexture.NO_OVERLAY);
		matrixStack.popPose();
	}

	public static interface PlayerNameTagRenderer<E extends Entity> {
		void cpm$renderNameTag(E entityIn, Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn);
		EntityRenderDispatcher cpm$entityRenderDispatcher();
	}

	public <E extends Entity> void renderNameTag(PlayerNameTagRenderer<E> r, E entityIn, GameProfile gprofile, String unique, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		double d0 = r.cpm$entityRenderDispatcher().distanceToSqr(entityIn);
		if (d0 < 100.0D) {
			FormatText st = manager.getStatus(gprofile, unique);
			if(st != null) {
				matrixStackIn.pushPose();
				matrixStackIn.translate(0.0D, 1.3F, 0.0D);
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
				r.cpm$renderNameTag(entityIn, st.remap(), matrixStackIn, bufferIn, packedLightIn);
				matrixStackIn.popPose();
			}
		}
	}

	public void playerRenderPre(Player player, MultiBufferSource buffer, PlayerModel model) {
		manager.bindPlayer(player, buffer, model);
	}

	public void playerRenderPost(MultiBufferSource buffer, PlayerModel model) {
		manager.unbindClear(model);
	}

	public void renderHand(MultiBufferSource buffer, PlayerModel model) {
		manager.bindHand(minecraft.player, buffer, model);
	}

	public void renderHandPost(MultiBufferSource buffer, HumanoidModel model) {
		manager.unbindClear(model);
	}

	public void renderSkull(Model skullModel, GameProfile profile, MultiBufferSource buffer) {
		manager.bindSkull(profile, buffer, skullModel);
	}

	public void renderSkullPost(MultiBufferSource buffer, Model model) {
		manager.unbindFlush(model);
	}

	public void renderElytra(HumanoidModel<LivingEntity> player, ElytraModel<LivingEntity> model) {
		manager.bindElytra(player, model);
	}

	public void renderArmor(HumanoidModel<LivingEntity> modelArmor, HumanoidModel<LivingEntity> modelLeggings,
			HumanoidModel<LivingEntity> player) {
		manager.bindArmor(player, modelArmor, 1);
		manager.bindArmor(player, modelLeggings, 2);
	}

	public void updateJump() {
		if(minecraft.player.isOnGround() && minecraft.player.input.jumping) {
			manager.jump(minecraft.player);
		}
	}

	public static interface ShaderLoader {
		void cpm$registerShader(String name, VertexFormat vertexFormat, Consumer<ShaderInstance> finish);
	}

	public void registerShaders(ShaderLoader loader) {
	}
}
