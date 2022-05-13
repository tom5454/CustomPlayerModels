package com.tom.cpm.shared.animation;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.animation.AnimationRegistry.Gesture;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.ScaleData;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.ScalingOptions;

public class AnimationEngine {
	private long tickCounter;
	private float partial;
	private ScaleData modelScale;
	private ScaleData modelScaleToReset;
	private long resetCounter;

	public void tick() {
		tickCounter++;
		if(MinecraftClientAccess.get().isInGame()) {
			Player<?> player = MinecraftClientAccess.get().getCurrentClientPlayer();//Keep client player loaded
			ModelDefinition def = player.getModelDefinition();
			if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED) {
				if(def != null && def.doRender()) {
					player.sendEventSubs();
					if(resetCounter > tickCounter) {
						resetCounter = 0;
						modelScale = modelScaleToReset;
						modelScaleToReset = null;
					}
					if(!Objects.equals(def.getScale(), modelScale)) {
						modelScale = def.getScale();
						if(modelScale == null) {
							MinecraftClientAccess.get().setModelScale(null);
						} else {
							MinecraftClientAccess.get().setModelScale(modelScale);
						}
					}
				} else if(modelScale != null) {
					MinecraftClientAccess.get().setModelScale(null);
					modelScale = null;
				}
			}
		} else {
			modelScale = null;
		}
	}

	public void update(float partial) {
		this.partial = partial;
	}

	public long getTime() {
		return (long) ((tickCounter + partial) * 50);
	}

	public void handleAnimation(Player<?> player, AnimationMode mode) {
		AnimationHandler h = player.getAnimationHandler(mode);
		try {
			long time = getTime();
			ModelDefinition def = player.getModelDefinition();
			AnimationRegistry reg = def.getAnimations();
			switch (mode) {
			case HAND:
				def.resetAnimationPos();
				return;

			case PLAYER:
			{
				player.animState.preAnimate();
				VanillaPose pose = player.animState.getMainPose(time, reg);
				int gesture = player.animState.encodedState;
				if(pose != player.prevPose || gesture == reg.getPoseResetId()) {
					player.currentPose = pose;
				}
				player.currentPose = reg.getPose(gesture, player.currentPose);
				player.prevPose = pose;
				List<IAnimation> anim = reg.getPoseAnimations(player.currentPose);
				h.addAnimations(anim, player.currentPose);
				player.animState.collectAnimations(p -> h.addAnimations(reg.getPoseAnimations(p), p));
				h.setGesture(reg.getGesture(gesture));
			}
			break;

			case SKULL:
			{
				List<IAnimation> anim = reg.getPoseAnimations(VanillaPose.SKULL_RENDER);
				List<IAnimation> global = reg.getPoseAnimations(VanillaPose.GLOBAL);
				h.addAnimations(anim, VanillaPose.SKULL_RENDER);
				h.addAnimations(global, VanillaPose.GLOBAL);
			}
			break;

			default:
				break;
			}
			h.animate(player.animState, time);
			reg.tickAnimated(time);
		} catch (Exception e) {
			Log.warn("Error animating model", e);
			player.getModelDefinition().resetAnimationPos();
			h.clear();
		}
	}

	public void handleGuiAnimation(AnimationHandler h, ModelDefinition def) {
		try {
			List<IAnimation> anim = def.getAnimations().getPoseAnimations(VanillaPose.STANDING);
			List<IAnimation> global = def.getAnimations().getPoseAnimations(VanillaPose.GLOBAL);
			h.addAnimations(anim, VanillaPose.STANDING);
			h.addAnimations(global, VanillaPose.GLOBAL);
			h.animate(null, getTime());
		} catch (Exception e) {
			e.printStackTrace();
			def.resetAnimationPos();
			h.clear();
		}
	}

	public void onKeybind(int id) {
		ModelDefinition def = MinecraftClientAccess.get().getCurrentClientPlayer().getModelDefinition();
		if(def != null) {
			ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.KEYBINDS);
			String c = ce.getString("qa_" + id, null);
			if(c != null) {
				if(c.startsWith("p")) {
					CustomPose pose = def.getAnimations().getCustomPoses().get(c.substring(1));
					if(pose != null) {
						setCustomPose(def.getAnimations(), pose);
					}
				} else if(c.startsWith("g")) {
					Gesture g = def.getAnimations().getGestures().get(c.substring(1));
					if(g != null) {
						playGesture(def.getAnimations(), g);
					}
				}
			}
		}
	}

	public void setCustomPose(AnimationRegistry reg, CustomPose pose) {
		ServerStatus status = MinecraftClientAccess.get().getServerSideStatus();
		if(status == ServerStatus.OFFLINE || status == ServerStatus.UNAVAILABLE)return;
		int enc = pose == null ? reg.getPoseResetId() : reg.getEncoded(pose);
		if(enc != -1) {
			if(enc == MinecraftClientAccess.get().getCurrentClientPlayer().animState.encodedState) {
				enc = reg.getPoseResetId();
			}
			MinecraftClientAccess.get().setEncodedGesture(enc);
		}
	}

	public void playGesture(AnimationRegistry reg, Gesture g) {
		ServerStatus status = MinecraftClientAccess.get().getServerSideStatus();
		if(status == ServerStatus.OFFLINE || status == ServerStatus.UNAVAILABLE)return;
		int enc = g == null ? reg.getBlankGesture() : reg.getEncoded(g);
		if(enc != -1) {
			if(enc == MinecraftClientAccess.get().getCurrentClientPlayer().animState.encodedState) {
				enc = reg.getBlankGesture();
			}
			MinecraftClientAccess.get().setEncodedGesture(enc);
		}
	}

	public int getTicks() {
		return (int) tickCounter;
	}

	public static enum AnimationMode {
		PLAYER,
		SKULL,
		HAND
	}

	public void setServerScaling(Map<ScalingOptions, Float> scaling) {
		modelScaleToReset = new ScaleData(scaling);
		resetCounter = tickCounter + 100;
	}
}
