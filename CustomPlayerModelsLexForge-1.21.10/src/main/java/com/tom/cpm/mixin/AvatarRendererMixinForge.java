package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CPMOrderedSubmitNodeCollector.CPMSubmitNodeCollector;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixinForge {

	@Inject(at = @At("HEAD"), target = @Desc(value = "submit", args = {AvatarRenderState.class, PoseStack.class, SubmitNodeCollector.class, CameraRenderState.class}))
	public void onSubmit(AvatarRenderState p_433493_, PoseStack p_434615_, SubmitNodeCollector p_433768_, CameraRenderState p_450931_, CallbackInfo cbi, @Local LocalRef<SubmitNodeCollector> snc) {
		snc.set(new CPMSubmitNodeCollector(p_433768_));
	}
}
