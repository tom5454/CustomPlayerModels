package com.tom.cpm.client.vr;

import org.vivecraft.render.VRPlayerModel;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ModelRenderer;

import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.animation.AnimationState.VRState;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectHolder;

public class VRPlayerRenderer {

	public static boolean isFPHand;

	public static boolean isVRPlayer(Object model) {
		return model instanceof VRPlayerModel;
	}

	public static RedirectHolder<?, IRenderTypeBuffer, ModelTexture, ModelRenderer> createVRPlayer(PlayerRenderManager mngr, Object model) {
		return new RedirectHolderVRPlayer(mngr, (VRPlayerModel<AbstractClientPlayerEntity>) model);
	}

	public static VRState getVRState(Object model) {
		if(isFPHand)return VRState.FIRST_PERSON;
		if(model instanceof VRPlayerModel) {
			VRPlayerModel m = (VRPlayerModel) model;
			return m.seated ? VRState.THIRD_PERSON_SITTING : VRState.THIRD_PERSON_STANDING;
		}
		return null;
	}
}
