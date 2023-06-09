package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
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

	@Override
	@Unique
	public void renderModel(AbstractClientPlayerEntity p_77036_1_, float p_77036_2_, float p_77036_3_,
			float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_) {
		super.renderModel(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
	}

	@Inject(at = @At("HEAD"), method = "(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;FFFFFF)V", cancellable = true)
	public void onRenderModel(AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo cbi) {
		boolean pBodyVisible = this.isVisible(player);
		boolean pTranslucent = !pBodyVisible && !player.isInvisibleTo(Minecraft.getInstance().player);
		if(!pBodyVisible && CustomPlayerModelsClient.mc.getPlayerRenderManager().isBound(getModel())) {
			boolean r = CustomPlayerModelsClient.mc.getPlayerRenderManager().getHolderSafe(getModel(), null, h -> h.setInvisState(), false, false);
			if(pTranslucent)return;
			boolean pGlowing = player.isGlowing();
			if(!pGlowing && !r)return;
			if (!this.bindTexture(player))return;

			CustomPlayerModelsClient.mc.getPlayerRenderManager().getHolderSafe(getModel(), null, h -> h.setInvis(false), false);
			this.model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

			cbi.cancel();
		}
	}
}
