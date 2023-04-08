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

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(value = PlayerRenderer.class, priority = 900)
public abstract class PlayerRendererMixinFabric extends LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

	public PlayerRendererMixinFabric(EntityRendererManager dispatcher, PlayerModel<AbstractClientPlayerEntity> model,
			float shadowRadius) {
		super(dispatcher, model, shadowRadius);
	}

	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;DDDFF)V")
	public void onRenderPre(AbstractClientPlayerEntity abstractClientPlayerEntity, double d, double e, double f, float g, float h, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.playerRenderPre(abstractClientPlayerEntity, getModel());
	}

	@Inject(at = @At("RETURN"), method = "render(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;DDDFF)V")
	public void onRenderPost(AbstractClientPlayerEntity abstractClientPlayerEntity, double d, double e, double f, float g, float h, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.playerRenderPost(getModel());
	}
}
