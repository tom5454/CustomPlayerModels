package com.tom.cpm.shared.parts.anim.menu;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.parts.anim.menu.CommandAction.LegacyCommandActionWriter;

public class ValueParameterValueAction implements CommandAction, LegacyCommandActionWriter {
	private final String name;
	private int parameter;
	private int max;
	private boolean cc;

	public ValueParameterValueAction(String name, int parameter, boolean cc, int max) {
		this.name = name;
		this.parameter = parameter;
		this.cc = cc;
		this.max = max;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void write(NBTTagCompound tag) {
		tag.setInteger("param", parameter);
		tag.setBoolean("cc", cc);
		tag.setInteger("max", max);
	}

	@Override
	public ActionType getType() {
		return ActionType.VALUE;
	}

	@Override
	public int getValue() {
		AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
		return Byte.toUnsignedInt(an.getGestureValue(parameter)) * max / 255;
	}

	@Override
	public void setValue(int v) {
		AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
		an.setGestureValue(parameter, v * 255 / max);
	}

	@Override
	public boolean isCommandControlled() {
		return cc;
	}

	public static class ServerAction implements ServerCommandAction {
		private PlayerData data;
		private String name;
		private int parameter, max;
		private boolean cc;

		public ServerAction(String name, NBTTagCompound tag, PlayerData data) {
			this.name = name;
			this.data = data;
			this.parameter = tag.getInteger("param");
			cc = tag.getBoolean("cc");
			max = tag.getInteger("max");
		}

		public ServerAction(PlayerData data, String name, int parameter, boolean cc) {
			this.data = data;
			this.name = name;
			this.parameter = parameter;
			this.cc = cc;
			this.max = 255;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getValue() {
			if (parameter < 0 || data.gestureData.length >= parameter)return -1;
			return Byte.toUnsignedInt(data.gestureData[parameter]) * max / 255;
		}

		@Override
		public boolean isCommandControlled() {
			return cc;
		}
	}

	@Override
	public void writeLegacy(NBTTagCompound tag) {
		tag.setString("name", name);
		tag.setByte("id", (byte) parameter);
		tag.setByte("type", (byte) ((cc ? 16 : 0) | 2));
	}
}
