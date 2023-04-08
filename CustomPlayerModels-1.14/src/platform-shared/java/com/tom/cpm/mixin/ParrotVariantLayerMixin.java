package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.layers.ParrotVariantLayer;
import net.minecraft.entity.player.PlayerEntity;

import com.mojang.blaze3d.platform.GlStateManager;

import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.render.ItemTransform;

@Mixin(ParrotVariantLayer.class)
public class ParrotVariantLayerMixin {

	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/entity/player/PlayerEntity;FFFFFFZ)V")
	public void onRenderPre(PlayerEntity entitylivingbaseIn, float f1, float f2, float f3, float f4, float f5, float f6, boolean leftShoulderIn, CallbackInfo cbi) {
		GlStateManager.pushMatrix();
		Player<?> pl = CustomPlayerModelsClient.INSTANCE.manager.getBoundPlayer();
		if(pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if(def != null) {
				ItemTransform tr = def.getTransform(leftShoulderIn ? ItemSlot.LEFT_SHOULDER : ItemSlot.RIGHT_SHOULDER);
				if(tr != null) {
					PlayerRenderManager.multiplyStacks(tr.getMatrix());
					if(entitylivingbaseIn.isVisuallySneaking())
						GlStateManager.translatef(0, -0.2f, 0);
				}
			}
		}
	}

	@Inject(at = @At("RETURN"), method = "render(Lnet/minecraft/entity/player/PlayerEntity;FFFFFFZ)V")
	public void onRenderPost(PlayerEntity entitylivingbaseIn, float f1, float f2, float f3, float f4, float f5, float f6, boolean leftShoulderIn, CallbackInfo cbi) {
		GlStateManager.popMatrix();
	}
}
