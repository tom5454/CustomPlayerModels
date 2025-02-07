package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;

public class NewSkinPopup extends PopupPanel {

	public NewSkinPopup(IGui gui, Editor editor) {
		super(gui);

		Label lblW = new Label(gui, gui.i18nFormat("label.cpm.width"));
		lblW.setBounds(new Box(5, 15, 40, 20));
		Label lblH = new Label(gui, gui.i18nFormat("label.cpm.height"));
		lblH.setBounds(new Box(75, 15, 40, 20));

		Spinner spinnerW = new Spinner(gui);
		Spinner spinnerH = new Spinner(gui);
		spinnerW.setDp(0);
		spinnerH.setDp(0);
		spinnerW.setBounds(new Box(5, 25, 65, 20));
		spinnerH.setBounds(new Box(75, 25, 65, 20));
		addElement(spinnerW);
		addElement(spinnerH);
		addElement(lblW);
		addElement(lblH);
		spinnerW.setValue(64);
		spinnerH.setValue(64);

		Checkbox customGridSize = new Checkbox(gui, gui.i18nFormat("label.cpm.customGridSize"));
		customGridSize.setBounds(new Box(5, 50, 100, 20));
		customGridSize.setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.customGridSize")));
		addElement(customGridSize);

		Label lblTW = new Label(gui, gui.i18nFormat("label.cpm.width"));
		lblTW.setBounds(new Box(5, 75, 40, 20));
		Label lblTH = new Label(gui, gui.i18nFormat("label.cpm.height"));
		lblTH.setBounds(new Box(75, 75, 40, 20));

		Spinner spinnerTW = new Spinner(gui);
		Spinner spinnerTH = new Spinner(gui);
		spinnerTW.setBounds(new Box(5, 85, 65, 20));
		spinnerTH.setBounds(new Box(75, 85, 65, 20));
		spinnerTW.setDp(0);
		spinnerTH.setDp(0);
		spinnerTW.setEnabled(false);
		spinnerTH.setEnabled(false);
		addElement(spinnerTW);
		addElement(spinnerTH);
		addElement(lblTW);
		addElement(lblTH);
		customGridSize.setAction(() -> {
			boolean v = !customGridSize.isSelected();
			if(!v) {
				spinnerTW.setValue(spinnerW.getValue());
				spinnerTH.setValue(spinnerH.getValue());
			}
			customGridSize.setSelected(v);
			spinnerTW.setEnabled(v);
			spinnerTH.setEnabled(v);
		});
		spinnerW.addChangeListener(() -> {
			if(!customGridSize.isSelected())spinnerTW.setValue(spinnerW.getValue());
		});
		spinnerH.addChangeListener(() -> {
			if(!customGridSize.isSelected())spinnerTH.setValue(spinnerH.getValue());
		});

		Checkbox keepOld = new Checkbox(gui, gui.i18nFormat("label.cpm.keepOldSkin"));
		keepOld.setBounds(new Box(5, 110, 100, 20));
		keepOld.setAction(() -> keepOld.setSelected(!keepOld.isSelected()));
		addElement(keepOld);

		Button ok = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			close();
			ETextures tex = editor.getTextureProvider();
			if(tex != null) {
				Image oldImg = tex.getImage();
				Image newImage = new Image((int) spinnerW.getValue(), (int) spinnerH.getValue());
				if(keepOld.isSelected()) {
					newImage.draw(oldImg);
				}
				Vec2i size = customGridSize.isSelected() ?
						new Vec2i((int) spinnerTW.getValue(), (int) spinnerTH.getValue()) :
							new Vec2i((int) spinnerW.getValue(), (int) spinnerH.getValue());

				editor.action("newTexture").
				updateValueOp(tex, tex.file, null, (a, b) -> a.file = b).
				updateValueOp(tex, tex.isEdited(), true, ETextures::setEdited).
				updateValueOp(tex, tex.provider.size, size, (a, b) -> a.provider.size = b).
				updateValueOp(tex, tex.customGridSize, customGridSize.isSelected(), (a, b) -> a.customGridSize = b).
				updateValueOp(tex, oldImg, newImage, ETextures::setImage).
				updateValueOp(tex, tex.isChangedLocally(), false, ETextures::setChangedLocally).
				onAction(tex::restitchTexture).
				onAction(editor::markElementsDirty).
				execute();
				editor.updateGui();
				if(editor.hasVanillaParts() && (size.x != 64 || size.y != 64)) {
					gui.displayMessagePopup(gui.i18nFormat("label.cpm.warning"), gui.i18nFormat("label.cpm.skin_has_vanilla_parts"));
				}
			}
		});
		ok.setBounds(new Box(5, 135, 80, 20));
		addElement(ok);

		setBounds(new Box(0, 0, 150, 160));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.newSkin");
	}
}
