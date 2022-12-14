package com.tom.cpm.shared.config;

import java.util.Base64;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.network.ModelEventType;
import com.tom.cpm.shared.util.ScalingOptions;

public class PlayerData {
	public long ticksSinceLogin;
	public byte[] data;
	public boolean forced, save;
	public Map<ScalingOptions, Float> scale = new EnumMap<>(ScalingOptions.class);
	public EnumSet<ModelEventType> eventSubs = EnumSet.noneOf(ModelEventType.class);
	public byte[] gestureData = new byte[0];
	public Map<String, NBTTagCompound> pluginStates = new HashMap<>();

	public PlayerData() {
	}

	public void setModel(byte[] data, boolean forced, boolean save) {
		this.data = data;
		this.forced = forced;
		this.save = save;
	}

	public void setModel(String data, boolean forced, boolean save) {
		this.data = data != null ? Base64.getDecoder().decode(data) : null;
		this.forced = forced;
		this.save = save;
	}

	public boolean canChangeModel() {
		return data == null || !forced;
	}

	public void load(String id) {
		ConfigEntry e = ModConfig.getWorldConfig().getEntry(ConfigKeys.SERVER_SKINS).getEntry(id);
		boolean forced = e.getBoolean(ConfigKeys.FORCED, false);
		String b64 = e.getString(ConfigKeys.MODEL, null);
		if(b64 != null) {
			setModel(b64, forced, true);
		}
		ConfigEntry sc = ModConfig.getWorldConfig().getEntry(ConfigKeys.PLAYER_SCALING);
		if(sc.hasEntry(id)) {
			sc = sc.getEntry(id);
			for(ScalingOptions opt : ScalingOptions.VALUES) {
				float v = sc.getFloat(opt.getNetKey(), 1F);
				if(v != 1)scale.put(opt, v);
			}
		}
	}

	public void save(String id) {
		ConfigEntry e = ModConfig.getWorldConfig().getEntry(ConfigKeys.SERVER_SKINS);
		if(save) {
			if(data == null)
				e.clearValue(id);
			else {
				e = e.getEntry(id);
				e.setString(ConfigKeys.MODEL, Base64.getEncoder().encodeToString(data));
				e.setBoolean(ConfigKeys.FORCED, forced);
			}
		} else {
			e.clearValue(id);
		}
		ConfigEntry sc = ModConfig.getWorldConfig().getEntry(ConfigKeys.PLAYER_SCALING).getEntry(id);
		sc.clear();
		scale.forEach((k, v) -> sc.setFloat(k.getNetKey(), v));
		ModConfig.getWorldConfig().save();
	}
}
