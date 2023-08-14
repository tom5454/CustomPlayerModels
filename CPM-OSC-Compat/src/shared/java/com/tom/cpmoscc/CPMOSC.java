package com.tom.cpmoscc;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.stream.Collectors;

import com.tom.cpl.block.BlockState;
import com.tom.cpl.item.Inventory;
import com.tom.cpl.item.NamedSlot;
import com.tom.cpl.item.Stack;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.AnimationState;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.Gesture;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.ServerAnimationState;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.network.ServerCaps;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;

public class CPMOSC {
	public static final OSCLog LOGGER = OSCLog.getLogger("OSC");

	public static final String OSC_ENABLE = "cpmosc_enable";
	public static final String OSC_ENABLE_TRANSMIT = "cpmosc_enable_transmit";
	public static final String OSC_PORT_KEY = "cpmosc_receive_port";
	public static final String OSC_OUT_KEY = "cpmosc_transmit_port";

	public static final String MOD_ID = "cpmoscc";
	public static final String VERSION_CHECK_URL = "https://raw.githubusercontent.com/tom5454/CustomPlayerModels/master/CPM-OSC-Compat/version-check.json";
	public static IClientAPI api;
	private static OSCReceiver osc;
	private static OSCTransmitter transmit;
	public static WeakReference<ModelDefinition> currentDefinition = new WeakReference<>(null);
	public static OSCMessageManager manager = new OSCMessageManager();
	private static final Field[] outputFields = AnimationState.class.getDeclaredFields();
	private static final Field[] outputFields2 = ServerAnimationState.class.getDeclaredFields();

