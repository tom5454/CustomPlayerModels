package com.tom.cpm.shared.editor.gui;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.gui.IGui;

public class ViewportPanelAnim extends ViewportPanel {

	public ViewportPanelAnim(IGui gui, Editor editor) {
		super(gui, editor);
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		editor.applyAnim = true;
		super.draw(mouseX, mouseY, partialTicks);
		editor.applyAnim = false;
	}
}
