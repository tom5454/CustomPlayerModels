package com.tom.cpm.shared.animation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.text.FormatText;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.animation.AnimationState.VRState;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.ScaleData;
import com.tom.cpm.shared.network.ServerCaps;
import com.tom.cpm.shared.network.packet.GestureC2S;
import com.tom.cpm.shared.parts.anim.ParameterDetails;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.CommandAction;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.ScalingOptions;

public class AnimationEngine {
	private long tickCounter;
	private float partial;
	private ScaleData modelScale;
	private ScaleData modelScaleToReset;
	private long resetCounter;
	private boolean[] quickAccessPressed = new boolean[IKeybind.QUICK_ACCESS_KEYBINDS_COUNT];
	private int gestureAutoResetTimer = -1;
	private ParameterDetails lastDetails;
	private byte[] gestureData = new byte[2];
	private boolean gesturesChanged;
	private boolean checkedUUID;

	public void tick() {
		tickCounter++;
		if(MinecraftClientAccess.get().isInGame()) {
			Player<?> player = MinecraftClientAccess.get().getCurrentClientPlayer();//Keep client player loaded
			ModelDefinition def = player.getModelDefinition();
			if(def != null) {
				ParameterDetails param = def.getAnimations().getParams();

				if (lastDetails != param) {
					byte v0 = gestureData[0];
					byte v1 = gestureData[1];
					gestureData = param.createSyncParams();
					gestureData[0] = v0;
					gestureData[1] = v1;
					if (def.getAnimations().getProfileId() != null) {
						ConfigEntry vals = ModConfig.getCommonConfig().getEntry(ConfigKeys.MODEL_PROPERTIES).getEntry(def.getAnimations().getProfileId()).getEntry(ConfigKeys.MODEL_PROPERTIES_VALUES);
						def.getAnimations().getNamedActions().forEach(a -> {
							if (a.isProperty()) a.loadFrom(vals);
						});
					}
					gesturesChanged = true;
					this.lastDetails = param;
				}
				if (player.animState.gestureData == null) {
					player.animState.gestureData = Arrays.copyOf(gestureData, gestureData.length);
				}
			}
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
			if(gestureAutoResetTimer >= 0)gestureAutoResetTimer--;
			if(gestureAutoResetTimer == 0 && def != null) {
				clearGesture(def);
			}
			if (!checkedUUID) {
				checkedUUID = true;
				ModelDefinitionLoader<Object> dl = MinecraftClientAccess.get().getDefinitionLoader();
				UUID client = dl.getGP_UUID(MinecraftClientAccess.get().getPlayerIDObject());
				UUID server = dl.getGP_UUID(MinecraftClientAccess.get().getCurrentPlayerIDObject());
				if (!Objects.equals(client, server) && ModConfig.getCommonConfig().getBoolean(ConfigKeys.SHOW_INGAME_WARNINGS, true)) {
					MinecraftClientAccess.get().getNetHandler().displayText(new FormatText("chat.cpm.clientUUIDMismatch"));
				}
			}
		} else {
			checkedUUID = false;
			modelScale = null;
			Arrays.fill(quickAccessPressed, false);
			gestureAutoResetTimer = -1;
		}
	}

	public void update(float partial) {
		this.partial = partial;
	}

	public long getTime() {
		return (long) ((tickCounter + partial) * 50);
	}

