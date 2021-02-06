package com.tom.cpm.shared.animation;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.animation.AnimationRegistry.Gesture;
import com.tom.cpm.shared.config.ConfigEntry;
import com.tom.cpm.shared.config.ConfigEntry.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;

public class AnimationEngine {
	private long tickCounter;
	private float partial;

	public void tick() {
		tickCounter++;
		if(MinecraftClientAccess.get().isInGame()) {
			MinecraftClientAccess.get().getClientPlayer();//Keep client player loaded
		}
	}

	public void update(float partial) {
		this.partial = partial;
	}

	public long getTime() {
		return (long) ((tickCounter + partial) * 50);
	}

	public void handleAnimation(Player player) {
		try {
			VanillaPose pose = player.getPose();
			ModelDefinition def = player.getModelDefinition();
			int gesture = player.getEncodedGestureId();
			AnimationRegistry reg = def.getAnimations();
			if(pose != player.prevPose || gesture == reg.getPoseResetId()) {
				player.currentPose = pose;
			}
			player.currentPose = def.getAnimations().getPose(gesture, player.currentPose);
			player.prevPose = pose;
			Animation anim = def.getAnimations().getPoseAnimation(player.currentPose);
			player.getAnimationHandler().setNextAnimation(anim);
			player.getAnimationHandler().setGesture(def.getAnimations().getGesture(gesture));
			player.getAnimationHandler().animate(getTime());
		} catch (Exception e) {
			e.printStackTrace();
			player.getModelDefinition().resetAnimationPos();
		}
	}

	public void onKeybind(int id) {
		ModelDefinition def = MinecraftClientAccess.get().getClientPlayer().getModelDefinition();
		if(def != null) {
			ConfigEntry ce = ModConfig.getConfig().getEntry("keybinds");
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
			if(enc == MinecraftClientAccess.get().getClientPlayer().getEncodedGestureId()) {
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
			if(enc == MinecraftClientAccess.get().getClientPlayer().getEncodedGestureId()) {
				enc = reg.getBlankGesture();
			}
			MinecraftClientAccess.get().setEncodedGesture(enc);
		}
	}

	public int getTicks() {
		return (int) tickCounter;
	}
}
