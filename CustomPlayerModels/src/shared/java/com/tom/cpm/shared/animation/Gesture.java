package com.tom.cpm.shared.animation;

import java.util.List;

import com.tom.cpm.shared.MinecraftClientAccess;

public class Gesture implements IManualGesture {
	public final AnimationType type;
	public List<IAnimation> animation;
	public boolean isLoop;
	public String name;
	public byte defVal;
	private int order;
	public boolean isProperty, command, layerCtrl;
	public String group;

	public Gesture(AnimationType type, List<IAnimation> animation, String name, boolean isLoop, int order) {
		this.type = type;
		this.animation = animation;
		this.name = name;
		this.isLoop = isLoop;
		this.order = order;
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

	@Override
	public AnimationType getType() {
		return type;
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public boolean isProperty() {
		return isProperty;
	}

	@Override
	public boolean isCommand() {
		return command;
	}

	@Override
	public boolean isLayerControlled() {
		return layerCtrl;
	}
}