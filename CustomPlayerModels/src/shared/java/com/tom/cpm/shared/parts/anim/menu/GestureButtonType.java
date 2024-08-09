package com.tom.cpm.shared.parts.anim.menu;

import java.util.function.Supplier;

public enum GestureButtonType {
	POSE(CustomPoseGestureButtonData::pose),
	GESTURE(CustomPoseGestureButtonData::gesture),
	BOOL_PARAMETER_TOGGLE(BoolParameterToggleButtonData::new),
	VALUE_PARAMETER_SLIDER(ValueParameterButtonData::new),
	DROPDOWN(DropdownButtonData::new),
	//SUBMENU(null), //TODO
	;
	private final Supplier<AbstractGestureButtonData> factory;

	private GestureButtonType(Supplier<AbstractGestureButtonData> factory) {
		this.factory = factory;
	}

	public AbstractGestureButtonData create() {
		return factory.get();
	}
}