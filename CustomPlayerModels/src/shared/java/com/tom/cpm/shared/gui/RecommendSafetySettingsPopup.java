package com.tom.cpm.shared.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.math.Box;
import com.tom.cpl.text.IText;
import com.tom.cpl.util.Util;
import com.tom.cpm.shared.config.BuiltInSafetyProfiles;
import com.tom.cpm.shared.config.ConfigChangeRequest;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey;
import com.tom.cpm.shared.config.SocialConfig;

public class RecommendSafetySettingsPopup extends PopupPanel {
	private String server;
	private List<ConfigChangeRequest<?, ?>> changes;

	protected RecommendSafetySettingsPopup(IGui gui, String server, List<ConfigChangeRequest<?, ?>> ch) {
		super(gui);

		this.server = server;
		this.changes = ch;

		List<String> text = new ArrayList<>();

		for (ConfigChangeRequest<?, ?> r : ch) {
			if(r.getKey().equals(ConfigKeys.SAFETY_PROFILE)) {
				text.addAll(Arrays.asList(gui.i18nFormat("label.cpm.recSettingsChange.title").split("\\\\")));
				text.add("");
				text.add(gui.i18nFormat("label.cpm.recSettingsChange", gui.i18nFormat("label.cpm.safetyProfileName"), translateProfile((String) r.getOldValue()), translateProfile((String) r.getNewValue())));
			} else if(r.getKey().equals(ConfigKeys.DISABLE_NETWORK)) {
				text.addAll(Arrays.asList(gui.i18nFormat("label.cpm.recSettingsChange.net").split("\\\\")));
				text.add("");
				text.add(gui.i18nFormat("label.cpm.recSettingsChange", gui.i18nFormat("label.cpm.safety.blockNetwork"), r.getOldValue(), r.getNewValue()));
			}
		}

		for (ConfigChangeRequest<?, ?> r : ch) {
			if(r.getKey() instanceof PlayerSpecificConfigKey) {
				PlayerSpecificConfigKey<?> key = (PlayerSpecificConfigKey<?>) r.getKey();
				String oldV = formatValue(key, r.getOldValue()).toString(gui);
				String newV = formatValue(key, r.getNewValue()).toString(gui);
				text.add(gui.i18nFormat("label.cpm.recSettingsChange", gui.i18nFormat("label.cpm.safety." + key.getName()), oldV, newV));
			}
		}

		int wm = 310;

		for (String line : text) {
			int w = gui.textWidth(line);
			if(w > wm)wm = w;
		}

		for (int i = 0; i < text.size(); i++) {
			String line = text.get(i);
			int w = gui.textWidth(line);
			addElement(new Label(gui, line).setBounds(new Box(wm / 2 - w / 2 + 10, 5 + i * 10, 0, 0)));
		}
		int txs = text.size() * 10;
		setBounds(new Box(0, 0, wm + 20, 40 + txs));

		Button accept = new Button(gui, gui.i18nFormat("button.cpm.safety.accept"), this::accept);
		accept.setBounds(new Box(5, txs + 15, 80, 20));
		addElement(accept);

		Button decline = new Button(gui, gui.i18nFormat("button.cpm.safety.decline"), this::decline);
		decline.setBounds(new Box(90, txs + 15, 80, 20));
		addElement(decline);

		Button ignore = new Button(gui, gui.i18nFormat("button.cpm.safety.ignore"), this::ignore);
		ignore.setBounds(new Box(175, txs + 15, 140, 20));
		addElement(ignore);
	}

	private void accept() {
		ConfigEntry cc = ModConfig.getCommonConfig();
		ConfigEntry ss = cc.getEntry(ConfigKeys.SERVER_SETTINGS);
		ConfigEntry se = ss.getEntry(server);

		String[] spf = null;
		BuiltInSafetyProfiles profile = null;
		for (ConfigChangeRequest<?, ?> r : changes) {
			if(r.getKey().equals(ConfigKeys.SAFETY_PROFILE)) {
				spf = ((String) r.getNewValue()).split(":");
				profile = SocialConfig.getProfile(spf);
				break;
			} else if(r.getKey().equals(ConfigKeys.DISABLE_NETWORK)) {
				se.setBoolean(ConfigKeys.DISABLE_NETWORK, false);
				ModConfig.getCommonConfig().save();
				changes.clear();
				close();
				return;
			}
		}

		if(profile == BuiltInSafetyProfiles.CUSTOM) {
			ConfigEntry ce = cc.getEntry(ConfigKeys.SAFETY_PROFILES).getEntry(spf[1]);
			se.setString(ConfigKeys.SAFETY_PROFILE, "custom:" + spf[1]);
			ce.setString(ConfigKeys.NAME, gui.i18nFormat("label.cpm.importedSettings", Util.hideIp(server)));
			ce.setBoolean(ConfigKeys.IMPORTED, true);
			for(PlayerSpecificConfigKey<?> k : ConfigKeys.SAFETY_KEYS) {
				boolean setV = false;
				for (ConfigChangeRequest<?, ?> r : changes) {
					if(r.getKey() == k) {
						setV = true;
						setValue(ce, k, r.getNewValue());
						break;
					}
				}
				if(!setV) {
					setValue(ce, k, k.getValueFor(server, null, cc));
				}
			}
		} else if(profile != null) {
			se.setString(ConfigKeys.SAFETY_PROFILE, profile.name().toLowerCase());
		}
		ModConfig.getCommonConfig().save();
		changes.clear();
		close();
	}

	private void decline() {
		changes.clear();
		close();
	}

	private void ignore() {
		changes.clear();
		ConfigEntry cc = ModConfig.getCommonConfig();
		ConfigEntry ss = cc.getEntry(ConfigKeys.SERVER_SETTINGS);
		ss.getEntry(server).setBoolean(ConfigKeys.IGNORE_SAFETY_RECOMMENDATIONS, true);
		ModConfig.getCommonConfig().save();
		close();
	}

	@SuppressWarnings("unchecked")
	private static <T> IText formatValue(PlayerSpecificConfigKey<T> key, Object value) {
		return key.formatValue((T) value);
	}

	@SuppressWarnings("unchecked")
	private static <T> void setValue(ConfigEntry ce, PlayerSpecificConfigKey<T> key, Object value) {
		key.setValue(ce, (T) value);
	}

	private String translateProfile(String p) {
		String[] spf = p.split(":");
		BuiltInSafetyProfiles profile = SocialConfig.getProfile(spf);
		if(profile == BuiltInSafetyProfiles.CUSTOM) {
			ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.SAFETY_PROFILES).getEntry(spf[1]);
			String name = ce.getString(ConfigKeys.NAME, "???");
			return gui.i18nFormat("label.cpm.safetyProfile.custom", name);
		} else {
			return gui.i18nFormat("label.cpm.safetyProfile." + profile.name().toLowerCase());
		}
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.serverRecommendsSafetySettings");
	}
}
