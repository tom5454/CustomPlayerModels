package com.tom.cpm.client.vr;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ModelRenderer;

import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.animation.AnimationState.VRState;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectHolder;

public class VRPlayerRenderer {

	public static <M> boolean isVRPlayer(M model) {
		return false;
	}

	public static <M> RedirectHolder<?, IRenderTypeBuffer, ModelTexture, ModelRenderer> createVRPlayer(
			PlayerRenderManager playerRenderManager, M model) {
		throw new UnsupportedOperationException();
	}

	public static VRState getVRState(Object model) {
		return VRState.FIRST_PERSON;
	}
}
