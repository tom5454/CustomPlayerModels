package com.tom.cpm.shared.parts.anim.menu;

import java.io.IOException;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.parts.anim.AnimLoaderState;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData.AbstractCommandTriggerableData;

public class ValueParameterButtonData extends AbstractCommandTriggerableData {
	public int parameter;
	public int maxValue;
	private ValueParameterValueAction action;

	@Override
	protected void parseData(IOHelper block, AnimLoaderState state) throws IOException {
		super.parseData(block, state);
		parameter = block.readVarInt();
		maxValue = block.read();
	}

	@Override
	public void onRegistered() {
		super.onRegistered();
		commandActions.add(action = new ValueParameterValueAction(name, parameter, command, maxValue));
	}

	@Override
	public GestureButtonType getType() {
		return GestureButtonType.VALUE_PARAMETER_SLIDER;
	}

	@Override
	public void write(IOHelper block) throws IOException {
		super.write(block);
		block.writeVarInt(parameter);
		block.writeByte(maxValue);
	}

	public void setValue(float value) {
		action.setValue((int) (value * maxValue));
	}

	public float getValue() {
		return action.getValue() / (float) maxValue;
	}

	public float getDefaultValue() {
		return Byte.toUnsignedInt(def.getAnimations().getParams().getDefaultParam(parameter)) / 255f;
	}

	@Override
	public void storeTo(ConfigEntry ce) {
		ce.setFloat(name, action.getValue() / (float) maxValue);
	}

	@Override
	public void loadFrom(ConfigEntry ce) {
		setValue(ce.getFloat(name, getDefaultValue()));
	}
}
