package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.ButtonGroup;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorTexture;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.editor.util.ModelDescription.CopyProtection;
import com.tom.cpm.shared.model.SkinType;

public class DescPopup extends PopupPanel {
	private EditorTexture icon;
	private SkinType skinType;

	public DescPopup(EditorGui g, boolean skinTypeSettings, Runnable onOk) {
		super(g.getGui());
		setBounds(new Box(0, 0, 320, 195));
		Editor editor = g.getEditor();
		skinType = editor.skinType;

		Label nameLbl = new Label(gui, gui.i18nFormat("label.cpm.name"));
		nameLbl.setBounds(new Box(5, 5, 130, 10));
		addElement(nameLbl);

		TextField nameField = new TextField(gui);
		nameField.setBounds(new Box(5, 15, 130, 20));
		addElement(nameField);

		Label descLbl = new Label(gui, gui.i18nFormat("label.cpm.desc"));
		descLbl.setBounds(new Box(5, 40, 130, 10));
		addElement(descLbl);

		TextField descField = new TextField(gui);
		descField.setBounds(new Box(5, 50, 130, 20));
		addElement(descField);

		icon = new EditorTexture();
		if(editor.description != null) {
			nameField.setText(editor.description.name);
			descField.setText(editor.description.desc);
			if(editor.description.icon != null) {
				icon.setImage(editor.description.icon);
			}
		}

		Button setIcon = new Button(gui, gui.i18nFormat("button.cpm.setIcon"), () -> openScreenshot(g, icon, this));
		setIcon.setBounds(new Box(5, 80, 100, 20));
		addElement(setIcon);

		Checkbox chbxClone = new Checkbox(gui, gui.i18nFormat("label.cpm.cloneable"));
		chbxClone.setTooltip(new Tooltip(g, gui.i18nFormat("tooltip.cpm.cloneable")));
		chbxClone.setBounds(new Box(5, 110, 60, 20));
		chbxClone.setSelected(editor.description != null && editor.description.copyProtection == CopyProtection.CLONEABLE);
		addElement(chbxClone);

		Checkbox chbxUUIDLock = new Checkbox(gui, gui.i18nFormat("label.cpm.uuidlock"));
		chbxUUIDLock.setTooltip(new Tooltip(g, gui.i18nFormat("tooltip.cpm.uuidlock", MinecraftClientAccess.get().getClientPlayer().getUUID().toString())));
		chbxUUIDLock.setBounds(new Box(5, 135, 60, 20));
		chbxUUIDLock.setSelected(editor.description != null && editor.description.copyProtection == CopyProtection.UUID_LOCK);
		addElement(chbxUUIDLock);

		chbxClone.setAction(() -> {
			if(!chbxClone.isSelected()) {
				chbxUUIDLock.setSelected(false);
				chbxClone.setSelected(true);
			} else {
				chbxClone.setSelected(false);
			}
		});

		chbxUUIDLock.setAction(() -> {
			if(!chbxUUIDLock.isSelected()) {
				chbxUUIDLock.setSelected(true);
				chbxClone.setSelected(false);
			} else {
				chbxUUIDLock.setSelected(false);
			}
		});

		if(skinTypeSettings) {
			ButtonGroup<SkinType, Checkbox> group = new ButtonGroup<>(Checkbox::setSelected, Checkbox::setAction, i -> skinType = i);
			for (int j = 0; j < SkinType.VANILLA_TYPES.length; j++) {
				SkinType s = SkinType.VANILLA_TYPES[j];
				Checkbox chbxSt = new Checkbox(gui, gui.i18nFormat("label.cpm.skin_type." + s.getName()));
				chbxSt.setBounds(new Box(bounds.w - 135, bounds.y + 5 + j * 25, 60, 20));
				addElement(chbxSt);
				group.addElement(s, chbxSt);
			}
			group.accept(skinType);
		}

		Button ok = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			if(skinTypeSettings && editor.skinType != skinType) {
				if(editor.elements.stream().anyMatch(m -> !m.hidden)) {
					g.openPopup(new ConfirmPopup(g, gui.i18nFormat("label.cpm.confirmSkinTypeEdit", gui.i18nFormat("label.cpm.skin_type." + editor.skinType.getName()), gui.i18nFormat("label.cpm.skin_type." + skinType.getName())), () -> {
						editor.skinType = skinType;
						editor.markDirty();
					}, null));
				} else {
					editor.skinType = skinType;
					editor.markDirty();
				}
			}
			if(editor.description == null)editor.description = new ModelDescription();
			editor.description.name = nameField.getText();
			editor.description.desc = descField.getText();
			editor.description.copyProtection = chbxUUIDLock.isSelected() ? CopyProtection.UUID_LOCK : chbxClone.isSelected() ? CopyProtection.CLONEABLE : CopyProtection.NORMAL;
			editor.markDirty();
			close();
			if(onOk != null)
				onOk.run();
		});
		ok.setBounds(new Box(5, 170, 60, 20));
		addElement(ok);
	}

	@Override
	public void onClosed() {
		icon.free();
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		super.draw(event, partialTicks);

		if(icon.getImage() != null) {
			gui.drawBox(bounds.x + bounds.w - 136, bounds.y + 14, 130, 130, gui.getColors().color_picker_border);
			gui.drawBox(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0xffffffff);

			icon.bind();
			gui.drawTexture(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0, 0, 1, 1);
		}
	}

	private static void openScreenshot(EditorGui e, EditorTexture icon, DescPopup popup) {
		popup.close();
		e.openPopup(new ScreenshotPopup(e, icon::setImage, () -> e.openPopup(popup)));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.desc");
	}
}
