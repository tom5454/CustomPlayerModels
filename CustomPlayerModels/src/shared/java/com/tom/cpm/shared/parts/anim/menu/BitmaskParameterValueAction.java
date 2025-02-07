package com.tom.cpm.shared.parts.anim.menu;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.config.PlayerData;

public class BitmaskParameterValueAction implements CommandAction {
	private final String name;
	public int parameter;
	public int mask;
	private boolean cc;

	public BitmaskParameterValueAction(String name, int parameter, int mask, boolean cc) {
		this.name = name;
		this.parameter = parameter;
		this.mask = mask;
		this.cc = cc;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void write(NBTTagCompound tag) {
		tag.setInteger("param", parameter);
		tag.setByte("mask", (byte) mask);
		tag.setBoolean("cc", cc);
	}

	@Override
	public ActionType getType() {
		return ActionType.BITMASK;
	}

	@Override
	public int getValue() {
		AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
		return (an.getGestureValue(parameter) & mask) == mask ? 1 : 0;
	}

	@Override
	public void setValue(int v) {
		AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
		byte val = an.getGestureValue(parameter);
		if (v == -1)val ^= mask;
		else if (v == 0)val &=~ mask;
		else val |= mask;
		an.setGestureValue(parameter, val);
	}

	@Override
	public int getMaxValue() {
		return 1;
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
			value = tag.getByte("mask");
			cc = tag.getBoolean("cc");
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getValue() {
			if (parameter < 0 || data.gestureData.length <= parameter)return -1;
			return (data.gestureData[parameter] & value) == value ? 1 : 0;
		}

		@Override
		public boolean isCommandControlled() {
			return cc;
		}
	}

	@Override
	public boolean isCommandControlled() {
		return cc;
	}
}
