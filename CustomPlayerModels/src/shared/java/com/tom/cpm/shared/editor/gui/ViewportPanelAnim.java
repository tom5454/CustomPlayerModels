package com.tom.cpm.shared.editor.gui;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpm.shared.editor.Editor;

public class ViewportPanelAnim extends ViewportPanel {

	public ViewportPanelAnim(IGui gui, Editor editor) {
		super(gui, editor);
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		editor.applyAnim = true;
		super.draw(event, partialTicks);
		editor.applyAnim = false;
	}
}
