package com.tom.cpm.client.vr;

import org.vivecraft.render.VRPlayerModel;
import org.vivecraft.render.VRPlayerModel_WithArms;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;

import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.AnimationState.VRState;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectHolder;

public class VRPlayerRenderer {

	public static boolean isVRPlayer(Object model) {
		return model instanceof VRPlayerModel;
	}

	public static RedirectHolder<?, MultiBufferSource, ModelTexture, ModelPart> createVRPlayer(PlayerRenderManager mngr, Object model) {
		return new RedirectHolderVRPlayer(mngr, (VRPlayerModel<AbstractClientPlayer>) model);
	}

	public static VRState getVRState(AnimationMode mode, Object model) {
		if(mode == AnimationMode.HAND)return VRState.FIRST_PERSON;
		if(model instanceof VRPlayerModel_WithArms)return VRState.THIRD_PERSON_STANDING;
		if(model instanceof VRPlayerModel)return VRState.THIRD_PERSON_SITTING;
		return null;
	}
}
