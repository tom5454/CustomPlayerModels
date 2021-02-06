package com.tom.cpm.shared.editor.gui;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.popup.ColorButton;
import com.tom.cpm.shared.editor.gui.popup.SkinSettingsPopup;
import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.ButtonIcon;
import com.tom.cpm.shared.gui.elements.Panel;
import com.tom.cpm.shared.math.Box;

public class DrawToolsPanel extends Panel {
	private Editor editor;
	private ColorButton colorBtn;
	private ButtonIcon pen, rubber;
	public DrawToolsPanel(EditorGui e, int x, int y, int w, int h) {
		super(e.getGui());
		this.editor = e.getEditor();
		setBounds(new Box(x, y, w, h));
		setBackgroundColor(gui.getColors().panel_background);

		Button openSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.skinSettings"), () -> e.openPopup(new SkinSettingsPopup(gui, e)));
		openSkinBtn.setBounds(new Box(5, 30, w - 10, 20));
		addElement(openSkinBtn);

		Button refreshSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.reloadSkin"), editor::reloadSkin);
		refreshSkinBtn.setBounds(new Box(5, 5, w - 10, 20));
		addElement(refreshSkinBtn);

		colorBtn = new ColorButton(gui, e, c -> {
			editor.penColor = c;
			editor.setPenColor.accept(c);
		});
		editor.setPenColor.add(colorBtn::setColor);
		colorBtn.setColor(editor.penColor);
		colorBtn.setBounds(new Box(5, 55, w - 10, 20));
		addElement(colorBtn);

		pen = new ButtonIcon(gui, "editor", 0, 32, () -> {
			editor.drawMode = 0;
			setMode();
		});
		pen.setBounds(new Box(5, 80, 20, 20));
		addElement(pen);

		rubber = new ButtonIcon(gui, "editor", 16, 32, () -> {
			editor.drawMode = 1;
			setMode();
		});
		rubber.setBounds(new Box(30, 80, 20, 20));
		addElement(rubber);

		setMode();
	}

	private void setMode() {
		pen.setEnabled(editor.drawMode != 0);
		rubber.setEnabled(editor.drawMode != 1);
	}
}
