package com.tom.cpm.shared.parts.anim.menu;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.parts.anim.menu.CommandAction.LegacyCommandActionWriter;

public class SimpleParameterValueAction implements CommandAction, LegacyCommandActionWriter {
	private final String name;
	public int parameter;
	public int value;
	private boolean cc;

	public SimpleParameterValueAction(String name, int parameter, int value, boolean cc) {
		this.name = name;
		this.parameter = parameter;
		this.value = value;
		this.cc = cc;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void write(NBTTagCompound tag) {
		tag.setInteger("param", parameter);
		tag.setByte("value", (byte) value);
		tag.setBoolean("cc", cc);
	}

	@Override
	public void writeLegacy(NBTTagCompound tag) {
		tag.setString("name", name);
		tag.setByte("id", (byte) parameter);
		tag.setByte("type", (byte) ((cc ? 16 : 0) | (parameter > 2 ? 2 : parameter)));
	}

	@Override
	public ActionType getType() {
		return ActionType.SIMPLE;
	}

	@Override
	public int getValue() {
		AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
		return an.getGestureValue(parameter) == value ? 1 : 0;
	}

	@Override
	public void setValue(int v) {
		AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
		if ((v == -1 && an.getGestureValue(parameter) == value) || v == 0)
			an.setGestureValue(parameter, 0);
		else
			an.setGestureValue(parameter, value);
	}

	@Override
	public int getMaxValue() {
		return 1;
	}

	@Override
	public boolean isCommandControlled() {
		return cc;
	}

	public static class ServerAction implements ServerCommandAction {
		private PlayerData data;
		private String name;
		private int parameter;
		private int value;
		private boolean cc;

		public ServerAction(String name, NBTTagCompound tag, PlayerData data) {
			this.name = name;
			this.data = data;
			parameter = tag.getInteger("param");
			value = tag.getByte("value");
			cc = tag.getBoolean("cc");
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getValue() {
			if (parameter < 0 || data.gestureData.length <= parameter)return -1;
			return data.gestureData[parameter] == value ? 1 : 0;
		}

		@Override
		public boolean isCommandControlled() {
			return cc;
		}
	}
}
