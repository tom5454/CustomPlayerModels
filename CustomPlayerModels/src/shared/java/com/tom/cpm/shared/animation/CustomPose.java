package com.tom.cpm.shared.animation;

import com.tom.cpl.gui.IGui;
import com.tom.cpm.shared.MinecraftClientAccess;

public class CustomPose implements IPose, IManualGesture {
	private String name;
	public int order;
	public boolean command;

	public CustomPose(String name, int order) {
		this.name = name;
		this.order = order;
	}

	@Override
	public String getName(IGui gui, String display) {
		return name;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getId() {
		int i = name.indexOf('#');
		if(i == -1)return name;
		if(i == 0)return "";
		return name.substring(0, i);
	}

	@Override
	public String toString() {
		return "Custom Pose: " + name;
	}

	@Override
	public String getGestureId() {
		return "p" + name;
	}

	@Override
	public void play(AnimationRegistry reg) {
		MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().setCustomPose(reg, this);
	}

	@Override
	public AnimationType getType() {
		return AnimationType.CUSTOM_POSE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CustomPose other = (CustomPose) obj;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		return true;
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public boolean isCommand() {
		return command;
	}
}
