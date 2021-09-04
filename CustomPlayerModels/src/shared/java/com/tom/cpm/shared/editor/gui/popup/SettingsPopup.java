package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.panel.SettingsPanel;

public class SettingsPopup extends PopupPanel {
	private SettingsPanel panel;

	public SettingsPopup(Frame frm) {
		super(frm.getGui());

		panel = new SettingsPanel(frm, this, 400, 300, this::close);
		addElement(panel);

		setBounds(new Box(0, 0, 400, 300));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.edit.settings");
	}

	@Override
	public void onClosed() {
		if(EditorGui.rescaleGui)
			gui.setScale(ModConfig.getCommonConfig().getInt(ConfigKeys.EDITOR_SCALE, -1));
	}
}
