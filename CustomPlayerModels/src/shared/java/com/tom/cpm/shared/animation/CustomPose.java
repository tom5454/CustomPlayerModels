package com.tom.cpm.shared.animation;

import java.util.function.Function;

public class CustomPose implements IPose {
	private String name;
	public CustomPose(String name) {
		this.name = name;
	}

	@Override
	public String getName(Function<String, String> i18n) {
		return name;
	}

	public String getName() {
		return name;
	}
}
