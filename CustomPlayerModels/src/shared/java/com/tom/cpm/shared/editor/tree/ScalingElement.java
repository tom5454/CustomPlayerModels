package com.tom.cpm.shared.editor.tree;

import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.PosPanel.ModeDisplType;

public class ScalingElement implements TreeElement {
	private Editor editor;
	private Tooltip tt;

	public ScalingElement(Editor editor) {
		this.editor = editor;
	}

	@Override
	public String getName() {
		return editor.gui().i18nFormat("label.cpm.display.scaling");
	}

	@Override
	public void updateGui() {
		editor.applyScaling = true;
		editor.setValue.accept(editor.scaling);
		editor.setModePanel.accept(ModeDisplType.VALUE);
	}

	@Override
	public Tooltip getTooltip() {
		if(tt == null)tt = new Tooltip(editor.frame, editor.gui().i18nFormat("tooltip.cpm.display.scaling"));
		return tt;
	}

	@Override
	public float getValue() {
		return editor.scaling;
	}

	@Override
	public void setValue(float value) {
		editor.scaling = value;
		if(editor.scaling > 10) {
			editor.scaling = 10;
			editor.setValue.accept(editor.scaling);
		}
		if(editor.scaling < 0.05f) {
			editor.scaling = 0.05f;
			editor.setValue.accept(editor.scaling);
		}
	}
}
