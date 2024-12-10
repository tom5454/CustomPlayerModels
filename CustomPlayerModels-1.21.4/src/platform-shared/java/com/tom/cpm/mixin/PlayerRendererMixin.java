package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.LivingRendererAccess;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerProfile;
import com.tom.cpm.client.PlayerRenderStateAccess;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(value = PlayerRenderer.class, priority = 900)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> implements LivingRendererAccess {
	private static @Unique final ResourceLocation CPM$EMPTY_TEX = ResourceLocation.parse("cpm:textures/template/empty.png");

	public PlayerRendererMixin(Context context, PlayerModel entityModel, float f) {
		super(context, entityModel, f);
	}

	@Inject(
			at = @At("RETURN"),
			method = {
					"getTextureLocation(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)Lnet/minecraft/resources/ResourceLocation;"
			},
			cancellable = true)
	public void onGetEntityTexture(PlayerRenderState entity, CallbackInfoReturnable<ResourceLocation> cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(getModel(), new ModelTexture(cbi), TextureSheetType.SKIN);
	}

	@ModifyArg(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent("
					+ "Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
			), method = "renderHand")
	public ResourceLocation getSkinTex(ResourceLocation arg) {
		ModelTexture tex = new ModelTexture(arg);
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(getModel(), tex, TextureSheetType.SKIN);
		return tex.getTexture();
	}

	@Inject(at = @At("HEAD"), method = "renderRightHand")
	public void onRenderRightArmPre(final PoseStack poseStack, final MultiBufferSource vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHand(vertexConsumers, getModel());
	}

	@Inject(at = @At("HEAD"), method = "renderLeftHand")
	public void onRenderLeftArmPre(final PoseStack poseStack, final MultiBufferSource vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHand(vertexConsumers, getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderRightHand")
	public void onRenderRightArmPost(final PoseStack poseStack, final MultiBufferSource vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(vertexConsumers, getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderLeftHand")
	public void onRenderLeftArmPost(final PoseStack poseStack, final MultiBufferSource vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(vertexConsumers, getModel());
	}

	@Inject(at = @At("HEAD"), method = "renderNameTag", cancellable = true)
	public void onRenderName1(final PlayerRenderState playerRenderState, final Component component,
			final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int packedLightIn, CallbackInfo cbi) {
		if(!Player.isEnableNames()) {
			cbi.cancel();
			return;
		}
		if (Player.isEnableLoadingInfo()) {
			PlayerRenderStateAccess sa = (PlayerRenderStateAccess) playerRenderState;
			if (sa.cpm$getModelStatus() != null) {
				poseStack.pushPose();
				poseStack.translate(0.0D, 1.3F, 0.0D);
				poseStack.scale(0.5f, 0.5f, 0.5f);
				super.renderNameTag(playerRenderState, sa.cpm$getModelStatus(), poseStack, multiBufferSource, packedLightIn);
				poseStack.popPose();
			}
		}
	}

	@Override
	public void cpm$onGetRenderType(LivingEntityRenderState player, boolean pTranslucent, boolean pGlowing,
			CallbackInfoReturnable<RenderType> cbi) {
		if (CustomPlayerModelsClient.mc.getPlayerRenderManager().isBound(getModel())) {
			boolean r = CustomPlayerModelsClient.mc.getPlayerRenderManager().getHolderSafe(getModel(), null, h -> h.setInvisState(), false, false);
			if(pTranslucent)return;
			if(!pGlowing && !r)return;
			ResourceLocation tex = getTextureLocation((PlayerRenderState) player);
			CustomPlayerModelsClient.mc.getPlayerRenderManager().getHolderSafe(getModel(), null, h -> h.setInvis(pGlowing), false);
			cbi.setReturnValue(
					pGlowing ?
							RenderType.outline(tex) :
								RenderType.entityCutout(CPM$EMPTY_TEX)
					);
		}
	}

	@Inject(at = @At("RETURN"), method = "extractRenderState")
	public void onExtractRenderState(final AbstractClientPlayer abstractClientPlayer, final PlayerRenderState playerRenderState, final float f, CallbackInfo cbi) {
		PlayerRenderStateAccess sa = (PlayerRenderStateAccess) playerRenderState;
		FormatText st = CustomPlayerModelsClient.INSTANCE.manager.getStatus(abstractClientPlayer.getGameProfile(), ModelDefinitionLoader.PLAYER_UNIQUE);
		sa.cpm$setModelStatus(st != null ? st.remap() : null);
		var pl = CustomPlayerModelsClient.INSTANCE.manager.loadPlayerState(abstractClientPlayer.getGameProfile(), abstractClientPlayer, ModelDefinitionLoader.PLAYER_UNIQUE, AnimationMode.PLAYER);
		sa.cpm$setPlayer(pl);
		if (pl != null) {
			((PlayerProfile) pl).updateFromState(getModel(), playerRenderState);
		}
	}
}
