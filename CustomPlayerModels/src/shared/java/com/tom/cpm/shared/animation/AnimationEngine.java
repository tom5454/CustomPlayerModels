package com.tom.cpm.shared.animation;

import java.util.List;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.animation.AnimationRegistry.Gesture;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.parts.ModelPartScale;

public class AnimationEngine {
	private long tickCounter;
	private float partial;
	private ModelPartScale modelScale;

	public void tick() {
		tickCounter++;
		if(MinecraftClientAccess.get().isInGame()) {
			Player<?, ?> player = MinecraftClientAccess.get().getCurrentClientPlayer();//Keep client player loaded
			ModelDefinition def = player.getModelDefinition();
			if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED) {
				if(def != null && def.doRender()) {
					if(def.getScale() != modelScale) {
						modelScale = def.getScale();
						if(modelScale == null) {
							MinecraftClientAccess.get().setModelScale(0);
						} else {
							MinecraftClientAccess.get().setModelScale(modelScale.getScale());
						}
					}
				} else if(modelScale != null) {
					MinecraftClientAccess.get().setModelScale(0);
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

	public void handleAnimation(Player<?, ?> player, AnimationMode mode) {
		AnimationHandler h = player.getAnimationHandler(mode);
		try {
			ModelDefinition def = player.getModelDefinition();
			switch (mode) {
			case HAND:
				def.resetAnimationPos();
				return;

			case PLAYER:
			{
				VanillaPose pose = player.getPose();
				int gesture = player.getEncodedGestureId();
				AnimationRegistry reg = def.getAnimations();
				if(pose != player.prevPose || gesture == reg.getPoseResetId()) {
					player.currentPose = pose;
				}
				player.currentPose = def.getAnimations().getPose(gesture, player.currentPose);
				player.prevPose = pose;
				List<Animation> anim = def.getAnimations().getPoseAnimations(player.currentPose);
				List<Animation> global = def.getAnimations().getPoseAnimations(VanillaPose.GLOBAL);
				h.addAnimations(anim);
				h.addAnimations(global);
				h.setGesture(def.getAnimations().getGesture(gesture));
			}
			break;

			case SKULL:
			{
				List<Animation> anim = def.getAnimations().getPoseAnimations(VanillaPose.SKULL_RENDER);
				List<Animation> global = def.getAnimations().getPoseAnimations(VanillaPose.GLOBAL);
				h.addAnimations(anim);
				h.addAnimations(global);
			}
			break;

			default:
				break;
			}
			h.animate(getTime());
		} catch (Exception e) {
			e.printStackTrace();
			player.getModelDefinition().resetAnimationPos();
			h.clear();
		}
	}

	public void handleGuiAnimation(AnimationHandler h, ModelDefinition def) {
		try {
			List<Animation> anim = def.getAnimations().getPoseAnimations(VanillaPose.STANDING);
			List<Animation> global = def.getAnimations().getPoseAnimations(VanillaPose.GLOBAL);
			h.addAnimations(anim);
			h.addAnimations(global);
			h.animate(getTime());
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
			if(enc == MinecraftClientAccess.get().getCurrentClientPlayer().getEncodedGestureId()) {
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
			if(enc == MinecraftClientAccess.get().getCurrentClientPlayer().getEncodedGestureId()) {
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
}
