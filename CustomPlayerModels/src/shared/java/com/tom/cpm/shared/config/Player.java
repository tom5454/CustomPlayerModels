package com.tom.cpm.shared.config;

import java.util.EnumMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.AnimationHandler;
import com.tom.cpm.shared.animation.AnimationState;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinition.ModelLoadingState;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.skin.PlayerTextureLoader;

public abstract class Player<P, M> {
	private static boolean enableRendering = true;
	private static boolean enableNames = true;

	private CompletableFuture<ModelDefinition> definition;
	private EnumMap<AnimationMode, AnimationHandler> animHandler = new EnumMap<>(AnimationMode.class);
	private PlayerTextureLoader textures;
	public AnimationState animState = new AnimationState();

	public VanillaPose prevPose;
	public IPose currentPose;

	public boolean forcedSkin;
	public boolean sentEventSubs;

	public PlayerTextureLoader getTextures() {
		if(textures == null) {
			textures = initTextures();
			textures.load();
		}
		return textures;
	}

	public abstract SkinType getSkinType();
	protected abstract PlayerTextureLoader initTextures();
	public abstract String getName();
	public abstract UUID getUUID();
	public abstract M getModel();
	public abstract void updateFromPlayer(P player);
	public abstract Object getGameProfile();
	public abstract void updateFromModel(Object model);

	public void setModelDefinition(CompletableFuture<ModelDefinition> definition) {
		this.definition = definition;
	}

	public ModelDefinition getModelDefinition0() {
		try {
			return enableRendering && definition != null ? definition.getNow(null) : null;
		} catch (Exception e) {
			return null;
		}
	}

	public ModelDefinition getModelDefinition() {
		ModelDefinition def = getModelDefinition0();
		if(def != null) {
			if(def.getResolveState() == ModelLoadingState.NEW)def.startResolve();
			else if(def.getResolveState() == ModelLoadingState.LOADED) {
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
		return animHandler.computeIfAbsent(mode, k -> new AnimationHandler(this::getModelDefinition));
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

	public void cleanup() {
		ModelDefinition def = getModelDefinition0();
		if(def != null)def.cleanup();
	}

	public boolean isClientPlayer() {
		return getUUID().equals(MinecraftClientAccess.get().getCurrentClientPlayer().getUUID());
	}

	public void sendEventSubs() {
		ModelDefinition def = getModelDefinition();
		if(!sentEventSubs && def != null) {
			sentEventSubs = true;
			MinecraftClientAccess.get().getNetHandler().sendEventSubs(def);
		}
	}
}
