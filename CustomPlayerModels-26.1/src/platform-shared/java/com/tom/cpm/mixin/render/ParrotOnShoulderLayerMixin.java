package com.tom.cpm.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.animal.parrot.Parrot;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.client.PlayerRenderStateAccess;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.render.ItemTransform;

@Mixin(value = ParrotOnShoulderLayer.class, priority = 2000)
public class ParrotOnShoulderLayerMixin {

	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", shift = Shift.AFTER), method = "submitOnShoulder")
	public void onRender(
			PoseStack poseStack,
			SubmitNodeCollector p_432918_,
			int p_434486_,
			AvatarRenderState playerRenderState,
			Parrot.Variant p_435522_,
			float p_435558_,
			float p_433589_,
			boolean leftShoulderIn,
			CallbackInfo cbi) {
		Player<?> pl = ((PlayerRenderStateAccess) playerRenderState).cpm$getPlayer();
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
