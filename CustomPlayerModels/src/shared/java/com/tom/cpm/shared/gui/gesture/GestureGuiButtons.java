package com.tom.cpm.shared.gui.gesture;

import java.util.function.BiFunction;

import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.BoolParameterToggleButtonData;
import com.tom.cpm.shared.parts.anim.menu.CustomPoseGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.DropdownButtonData;
import com.tom.cpm.shared.parts.anim.menu.GestureButtonType;
import com.tom.cpm.shared.parts.anim.menu.ValueParameterButtonData;

public enum GestureGuiButtons {
	POSE(GestureButtonType.POSE, CustomPoseGestureButtonData.class, CustomPoseGestureButton::new),
	GESTURE(GestureButtonType.GESTURE, CustomPoseGestureButtonData.class, CustomPoseGestureButton::new),
	BOOL_PARAMETER_TOGGLE(GestureButtonType.BOOL_PARAMETER_TOGGLE, BoolParameterToggleButtonData.class, GestureToggleButton::new),
	VALUE_PARAMETER_SLIDER(GestureButtonType.VALUE_PARAMETER_SLIDER, ValueParameterButtonData.class, GestureValueSlider::new),
	DROPDOWN(GestureButtonType.DROPDOWN, DropdownButtonData.class, GestureDropdown::new),
	;
	private final GestureButtonType type;
	private final Factory<AbstractGestureButtonData, ? extends GuiElement> factory;

	@SuppressWarnings("unchecked")
	private <T extends AbstractGestureButtonData> GestureGuiButtons(GestureButtonType type, Class<T> data, Factory<T, ? extends GuiElement> factory) {
		this.type = type;
		this.factory = (Factory<AbstractGestureButtonData, ? extends GuiElement>) factory;
	}

	public static GuiElement make(IGestureButtonContainer gui, AbstractGestureButtonData data) {
		for (GestureGuiButtons e : values()) {
			if (e.type == data.getType()) {
				return e.factory.apply(gui, data);
			}
		}
		return null;
	}

	@FunctionalInterface
	public static interface Factory<T extends AbstractGestureButtonData, E extends GuiElement & IGestureButton> extends BiFunction<IGestureButtonContainer, T, E> {
	}
}
