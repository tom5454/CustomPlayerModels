package com.tom.cpm.shared.config;

import java.util.EnumMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.AnimationHandler;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.skin.PlayerTextureLoader;

public abstract class Player<P, M> {
	private static boolean enableRendering = true;
	private static boolean enableNames = true;

	private CompletableFuture<ModelDefinition> definition;
	private EnumMap<AnimationMode, AnimationHandler> animHandler = new EnumMap<>(AnimationMode.class);
	private PlayerTextureLoader textures;
	public VanillaPose prevPose;
	public IPose currentPose;
	public boolean forcedSkin;

	public PlayerTextureLoader getTextures() {
		if(textures == null)textures = initTextures();
		return textures;
	}

	public abstract SkinType getSkinType();
	protected abstract PlayerTextureLoader initTextures();
	public abstract UUID getUUID();
	public abstract VanillaPose getPose();
	public abstract int getEncodedGestureId();
	public abstract M getModel();
	public abstract void updateFromPlayer(P player);

	public void setModelDefinition(CompletableFuture<ModelDefinition> definition) {
		this.definition = definition;
	}

	public ModelDefinition getModelDefinition() {
		try {
			return enableRendering && definition != null ? definition.getNow(null) : null;
		} catch (Exception e) {
			return null;
		}
	}

	public ModelDefinition getAndResolveDefinition() {
		ModelDefinition def = getModelDefinition();
		if(def != null) {
			if(def.getResolveState() == 0)def.startResolve();
			else if(def.getResolveState() == 2) {
				if(def.doRender()) {
					return def;
				}
			}
		}
		return null;
	}

	public CompletableFuture<ModelDefinition> getDefinitionFuture() {
		return definition;
	}

	public AnimationHandler getAnimationHandler(AnimationMode mode) {
		return animHandler.computeIfAbsent(mode, k -> new AnimationHandler(this));
	}

	public static boolean isEnableRendering() {
		return enableRendering;
	}

	public static void setEnableRendering(boolean enableRendering) {
		Player.enableRendering = enableRendering;
	}

	public static boolean isEnableNames() {
		return enableNames;
	}

	public static void setEnableNames(boolean enableNames) {
		Player.enableNames = enableNames;
	}
}
