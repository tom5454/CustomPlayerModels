package com.tom.cpm.shared.animation;

import com.tom.cpl.gui.IGui;

public class CustomPose implements IPose {
	private String name;
	public CustomPose(String name) {
		this.name = name;
	}

	@Override
	public String getName(IGui gui, String display) {
		return name;
	}

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
}
