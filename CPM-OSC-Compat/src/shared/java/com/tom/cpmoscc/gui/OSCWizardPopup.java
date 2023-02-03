package com.tom.cpmoscc.gui;

import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.gui.util.TabFocusHandler;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.interpolator.InterpolatorType;
import com.tom.cpm.shared.editor.anim.AnimationProperties;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.gui.popup.AnimationSettingsPopup;
import com.tom.cpmoscc.OSCMapping;
import com.tom.cpmoscc.gui.OSCDataPanel.OSCChannel;

public class OSCWizardPopup extends PopupPanel {
	private TextField pathField, firstArgField;
	private Spinner argId, min, max;
	private Checkbox chbxValRange;

	public OSCWizardPopup(EditorGui eg) {
		super(eg.getGui());

		TabFocusHandler tabHandler = new TabFocusHandler(gui);

		setBounds(new Box(0, 0, 210, 300));

		FlowLayout layout = new FlowLayout(this, 5, 1);

		Button selectFromIn = new Button(gui, gui.i18nFormat("osc-button.cpmosc.selectFromInputs"), () -> {
			eg.openPopup(new OSCSelectPopup(eg, this::loadChannel));
		});
		selectFromIn.setBounds(new Box(5, 0, 180, 20));
		addElement(selectFromIn);

		Button importFromSelected = new Button(gui, gui.i18nFormat("osc-button.cpmosc.importFromSelected"), () -> {
			EditorAnim anim = eg.getEditor().selectedAnim;
			if(anim != null) {
				OSCMapping mapping = new OSCMapping(anim.displayName);
				if(mapping.getOscPacketId() != null) {
					loadChannel(mapping.toChannel());
				}
			}
		});
		importFromSelected.setBounds(new Box(5, 0, 180, 20));
		importFromSelected.setEnabled(eg.getEditor().selectedAnim != null);
		addElement(importFromSelected);

		addElement(new Label(gui, gui.i18nFormat("osc-label.cpmosc.oscPath")).setBounds(new Box(5, 0, 200, 10)));

		pathField = new TextField(gui);
		pathField.setBounds(new Box(5, 0, 160, 20));
		addElement(pathField);
		tabHandler.add(pathField);

		Label lblFirstArg = new Label(gui, gui.i18nFormat("osc-label.cpmosc.oscFirstArg"));
		lblFirstArg.setBounds(new Box(5, 0, 200, 10));
		lblFirstArg.setTooltip(new Tooltip(eg, gui.i18nFormat("osc-tooltip.cpmosc.oscFirstArg")));
		addElement(lblFirstArg);

		firstArgField = new TextField(gui);
		firstArgField.setBounds(new Box(5, 0, 160, 20));
		addElement(firstArgField);
		tabHandler.add(firstArgField);

		addElement(new Label(gui, gui.i18nFormat("osc-label.cpmosc.oscArgId")).setBounds(new Box(5, 0, 200, 10)));

		argId = new Spinner(gui);
		argId.setBounds(new Box(5, 10, 160, 18));
		argId.setDp(0);
		argId.addChangeListener(() -> {
			if(argId.getValue() < 0)argId.setValue(0);
		});
		addElement(argId);
		tabHandler.add(argId);

		chbxValRange = new Checkbox(gui, gui.i18nFormat("osc-label.cpmosc.valueRange"));
		chbxValRange.setBounds(new Box(5, 0, 160, 18));
		addElement(chbxValRange);

		Panel p = new Panel(gui);
		p.setBounds(new Box(0, 0, 160, 18));
		addElement(p);

		min = new Spinner(gui);
		min.setBounds(new Box(5, 0, 70, 18));
		min.setDp(2);
		p.addElement(min);
		tabHandler.add(min);
		min.setEnabled(false);

		p.addElement(new Label(gui, "~").setBounds(new Box(77, 5, 10, 10)));

		max = new Spinner(gui);
		max.setBounds(new Box(85, 0, 70, 18));
		max.setDp(2);
		p.addElement(max);
		tabHandler.add(max);
		max.setEnabled(false);
		max.setValue(1);

		chbxValRange.setAction(() -> {
			boolean v = !chbxValRange.isSelected();
			chbxValRange.setSelected(v);
			min.setEnabled(v);
			max.setEnabled(v);
		});

		p = new Panel(gui);
		p.setBounds(new Box(0, 0, 200, 20));
		addElement(p);

		Button ok = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			close();
			eg.getEditor().addNewAnim(new AnimationProperties(null, createPath(), AnimationType.LAYER, true, false, InterpolatorType.POLY_LOOP, true, false));
			eg.openPopup(new AnimationSettingsPopup(gui, eg.getEditor(), true));
		});
		ok.setBounds(new Box(5, 0, 80, 20));
		p.addElement(ok);

		Button copyName = new Button(gui, gui.i18nFormat("osc-button.cpmosc.copyName"), () -> {
			close();
			gui.setClipboardText(createPath());
		});
		copyName.setBounds(new Box(90, 0, 80, 20));
		p.addElement(copyName);

		layout.run();
		addElement(tabHandler);
	}

	private void loadChannel(OSCChannel c) {
		pathField.setText(c.address);
		if(c.arg1 != null)
			firstArgField.setText(c.arg1);
		else
			firstArgField.setText("");
		argId.setValue(c.argId);
		min.setValue(c.min);
		max.setValue(c.max);
		chbxValRange.setSelected(true);
		min.setEnabled(true);
		max.setEnabled(true);
	}

	private String createPath() {
		StringBuilder sb = new StringBuilder("osc:");
		sb.append(pathField.getText());
		if(argId.getValue() != 0 || !firstArgField.getText().isEmpty()) {
			sb.append('[');
			boolean col = false;
			if(argId.getValue() != 0) {
				sb.append((int) argId.getValue());
				col = true;
			}
			if(!firstArgField.getText().isEmpty()) {
				if(col)sb.append(':');
				sb.append(firstArgField.getText());
			}
			sb.append(']');
		}
		if(chbxValRange.isSelected()) {
			sb.append('(');
			sb.append(String.format("%.2f", min.getValue()));
			sb.append(':');
			sb.append(String.format("%.2f", max.getValue()));
			sb.append(')');
		}
		return sb.toString();
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("osc-button.cpmosc.oscWizard");
	}
}
