package com.tom.cpm.shared.network;

import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;

public enum ModelEventType {
	FALLING(VanillaPose.FALLING),
	CREATIVE_FLYING(VanillaPose.CREATIVE_FLYING),
	JUMPING(VanillaPose.JUMPING),
	;
	public static final ModelEventType[] VALUES = values();
	private final String name;
	private final VanillaPose pose;

	private ModelEventType(VanillaPose pose) {
		name = name().toLowerCase();
		this.pose = pose;
	}

	public static <T extends Enum<T>> ModelEventType of(String value) {
		for (int i = 0; i < VALUES.length; i++) {
			ModelEventType type = VALUES[i];
			if(type.name.equalsIgnoreCase(value))
				return type;
		}
		return null;
	}

	public static ModelEventType getType(IPose value) {
		for (int i = 0; i < VALUES.length; i++) {
			ModelEventType type = VALUES[i];
			if(type.pose == value)
				return type;
		}
		return null;
	}

	public String getName() {
		return name;
	}
}
