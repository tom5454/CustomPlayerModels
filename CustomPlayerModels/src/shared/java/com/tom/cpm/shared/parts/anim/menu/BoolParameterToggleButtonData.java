package com.tom.cpm.shared.parts.anim.menu;

import java.io.IOException;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.parts.anim.AnimLoaderState;
import com.tom.cpm.shared.parts.anim.ParameterDetails.ParameterAllocator.BitInfo;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData.AbstractCommandTriggerableData;

public class BoolParameterToggleButtonData extends AbstractCommandTriggerableData {
	public int parameter;
	public int mask;
	private BitmaskParameterValueAction action;

	@Override
	protected void parseData(IOHelper block, AnimLoaderState state) throws IOException {
		super.parseData(block, state);
		parameter = block.readVarInt();
		mask = block.readUnsignedByte();
	}

	@Override
	public void onRegistered() {
		super.onRegistered();
		commandActions.add(action = new BitmaskParameterValueAction(name, parameter, mask, command));
	}

	@Override
	public GestureButtonType getType() {
		return GestureButtonType.BOOL_PARAMETER_TOGGLE;
	}

	@Override
	public void write(IOHelper block) throws IOException {
		super.write(block);
		block.writeVarInt(parameter);
		block.write(mask);
	}

	public void setInfo(BitInfo info) {
		this.parameter = info.param;
		this.mask = info.mask;
	}

	public void toggle() {
		action.setValue(-1);
	}

	public boolean getValue() {
		return action.getValue() > 0;
	}

	@Override
	public void onKeybind(String arg, boolean press, boolean toggleMode) {
		if (toggleMode)action.setValue(-1);
		else action.setValue(press ? 1 : 0);
	}

	@Override
	public String getKeybindId() {
		return "l" + name;
	}

	@Override
	public void storeTo(ConfigEntry ce) {
		ce.setFloat(name, action.getValue());
	}

	@Override
	public void loadFrom(ConfigEntry ce) {
		action.setValue(ce.getFloat(name, (def.getAnimations().getParams().getDefaultParam(parameter) & mask) == mask ? 1 : 0) > 0.5f ? 1 : 0);
	}
}
