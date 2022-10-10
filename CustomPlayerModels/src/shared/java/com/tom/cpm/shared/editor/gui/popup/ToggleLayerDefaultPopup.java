package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.editor.Editor;

public class ToggleLayerDefaultPopup extends PopupPanel {
	private Editor editor;

	public ToggleLayerDefaultPopup(IGui gui, Editor e) {
		super(gui);
		this.editor = e;

		float ld = e.selectedAnim.layerDefault;

		Checkbox chbx = new Checkbox(gui, gui.i18nFormat("label.cpm.defLayerSettings.toggle"));
		chbx.setBounds(new Box(5, 5, 50, 20));
		chbx.setAction(() -> {
			chbx.setSelected(!chbx.isSelected());
		});
		chbx.setSelected(ld != 0);
		addElement(chbx);

		Button btn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			close();
			setDefaultLayerValue(chbx.isSelected() ? 1f : 0f);
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