	public static void tick(Object playerIn) {
		if(MinecraftClientAccess.get() != null && MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED && api != null && isEnabled()) {
			getOsc();
			@SuppressWarnings("unchecked")
			Player<Object> player = (Player<Object>) MinecraftClientAccess.get().getCurrentClientPlayer();
			if(player != null) {
				ModelDefinition def = player.getModelDefinition();
				if(def != null && currentDefinition.get() != def) {
					manager.update(def);
					currentDefinition = new WeakReference<>(def);
				}
				manager.tick();
				if(ModConfig.getCommonConfig().getBoolean(CPMOSC.OSC_ENABLE_TRANSMIT, false)) {
					if (transmit == null) {
						String outputAddr = ModConfig.getCommonConfig().getString(OSC_OUT_KEY, "localhost:9001");
						transmit = new OSCTransmitter(outputAddr);
						if(!transmit.canSend()) {
							ErrorLog.addLog(LogLevel.WARNING, "OSC Transmitter error", transmit.getError());
						}
					}
					player.updatePlayer(playerIn);
					long time = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
					try {
						for (Field f : outputFields) {
							send(player.animState, f);
						}
						for (Field f : outputFields2) {
							send(player.animState.localState, f);
						}
						if(def != null) {
							AnimationRegistry reg = def.getAnimations();
							reg.forEachLayer((g, id) -> {
								if(g.command)return;
								String gid = g.getName().replaceAll("[^a-zA-Z0-9\\.\\-]", "");
								try {
									if(player.animState.gestureData.length > id) {
										if(g.type == AnimationType.VALUE_LAYER)
											transmit.send("/cpm/layer/" + gid + "/value", player.animState.gestureData[id]);
										else
											transmit.send("/cpm/layer/" + gid + "/toggle", player.animState.gestureData[id] != 0);
									} else {
										if(g.type == AnimationType.VALUE_LAYER)
											transmit.send("/cpm/layer/" + gid + "/value", g.defVal);
										else
											transmit.send("/cpm/layer/" + gid + "/toggle", g.defVal != 0);
									}
								} catch (IOException e) {
								}
							});
							int gesture = player.animState.encodedState;
							Gesture g = null;
							if (MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES) && player.animState.gestureData != null && player.animState.gestureData.length > 1) {
								g = reg.getGesture(player.animState.gestureData[1]);
							} else {
								g = reg.getGesture(gesture);
							}
							transmit.send("/cpm/gesture", g != null ? g.getName().replaceAll("[^a-zA-Z0-9\\.\\-]", "") : "null");
							VanillaPose pose = player.animState.getMainPose(time, reg);
							sendEnum("/cpm/vanilla_pose", pose);
							IPose currentPose = pose;
							if (MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES) && player.animState.gestureData != null && player.animState.gestureData.length > 1) {
								if(player.animState.gestureData[0] != 0) {
									currentPose = reg.getPose(player.animState.gestureData[0], player.currentPose);
								}
							} else {
								currentPose = reg.getPose(gesture, player.currentPose);
							}
							if(currentPose instanceof VanillaPose) {
								VanillaPose e = (VanillaPose) currentPose;
								sendEnum("/cpm/pose", e);
							} else if(currentPose instanceof CustomPose) {
								CustomPose p = (CustomPose) currentPose;
								transmit.send("/cpm/pose/name", p.getId());
								transmit.send("/cpm/pose/id", -1);
							}
							if (player.animState.playerInventory != null) {
								Inventory i = player.animState.playerInventory;
								sendItem(def, "held_item", i.getStack(i.getNamedSlotId(NamedSlot.MAIN_HAND)));
								sendItem(def, "armor/helmet", i.getStack(i.getNamedSlotId(NamedSlot.ARMOR_HELMET)));
								sendItem(def, "armor/chestplate", i.getStack(i.getNamedSlotId(NamedSlot.ARMOR_CHESTPLATE)));
								sendItem(def, "armor/leggings", i.getStack(i.getNamedSlotId(NamedSlot.ARMOR_LEGGINGS)));
								sendItem(def, "armor/boots", i.getStack(i.getNamedSlotId(NamedSlot.ARMOR_BOOTS)));
								sendItem(def, "off_hand", i.getStack(i.getNamedSlotId(NamedSlot.OFF_HAND)));
								transmit.send("/cpm/heldSlot", i.getNamedSlotId(NamedSlot.MAIN_HAND));
							}
							if (player.animState.world != null) {
								sendBlock(def, "below", player.animState.world.getBlock(0, -1, 0));
								sendBlock(def, "feet", player.animState.world.getBlock(0, 0, 0));
								sendBlock(def, "head", player.animState.world.getBlock(0, 1, 0));
							}
						}
						transmit.send("/cpm/gameTime", time);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static void sendBlock(ModelDefinition def, String name, BlockState st) throws IOException {
		transmit.send("/cpm/world/" + name + "/id", st.getBlockId());
		transmit.send("/cpm/world/" + name + "/tags", def.modelTagManager.getBlockTags().listStackTags(st).stream().map(t -> t.getId()).collect(Collectors.toList()));
	}

	private static void sendItem(ModelDefinition def, String name, Stack stack) throws IOException {
		transmit.send("/cpm/" + name + "/id", stack.getItemId());
		transmit.send("/cpm/" + name + "/count", stack.getCount(), stack.getMaxCount());
		transmit.send("/cpm/" + name + "/damage", stack.getDamage(), stack.getMaxDamage());
		transmit.send("/cpm/" + name + "/tags", def.modelTagManager.getItemTags().listStackTags(stack).stream().map(t -> t.getId()).collect(Collectors.toList()));
	}

	private static void send(Object inst, Field f) throws Exception {
		Object v = f.get(inst);
		if(v instanceof Enum) {
			Enum<?> e = (Enum<?>) v;
			sendEnum("/cpm/" + f.getName(), e);
		} else if (v instanceof Number || v instanceof Boolean) {
			transmit.send("/cpm/" + f.getName(), v);
		}
	}

	private static void sendEnum(String name, Enum<?> e) throws IOException {
		transmit.send(name + "/name", e.name());
		transmit.send(name + "/id", e.ordinal());
	}

	public static boolean isEnabled() {
		return ModConfig.getCommonConfig().getBoolean(CPMOSC.OSC_ENABLE, false);
	}

	public static void resetOsc() {
		if(osc != null) {
			try {
				osc.close();
			} catch (IOException e) {
			}
		}
		if(transmit != null) {
			try {
				transmit.close();
			} catch (IOException e) {
			}
		}
		osc = null;
		transmit = null;
	}

	public static OSCReceiver getOsc() {
		if(osc == null) {
			osc = new OSCReceiver(ModConfig.getCommonConfig().getInt(OSC_PORT_KEY, 9000));
			if(osc.canStart()) {
				osc.start();
				LOGGER.info("Started OSC listener");
			} else {
				ErrorLog.addLog(LogLevel.WARNING, "OSC Receiver error", osc.getError());
			}
		}
		return osc;
	}
}
