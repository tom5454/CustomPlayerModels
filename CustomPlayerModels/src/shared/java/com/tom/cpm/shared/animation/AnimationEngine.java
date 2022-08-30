package com.tom.cpm.shared.animation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.ScaleData;
import com.tom.cpm.shared.network.ServerCaps;
import com.tom.cpm.shared.network.packet.GestureC2S;
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
	private byte[] gestureData = new byte[2];

	public void tick() {
		tickCounter++;
		if(MinecraftClientAccess.get().isInGame()) {
			Player<?> player = MinecraftClientAccess.get().getCurrentClientPlayer();//Keep client player loaded
			ModelDefinition def = player.getModelDefinition();
			if(def != null) {
				int lc = def.getAnimations().getLayerCount();

				if(gestureData.length != lc) {
					byte v0 = gestureData[0];
					byte v1 = gestureData[1];
					gestureData = new byte[lc];
					gestureData[0] = v0;
					gestureData[1] = v1;
					sendGestureData();
				}
				if(player.animState.gestureData == null) {
					player.animState.gestureData = Arrays.copyOf(gestureData, gestureData.length);
					sendGestureData();
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
				playGesture(def.getAnimations(), null);
			}
		} else {
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
				if (MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES) && player.animState.gestureData != null && player.animState.gestureData.length > 1) {
					if(player.animState.gestureData[0] == 0) {
						player.currentPose = pose;
					} else {
						player.currentPose = reg.getPose(player.animState.gestureData[0], player.currentPose);
						player.prevPose = pose;
					}
					List<IAnimation> anim = reg.getPoseAnimations(player.currentPose);
					h.addAnimations(anim, player.currentPose);
					player.animState.collectAnimations(p -> h.addAnimations(reg.getPoseAnimations(p), p));
					h.setGesture(reg.getGesture(player.animState.gestureData[1]));
					reg.forEachLayer((g, id) -> {
						if(player.animState.gestureData.length > id) {
							if(g.name.startsWith(Gesture.VALUE_LAYER_PREFIX)) {
								h.addAnimations(g.animation, new ValueLayerPose(id));
							} else if(player.animState.gestureData[id] != 0) {
								h.addAnimations(g.animation, null);
							}
						}
					});
				} else {
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

	private static class ValueLayerPose implements IPose {
		private final int id;

		public ValueLayerPose(int id) {
			this.id = id;
		}

		@Override
		public String getName(IGui gui, String display) {
			return "value";
		}

		@Override
		public long getTime(AnimationState state, long time) {
			if(state.gestureData.length > id)
				return (long) ((Byte.toUnsignedInt(state.gestureData[id]) / 256f) * VanillaPose.DYNAMIC_DURATION_MUL);
			else
				return 0L;
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

	private void onKeybind(int id) {
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
		if (MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES)) {
			gestureData[0] = pose == null || gestureData[0] == enc ? 0 : (byte) enc;
			sendGestureData();
		} else {
			if(enc != -1) {
				if(enc == MinecraftClientAccess.get().getCurrentClientPlayer().animState.encodedState) {
					enc = reg.getPoseResetId();
				}
				MinecraftClientAccess.get().setEncodedGesture(enc);
			}
		}
	}

	public void playGesture(AnimationRegistry reg, Gesture g) {
		ServerStatus status = MinecraftClientAccess.get().getServerSideStatus();
		boolean serverGc = MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES);
		if(status == ServerStatus.OFFLINE || status == ServerStatus.UNAVAILABLE)return;
		if(g != null) {
			if (g.name.startsWith(Gesture.LAYER_PREFIX)) {
				if(serverGc) {
					int id = reg.getLayerId(g);
					gestureData[id] = (byte) (gestureData[id] != 0 ? 0 : 1);
					sendGestureData();
				}
				return;
			}
		} else if (serverGc) {
			gestureData[1] = 0;
			sendGestureData();
			return;
		}
		if(g != null && !g.isLoop && ModConfig.getCommonConfig().getBoolean(ConfigKeys.GESTURE_AUTO_RESET, true)) {
			int len = g.animation.stream().mapToInt(IAnimation::getDuration).max().orElse(-1);
			gestureAutoResetTimer = len == -1 ? -1 : ((int) Math.ceil(len / 50f) + 5);
		}
		int enc = g == null ? reg.getBlankGesture() : reg.getEncoded(g);
		if (serverGc) {
			gestureData[1] = g == null || gestureData[1] == enc ? 0 : (byte) enc;
			sendGestureData();
		} else {
			if(enc != -1) {
				if(enc == MinecraftClientAccess.get().getCurrentClientPlayer().animState.encodedState) {
					enc = reg.getBlankGesture();
				}
				MinecraftClientAccess.get().setEncodedGesture(enc);
			}
		}
	}

	public void setLayerValue(AnimationRegistry reg, Gesture g, float value) {
		if(!MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES))return;
		if (g.name.startsWith(Gesture.VALUE_LAYER_PREFIX)) {
			int id = reg.getLayerId(g);
			if(id == -1)return;
			gestureData[id] = (byte) (value * 0xff);
			sendGestureData();
		}
	}

	private void sendGestureData() {
		MinecraftClientAccess.get().getNetHandler().sendPacketToServer(new GestureC2S(gestureData));
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

	public void updateKeys(IKeybind[] kbs) {
		ModelDefinition def = MinecraftClientAccess.get().getCurrentClientPlayer().getModelDefinition();
		if(def != null) {
			for (int i = 1; i <= kbs.length; i++) {
				IKeybind kb = kbs[i - 1];
				boolean pr = kb.isPressed();
				boolean prevPr = quickAccessPressed[i - 1];
				if (!prevPr && pr) {
					onKeybind(i);
				} else if (prevPr && !pr) {
					ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.KEYBINDS);
					String mode = ce.getString("qa_" + i + "_mode", "press");
					String c = ce.getString("qa_" + i, null);
					if(c != null && mode.equals("hold")) {
						if(c.startsWith("p")) {
							setCustomPose(def.getAnimations(), null);
						} else if(c.startsWith("g")) {
							Gesture g = def.getAnimations().getGestures().get(c.substring(1));
							if(g != null && g.isLoop)
								playGesture(def.getAnimations(), null);
						}
					}
				}
				quickAccessPressed[i - 1] = pr;
			}
		} else {
			Arrays.fill(quickAccessPressed, false);
		}
	}

	public byte getGestureValue(AnimationRegistry reg, Gesture g) {
		int id = reg.getLayerId(g);
		if(id == -1)return 0;
		return gestureData[id];
	}
}
