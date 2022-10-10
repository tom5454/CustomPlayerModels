package com.tom.cpm.shared.animation;

import java.util.List;

import com.tom.cpm.shared.MinecraftClientAccess;

public class Gesture implements IManualGesture {
	public static final String LAYER_PREFIX = "$layer$";
	public static final String VALUE_LAYER_PREFIX = "$value$";
	public List<IAnimation> animation;
	public boolean isLoop;
	public String name;
	public byte defVal;

	public Gesture(List<IAnimation> animation, String name, boolean isLoop) {
		this.animation = animation;
		this.name = name;
		this.isLoop = isLoop;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getGestureId() {
		return "g" + name;
	}

	@Override
	public void play(AnimationRegistry reg) {
		MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().playGesture(reg, this);
	}
}