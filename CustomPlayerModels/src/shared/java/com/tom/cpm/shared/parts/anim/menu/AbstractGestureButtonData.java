package com.tom.cpm.shared.parts.anim.menu;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.parts.anim.AnimLoaderState;

public abstract class AbstractGestureButtonData {
	protected String name;
	protected ModelDefinition def;

	public static void parse(IOHelper block, AnimLoaderState state) throws IOException {
		GestureButtonType type = block.readEnum(GestureButtonType.values());
		AbstractGestureButtonData dt = type.create();
		dt.def = state.getDefinition();
		dt.parseData(block, state);
		state.addGestureButton(dt);
		//TODO conditions
	}

	protected void parseData(IOHelper block, AnimLoaderState state) throws IOException {
		name = block.readUTF();
	}

	public void write(IOHelper block) throws IOException {
		block.writeUTF(name);
	}

	public abstract GestureButtonType getType();

	public boolean isProperty() {
		return false;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDef(ModelDefinition def) {
		this.def = def;
	}

	public String getName() {
		return name;
	}

	public Set<CommandAction> getCommandActions() {
		return Collections.emptySet();
	}

	public static abstract class AbstractCommandTriggerableData extends AbstractGestureButtonData {
		public static final int LAYER_CTRL   = 1 << 0;
		public static final int COMMAND_CTRL = 1 << 1;
		public static final int PROPERTY     = 1 << 2;
		public static final int CONDITIONAL  = 1 << 3;//TODO
		public static final int HIDDEN       = 1 << 4;

		public boolean isProperty;
		public boolean command;
		public boolean layerCtrl;
		public boolean hidden;
		protected Set<CommandAction> commandActions = new HashSet<>();

		@Override
		protected void parseData(IOHelper block, AnimLoaderState state) throws IOException {
			super.parseData(block, state);
			int flags = block.read();
			command = (flags & COMMAND_CTRL) != 0;
			layerCtrl = (flags & LAYER_CTRL) != 0;
			isProperty = (flags & PROPERTY) != 0;
			hidden = (flags & HIDDEN) != 0;
		}

		@Override
		public void write(IOHelper block) throws IOException {
			super.write(block);
			int flags = 0;
			if (this.layerCtrl)flags |= LAYER_CTRL;
			if (this.command)flags |= COMMAND_CTRL;
			if (this.isProperty)flags |= PROPERTY;
			if (this.hidden)flags |= HIDDEN;
			block.write(flags);
		}

		@Override
		public boolean isProperty() {
			return isProperty;
		}

		@Override
		public boolean canShow() {
			return !command && !hidden;
		}

		@Override
		public Set<CommandAction> getCommandActions() {
			return commandActions;
		}
	}

	public boolean canShow() {
		return true;
	}

	public String getKeybindId() {
		return null;
	}

	public void loadFrom(ConfigEntry ce) {
	}

	public void storeTo(ConfigEntry ce) {
	}

	public void onRegistered() {
	}

	public ModelDefinition getDefinition() {
		return def;
	}

	public void onKeybind(String arg, boolean press, boolean toggleMode) {
	}
}
