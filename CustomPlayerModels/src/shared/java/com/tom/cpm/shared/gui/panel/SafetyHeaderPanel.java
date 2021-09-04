package com.tom.cpm.shared.gui.panel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.InputPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.config.BuiltInSafetyProfiles;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey.KeyGroup;
import com.tom.cpm.shared.config.SocialConfig;

public class SafetyHeaderPanel extends Panel {
	private ConfigEntry main;
	private SafetyPanel sp;
	private Frame frm;
	private Button btnProfile, btnDelP;
	private Panel namePanel;
	private KeyGroup keyGroup;

	public SafetyHeaderPanel(Frame frm, ConfigEntry ce, int w, String title, Map<String, Object> fields, KeyGroup keyGroup, ConfigEntry main) {
		super(frm.getGui());
		this.frm = frm;
		this.keyGroup = keyGroup;
		setBounds(new Box(0, 0, w, 0));
		this.main = main;

		FlowLayout layout = new FlowLayout(this, 5, 1);

		addElement(new Label(gui, title).setBounds(new Box(1, 0, 100, 10)));

		if(fields.containsKey("nameBox")) {
			Panel panel = new Panel(gui);
			addElement(panel);
			panel.setBounds(new Box(0, 0, 170, 30));

			panel.addElement(new Label(gui, gui.i18nFormat("label.cpm.name")).setBounds(new Box(5, 0, 0, 0)));

			TextField txt = new TextField(gui);
			txt.setText(ce.getString(ConfigKeys.NAME, (String) fields.get("nameBox")));
			txt.setBounds(new Box(5, 10, 160, 20));
			panel.addElement(txt);
			txt.setEventListener(() -> ce.setString(ConfigKeys.NAME, txt.getText()));
		}

		if(fields.containsKey("safetyBtn")) {
			List<String> profiles = new ArrayList<>();
			for(BuiltInSafetyProfiles p : BuiltInSafetyProfiles.VALUES) {
				if(p == BuiltInSafetyProfiles.CUSTOM)continue;
				profiles.add(p.name().toLowerCase());
			}
			ConfigEntry spfs = (ConfigEntry) fields.get("safetyBtn");
			spfs.keySet().stream().map(e -> "custom:" + e).forEach(profiles::add);

			Supplier<String> current = () -> ce.getString(ConfigKeys.SAFETY_PROFILE, BuiltInSafetyProfiles.MEDIUM.name().toLowerCase());

			btnProfile = new Button(gui, "", null);
			btnProfile.setBounds(new Box(5, 0, w - 10, 20));

			Panel ctrls = new Panel(gui);
			ctrls.setBounds(new Box(0, 0, w, 20));

			Button newP = new Button(gui, gui.i18nFormat("button.cpm.safety.newProfile"), new InputPopup(frm, gui.i18nFormat("label.cpm.name"), gui.i18nFormat("label.cpm.safetyProfileName"), name -> {
				if(!name.isEmpty()) {
					String id = UUID.randomUUID().toString();
					String profile = "custom:" + id;
					profiles.add(profile);
					initNewProfile(spfs, id, name, current.get());
					ce.setString(ConfigKeys.SAFETY_PROFILE, profile);
					setupProfile(profile, spfs);
					layout.reflow();
				}
			}, null));
			newP.setBounds(new Box(5, 0, 70, 20));
			ctrls.addElement(newP);

			btnDelP = new Button(gui, gui.i18nFormat("button.cpm.safety.deleteProfile"), new ConfirmPopup(frm, gui.i18nFormat("label.cpm.confirmDel"), () -> {
				String[] spf = current.get().split(":");
				BuiltInSafetyProfiles profile = SocialConfig.getProfile(spf);
				if(profile == BuiltInSafetyProfiles.CUSTOM) {
					String profileN = profiles.get((profiles.indexOf(current.get()) + 1) % profiles.size());
					ce.setString(ConfigKeys.SAFETY_PROFILE, profileN);
					setupProfile(profileN, spfs);
					layout.reflow();
					profiles.remove("custom:" + spf[1]);
					spfs.clearValue(spf[1]);
				}
			}, null));
			btnDelP.setBounds(new Box(80, 0, 70, 20));
			ctrls.addElement(btnDelP);

			addElement(btnProfile);
			addElement(ctrls);

			btnProfile.setAction(() -> {
				String profile = profiles.get((profiles.indexOf(current.get()) + 1) % profiles.size());
				ce.setString(ConfigKeys.SAFETY_PROFILE, profile);
				setupProfile(profile, spfs);
				layout.reflow();
			});
			setupProfile(current.get(), spfs);
		} else {
			addElement(new SafetyPanel(gui, ce, w, keyGroup, null, main));
		}

		layout.reflow();
	}

	private void initNewProfile(ConfigEntry spfs, String id, String name, String currentProfile) {
		ConfigEntry ce = spfs.getEntry(id);
		ce.setString(ConfigKeys.NAME, name);
		String[] spf = currentProfile.split(":");
		BuiltInSafetyProfiles profile = SocialConfig.getProfile(spf);
		if(profile == BuiltInSafetyProfiles.CUSTOM) {
			ConfigEntry old = spfs.getEntry(spf[1]);
			for(PlayerSpecificConfigKey<?> key : ConfigKeys.SAFETY_KEYS) {
				key.copyValue(old, ce);
			}
		} else {
			profile.copyTo(ce);
		}
	}

	private void setupProfile(String profileIn, ConfigEntry spfs) {
		String[] spf = profileIn.split(":");
		BuiltInSafetyProfiles profile = SocialConfig.getProfile(spf);
		if(sp != null)remove(sp);
		if(namePanel != null)remove(namePanel);
		String txt;
		boolean custom = profile == BuiltInSafetyProfiles.CUSTOM;
		btnDelP.setEnabled(custom);
		if(custom) {
			ConfigEntry ce = spfs.getEntry(spf[1]);
			sp = new SafetyPanel(gui, ce, bounds.w, keyGroup, null, main);
			String name = ce.getString(ConfigKeys.NAME, "???");
			txt = gui.i18nFormat("label.cpm.safetyProfile.custom", name);

			namePanel = new Panel(gui);
			addElement(namePanel);
			namePanel.setBounds(new Box(0, 0, 170, 30));

			namePanel.addElement(new Label(gui, gui.i18nFormat("label.cpm.name")).setBounds(new Box(5, 0, 0, 0)));

			TextField txtF = new TextField(gui);
			txtF.setText(name);
			txtF.setBounds(new Box(5, 10, 160, 20));
			namePanel.addElement(txtF);
			txtF.setEventListener(() -> ce.setString(ConfigKeys.NAME, txtF.getText()));
		} else {
			ConfigEntry dummy = new ConfigEntry(new HashMap<>(), () -> {});
			profile.copyTo(dummy);
			sp = new SafetyPanel(gui, dummy, bounds.w, keyGroup, null, main);
			sp.setEnabled(false);
			txt = gui.i18nFormat("label.cpm.safetyProfile." + profile.name().toLowerCase());
		}
		btnProfile.setText(gui.i18nFormat("button.cpm.safetyProfile", txt));
		btnProfile.setTooltip(!custom ? new Tooltip(frm, gui.i18nFormat("tooltip.cpm.safetyProfile." + profile.name().toLowerCase())) : null);
		addElement(sp);
	}
}
