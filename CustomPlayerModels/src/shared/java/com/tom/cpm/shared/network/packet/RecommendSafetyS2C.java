package com.tom.cpm.shared.network.packet;

import java.util.Locale;
import java.util.Map;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.text.KeybindText;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.BuiltInSafetyProfiles;
import com.tom.cpm.shared.config.ConfigChangeRequest;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey.KeyGroup;
import com.tom.cpm.shared.config.SocialConfig;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetworkUtil;

public class RecommendSafetyS2C extends NBTS2C {

	public RecommendSafetyS2C(NBTTagCompound data) {
		super(data);
	}

	public RecommendSafetyS2C() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handle(NetHandler<?, ?, ?> handler, NetH from) {
		String server = MinecraftClientAccess.get().getConnectedServer();
		ConfigEntry cc = ModConfig.getCommonConfig();
		ConfigEntry ss = cc.getEntry(ConfigKeys.SERVER_SETTINGS);
		if(server != null && ss.hasEntry(server)) {
			if(ss.getEntry(server).getBoolean(ConfigKeys.IGNORE_SAFETY_RECOMMENDATIONS, false))return;
		}

		BuiltInSafetyProfiles netProfile = BuiltInSafetyProfiles.get(tag.getString(NetworkUtil.PROFILE_TAG));

		ConfigEntry ce = null;
		if(netProfile == BuiltInSafetyProfiles.CUSTOM) {
			try {
				Map<String, Object> map = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(tag.getString(NetworkUtil.PROFILE_DATA), Object.class);
				ce = new ConfigEntry(map, () -> {});
			} catch (Exception e) {
				return;
			}
		}

		handler.getRecommendedSettingChanges().clear();
		for(PlayerSpecificConfigKey<?> k : ConfigKeys.SAFETY_KEYS) {
			Object rv = ce != null ? k.getValue(ce, KeyGroup.GLOBAL) : k.getValue(netProfile);
			Object sv = k.getValueFor(server, null, cc);
			if(!rv.equals(sv)) {
				handler.getRecommendedSettingChanges().add(new ConfigChangeRequest<>(k, sv, rv));
			}
		}
		if(!handler.getRecommendedSettingChanges().isEmpty()) {
			ConfigEntry gs = cc.getEntry(ConfigKeys.GLOBAL_SETTINGS);
			String[] spf = gs.getString(ConfigKeys.SAFETY_PROFILE, BuiltInSafetyProfiles.MEDIUM.name().toLowerCase(Locale.ROOT)).split(":", 2);
			BuiltInSafetyProfiles profile = SocialConfig.getProfile(spf);

			if(server != null && ss.hasEntry(server)) {
				ConfigEntry e = ss.getEntry(server);
				if(e.hasEntry(ConfigKeys.SAFETY_PROFILE)) {
					spf = e.getString(ConfigKeys.SAFETY_PROFILE, BuiltInSafetyProfiles.MEDIUM.name().toLowerCase(Locale.ROOT)).split(":", 2);
					profile = SocialConfig.getProfile(spf);
				}
			}

			String old;
			if(profile == BuiltInSafetyProfiles.CUSTOM) {
				old = "custom:" + spf[1];
			} else {
				old = profile.name().toLowerCase(Locale.ROOT);
			}

			if(netProfile == BuiltInSafetyProfiles.CUSTOM) {
				handler.getRecommendedSettingChanges().add(new ConfigChangeRequest<>(ConfigKeys.SAFETY_PROFILE, old, "custom:import-" + server));
			} else {
				handler.getRecommendedSettingChanges().add(new ConfigChangeRequest<>(ConfigKeys.SAFETY_PROFILE, old, netProfile.name().toLowerCase(Locale.ROOT)));
			}

			handler.displayText(new FormatText("chat.cpm.serverSafetySettings", new KeybindText("key.cpm.gestureMenu", "gestureMenu")));
		}
	}
}
