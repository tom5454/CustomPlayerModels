package com.tom.cpm.shared.parts.anim.menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.parts.anim.AnimLoaderState;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData.AbstractCommandTriggerableData;

public class DropdownButtonData extends AbstractCommandTriggerableData {
	public int parameter;
	public List<String> opts = new ArrayList<>();

	@Override
	protected void parseData(IOHelper block, AnimLoaderState state) throws IOException {
		super.parseData(block, state);
		parameter = block.readVarInt();
		int options = block.readByte();
		opts = new ArrayList<>();
		for (int i = 0;i<options;i++) {
			String opt = block.readUTF();
			opts.add(opt);
		}
		//TODO conditions
	}

	@Override
	public void onRegistered() {
		super.onRegistered();
		for (int i = 0; i < opts.size(); i++) {
			String opt = opts.get(i);
			commandActions.add(new SimpleParameterValueAction(opt, parameter, i, command));
		}
		commandActions.add(new ValueParameterValueAction(name, parameter, command, opts.size() - 1));
	}

	@Override
	public GestureButtonType getType() {
		return GestureButtonType.DROPDOWN;
	}

	@Override
	public void write(IOHelper block) throws IOException {
		super.write(block);
		block.writeVarInt(parameter);
		block.writeByte(opts.size());
		for (String string : opts) {
			block.writeUTF(string);
		}
	}

	public int add(String id) {
		int i = opts.size();
		opts.add(id);
		return i;
	}

	public List<String> getActiveOptions() {
		return opts;
	}

	public void set(String selected) {
		AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
		int i = opts.indexOf(selected);
		an.setGestureValue(parameter, i == -1 ? 0 : i);
	}

	public String get() {
		AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
		int val = Byte.toUnsignedInt(an.getGestureValue(parameter));
		return opts.size() > val && val >= 0 ? opts.get(val) : "";
	}

	@Override
	public String getKeybindId() {
		return "d" + name;
	}

	@Override
	public void storeTo(ConfigEntry ce) {
		ce.setString(name, get());
	}

	@Override
	public void loadFrom(ConfigEntry ce) {
		int val = Byte.toUnsignedInt(def.getAnimations().getParams().getDefaultParam(parameter));
		set(ce.getString(name, opts.size() > val && val >= 0 ? opts.get(val) : ""));
	}
}
