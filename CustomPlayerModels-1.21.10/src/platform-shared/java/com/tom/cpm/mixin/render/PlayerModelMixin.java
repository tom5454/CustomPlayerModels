package com.tom.cpm.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

import com.tom.cpm.client.PlayerRenderStateAccess;

@Mixin(value = PlayerModel.class, priority = 999999)
public class PlayerModelMixin {

	@Inject(at = @At("RETURN"), method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V")
	public void onSetupAnim(AvatarRenderState state, CallbackInfo cbi) {
		((PlayerRenderStateAccess) state).cpm$loadState((PlayerModel) (Object) this);
	}
}
