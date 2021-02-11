package com.tom.cpm.shared.config;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.AnimationHandler;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.util.Image;
import com.tom.cpm.shared.util.LegacySkinConverter;

public abstract class Player {
	private static boolean enableRendering = true;
	private static boolean enableNames = true;

	private ModelDefinition definition;
	private AnimationHandler animHandler = new AnimationHandler(this);
	public VanillaPose prevPose;
	public IPose currentPose;
	public String url;

	public CompletableFuture<Image> getSkin() {
		if(MinecraftObjectHolder.DEBUGGING && new File("skin_test.png").exists()) {
			return CompletableFuture.supplyAsync(() -> {
				try {
					return Image.loadFrom(new File("skin_test.png"));
				} catch (IOException e) {
					return null;
				}
			});
		}
		if(url == null)return CompletableFuture.completedFuture(null);
		return Image.download(url).thenApply(i -> new LegacySkinConverter().convertSkin(i)).exceptionally(e -> null);
	}

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
