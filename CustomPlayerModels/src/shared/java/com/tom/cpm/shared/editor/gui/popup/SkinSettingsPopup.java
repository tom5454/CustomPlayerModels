package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.EmbeddedLocalizations;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.gui.EditorGui;

public class SkinSettingsPopup extends PopupPanel {
	private static boolean shownWarning = false;

	private ETextures tex;

	public static void showPopup(EditorGui e) {
		if(canEdit(e)) {
			e.openPopup(new SkinSettingsPopup(e.getGui(), e));
		}
	}

	public static boolean canEdit(EditorGui e) {
		Editor editor = e.getEditor();
		ETextures tex = editor.getTextureProvider();
		return tex != null && tex.isEditable();
	}

	private SkinSettingsPopup(IGui gui, EditorGui e) {
		super(gui);

		Editor editor = e.getEditor();
		tex = editor.getTextureProvider();

		Button openSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.openSkin"), () -> {
			FileChooserPopup fc = new FileChooserPopup(e);
			fc.setTitle(EmbeddedLocalizations.loadSkin);
			fc.setFileDescText(EmbeddedLocalizations.filePng);
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
				FileChooserPopup fc = new FileChooserPopup(e);
				fc.setTitle(EmbeddedLocalizations.saveSkin);
				fc.setFileDescText(EmbeddedLocalizations.filePng);
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
					updateValueOp(tex, tex.file, null, (a, b) -> a.file = b).
					onAction(() -> {
						editor.restitchTextures();
						editor.updateGui();
					}).
					execute();
				}, null));
			}
		});
		delSkin.setBounds(new Box(5, 30, 60, 20));
		addElement(delSkin);

		Checkbox customGridSize = new Checkbox(gui, gui.i18nFormat("label.cpm.customGridSize"));
		customGridSize.setBounds(new Box(5, 80, 100, 20));
		customGridSize.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.customGridSize")));
		customGridSize.setSelected(tex.customGridSize);
		addElement(customGridSize);

		Label lblTW = new Label(gui, gui.i18nFormat("label.cpm.width"));
		lblTW.setBounds(new Box(5, 105, 40, 18));
		Label lblTH = new Label(gui, gui.i18nFormat("label.cpm.height"));
		lblTH.setBounds(new Box(75, 105, 40, 18));

		Spinner spinnerTW = new Spinner(gui);
		Spinner spinnerTH = new Spinner(gui);
		spinnerTW.setBounds(new Box(5, 115, 65, 20));
		spinnerTH.setBounds(new Box(75, 115, 65, 20));
		spinnerTW.setDp(0);
		spinnerTH.setDp(0);
		spinnerTW.setEnabled(tex.customGridSize);
		spinnerTH.setEnabled(tex.customGridSize);
		addElement(spinnerTW);
		addElement(spinnerTH);
		addElement(lblTW);
		addElement(lblTH);
		customGridSize.setAction(() -> {
			boolean v = !tex.customGridSize;
			ActionBuilder ab = editor.action("switch", "label.cpm.customGridSize").
					updateValueOp(tex, tex.customGridSize, v, (a, b) -> a.customGridSize = b);
			if(!v) {
				ab.updateValueOp(tex, tex.provider.size.x, tex.provider.getImage().getWidth(), (a, b) -> a.provider.size.x = b).
				updateValueOp(tex, tex.provider.size.y, tex.provider.getImage().getHeight(), (a, b) -> a.provider.size.y = b).
				onAction(tex::restitchTexture).
				onAction(editor::markElementsDirty);
				spinnerTW.setValue(tex.provider.getImage().getWidth());
				spinnerTH.setValue(tex.provider.getImage().getHeight());
			}
			ab.execute();
			customGridSize.setSelected(v);
			spinnerTW.setEnabled(v);
			spinnerTH.setEnabled(v);
		});

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

		setBounds(new Box(0, 0, 210, 140));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.skinSettings.title", tex.getName());
	}
}
