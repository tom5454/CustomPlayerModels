package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpm.shared.config.ConfigEntry.ModConfig;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.Frame;
import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.Checkbox;
import com.tom.cpm.shared.gui.elements.PopupPanel;
import com.tom.cpm.shared.gui.elements.Tooltip;
import com.tom.cpm.shared.math.Box;

public class SettingsPopup extends PopupPanel {

	public SettingsPopup(Frame frm) {
		super(frm.getGui());

		Checkbox chxbxTSBtn = new Checkbox(gui, gui.i18nFormat("label.cpm.config.titleScreenButton"));
		chxbxTSBtn.setSelected(ModConfig.getConfig().getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true));
		chxbxTSBtn.setAction(() -> {
			boolean b = !ModConfig.getConfig().getBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true);
			chxbxTSBtn.setSelected(b);
			ModConfig.getConfig().setBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, b);
		});
		chxbxTSBtn.setBounds(new Box(5, 5, 200, 20));
		chxbxTSBtn.setTooltip(new Tooltip(frm, gui.i18nFormat("tooltip.cpm.config.titleScreenButton")));
		addElement(chxbxTSBtn);

		Button buttonRMB = new Button(gui, gui.i18nFormat("button.cpm.config.rotateButton", getRotateBtn()), null);
		buttonRMB.setAction(() -> {
			ModConfig.getConfig().setInt(ConfigKeys.EDITOR_ROTATE_MOUSE_BUTTON, EditorGui.getRotateMouseButton() == 2 ? 1 : 2);
			buttonRMB.setText(gui.i18nFormat("button.cpm.config.rotateButton", getRotateBtn()));
		});
		buttonRMB.setBounds(new Box(5, 30, 200, 20));
		addElement(buttonRMB);

		setBounds(new Box(0, 0, 210, 100));
	}

	private String getRotateBtn() {
		return gui.i18nFormat("button.cpm.config.rotateButton." + EditorGui.getRotateMouseButton());
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.edit.settings");
	}
}
