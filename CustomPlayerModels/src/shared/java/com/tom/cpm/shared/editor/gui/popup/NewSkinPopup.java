package com.tom.cpm.shared.editor.gui.popup;

import java.awt.image.BufferedImage;
import java.io.File;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.Label;
import com.tom.cpm.shared.gui.elements.PopupPanel;
import com.tom.cpm.shared.gui.elements.Spinner;
import com.tom.cpm.shared.math.Box;
import com.tom.cpm.shared.math.Vec2i;

public class NewSkinPopup extends PopupPanel {

	public NewSkinPopup(IGui gui, Editor editor) {
		super(gui);

		Label lblTW = new Label(gui, gui.i18nFormat("label.cpm.width"));
		lblTW.setBounds(new Box(5, 15, 40, 18));
		Label lblTH = new Label(gui, gui.i18nFormat("label.cpm.height"));
		lblTH.setBounds(new Box(75, 15, 40, 18));

		Spinner spinnerW = new Spinner(gui);
		Spinner spinnerH = new Spinner(gui);
		spinnerW.setDp(0);
		spinnerH.setDp(0);
		spinnerW.setBounds(new Box(5, 25, 65, 18));
		spinnerH.setBounds(new Box(75, 25, 65, 18));
		addElement(spinnerW);
		addElement(spinnerH);
		addElement(lblTW);
		addElement(lblTH);
		spinnerW.setValue(64);
		spinnerH.setValue(64);

		Label lblT = new Label(gui, gui.i18nFormat("label.cpm.sheetSize"));
		lblT.setBounds(new Box(5, 50, 40, 18));
		addElement(lblT);

		lblTW = new Label(gui, gui.i18nFormat("label.cpm.width"));
		lblTW.setBounds(new Box(5, 60, 40, 18));
		lblTH = new Label(gui, gui.i18nFormat("label.cpm.height"));
		lblTH.setBounds(new Box(75, 60, 40, 18));

		Spinner spinnerTW = new Spinner(gui);
		Spinner spinnerTH = new Spinner(gui);
		spinnerTW.setDp(0);
		spinnerTH.setDp(0);
		spinnerTW.setBounds(new Box(5, 70, 65, 18));
		spinnerTH.setBounds(new Box(75, 70, 65, 18));
		addElement(spinnerTW);
		addElement(spinnerTH);
		addElement(lblTW);
		addElement(lblTH);
		spinnerTW.setValue(64);
		spinnerTH.setValue(64);

		Button ok = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			close();
			BufferedImage oldImg = editor.skinProvider.getImage();
			File oldSkin = editor.skinFile;
			boolean edited = editor.skinProvider.isEdited();
			Vec2i oldSize = editor.skinProvider.size;
			editor.addUndo(() -> {
				editor.skinFile = oldSkin;
				editor.skinProvider.setImage(oldImg);
				editor.skinProvider.markDirty();
				editor.skinProvider.setEdited(edited);
				editor.skinProvider.size = oldSize;
			});
			BufferedImage newImage = new BufferedImage((int) spinnerW.getValue(), (int) spinnerH.getValue(), BufferedImage.TYPE_INT_ARGB);
			Vec2i size = new Vec2i((int) spinnerTW.getValue(), (int) spinnerTH.getValue());
			if(size.x == 0)size.x = (int) spinnerW.getValue();
			if(size.y == 0)size.y = (int) spinnerH.getValue();
			editor.runOp(() -> {
				editor.skinFile = null;
				editor.skinProvider.setImage(newImage);
				editor.skinProvider.markDirty();
				editor.skinProvider.size = size;
			});
			editor.updateGui();
			editor.markDirty();
		});
		ok.setBounds(new Box(5, 100, 80, 20));
		addElement(ok);

		setBounds(new Box(0, 0, 150, 130));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.newSkin");
	}
}
