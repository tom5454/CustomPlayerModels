package com.tom.cpm.client;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;

public interface LivingRendererAccess {
	default void cpm$onGetRenderType(LivingEntityRenderState player, boolean pTranslucent, boolean pGlowing, CallbackInfoReturnable<RenderType> cbi) {}
}
