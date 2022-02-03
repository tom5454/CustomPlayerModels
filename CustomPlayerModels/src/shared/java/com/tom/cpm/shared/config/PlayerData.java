package com.tom.cpm.shared.config;

import java.util.Base64;
import java.util.EnumSet;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpm.shared.network.ModelEventType;

public class PlayerData {
	public long ticksSinceLogin;
	public byte[] data;
	public boolean forced, save;
	public float scale, eyeH, hitboxW, hitboxH;
	public EnumSet<ModelEventType> eventSubs = EnumSet.noneOf(ModelEventType.class);

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
		ModConfig.getWorldConfig().save();
	}
}
