package com.tom.cpm.shared.gui.panel;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey.KeyGroup;

public class SafetyPanel extends Panel {
	private final ConfigEntry ce;
	private final KeyGroup keyGroup;
	public final String uuid;
	public final ConfigEntry mainConfig;

	public SafetyPanel(IGui gui, ConfigEntry ce, int w, KeyGroup keyGroup, String uuid, ConfigEntry main) {
		super(gui);
		setBounds(new Box(0, 0, w, 0));
		this.ce = ce;
		this.uuid = uuid;
		this.keyGroup = keyGroup;
		this.mainConfig = main;

		FlowLayout layout = new FlowLayout(this, 5, 1);

		for (PlayerSpecificConfigKey<?> key : ConfigKeys.SAFETY_KEYS) {
			addElement(key.createConfigElement(this));
		}

		layout.reflow();
	}

	public ConfigEntry getConfig() {
		return ce;
	}

	public KeyGroup getKeyGroup() {
		return keyGroup;
	}
}
