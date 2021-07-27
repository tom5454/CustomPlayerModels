package com.tom.cpm.shared.model;

import java.util.function.Function;

import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.render.ModelRenderManager;

public class RenderManager<G, P, M, D> {
	private Player<P, M> profile;
	private final ModelRenderManager<D, ?, ?, M> renderManager;
	private final ModelDefinitionLoader<G> loader;
	private Function<P, G> getProfile;

	public RenderManager(ModelRenderManager<D, ?, ?, M> renderManager,
			ModelDefinitionLoader<G> loader, Function<P, G> getProfile) {
		this.renderManager = renderManager;
		this.loader = loader;
		this.getProfile = getProfile;
	}

	@SuppressWarnings("unchecked")
	public boolean tryBindModel(G gprofile, P player, D buffer, M toBind, String arg, AnimationMode mode) {
		if(gprofile == null)gprofile = getProfile.apply(player);
		Player<P, M> profile = (Player<P, M>) loader.loadPlayer(gprofile);
		if(profile == null)return false;
		if(toBind == null)toBind = profile.getModel();
		ModelDefinition def = profile.getAndResolveDefinition();
		if(def != null) {
			this.profile = profile;
			if(player != null)
				profile.updateFromPlayer(player);
			renderManager.bindModel(toBind, arg, buffer, def, profile, mode);
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

	public void tryUnbind(M model) {
		renderManager.unbindModel(model);
	}

	@SuppressWarnings("unchecked")
	public void tryUnbindPlayer(P player) {
		G gprofile = getProfile.apply(player);
		Player<P, M> profile = (Player<P, M>) loader.loadPlayer(gprofile);
		if(profile == null)return;
		renderManager.unbindModel(profile.getModel());
	}

	public void bindHand(P player, D buffer) {
		tryBindModel(null, player, buffer, null, null, AnimationMode.HAND);
	}

	public void bindSkull(G profile, D buffer, M model) {
		Player<P, M> prev = this.profile;
		tryBindModel(profile, null, buffer, model, null, AnimationMode.SKULL);
		this.profile = prev;
	}

	public void bindPlayer(P player, D buffer) {
		tryBindModel(null, player, buffer, null, null, AnimationMode.PLAYER);
	}

	public void bindElytra(P player, D buffer, M model) {
		tryBindModel(null, player, buffer, model, null, AnimationMode.PLAYER);
	}

	public void bindArmor(P player, D buffer, M model, int layer) {
		tryBindModel(null, player, buffer, model, "armor" + layer, AnimationMode.PLAYER);
	}

	public void bindSkin(M model, TextureSheetType tex) {
		renderManager.bindSkin(model, null, tex);
	}

	public void bindSkin(TextureSheetType tex) {
		if(profile != null) {
			bindSkin(profile.getModel(), tex);
		}
	}

	public Player<P, M> getBoundPlayer() {
		return profile;
	}
}
