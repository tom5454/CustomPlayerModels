package com.tom.cpm.client;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;

public interface LivingRendererAccess {
	void cpm$onGetRenderType(LivingEntity player, boolean pTranslucent, boolean pGlowing, CallbackInfoReturnable<RenderType> cbi);
}
