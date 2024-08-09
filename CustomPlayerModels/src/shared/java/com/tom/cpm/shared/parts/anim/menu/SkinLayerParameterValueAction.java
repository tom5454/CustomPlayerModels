package com.tom.cpm.shared.parts.anim.menu;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.network.ServerCaps;
import com.tom.cpm.shared.parts.anim.menu.CommandAction.LegacyCommandActionWriter;

public class SkinLayerParameterValueAction implements CommandAction, LegacyCommandActionWriter {
	private final String name;
	private ModelDefinition def;
	public int parameter;
	public int value;
	public boolean pose;
	public int gid;
	private boolean cc;

	public SkinLayerParameterValueAction(String name, ModelDefinition def, int parameter, int value, boolean pose, int gid, boolean cc) {
		this.name = name;
		this.def = def;
		this.parameter = parameter;
		this.pose = pose;
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
		tag.setByte("type", (byte) ((cc ? 16 : 0) | (pose ? 0 : 1)));
	}

	@Override
	public ActionType getType() {
		return ActionType.SIMPLE;//Works like the simple variant on server side
	}

	@Override
	public int getValue() {
		AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
		return an.getGestureValue(parameter) == value ? 1 : 0;
	}

	@Override
	public void setValue(int v) {
		if (MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES)) {
			AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
			if ((v == -1 && an.getGestureValue(parameter) == value) || v == 0)
				an.setGestureValue(parameter, 0);
			else
				an.setGestureValue(parameter, value);
		} else {
			Player<?> pl = def.getPlayerObj();
			int id;
			if (pl.animState.encodedState == gid) {
				id = pose ? def.getAnimations().getPoseResetId() : def.getAnimations().getBlankGesture();
			} else {
				id = gid;
			}
			MinecraftClientAccess.get().setEncodedGesture(id);
		}
	}
}
