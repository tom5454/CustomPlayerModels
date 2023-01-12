package com.tom.cpmoscc.gui;

import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpmoscc.CPMOSC;

public class OSCSettingsPopup extends PopupPanel {
	private OSCDataPanel data;

	public OSCSettingsPopup(EditorGui eg) {
		super(eg.getGui());

		Box b = eg.getBounds();
		setBounds(new Box(0, 0, b.w - 50, b.h - 50));

		Checkbox chbxEn = new Checkbox(gui, gui.i18nFormat("osc-label.cpmosc.oscEnable"));
		chbxEn.setBounds(new Box(5, 5, 100, 18));
		chbxEn.setSelected(ModConfig.getCommonConfig().getBoolean(CPMOSC.OSC_ENABLE, false));
		addElement(chbxEn);

		addElement(new Label(gui, gui.i18nFormat("osc-label.cpmosc.oscPort")).setBounds(new Box(106, 10, 20, 10)));

		Spinner oscPort = new Spinner(gui);
		oscPort.setValue(ModConfig.getCommonConfig().getInt(CPMOSC.OSC_PORT_KEY, 9000));
		oscPort.setDp(0);
		oscPort.setEnabled(chbxEn.isSelected());
		chbxEn.setAction(() -> {
			boolean v = !chbxEn.isSelected();
			chbxEn.setSelected(v);
			oscPort.setEnabled(v);
		});
		oscPort.setBounds(new Box(140, 4, 50, 20));
		oscPort.addChangeListener(() -> {
			int r = MathHelper.clamp((int) oscPort.getValue(), 1, 65535);
			if(Math.abs(oscPort.getValue() - r) > 0.1f)oscPort.setValue(r);
		});
		addElement(oscPort);

		data = new OSCDataPanel(gui, null, b.w - 60);

		Button save = new Button(gui, gui.i18nFormat("button.cpm.saveCfg"), () -> {
			ModConfig.getCommonConfig().setBoolean(CPMOSC.OSC_ENABLE, chbxEn.isSelected());
			ModConfig.getCommonConfig().setInt(CPMOSC.OSC_PORT_KEY, (int) oscPort.getValue());
			CPMOSC.resetOsc();
			data.reset();
		});
		save.setBounds(new Box(195, 4, 50, 20));
		addElement(save);

		ScrollPanel scp = new ScrollPanel(gui);
		scp.setDisplay(data);
		scp.setBounds(new Box(5, 25, b.w - 60, b.h - 80));
		addElement(scp);
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("osc-button.cpmosc.oscSettings");
	}

	@Override
	public void onClosed() {
		data.close();
	}
}
