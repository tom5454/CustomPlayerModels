package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.Label;
import com.tom.cpm.shared.gui.elements.PopupPanel;
import com.tom.cpm.shared.gui.elements.Spinner;
import com.tom.cpm.shared.math.Box;

public class SkinSettingsPopup extends PopupPanel {

	public SkinSettingsPopup(IGui gui, EditorGui e) {
		super(gui);

		Editor editor = e.getEditor();

		Button openSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.openSkin"), () -> {
			FileChooserGui fc = new FileChooserGui(editor.gui);
			fc.setTitle(gui.i18nFormat("label.cpm.loadSkin"));
			fc.setFileDescText(gui.i18nFormat("label.cpm.file_png"));
			fc.setFilter((f, n) -> n.endsWith(".png") && !f.isDirectory());
			fc.setAccept(e::loadSkin);
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			e.openPopup(fc);
			close();
		});
		openSkinBtn.setBounds(new Box(5, 5, 60, 20));
		addElement(openSkinBtn);

		Button saveSkin = new Button(gui, gui.i18nFormat("button.cpm.saveSkin"), () -> {
			if(gui.isShiftDown() || editor.skinFile == null) {
				FileChooserGui fc = new FileChooserGui(editor.gui);
				fc.setTitle(gui.i18nFormat("label.cpm.saveSkin"));
				fc.setFileDescText(gui.i18nFormat("label.cpm.file_png"));
				fc.setFilter((f, n) -> n.endsWith(".png") && !f.isDirectory());
				fc.setSaveDialog(true);
				fc.setExtAdder(f -> f + ".png");
				fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
				fc.setAccept(editor::saveSkin);
				e.openPopup(fc);
			} else {
				editor.saveSkin(editor.skinFile);
			}
			close();
		});
		saveSkin.setBounds(new Box(75, 5, 60, 20));
		addElement(saveSkin);

		Button newSkin = new Button(gui, gui.i18nFormat("button.cpm.newSkin"), () -> {
			close();
			e.openPopup(new NewSkinPopup(gui, editor));
		});
		newSkin.setBounds(new Box(145, 5, 60, 20));
		addElement(newSkin);

		Label lblT = new Label(gui, gui.i18nFormat("label.cpm.sheetSize"));
		lblT.setBounds(new Box(5, 30, 40, 18));
		addElement(lblT);

		Label lblTW = new Label(gui, gui.i18nFormat("label.cpm.width"));
		lblTW.setBounds(new Box(5, 40, 40, 18));
		Label lblTH = new Label(gui, gui.i18nFormat("label.cpm.height"));
		lblTH.setBounds(new Box(75, 40, 40, 18));

		Spinner spinnerTW = new Spinner(gui);
		Spinner spinnerTH = new Spinner(gui);
		spinnerTW.setBounds(new Box(5, 50, 65, 18));
		spinnerTH.setBounds(new Box(75, 50, 65, 18));
		spinnerTW.setDp(0);
		spinnerTH.setDp(0);
		addElement(spinnerTW);
		addElement(spinnerTH);
		addElement(lblTW);
		addElement(lblTH);

		Runnable r = () -> editor.setTexSize((int) spinnerTW.getValue(), (int) spinnerTH.getValue());
		spinnerTW.addChangeListener(r);
		spinnerTH.addChangeListener(r);
		spinnerTW.setValue(editor.skinProvider.size.x);
		spinnerTH.setValue(editor.skinProvider.size.y);

		setBounds(new Box(0, 0, 210, 100));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.skinSettings");
	}
}
