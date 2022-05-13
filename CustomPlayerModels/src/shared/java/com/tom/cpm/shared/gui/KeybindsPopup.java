package com.tom.cpm.shared.gui;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.Keybind;

public class KeybindsPopup extends PopupPanel {

	public KeybindsPopup(Frame frm, ConfigEntry ce) {
		super(frm.getGui());

		setBounds(new Box(0, 0, 350, 300));

		ScrollPanel scp = new ScrollPanel(gui);
		scp.setBounds(new Box(0, 5, 350, 290));
		addElement(scp);

		Panel panel = new Panel(gui);
		panel.setBounds(new Box(0, 0, 350, 0));
		scp.setDisplay(panel);

		FlowLayout layout = new FlowLayout(panel, 5, 1);

		for (Object kb : Keybinds.KEYBINDS) {
			if(kb instanceof Keybind)
				panel.addElement(new KeybindPanel(frm, (Keybind) kb, ce));
			else
				panel.addElement(new Label(gui, gui.i18nFormat("label.cpm.keybinds." + kb)).setBounds(new Box(5, 0, 100, 10)));
		}

		MinecraftClientAccess.get().populatePlatformSettings("keybind", panel);

		layout.reflow();
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.keybinds.title");
	}
}
