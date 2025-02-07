package com.tom.cpm.shared.parts.anim.menu;

import java.util.Locale;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.config.PlayerData;

public interface CommandAction {
	String getName();
	void write(NBTTagCompound tag);
	ActionType getType();
	int getValue();
	void setValue(int value);
	int getMaxValue();
	boolean isCommandControlled();

	public static enum ActionType {
		SIMPLE(SimpleParameterValueAction.ServerAction::new),
		BITMASK(BitmaskParameterValueAction.ServerAction::new),
		VALUE(ValueParameterValueAction.ServerAction::new),
		;
		public ActionTypeFactory factory;

		private ActionType(ActionTypeFactory factory) {
			this.factory = factory;
		}

		public static ServerCommandAction make(NBTTagCompound tag, PlayerData data) {
			String t = tag.getString("type");
			for (ActionType at : values()) {
				if (at.name().equalsIgnoreCase(t)) {
					return at.factory.create(tag.getString("name"), tag, data);
				}
			}
			return null;
		}
	}

	public static interface ServerCommandAction {
		String getName();
		int getValue();
		boolean isCommandControlled();
	}

	public static interface LegacyCommandActionWriter {
		void writeLegacy(NBTTagCompound tag);
	}

	public static interface ActionTypeFactory {
		ServerCommandAction create(String name, NBTTagCompound tag, PlayerData data);
	}

	public static NBTTagCompound serialize(CommandAction action) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("type", action.getType().name().toLowerCase(Locale.ROOT));
		tag.setString("name", action.getName());
		action.write(tag);
		return tag;
	}
}
