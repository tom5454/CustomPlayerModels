package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ShoulderParrotFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.render.ItemTransform;

@Mixin(value = ShoulderParrotFeatureRenderer.class, priority = 2000)
public class ShoulderParrotFeatureRendererMixin {

	@Inject(at = @At("HEAD"),
			method = "renderShoulderParrot(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/player/PlayerEntity;FFFFZ)V")
	public void onRenderPre(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, PlayerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, boolean leftShoulderIn, CallbackInfo cbi) {
		matrixStackIn.push();
		Player<?> pl = CustomPlayerModelsClient.INSTANCE.manager.getBoundPlayer();
		if(pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if(def != null) {
				ItemTransform tr = def.getTransform(leftShoulderIn ? ItemSlot.LEFT_SHOULDER : ItemSlot.RIGHT_SHOULDER);
				if(tr != null) {
					PlayerRenderManager.multiplyStacks(tr.getMatrix(), matrixStackIn);
					if(entitylivingbaseIn.isSneaking())
						matrixStackIn.translate(0, -0.2f, 0);
				}
			}
		}
	}

	@Inject(at = @At("RETURN"),
			method = "renderShoulderParrot(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/player/PlayerEntity;FFFFZ)V")
	public void onRenderPost(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, PlayerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, boolean leftShoulderIn, CallbackInfo cbi) {
		matrixStackIn.pop();
	}
}
