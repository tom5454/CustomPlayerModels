package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.entity.animal.Parrot;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.render.ItemTransform;

@Mixin(value = ParrotOnShoulderLayer.class, priority = 2000)
public class ParrotOnShoulderLayerMixin {

	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", shift = Shift.AFTER), method = "renderOnShoulder")
	public void onRender(final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i,
			final PlayerRenderState playerRenderState, final Parrot.Variant variant, final float f, final float g,
			final boolean leftShoulderIn, CallbackInfo cbi) {
		Player<?> pl = CustomPlayerModelsClient.INSTANCE.manager.getBoundPlayer();
		if(pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if(def != null) {
				ItemTransform tr = def.getTransform(leftShoulderIn ? ItemSlot.LEFT_SHOULDER : ItemSlot.RIGHT_SHOULDER);
				if(tr != null) {
					PlayerRenderManager.multiplyStacks(tr.getMatrix(), poseStack);
					if(playerRenderState.isCrouching)
						poseStack.translate(0, -0.2f, 0);
				}
			}
		}
	}
}
