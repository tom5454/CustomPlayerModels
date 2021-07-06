package com.tom.cpm.shared.editor.gui.popup;

import java.io.File;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorTexture;

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
		lblT.setBounds(new Box(5, 50, 120, 18));
		lblT.setTooltip(new Tooltip(editor.frame, gui.i18nFormat("tooltip.cpm.texture_sheet")));
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

		Checkbox keepOld = new Checkbox(gui, gui.i18nFormat("label.cpm.keepOldSkin"));
		keepOld.setBounds(new Box(5, 100, 100, 18));
		keepOld.setAction(() -> keepOld.setSelected(!keepOld.isSelected()));
		addElement(keepOld);

		Button ok = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			close();
			EditorTexture tex = editor.getTextureProvider();
			if(tex != null) {
				Image oldImg = tex.getImage();
				File oldSkin = tex.file;
				boolean edited = tex.isEdited();
				Vec2i oldSize = tex.size;
				editor.addUndo(() -> {
					tex.file = oldSkin;
					tex.setImage(oldImg);
					tex.markDirty();
					tex.setEdited(edited);
					tex.size = oldSize;
				});
				Image newImage = new Image((int) spinnerW.getValue(), (int) spinnerH.getValue());
				if(keepOld.isSelected()) {
					newImage.draw(oldImg);
				}
				Vec2i size = new Vec2i((int) spinnerTW.getValue(), (int) spinnerTH.getValue());
				if(size.x == 0)size.x = (int) spinnerW.getValue();
				if(size.y == 0)size.y = (int) spinnerH.getValue();
				editor.runOp(() -> {
					tex.file = null;
					tex.setImage(newImage);
					tex.markDirty();
					tex.size = size;
				});
				editor.markDirty();
				editor.restitchTexture();
				editor.updateGui();
				if(editor.hasVanillaParts() && (size.x != 64 || size.y != 64)) {
					editor.frame.openPopup(new MessagePopup(editor.frame, gui.i18nFormat("label.cpm.warning"), gui.i18nFormat("label.cpm.skin_has_vanilla_parts")));
				}
			}
		});
		ok.setBounds(new Box(5, 125, 80, 20));
		addElement(ok);

		setBounds(new Box(0, 0, 150, 150));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.newSkin");
	}
}
