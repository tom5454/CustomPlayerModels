package com.tom.cpm.shared.retro;

import com.tom.cpm.api.IClientAPI.LocalModel;
import com.tom.cpm.api.IClientAPI.PlayerRenderer;
import com.tom.cpm.api.IClientAPI.RetroPlayerRenderer;
import com.tom.cpm.api.IClientAPI.SubModelType;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.AnimationState;

public class RetroPlayerRendererImpl<M, GP> implements RetroPlayerRenderer<M, GP> {
	private PlayerRenderer<M, Void, Void, Void, GP> pr;
	public RetroPlayerRendererImpl(PlayerRenderer<M, Void, Void, Void, GP> pr) {
		this.pr = pr;
	}

	@Override
	public void setGameProfile(GP profile) {
		pr.setGameProfile(profile);
	}

	@Override
	public AnimationState getAnimationState() {
		return pr.getAnimationState();
	}

	@Override
	public void preRender(AnimationMode renderMode) {
		pr.preRender(null, renderMode);
	}

	@Override
	public void prepareSubModel(M model, SubModelType type) {
		pr.prepareSubModel(model, type, null);
	}

	@Override
	public void postRender() {
		pr.postRender();
	}

	@Override
	public void setLocalModel(LocalModel model) {
		pr.setLocalModel(model);
	}

	@Override
	public void setRenderModel(M model) {
		pr.setRenderModel(model);
	}

	@Override
	public void setActivePose(String pose) {
		pr.setActivePose(pose);
	}

	@Override
	public void setActiveGesture(String gesture) {
		pr.setActiveGesture(gesture);
	}
}
