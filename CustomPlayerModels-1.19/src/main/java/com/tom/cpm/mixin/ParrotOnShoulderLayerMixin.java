package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.render.ItemTransform;

@Mixin(ParrotOnShoulderLayer.class)
public class ParrotOnShoulderLayerMixin {

	@Inject(at = @At("HEAD"),
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/player/Player;FFFFZ)V")
	public void onRenderPre(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, net.minecraft.world.entity.player.Player entitylivingbaseIn, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, boolean leftShoulderIn, CallbackInfo cbi) {
		matrixStackIn.pushPose();
		Player<?> pl = ClientProxy.INSTANCE.manager.getBoundPlayer();
		if(pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if(def != null) {
				ItemTransform tr = def.getTransform(leftShoulderIn ? ItemSlot.LEFT_SHOULDER : ItemSlot.RIGHT_SHOULDER);
				if(tr != null) {
					PlayerRenderManager.multiplyStacks(tr.getMatrix(), matrixStackIn);
					if(entitylivingbaseIn.isCrouching())
						matrixStackIn.translate(0, -0.2f, 0);
				}
			}
		}
	}

	@Inject(at = @At("RETURN"),
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/player/Player;FFFFZ)V")
	public void onRenderPost(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, net.minecraft.world.entity.player.Player entitylivingbaseIn, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, boolean leftShoulderIn, CallbackInfo cbi) {
		matrixStackIn.popPose();
	}
}
