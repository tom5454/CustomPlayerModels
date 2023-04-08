package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;

import com.tom.cpm.client.ClientBase.PlayerNameTagRenderer;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;

@Mixin(value = PlayerRenderer.class, priority = 900)
public abstract class PlayerRendererMixin extends LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> implements PlayerNameTagRenderer<AbstractClientPlayerEntity> {

	public PlayerRendererMixin(EntityRendererManager rendererManager,
			PlayerModel<AbstractClientPlayerEntity> entityModelIn, float shadowSizeIn) {
		super(rendererManager, entityModelIn, shadowSizeIn);
	}

	@Inject(at = @At("HEAD"), method = "renderRightHand(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V")
	public void onRenderRightArmPre(AbstractClientPlayerEntity player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHand(getModel());
	}

	@Inject(at = @At("HEAD"), method = "renderLeftHand(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V")
	public void onRenderLeftArmPre(AbstractClientPlayerEntity player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHand(getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderRightHand(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V")
	public void onRenderRightArmPost(AbstractClientPlayerEntity player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderLeftHand(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V")
	public void onRenderLeftArmPost(AbstractClientPlayerEntity player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(getModel());
	}

	@Inject(at = @At("HEAD"), method = "renderNameTags(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;DDDLjava/lang/String;D)V", cancellable = true)
	public void onRenderName1(AbstractClientPlayerEntity entityIn, double x, double y, double z, String displayNameIn, double dst, CallbackInfo cbi) {
		if(!Player.isEnableNames())cbi.cancel();
	}

	@Inject(at = @At(value = "RETURN"), method = "renderNameTags(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;DDDLjava/lang/String;D)V")
	public void onRenderName2(AbstractClientPlayerEntity entityIn, double x, double y, double z, String displayNameIn, double dst, CallbackInfo cbi) {
		if(Player.isEnableLoadingInfo())
			CustomPlayerModelsClient.renderNameTag(this, entityIn, entityIn.getGameProfile(), ModelDefinitionLoader.PLAYER_UNIQUE, x, y, z, dst);
	}

	@Override
	public void cpm$renderNameTag(AbstractClientPlayerEntity entityIn, String displayNameIn, double x, double y, double z, double i) {
		super.renderNameTags(entityIn, x, y, z, displayNameIn, i);
	}

	@Override
	public EntityRendererManager cpm$entityRenderDispatcher() {
		return entityRenderDispatcher;
	}
}
