package com.tom.cpm.shared.editor.gui;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.util.TabFocusHandler;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.ModeDisplayType.ModePanel;
import com.tom.cpm.shared.editor.tree.TreeElement.VecType;

public class UVPanel extends ModePanel {
	private Spinner spinnerU, spinnerV, spinnerT;

	public UVPanel(Frame frm, Editor editor, TabFocusHandler tabHandler) {
		super(frm, editor, tabHandler);
		setBounds(new Box(0, 0, 170, 57));

		spinnerU = new Spinner(gui);
		spinnerV = new Spinner(gui);
		spinnerT = new Spinner(gui);
		Label lblU = new Label(gui, "U:");
		lblU.setBounds(new Box(5, 25, 50, 20));
		Label lblV = new Label(gui, "V:");
		lblV.setBounds(new Box(60, 25, 50, 20));
		Label lblT = new Label(gui, gui.i18nFormat("label.cpm.texSize"));
		lblT.setBounds(new Box(115, 25, 50, 20));

		spinnerU.setBounds(new Box(5, 35, 50, 20));
		spinnerV.setBounds(new Box(60, 35, 50, 20));
		spinnerT.setBounds(new Box(115, 35, 50, 20));
		spinnerU.setDp(0);
		spinnerV.setDp(0);
		spinnerT.setDp(0);

		Runnable r = () -> editor.setVec(new Vec3f(spinnerU.getValue(), spinnerV.getValue(), spinnerT.getValue()), VecType.TEXTURE);
		spinnerU.addChangeListener(r);
		spinnerV.addChangeListener(r);
		spinnerT.addChangeListener(r);

		tabHandler.add(spinnerU);
		tabHandler.add(spinnerV);
		tabHandler.add(spinnerT);

		editor.setTexturePanel.add(v -> {
			if(v != null) {
				spinnerU.setValue(v.x);
				spinnerV.setValue(v.y);
				spinnerT.setValue(v.z);
			}
		});
		addElement(spinnerU);
		addElement(spinnerV);
		addElement(spinnerT);
		addElement(lblU);
		addElement(lblV);
		addElement(lblT);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		spinnerU.setVisible(visible);
		spinnerV.setVisible(visible);
		spinnerT.setVisible(visible);
	}
}