	public void prepareAnimations(Player<?> player, AnimationMode mode, ModelDefinition def) {
		long time = getTime();
		AnimationRegistry reg = def.getAnimations();
		if(mode == AnimationMode.PLAYER) {
			player.animState.preAnimate();
			VanillaPose pose = player.animState.getMainPose(time, reg);
			if (MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES) && player.animState.gestureData != null && player.animState.gestureData.length > 1) {
				if(player.animState.gestureData[0] == 0) {
					player.currentPose = pose;
				} else {
					player.currentPose = reg.getPoseById(player.animState.gestureData[0], player.currentPose);
					player.prevPose = pose;
				}
			} else {
				int gesture = player.animState.encodedState;
				if(pose != player.prevPose || gesture == reg.getPoseResetId()) {
					player.currentPose = pose;
				}
				player.currentPose = reg.getPoseEncoded(gesture, player.currentPose);
				player.prevPose = pose;
			}
		}
		reg.tickAnimated(time, false);
	}

	public void handleAnimation(Player<?> player, AnimationMode mode, ModelDefinition def) {
		if (mode == AnimationMode.PLAYER) {
			if (player.animState.firstPersonMod)mode = AnimationMode.FIRST_PERSON;
			else if (player.animState.inGui)mode = AnimationMode.GUI;
		}
		AnimationHandler h = player.getAnimationHandler(mode);
		try {
			long time = getTime();
			AnimationRegistry reg = def.getAnimations();
			switch (mode) {
			case HAND:
				def.resetAnimationPos();
				VanillaPose pose = player.animState.vrState == VRState.FIRST_PERSON ? VanillaPose.VR_FIRST_PERSON : VanillaPose.FIRST_PERSON_HAND;
				List<AnimationTrigger> a = reg.getPoseAnimations(pose);
				h.addAnimations(a, pose);
				break;

			case PLAYER:
			case FIRST_PERSON:
			case GUI:
			{
				player.animState.preAnimate();
				List<AnimationTrigger> anim = reg.getPoseAnimations(player.currentPose);
				h.addAnimations(anim, player.currentPose);
				player.animState.collectAnimations(p -> h.addAnimations(reg.getPoseAnimations(p), p), reg);
			}
			break;

			case SKULL:
			{
				List<AnimationTrigger> anim = reg.getPoseAnimations(VanillaPose.SKULL_RENDER);
				List<AnimationTrigger> global = reg.getPoseAnimations(VanillaPose.GLOBAL);
				h.addAnimations(anim, VanillaPose.SKULL_RENDER);
				h.addAnimations(global, VanillaPose.GLOBAL);
			}
			break;

			default:
				break;
			}
			h.animate(player.animState, time);
		} catch (Exception e) {
			Log.warn("Error animating model", e);
			try {
				def.resetAnimationPos();
				h.clear();
			} catch (Exception ex) {
				ex.addSuppressed(e);
				Log.error("Error animating model", e);
				def.setError(ex);
			}
		}
	}

	public void handleGuiAnimation(AnimationHandler h, ModelDefinition def) {
		try {
			long time = getTime();
			AnimationRegistry reg = def.getAnimations();
			reg.tickAnimated(time, true);
			List<AnimationTrigger> anim = def.getAnimations().getPoseAnimations(VanillaPose.STANDING);
			List<AnimationTrigger> global = def.getAnimations().getPoseAnimations(VanillaPose.GLOBAL);
			List<AnimationTrigger> inGui = def.getAnimations().getPoseAnimations(VanillaPose.IN_GUI);
			h.addAnimations(anim, VanillaPose.STANDING);
			h.addAnimations(global, VanillaPose.GLOBAL);
			h.addAnimations(inGui, VanillaPose.IN_GUI);
			h.animate(null, time);
		} catch (Exception e) {
			e.printStackTrace();
			def.resetAnimationPos();
			h.clear();
		}
	}

	private void sendGestureData() {
		System.out.println("Gesture Sync: " + Arrays.toString(gestureData));
		MinecraftClientAccess.get().getNetHandler().sendPacketToServer(new GestureC2S(gestureData));
	}

	public int getTicks() {
		return (int) tickCounter;
	}

	public static enum AnimationMode {
		PLAYER,
		SKULL,
		HAND,
		GUI,
		FIRST_PERSON,
	}

	public void setServerScaling(Map<ScalingOptions, Float> scaling) {
		modelScaleToReset = new ScaleData(scaling);
		resetCounter = tickCounter + 100;
	}

	public static ConfigEntry getEntryForModel(ModelDefinition def, boolean make) {
		String pf = def.getAnimations().getProfileId();
		if (pf != null) {
			ConfigEntry cem = ModConfig.getCommonConfig().getEntry(ConfigKeys.KEYBINDS_MODEL);
			if (cem.hasEntry(pf) || make) {
				boolean copy = !cem.hasEntry(pf);
				ConfigEntry cfg = cem.getEntry(pf);
				if (copy) {
					ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.KEYBINDS);
					for (String k : ce.keySet()) {
						cfg.setString(k, ce.getString(k, ""));
					}
				}
				return cfg;
			}
		}
		return ModConfig.getCommonConfig().getEntry(ConfigKeys.KEYBINDS);
	}

	public void updateKeys(IKeybind[] kbs) {
		Player<?> pl = MinecraftClientAccess.get().getCurrentClientPlayer();
		ModelDefinition def = pl.getModelDefinition();
		if(def != null) {
			ConfigEntry ce = getEntryForModel(def, false);
			for (int i = 1; i <= kbs.length; i++) {
				IKeybind kb = kbs[i - 1];
				boolean pr = kb.isPressed();
				boolean prevPr = quickAccessPressed[i - 1];
				if (!prevPr && pr) {
					String mode = ce.getString("qa_" + i + "_mode", "press");
					String c = ce.getString("qa_" + i, null);
					if (c != null) {
						String[] sp = c.split("/");
						AbstractGestureButtonData dt = def.getAnimations().getNamedActionByKeybind().get(sp[0]);
						dt.onKeybind(sp.length > 1 ? sp[1] : null, true, !mode.equals("hold"));
					}
				} else if (prevPr && !pr) {
					String mode = ce.getString("qa_" + i + "_mode", "press");
					String c = ce.getString("qa_" + i, null);
					if (c != null && mode.equals("hold")) {
						String[] sp = c.split("/");
						AbstractGestureButtonData dt = def.getAnimations().getNamedActionByKeybind().get(sp[0]);
						dt.onKeybind(sp.length > 1 ? sp[1] : null, false, false);
					}
				}
				quickAccessPressed[i - 1] = pr;
			}

			if (MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED) {
				if (gesturesChanged) {
					sendGestureData();
					gesturesChanged = false;
				}
			}
		} else {
			Arrays.fill(quickAccessPressed, false);
		}
	}

	public void resetGestureData() {
		gestureData = new byte[2];
	}

	public boolean applyCommand(String id, int value, Boolean cmd) {
		Player<?> pl = MinecraftClientAccess.get().getCurrentClientPlayer();
		ModelDefinition def = pl.getModelDefinition();
		if(def != null) {
			CommandAction ca = def.getAnimations().getCommandActionsMap().get(id);
			if (ca != null && (cmd == null || ca.isCommandControlled() == cmd)) {
				ca.setValue(value);
				return true;
			}
		}
		return false;
	}

	public void clearCustomPose(ModelDefinition def) {
		if (MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES)) {
			setGestureValue(0, 0);
		} else {
			MinecraftClientAccess.get().setEncodedGesture(def.getAnimations().getPoseResetId());
		}
	}

	public void clearGesture(ModelDefinition def) {
		if (MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES)) {
			setGestureValue(1, 0);
		} else {
			MinecraftClientAccess.get().setEncodedGesture(def.getAnimations().getBlankGesture());
		}
		gestureAutoResetTimer = -1;
	}

	public byte getGestureValue(int id) {
		return gestureData.length > id && id >= 0 ? gestureData[id] : 0;
	}

	public void setGestureValue(int id, int val) {
		System.out.println("Set: " + id + " to " + val);
		byte v = (byte) val;
		if (gestureData[id] != v) {
			gestureData[id] = v;
			gesturesChanged = true;
		}
	}

	public void setGestureTimeout(int gestureTimeout) {
		gestureAutoResetTimer = gestureTimeout;
	}
}
