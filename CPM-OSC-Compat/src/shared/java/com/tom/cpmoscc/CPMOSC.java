package com.tom.cpmoscc;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.animation.AnimationState;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;

public class CPMOSC {
	public static final Logger LOGGER = LogManager.getLogger("CPM-OSC Compat");

	public static final String OSC_ENABLE = "cpmosc_enable";
	public static final String OSC_ENABLE_TRANSMIT = "cpmosc_enable_transmit";
	public static final String OSC_PORT_KEY = "cpmosc_receive_port";
	public static final String OSC_OUT_KEY = "cpmosc_transmit_port";

	public static final String MOD_ID = "cpmoscc";
	public static IClientAPI api;
	private static OSCReceiver osc;
	private static OSCTransmitter transmit;
	public static WeakReference<ModelDefinition> currentDefinition = new WeakReference<>(null);
	public static OSCMessageManager manager = new OSCMessageManager();
	private static final Field[] outputFields = AnimationState.class.getDeclaredFields();

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
					try {
						for (Field f : outputFields) {
							Object v = f.get(player.animState);
							if(v instanceof Enum) {
								Enum<?> e = (Enum<?>) v;
								transmit.send("/cpm/" + f.getName() + "/name", e.name());
								transmit.send("/cpm/" + f.getName() + "/id", e.ordinal());
							} else if (v instanceof Number || v instanceof Boolean) {
								transmit.send("/cpm/" + f.getName(), v);
							}
						}
						transmit.send("/cpm/gameTime", MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime());
					} catch (Exception e) {
					}
				}
			}
		}
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
