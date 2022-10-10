package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Slider;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.editor.Editor;

public class ValueLayerDefaultPopup extends PopupPanel {
	private Editor editor;

	public ValueLayerDefaultPopup(IGui gui, Editor e) {
		super(gui);
		this.editor = e;

		float ld = e.selectedAnim.layerDefault;

		Slider progressSlider = new Slider(gui, gui.i18nFormat("label.cpm.defLayerSettings.value", (int) (ld * 100)));
		progressSlider.setBounds(new Box(5, 5, 160, 20));
		progressSlider.setValue(ld);
		progressSlider.setAction(() -> {
			progressSlider.setText(gui.i18nFormat("label.cpm.defLayerSettings.value", (int) (progressSlider.getValue() * 100)));
		});
		addElement(progressSlider);

		Button btn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			close();
			setDefaultLayerValue(progressSlider.getValue());
		});
		btn.setBounds(new Box(5, 30, 50, 20));
		addElement(btn);

		setBounds(new Box(0, 0, 180, 55));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.defLayerSettings");
	}

	private void setDefaultLayerValue(float v) {
		editor.action("set", gui.i18nFormat("button.cpm.defLayerSettings")).
		updateValueOp(editor.selectedAnim, editor.selectedAnim.layerDefault, v, (a, b) -> a.layerDefault = b).
		execute();
	}
}
