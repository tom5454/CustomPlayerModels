package com.tom.cpm.shared.gui.panel;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Slider;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.config.ConfigKeys;

public class MouseControlsPanel extends Panel {
	private final ConfigEntry ce;
	private Button buttonRMB;
	private Button buttonDMB;
	private Button buttonMMB;
	private Button buttonSMB;
	private Slider wheelS;

	public MouseControlsPanel(IGui gui, ConfigEntry ce) {
		super(gui);
		this.ce = ce;

		setBounds(new Box(5, 0, 250, 120));

		buttonRMB = new Button(gui, "", null);
		buttonDMB = new Button(gui, "", null);
		buttonMMB = new Button(gui, "", null);
		buttonSMB = new Button(gui, "", null);
		wheelS = new Slider(gui, "");

		buttonRMB.setAction(() -> {
			int b = ce.getSetInt(ConfigKeys.EDITOR_ROTATE_MOUSE_BUTTON, 2) - 1;
			int d = ce.getSetInt(ConfigKeys.EDITOR_DRAG_MOUSE_BUTTON, -1);
			if(b < 0)b = 2;
			if(b == d)ce.setInt(ConfigKeys.EDITOR_DRAG_MOUSE_BUTTON, -1);
			ce.setInt(ConfigKeys.EDITOR_ROTATE_MOUSE_BUTTON, b);
			updateMouseButtons();
		});
		buttonRMB.setBounds(new Box(0, 0, 250, 20));
		addElement(buttonRMB);

		buttonDMB.setAction(() -> {
			int b = ce.getSetInt(ConfigKeys.EDITOR_DRAG_MOUSE_BUTTON, -1) - 1;
			int r = ce.getSetInt(ConfigKeys.EDITOR_ROTATE_MOUSE_BUTTON, 2);
			if(b < -1)b = 2;
			if(b == r)b--;
			ce.setInt(ConfigKeys.EDITOR_DRAG_MOUSE_BUTTON, b);
			updateMouseButtons();
		});
		buttonDMB.setBounds(new Box(0, 25, 250, 20));
		addElement(buttonDMB);

		buttonMMB.setAction(() -> {
			int b = ce.getSetInt(ConfigKeys.EDITOR_MENU_MOUSE_BUTTON, 1) - 1;
			int r = ce.getSetInt(ConfigKeys.EDITOR_SELECT_MOUSE_BUTTON, 0);
			if(b < -1)b = 2;
			if(b == r)b--;
			ce.setInt(ConfigKeys.EDITOR_MENU_MOUSE_BUTTON, b);
			updateMouseButtons();
		});
		buttonMMB.setBounds(new Box(0, 50, 250, 20));
		addElement(buttonMMB);

		buttonSMB.setAction(() -> {
			int b = ce.getSetInt(ConfigKeys.EDITOR_SELECT_MOUSE_BUTTON, 0) - 1;
			int d = ce.getSetInt(ConfigKeys.EDITOR_MENU_MOUSE_BUTTON, 1);
			if(b < 0)b = 2;
			if(b == d)ce.setInt(ConfigKeys.EDITOR_MENU_MOUSE_BUTTON, -1);
			ce.setInt(ConfigKeys.EDITOR_SELECT_MOUSE_BUTTON, b);
			updateMouseButtons();
		});
		buttonSMB.setBounds(new Box(0, 75, 250, 20));
		addElement(buttonSMB);

		wheelS.setAction(() -> {
			updateWheelS();
			ce.setInt(ConfigKeys.MOUSE_WHEEL_SENSITIVITY, (int) ((wheelS.getValue() * 3.75F + 0.25F) * 100));
		});
		float wsv = ce.getInt(ConfigKeys.MOUSE_WHEEL_SENSITIVITY, 100) / 100f;
		wheelS.setValue((wsv - 0.5F) / 1.5F);
		wheelS.setBounds(new Box(0, 100, 250, 20));
		addElement(wheelS);

		updateMouseButtons();
		updateWheelS();
	}

	private void updateWheelS() {
		wheelS.setText(gui.i18nFormat("label.cpm.mouseWheelSensitivity", (int) ((wheelS.getValue() * 3.75F + 0.25F) * 100)));
	}

	private void updateMouseButtons() {
		buttonRMB.setText(gui.i18nFormat("button.cpm.config.rotateButton", getRotateBtn()));
		buttonDMB.setText(gui.i18nFormat("button.cpm.config.dragButton", getDragBtn()));
		buttonMMB.setText(gui.i18nFormat("button.cpm.config.menuButton", getMenuBtn()));
		buttonSMB.setText(gui.i18nFormat("button.cpm.config.selectButton", getSelectBtn()));
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
		if(d == -1)return gui.i18nFormat("label.cpm.keybind.mod.alt", getSelectBtn());
		else return gui.i18nFormat("label.cpm.mouse." + d);
	}

	private String getSelectBtn() {
		return gui.i18nFormat("label.cpm.mouse." + ce.getInt(ConfigKeys.EDITOR_SELECT_MOUSE_BUTTON, 0));
	}
}
