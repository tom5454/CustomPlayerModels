package com.tom.cpm.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.render.VRPlayerModel;
import org.vivecraft.render.VRPlayerModel_WithArms;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectRenderer;

@Mixin(VRPlayerModel_WithArms.class)
public class VRPlayerModelWithArmsMixin_$vivecraft<T extends LivingEntity> extends VRPlayerModel<T> {

	public VRPlayerModelWithArmsMixin_$vivecraft(ModelPart pRoot, boolean pSlim) {
		super(pRoot, pSlim);
	}

	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"), method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", cancellable = true)
	public void noCopy(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo cbi) {
		if(leftSleeve instanceof RedirectRenderer)
			cbi.cancel();
	}
}
