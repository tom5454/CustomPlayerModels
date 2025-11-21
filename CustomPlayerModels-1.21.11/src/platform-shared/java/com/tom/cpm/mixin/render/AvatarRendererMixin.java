package com.tom.cpm.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.client.CPMOrderedSubmitNodeCollector.CPMSubmitNodeCollector;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.LivingRendererAccess;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerProfile;
import com.tom.cpm.client.PlayerRenderStateAccess;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity>
extends LivingEntityRenderer<AvatarlikeEntity, AvatarRenderState, PlayerModel> implements LivingRendererAccess {
	private static @Unique final Identifier CPM$EMPTY_TEX = Identifier.parse("cpm:textures/template/empty.png");

	public AvatarRendererMixin(Context context, PlayerModel entityModel, float f) {
		super(context, entityModel, f);
	}

	@Inject(
			at = @At("RETURN"),
			method = {
					"getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/resources/Identifier;"
			},
			cancellable = true)
	public void onGetEntityTexture(AvatarRenderState entity, CallbackInfoReturnable<Identifier> cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(getModel(), new ModelTexture(cbi), TextureSheetType.SKIN);
	}

	@Inject(at = @At("HEAD"), method = "submitNameTag", cancellable = true)
	public void onSubmitNameTag(AvatarRenderState avatarRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo cbi) {
		if(!Player.isEnableNames()) {
			cbi.cancel();
			return;
		}
		if (Player.isEnableLoadingInfo()) {
			PlayerRenderStateAccess sa = (PlayerRenderStateAccess) avatarRenderState;
			if (sa.cpm$getModelStatus() != null) {
				poseStack.pushPose();
				poseStack.translate(0.0D, 1.3F, 0.0D);
				poseStack.scale(0.5f, 0.5f, 0.5f);
				submitNodeCollector.submitNameTag(
						poseStack,
						avatarRenderState.nameTagAttachment,
						0,
						sa.cpm$getModelStatus(),
						!avatarRenderState.isDiscrete,
						avatarRenderState.lightCoords,
						avatarRenderState.distanceToCameraSq,
						cameraRenderState);
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
			Identifier tex = getTextureLocation((AvatarRenderState) player);
			CustomPlayerModelsClient.mc.getPlayerRenderManager().getHolderSafe(getModel(), null, h -> h.setInvis(pGlowing), false);
			cbi.setReturnValue(
					pGlowing ?
							RenderTypes.outline(tex) :
								RenderTypes.entityCutout(CPM$EMPTY_TEX)
					);
		}
	}

	@Inject(at = @At("RETURN"), method = "extractRenderState")
	public void onExtractRenderState(final Avatar abstractClientPlayer, final AvatarRenderState playerRenderState, final float f, CallbackInfo cbi) {
		PlayerRenderStateAccess sa = (PlayerRenderStateAccess) playerRenderState;
		GameProfile profile = PlayerProfile.getPlayerProfile(abstractClientPlayer);
		String unique;
		if (abstractClientPlayer instanceof AbstractClientPlayer) {
			unique = ModelDefinitionLoader.PLAYER_UNIQUE;
		} else if(abstractClientPlayer instanceof ClientMannequin) {
			unique = "man:" + abstractClientPlayer.getStringUUID();
		} else {
			return;
		}
		FormatText st = CustomPlayerModelsClient.INSTANCE.manager.getStatus(profile, unique);
		sa.cpm$setModelStatus(st != null ? st.remap() : null);
		var pl = CustomPlayerModelsClient.INSTANCE.manager.loadPlayerState(profile, abstractClientPlayer, unique, AnimationMode.PLAYER);
		sa.cpm$setPlayer(pl);
		if (pl != null) {
			((PlayerProfile) pl).updateFromState(playerRenderState);
		}
	}

	@Inject(at = @At("HEAD"), method = "renderRightHand")
	public void onRenderRightArmPre(final PoseStack poseStack, final SubmitNodeCollector vertexConsumers, final int i, final Identifier Identifier, final boolean sleeve, CallbackInfo cbi, @Local LocalRef<SubmitNodeCollector> snc) {
		CPMSubmitNodeCollector.injectSNC(snc);
		CustomPlayerModelsClient.INSTANCE.renderHand(getModel());
	}

	@Inject(at = @At("HEAD"), method = "renderLeftHand")
	public void onRenderLeftArmPre(final PoseStack poseStack, final SubmitNodeCollector vertexConsumers, final int i, final Identifier Identifier, final boolean sleeve, CallbackInfo cbi, @Local LocalRef<SubmitNodeCollector> snc) {
		CPMSubmitNodeCollector.injectSNC(snc);
		CustomPlayerModelsClient.INSTANCE.renderHand(getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderRightHand")
	public void onRenderRightArmPost(final PoseStack poseStack, final SubmitNodeCollector vertexConsumers, final int i, final Identifier Identifier, final boolean sleeve, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderLeftHand")
	public void onRenderLeftArmPost(final PoseStack poseStack, final SubmitNodeCollector vertexConsumers, final int i, final Identifier Identifier, final boolean sleeve, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(getModel());
	}

	@ModifyArg(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;entityTranslucent("
					+ "Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"
			), method = "renderHand")
	public Identifier getSkinTex(Identifier arg) {
		ModelTexture tex = new ModelTexture(arg);
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(getModel(), tex, TextureSheetType.SKIN);
		return tex.getTexture();
	}
}
