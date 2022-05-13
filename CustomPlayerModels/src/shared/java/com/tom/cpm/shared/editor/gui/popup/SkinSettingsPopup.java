package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.EditorGui;

public class SkinSettingsPopup extends PopupPanel {
	private static boolean shownWarning = false;

	public static void showPopup(EditorGui e) {
		Editor editor = e.getEditor();
		ETextures tex = editor.getTextureProvider();
		if(tex != null && tex.isEditable()) {
			e.openPopup(new SkinSettingsPopup(e.getGui(), e));
		}
	}

	private SkinSettingsPopup(IGui gui, EditorGui e) {
		super(gui);

		Editor editor = e.getEditor();
		ETextures tex = editor.getTextureProvider();

		Button openSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.openSkin"), () -> {
			FileChooserPopup fc = new FileChooserPopup(editor.frame);
			fc.setTitle(gui.i18nFormat("label.cpm.loadSkin"));
			fc.setFileDescText(gui.i18nFormat("label.cpm.file_png"));
			fc.setFilter(new FileFilter("png"));
			fc.setAccept(e::loadSkin);
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			e.openPopup(fc);
			close();
		});
		openSkinBtn.setBounds(new Box(5, 5, 60, 20));
		addElement(openSkinBtn);

		Button saveSkin = new Button(gui, gui.i18nFormat("button.cpm.saveSkin"), () -> {
			if(gui.isShiftDown() || tex.file == null) {
				FileChooserPopup fc = new FileChooserPopup(editor.frame);
				fc.setTitle(gui.i18nFormat("label.cpm.saveSkin"));
				fc.setFileDescText(gui.i18nFormat("label.cpm.file_png"));
				fc.setFilter(new FileFilter("png"));
				fc.setSaveDialog(true);
				fc.setExtAdder(f -> f + ".png");
				fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
				fc.setAccept(editor::saveSkin);
				e.openPopup(fc);
			} else {
				editor.saveSkin(tex.file);
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

		Button delSkin = new Button(gui, gui.i18nFormat("button.cpm.delSkin"), () -> {
			boolean edited = tex.isEdited();
			if(edited) {
				e.openPopup(new ConfirmPopup(e, gui.i18nFormat("label.cpm.delSkin"), () -> {
					editor.action("delTexture").
					updateValueOp(tex, tex.getImage(), tex.copyDefaultImg(), ETextures::setImage).
					updateValueOp(tex, tex.isEdited(), false, ETextures::setEdited).
					onAction(() -> {
						editor.reloadSkin();
						editor.restitchTextures();
						editor.updateGui();
					}).
					execute();
				}, null));
			}
		});
		delSkin.setBounds(new Box(5, 30, 60, 20));
		addElement(delSkin);

		if(editor.displayAdvScaling) {
			Label lblT = new Label(gui, gui.i18nFormat("label.cpm.sheetSize"));
			lblT.setBounds(new Box(5, 80, 80, 18));
			lblT.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.texture_sheet")));
			addElement(lblT);

			Label lblTW = new Label(gui, gui.i18nFormat("label.cpm.width"));
			lblTW.setBounds(new Box(5, 90, 40, 18));
			Label lblTH = new Label(gui, gui.i18nFormat("label.cpm.height"));
			lblTH.setBounds(new Box(75, 90, 40, 18));

			Spinner spinnerTW = new Spinner(gui);
			Spinner spinnerTH = new Spinner(gui);
			spinnerTW.setBounds(new Box(5, 100, 65, 18));
			spinnerTH.setBounds(new Box(75, 100, 65, 18));
			spinnerTW.setDp(0);
			spinnerTH.setDp(0);
			addElement(spinnerTW);
			addElement(spinnerTH);
			addElement(lblTW);
			addElement(lblTH);

			Runnable r = () -> {
				if(editor.hasVanillaParts() && !shownWarning) {
					shownWarning = true;
					e.openPopup(new MessagePopup(e, gui.i18nFormat("label.cpm.warning"), gui.i18nFormat("label.cpm.skin_has_vanilla_parts")));
				} else
					editor.setTexSize((int) spinnerTW.getValue(), (int) spinnerTH.getValue());
			};
			spinnerTW.addChangeListener(r);
			spinnerTH.addChangeListener(r);
			spinnerTW.setValue(tex.provider.size.x);
			spinnerTH.setValue(tex.provider.size.y);
		}

		setBounds(new Box(0, 0, 210, 140));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.skinSettings");
	}
}
