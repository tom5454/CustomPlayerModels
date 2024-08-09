package com.tom.cpm.shared.parts.anim.menu;

import java.io.IOException;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.ServerCaps;
import com.tom.cpm.shared.parts.anim.AnimLoaderState;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData.AbstractCommandTriggerableData;

public class CustomPoseGestureButtonData extends AbstractCommandTriggerableData {
	private boolean pose;
	public int id;
	public int gid;
	private CommandAction action;
	public int gestureTimeout;

	public CustomPoseGestureButtonData(boolean pose) {
		this.pose = pose;
	}

	@Override
	protected void parseData(IOHelper block, AnimLoaderState state) throws IOException {
		super.parseData(block, state);
		id = block.read();
		if (layerCtrl)gid = block.read();
		if (!pose)gestureTimeout = block.readVarInt();

	}

	@Override
	public void onRegistered() {
		super.onRegistered();
		if (layerCtrl)
			commandActions.add(action = new SkinLayerParameterValueAction(name, def, pose ? 0 : 1, id, pose, gid, command));
		else
			commandActions.add(action = new SimpleParameterValueAction(name, pose ? 0 : 1, id, command));
	}

	@Override
	public GestureButtonType getType() {
		return pose ? GestureButtonType.POSE : GestureButtonType.GESTURE;
	}

	@Override
	public void write(IOHelper block) throws IOException {
		super.write(block);
		block.write(id);
		if (layerCtrl)block.write(gid);
		if (!pose)block.writeVarInt(gestureTimeout);
	}

	public static CustomPoseGestureButtonData pose() {
		return new CustomPoseGestureButtonData(true);
	}

	public static CustomPoseGestureButtonData gesture() {
		return new CustomPoseGestureButtonData(false);
	}

	public boolean isPose() {
		return pose;
	}

	public void activate() {
		setValue(-1);
	}

	private void setValue(int val) {
		ServerStatus status = MinecraftClientAccess.get().getServerSideStatus();
		if (status == ServerStatus.OFFLINE || status == ServerStatus.UNAVAILABLE)return;
		if (MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES)) {
			action.setValue(val);
		} else if (layerCtrl) {
			Player<?> pl = def.getPlayerObj();
			int id;
			if (val == 0 || (val == -1 && pl.animState.encodedState == gid)) {
				id = pose ? def.getAnimations().getPoseResetId() : def.getAnimations().getBlankGesture();
			} else {
				id = gid;
			}
			MinecraftClientAccess.get().setEncodedGesture(id);
		}
		if (gestureTimeout > 0 && ModConfig.getCommonConfig().getBoolean(ConfigKeys.GESTURE_AUTO_RESET, true)) {
			AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
			an.setGestureTimeout(gestureTimeout);
		}
	}

	@Override
	public void onKeybind(String arg, boolean press, boolean toggleMode) {
		if (toggleMode)setValue(-1);
		else setValue(press ? 1 : 0);
	}

	@Override
	public String getKeybindId() {
		return (pose ? "p" : "g") + name;
	}
}
