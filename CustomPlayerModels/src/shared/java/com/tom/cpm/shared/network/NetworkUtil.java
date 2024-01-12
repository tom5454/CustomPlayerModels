package com.tom.cpm.shared.network;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.function.TriFunction;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.text.FormatText;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.BuiltInSafetyProfiles;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey.KeyGroup;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.packet.PluginMessageS2C;
import com.tom.cpm.shared.network.packet.ReceiveEventS2C;
import com.tom.cpm.shared.network.packet.RecommendSafetyS2C;
import com.tom.cpm.shared.network.packet.SetSkinC2S;
import com.tom.cpm.shared.network.packet.SetSkinS2C;
import com.tom.cpm.shared.util.ScalingOptions;

public class NetworkUtil {
	public static final String FORCED_TAG = "forced";
	public static final String DATA_TAG = "data";
	public static final String PROFILE_TAG = "profile";
	public static final String PROFILE_DATA = "data";
	public static final String SERVER_CAPS = "caps";
	public static final String EVENT_LIST = "eventList";
	public static final String KICK_TIME = "kickTime";
	public static final String SCALING = "scaling";
	public static final String GESTURE = "gesture";
	public static final String ANIMATIONS = "anims";

	public static final FormatText FORCED_CHAT_MSG = new FormatText("chat.cpm.skinForced");

	public static <P> void sendPlayerData(NetHandler<?, P, ?> handler, P target, P to) {
		ServerNetH netTo = handler.getSNetH(to);
		PlayerData dt = handler.getSNetH(target).cpm$getEncodedModelData();
		if(dt == null)return;
		handler.sendPacketTo(netTo, writeSkinData(handler, dt, target));
		sendPlayerState(handler, target, dt, netTo);
	}

	private static <P> void sendPlayerState(NetHandler<?, P, ?> handler, P target, PlayerData dt, ServerNetH netTo) {
		if(dt.gestureData.length > 0) {
			NBTTagCompound evt = new NBTTagCompound();
			evt.setByteArray(NetworkUtil.GESTURE, dt.gestureData);
			handler.sendPacketTo(netTo, new ReceiveEventS2C(handler.getPlayerId(target), evt));
		}
		if(!dt.pluginStates.isEmpty()) {
			int id = handler.getPlayerId(target);
			dt.pluginStates.forEach((k, v) -> handler.sendPacketTo(netTo, new PluginMessageS2C(k, id, v)));
		}
	}

	public static <P> void sendPlayerState(NetHandler<?, P, ?> handler, P target, P to) {
		ServerNetH netTo = handler.getSNetH(to);
		PlayerData dt = handler.getSNetH(target).cpm$getEncodedModelData();
		if(dt == null)return;
		sendPlayerState(handler, target, dt, netTo);
	}

	public static <P> IPacket writeSkinData(NetHandler<?, P, ?> handler, PlayerData dt, P target) {
		NBTTagCompound data = new NBTTagCompound();
		if(dt.data != null) {
			data.setBoolean(FORCED_TAG, dt.forced);
			data.setByteArray(DATA_TAG, dt.data);
		}
		return new SetSkinS2C(handler.getPlayerId(target), data);
	}

	public static void sendSafetySettings(NetHandler<?, ?, ?> handler, ServerNetH net) {
		BuiltInSafetyProfiles profile = BuiltInSafetyProfiles.get(ModConfig.getWorldConfig().getString(ConfigKeys.SAFETY_PROFILE, BuiltInSafetyProfiles.MEDIUM.name().toLowerCase(Locale.ROOT)));
		if(profile != null) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString(PROFILE_TAG, profile.name().toLowerCase(Locale.ROOT));
			if(profile == BuiltInSafetyProfiles.CUSTOM) {
				Map<String, Object> map = new HashMap<>();
				ConfigEntry main = ModConfig.getWorldConfig().getEntry(ConfigKeys.SAFETY_SETTINGS);
				ConfigEntry ce = new ConfigEntry(map, () -> {});
				for(PlayerSpecificConfigKey<?> key : ConfigKeys.SAFETY_KEYS) {
					Object v = key.getValue(main, KeyGroup.GLOBAL);
					sendSafetySettings$setValue(ce, key, v);
				}
				tag.setString(PROFILE_DATA, MinecraftObjectHolder.gson.toJson(map));
			}
			handler.sendPacketTo(net, new RecommendSafetyS2C(tag));
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> void sendSafetySettings$setValue(ConfigEntry ce, PlayerSpecificConfigKey<T> key, Object value) {
		key.setValue(ce, (T) value);
	}

	public static ScalingSettings getScalingLimits(ScalingOptions o, String id) {
		ConfigEntry e = ModConfig.getWorldConfig();
		ConfigEntry pl = e.getEntry(ConfigKeys.PLAYER_SCALING_SETTINGS);
		ConfigEntry g = e.getEntry(ConfigKeys.SCALING_SETTINGS);
		String sId = o.name().toLowerCase(Locale.ROOT);
		String scaler = getValue(pl, g, id, sId, ConfigKeys.SCALING_METHOD, ConfigEntry::getString, null);
		if(!getValue(pl, g, id, sId, ConfigKeys.ENABLED, ConfigEntry::getBoolean, o.getDefualtEnabled()))
			return new ScalingSettings(1, 1, scaler);
		float min = getValue(pl, g, id, sId, ConfigKeys.MIN, ConfigEntry::getFloat, o.getMin());
		float max = getValue(pl, g, id, sId, ConfigKeys.MAX, ConfigEntry::getFloat, o.getMax());
		return new ScalingSettings(min, max, scaler);
	}

	public static class ScalingSettings {
		public final float min, max;
		public final String scaler;

		public ScalingSettings(float min, float max, String scaler) {
			this.min = min;
			this.max = max;
			this.scaler = scaler;
		}
	}

	private static <T> T getValue(ConfigEntry pl, ConfigEntry g, String id, String opt, String key, TriFunction<ConfigEntry, String, T, T> getter, T def) {
		if(pl.hasEntry(id)) {
			pl = pl.getEntry(id);
			if(pl.hasEntry(opt)) {
				pl = pl.getEntry(opt);
				if(pl.hasEntry(key))
					return getter.apply(pl, key, def);
			}
		}
		g = g.getEntry(opt);
		return getter.apply(g, key, def);
	}

	public static void sendSkinDataToServer(NetHandler<?, ?, ?> handler) {
		String model = ModConfig.getCommonConfig().getString(ConfigKeys.SELECTED_MODEL, null);
		if(model != null) {
			File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
			try {
				ModelFile file = ModelFile.load(new File(modelsDir, model));
				NBTTagCompound data = new NBTTagCompound();
				data.setByteArray(DATA_TAG, file.getDataBlock());
				file.registerLocalCache(MinecraftClientAccess.get().getDefinitionLoader());
				handler.sendPacketToServer(new SetSkinC2S(data));
			} catch (IOException e) {
				handler.sendPacketToServer(new SetSkinC2S(new NBTTagCompound()));
				//warn
			}
		} else {
			handler.sendPacketToServer(new SetSkinC2S(new NBTTagCompound()));
		}
	}
}
