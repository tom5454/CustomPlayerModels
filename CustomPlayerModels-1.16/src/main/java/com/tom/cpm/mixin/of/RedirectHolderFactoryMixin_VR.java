package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.render.VRPlayerModel;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;

import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.client.vr.RedirectHolderVRPlayer;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectHolder;

@Mixin(value = PlayerRenderManager.class, remap = false)
public class RedirectHolderFactoryMixin_VR {

	@SuppressWarnings("unchecked")
	@Inject(at = @At("HEAD"), method = "mixin(Ljava/lang/Object;Ljava/lang/String;)Lcom/tom/cpm/shared/model/render/ModelRenderManager$RedirectHolder;", cancellable = true)
	public void onCreate(Object model, String arg, CallbackInfoReturnable<RedirectHolder> cbi) {
		if(model instanceof VRPlayerModel) {
			cbi.setReturnValue(new RedirectHolderVRPlayer((PlayerRenderManager) (Object) this, (VRPlayerModel<AbstractClientPlayerEntity>) model));
		}
	}
}
