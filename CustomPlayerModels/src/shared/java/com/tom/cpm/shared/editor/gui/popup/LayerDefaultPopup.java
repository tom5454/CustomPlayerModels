package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Slider;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.actions.ActionBuilder;

public abstract class LayerDefaultPopup extends PopupPanel {
	protected Editor editor;
	private Checkbox chbxProp;

	protected LayerDefaultPopup(IGui gui, Editor e, int height) {
		super(gui);
		this.editor = e;

		chbxProp = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_is_property"));
		String tt = gui.i18nFormat("tooltip.cpm.anim_is_property");
		if(e.modelId == null)chbxProp.setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.anim_is_property.disable", tt)));
		else chbxProp.setTooltip(new Tooltip(gui.getFrame(), tt));
		chbxProp.setBounds(new Box(5, height - 50, 160, 20));
		chbxProp.setEnabled(e.modelId != null);
		chbxProp.setAction(() -> {
			chbxProp.setSelected(!chbxProp.isSelected());
		});
		chbxProp.setSelected(e.selectedAnim.isProperty);
		addElement(chbxProp);

		Button btn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			close();
			apply();
		});
		btn.setBounds(new Box(5, height - 25, 50, 20));
		addElement(btn);

		setBounds(new Box(0, 0, 180, height));
	}

	protected abstract void apply();

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.defLayerSettings");
	}

	protected ActionBuilder setDefaultLayerValue(float v) {
		return editor.action("set", gui.i18nFormat("button.cpm.defLayerSettings")).
				updateValueOp(editor.selectedAnim, editor.selectedAnim.layerDefault, v, (a, b) -> a.layerDefault = b).
				updateValueOp(editor.selectedAnim, editor.selectedAnim.isProperty, chbxProp.isSelected(), (a, b) -> a.isProperty = b);
	}

	public static class Toggle extends LayerDefaultPopup {
		private Checkbox chbx;
		private TextField groupField;

		public Toggle(IGui gui, Editor e) {
			super(gui, e, 115);

			float ld = e.selectedAnim.layerDefault;
			chbx = new Checkbox(gui, gui.i18nFormat("label.cpm.defLayerSettings.toggle"));
			chbx.setBounds(new Box(5, 5, 160, 20));
			chbx.setAction(() -> {
				chbx.setSelected(!chbx.isSelected());
			});
			chbx.setSelected(ld != 0);
			addElement(chbx);

			Label lbl = new Label(gui, gui.i18nFormat("label.cpm.layerGroup"));
			lbl.setBounds(new Box(5, 30, 160, 10));
			lbl.setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.layerGroup")));
			addElement(lbl);

			groupField = new TextField(gui);
			groupField.setBounds(new Box(5, 40, 160, 20));
			if(e.selectedAnim.group != null)groupField.setText(e.selectedAnim.group);
			addElement(groupField);
		}

		@Override
		protected void apply() {
			setDefaultLayerValue(chbx.isSelected() ? 1f : 0f).
			updateValueOp(editor.selectedAnim, editor.selectedAnim.group, groupField.getText().isEmpty() ? null : groupField.getText(), (a, b) -> a.group = b).
			execute();
		}
	}

	public static class Value extends LayerDefaultPopup {
		private Slider progressSlider;
		private Spinner maxValueSpinner;
		private Checkbox interpolate;

		public Value(IGui gui, Editor e) {
			super(gui, e, 130);

			float ld = e.selectedAnim.layerDefault;
			progressSlider = new Slider(gui, gui.i18nFormat("label.cpm.defLayerSettings.value", (int) (ld * e.selectedAnim.maxValue)));
			progressSlider.setBounds(new Box(5, 5, 160, 20));
			progressSlider.setValue(ld);
			progressSlider.setAction(() -> {
				progressSlider.setText(gui.i18nFormat("label.cpm.defLayerSettings.value", (int) (progressSlider.getValue() * e.selectedAnim.maxValue)));
			});
			progressSlider.setSteps(1f / e.selectedAnim.maxValue);
			addElement(progressSlider);

			addElement(new Label(gui, gui.i18nFormat("label.cpm.defLayerSettings.max")).setBounds(new Box(5, 30, 0, 0)));
			maxValueSpinner = new Spinner(gui);
			maxValueSpinner.setBounds(new Box(95, 30, 70, 20));
			maxValueSpinner.setDp(0);
			maxValueSpinner.setValue(e.selectedAnim.maxValue);
			maxValueSpinner.addChangeListener(() -> {
				int v = (int) maxValueSpinner.getValue();
				if (v < 1)maxValueSpinner.setValue(1);
				else if(v > 255)maxValueSpinner.setValue(255);
				progressSlider.setSteps(1f / v);
				progressSlider.setText(gui.i18nFormat("label.cpm.defLayerSettings.value", (int) (progressSlider.getValue() * v)));
			});
			addElement(maxValueSpinner);

			interpolate = new Checkbox(gui, gui.i18nFormat("label.cpm.defLayerSettings.interpolate"));
			interpolate.setBounds(new Box(5, 55, 160, 20));
			interpolate.setAction(() -> {
				interpolate.setSelected(!interpolate.isSelected());
			});
			interpolate.setSelected(e.selectedAnim.interpolateValue);
			addElement(interpolate);
		}

		@Override
		protected void apply() {
			setDefaultLayerValue(progressSlider.getValue()).
			updateValueOp(editor.selectedAnim, editor.selectedAnim.maxValue, (int) maxValueSpinner.getValue(), (a, b) -> a.maxValue = b).
			updateValueOp(editor.selectedAnim, editor.selectedAnim.interpolateValue, interpolate.isSelected(), (a, b) -> a.interpolateValue = b).
			execute();
		}
	}
}
