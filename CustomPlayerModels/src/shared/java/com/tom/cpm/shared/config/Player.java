package com.tom.cpm.shared.config;

import java.awt.image.BufferedImage;
import java.util.UUID;

import com.tom.cpm.shared.animation.AnimationHandler;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.definition.ModelDefinition;

public abstract class Player {
	private static boolean enableRendering = true;
	private static boolean enableNames = true;

	private ModelDefinition definition;
	private AnimationHandler animHandler = new AnimationHandler(this);
	public VanillaPose prevPose;
	public IPose currentPose;

	public abstract BufferedImage getSkin();
	public abstract int getSkinType();
	public abstract void loadSkin(Runnable onLoaded);
	public abstract UUID getUUID();
	public abstract VanillaPose getPose();
	public abstract int getEncodedGestureId();

	public void setModelDefinition(ModelDefinition definition) {
		this.definition = definition;
	}

	public ModelDefinition getModelDefinition() {
		return enableRendering ? definition : null;
	}

	public AnimationHandler getAnimationHandler() {
		return animHandler;
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
