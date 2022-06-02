package com.tom.cpm.shared.gui;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.config.ModConfigFile.ConfigEntryTemp;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.Keybind;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.gui.panel.KeybindPanel;
import com.tom.cpm.shared.gui.panel.MouseControlsPanel;

public class KeybindsPopup extends PopupPanel {
	private final ConfigEntry ce;

	public KeybindsPopup(Frame frm, ConfigEntry ce) {
		super(frm.getGui());
		this.ce = ce;

		setBounds(new Box(0, 0, 350, 300));

		ScrollPanel scp = new ScrollPanel(gui);
		scp.setBounds(new Box(0, 5, 350, 290));
		addElement(scp);

		Panel panel = new Panel(gui);
		panel.setBounds(new Box(0, 0, 350, 0));
		scp.setDisplay(panel);

		FlowLayout layout = new FlowLayout(panel, 5, 1);

		for (Object kb : Keybinds.KEYBINDS) {
			if(kb == Keybinds.MOUSE_MARKER) {
				panel.addElement(new MouseControlsPanel(gui, ce));
			} else if(kb instanceof Keybind)
				panel.addElement(new KeybindPanel(frm, (Keybind) kb, ce));
			else
				panel.addElement(new Label(gui, gui.i18nFormat("label.cpm.keybinds." + kb)).setBounds(new Box(5, 0, 100, 10)));
		}

		MinecraftClientAccess.get().populatePlatformSettings("keybind", panel);

		layout.reflow();
	}

	public static KeybindsPopup create(Frame frm) {
		ConfigEntryTemp ce = ModConfig.getCommonConfig().createTemp();
		KeybindsPopup p = new KeybindsPopup(frm, ce);
		p.setBounds(new Box(0, 0, 350, 330));

		Button save = new Button(frm.getGui(), frm.getGui().i18nFormat("button.cpm.saveCfg"), () -> {
			ce.saveConfig();
			p.close();
		});
		save.setBounds(new Box(5, 300, 80, 20));
		p.addElement(save);

		return p;
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.keybinds.title");
	}

	private void updateMouseButtons() {

	}

	private String getRotateBtn() {
		return gui.i18nFormat("label.cpm.mouse." + ce.getInt(ConfigKeys.EDITOR_ROTATE_MOUSE_BUTTON, 2));
	}

	private String getDragBtn() {
		int d = ce.getInt(ConfigKeys.EDITOR_DRAG_MOUSE_BUTTON, -1);
		if(d == -1)return gui.i18nFormat("label.cpm.keybind.mod.shift", getRotateBtn());
		else return gui.i18nFormat("label.cpm.mouse." + d);
	}

	private String getMenuBtn() {
		int d = ce.getInt(ConfigKeys.EDITOR_MENU_MOUSE_BUTTON, -1);
		if(d == -1)return gui.i18nFormat("label.cpm.keybind.mod.ctrl", getRotateBtn());
		else return gui.i18nFormat("label.cpm.mouse." + d);
	}
}
