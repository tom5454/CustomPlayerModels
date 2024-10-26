package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;

@Mixin(HumanoidModel.class)
public interface HumanoidModelAccessor {

	@Invoker
	HumanoidModel.ArmPose callGetArmPose(final HumanoidRenderState playerRenderState, final HumanoidArm humanoidArm);
}
