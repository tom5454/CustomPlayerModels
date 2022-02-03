package com.tom.cpm.shared.model;

import java.util.function.Function;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

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
	private Function<G, String> getSkullModel;
	private Function<G, String> getTexture;

	public RenderManager(ModelRenderManager<D, ?, ?, M> renderManager,
			ModelDefinitionLoader<G> loader, Function<P, G> getProfile) {
		this.renderManager = renderManager;
		this.loader = loader;
		this.getProfile = getProfile;
	}

	@SuppressWarnings("unchecked")
	public boolean tryBindModel(G gprofile, P player, D buffer, M toBind, String arg, String unique, AnimationMode mode) {
		if(gprofile == null)gprofile = getProfile.apply(player);
		Player<P, M> profile = (Player<P, M>) loader.loadPlayer(gprofile, unique);
		if(profile == null)return false;
		if(toBind == null)toBind = profile.getModel();
		ModelDefinition def = profile.getModelDefinition();
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

	public void unbindClear() {
		if(profile != null) {
			renderManager.unbindModel(profile.getModel());
			profile = null;
		}
	}

	public void unbindClearPlayer(P player) {
		unbindPlayer(player);
		clearBoundPlayer();
	}

	public void unbindClear(M model) {
		unbind(model);
		clearBoundPlayer();
	}

	public void unbind(M model) {
		renderManager.unbindModel(model);
	}

	@SuppressWarnings("unchecked")
	public void unbindPlayer(P player) {
		G gprofile = getProfile.apply(player);
		Player<P, M> profile = (Player<P, M>) loader.loadPlayer(gprofile, ModelDefinitionLoader.PLAYER_UNIQUE);
		if(profile == null)return;
		renderManager.unbindModel(profile.getModel());
	}

	public void bindHand(P player, D buffer) {
		tryBindModel(null, player, buffer, null, null, ModelDefinitionLoader.PLAYER_UNIQUE, AnimationMode.HAND);
	}

	public void bindHand(P player, D buffer, M model) {
		tryBindModel(null, player, buffer, model, null, ModelDefinitionLoader.PLAYER_UNIQUE, AnimationMode.HAND);
	}

	public void bindSkull(G profile, D buffer, M model) {
		Player<P, M> prev = this.profile;
		String unique;
		if(getSkullModel == null)unique = ModelDefinitionLoader.SKULL_UNIQUE;
		else {
			unique = getSkullModel.apply(profile);
			if(unique == null) {
				if(getTexture == null)unique = ModelDefinitionLoader.SKULL_UNIQUE;
				else unique = getTexture.apply(profile);
				if(unique == null)unique = ModelDefinitionLoader.SKULL_UNIQUE;
				else unique = "skull_tex:" + unique;
			}
			else unique = "model:" + unique;
		}
		tryBindModel(profile, null, buffer, model, null, unique, AnimationMode.SKULL);
		this.profile = prev;
	}

	public void bindPlayer(P player, D buffer) {
		tryBindModel(null, player, buffer, null, null, ModelDefinitionLoader.PLAYER_UNIQUE, AnimationMode.PLAYER);
	}

	public void bindPlayer(P player, D buffer, M model) {
		tryBindModel(null, player, buffer, model, null, ModelDefinitionLoader.PLAYER_UNIQUE, AnimationMode.PLAYER);
	}

	public void bindElytra(P player, D buffer, M model) {
		tryBindModel(null, player, buffer, model, null, ModelDefinitionLoader.PLAYER_UNIQUE, AnimationMode.PLAYER);
	}

	public void bindArmor(P player, D buffer, M model, int layer) {
		tryBindModel(null, player, buffer, model, "armor" + layer, ModelDefinitionLoader.PLAYER_UNIQUE, AnimationMode.PLAYER);
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

	public void clearBoundPlayer() {
		profile = null;
	}

	public void setGetSkullModel(Function<G, String> getSkullModel) {
		this.getSkullModel = getSkullModel;
	}

	public void setGetTexture(Function<G, String> getTexture) {
		this.getTexture = getTexture;
	}

	public <PR> void setGPGetters(Function<G, Multimap<String, PR>> getMap, Function<PR, String> getValue) {
		setGetSkullModel(profile -> {
			PR property = Iterables.getFirst(getMap.apply(profile).get("cpm:model"), null);
			if(property != null)return getValue.apply(property);
			return null;
		});
		setGetTexture(profile -> {
			PR property = Iterables.getFirst(getMap.apply(profile).get("textures"), null);
			if(property != null)return getValue.apply(property);
			return null;
		});
	}

	@SuppressWarnings("unchecked")
	public void jump(P player) {
		G gprofile = getProfile.apply(player);
		Player<P, M> profile = (Player<P, M>) loader.loadPlayer(gprofile, ModelDefinitionLoader.PLAYER_UNIQUE);
		if(profile == null)return;
		profile.animState.jump();
	}
}
