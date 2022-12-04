package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.render.VRPlayerModel;
import org.vivecraft.render.VRPlayerModel_WithArms;

import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;

import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectRenderer;

@Mixin(VRPlayerModel_WithArms.class)
public class VRPlayerModelWithArmsMixin_VR<T extends LivingEntity> extends VRPlayerModel<T> {

	public VRPlayerModelWithArmsMixin_VR(ModelPart pRoot, boolean pSlim) {
		super(pRoot, pSlim);
	}

	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"), method = {
			"setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
			"method_17087(Lnet/minecraft/class_1309;FFFFF)V"
	}, cancellable = true, remap = false)
	public void noCopy(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo cbi) {
		if(leftSleeve instanceof RedirectRenderer)
			cbi.cancel();
	}
}
