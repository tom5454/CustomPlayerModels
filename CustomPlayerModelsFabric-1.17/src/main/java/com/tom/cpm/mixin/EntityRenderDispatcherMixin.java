package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.resource.ResourceManager;

import com.tom.cpm.client.ERDAccess;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements ERDAccess {
	private BipedEntityModel<AbstractClientPlayerEntity> cpm$armorLegs;
	private BipedEntityModel<AbstractClientPlayerEntity> cpm$armorBody;
	private ElytraEntityModel<AbstractClientPlayerEntity> cpm$elytra;

	@Override
	public BipedEntityModel<AbstractClientPlayerEntity> cpm$armorLegs() {
		return cpm$armorLegs;
	}

	@Override
	public BipedEntityModel<AbstractClientPlayerEntity> cpm$armorBody() {
		return cpm$armorBody;
	}

	@Override
	public ElytraEntityModel<AbstractClientPlayerEntity> cpm$elytra() {
		return cpm$elytra;
	}

	@Inject(at = @At("RETURN"), method = "reload(Lnet/minecraft/resource/ResourceManager;)V", locals = LocalCapture.CAPTURE_FAILHARD)
	public void reload(ResourceManager manager, CallbackInfo cbi, Context ctx) {
		cpm$elytra = new ElytraEntityModel<>(ctx.getModelLoader().getModelPart(EntityModelLayers.ELYTRA));
		cpm$armorLegs = new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_INNER_ARMOR));
		cpm$armorBody = new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR));
	}
}
