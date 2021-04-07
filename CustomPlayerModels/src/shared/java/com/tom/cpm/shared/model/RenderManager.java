package com.tom.cpm.shared.model;

import java.util.function.Function;
import java.util.function.Predicate;

import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;

public class RenderManager<G, P, M, D> {
	private Player<P, M> profile;
	private final ModelRenderManager<D, ?, ?, M> renderManager;
	private final ModelDefinitionLoader loader;
	private Function<P, G> getProfile;

	public RenderManager(ModelRenderManager<D, ?, ?, M> renderManager,
			ModelDefinitionLoader loader, Function<P, G> getProfile) {
		this.renderManager = renderManager;
		this.loader = loader;
		this.getProfile = getProfile;
	}

	@SuppressWarnings("unchecked")
	public boolean tryBindModel(G gprofile, P player, D buffer, Predicate<Object> unbindRule, M toBind, AnimationMode mode) {
		if(gprofile == null)gprofile = getProfile.apply(player);
		Player<P, M> profile = (Player<P, M>) loader.loadPlayer(gprofile);
		if(profile == null)return false;
		if(toBind == null)toBind = profile.getModel();
		ModelDefinition def = profile.getAndResolveDefinition();
		if(def != null) {
			this.profile = profile;
			if(player != null)
				profile.updateFromPlayer(player);
			renderManager.bindModel(toBind, buffer, def, unbindRule, profile);
			if(unbindRule == null || player == null)
				renderManager.getAnimationEngine().handleAnimation(profile, mode);
			return true;
		}
		renderManager.unbindModel(toBind);
		return false;
	}

	public void tryUnbind() {
		if(profile != null) {
			renderManager.unbindModel(profile.getModel());
			profile = null;
		}
	}

	public void bindHand(P player, D buffer, Predicate<Object> unbindRule) {
		tryBindModel(null, player, buffer, unbindRule, null, AnimationMode.PLAYER);
		this.profile = null;
	}

	public void bindSkull(G profile, D buffer, Predicate<Object> unbindRule, M model) {
		Player<P, M> prev = this.profile;
		tryBindModel(profile, null, buffer, unbindRule, model, AnimationMode.SKULL);
		this.profile = prev;
	}

	public void bindPlayer(P player, D buffer) {
		tryBindModel(null, player, buffer, null, null, AnimationMode.PLAYER);
	}
}
