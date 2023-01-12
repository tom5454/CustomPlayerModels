package com.tom.cpmoscc;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;

public class CPMOSC {
	public static final Logger LOGGER = LogManager.getLogger("CPM-OSC Compat");

	public static final String OSC_ENABLE = "cpmosc_enable";
	public static final String OSC_PORT_KEY = "cpmosc_receive_port";

	public static final String MOD_ID = "cpmoscc";
	public static IClientAPI api;
	private static OSCReceiver osc;
	public static WeakReference<ModelDefinition> currentDefinition = new WeakReference<>(null);
	public static OSCMessageManager manager = new OSCMessageManager();

	public static void tick() {
		if(MinecraftClientAccess.get() != null && MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED && api != null && isEnabled()) {
			getOsc();
			Player<?> player = MinecraftClientAccess.get().getCurrentClientPlayer();
			if(player != null) {
				ModelDefinition def = player.getModelDefinition();
				if(def != null && currentDefinition.get() != def) {
					manager.update(def);
					currentDefinition = new WeakReference<>(def);
				}
				manager.tick();
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
		osc = null;
	}

	public static OSCReceiver getOsc() {
		if(osc == null) {
			osc = new OSCReceiver(ModConfig.getCommonConfig().getInt(OSC_PORT_KEY, 9000));
			if(osc.canStart()) {
				osc.start();
				LOGGER.info("Started OSC listener");
			}
		}
		return osc;
	}
}
