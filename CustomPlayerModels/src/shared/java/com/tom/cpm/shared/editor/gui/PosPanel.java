package com.tom.cpm.shared.editor.gui;

import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.UpdaterRegistry.Updater;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.popup.ColorButton;
import com.tom.cpm.shared.editor.gui.popup.SkinSettingsPopup;
import com.tom.cpm.shared.editor.tree.TreeElement.VecType;

public class PosPanel extends Panel {

	public PosPanel(IGui gui, EditorGui e) {
		super(gui);
		Editor editor = e.getEditor();
		setBounds(new Box(0, 0, 170, 475));
		setBackgroundColor(gui.getColors().panel_background);

		{
			addElement(new Label(gui, gui.i18nFormat("label.cpm.name")).setBounds(new Box(5, 5, 0, 0)));
			TextField nameField = new TextField(gui);
			nameField.setBounds(new Box(5, 15, 160, 20));
			editor.updateName.add(t -> {
				nameField.setEnabled(t != null);
				if(t != null)nameField.setText(t);
				else nameField.setText("");
			});
			nameField.setEventListener(() -> editor.setName(nameField.getText()));
			addElement(nameField);
		}

		addVec3("size", 40, v -> editor.setVec(v, VecType.SIZE), this, editor.setSize, 1);
		addVec3("offset", 70, v -> editor.setVec(v, VecType.OFFSET), this, editor.setOffset, 2);
		addVec3("rotation", 100, v -> editor.setVec(v, VecType.ROTATION), this, editor.setRot, 1);
		addVec3("position", 130, v -> editor.setVec(v, VecType.POSITION), this, editor.setPosition, 2);
		addVec3("scale", 160, v -> editor.setVec(v, VecType.SCALE), this, editor.setScale, 2);

		{
			addElement(new Label(gui, gui.i18nFormat("label.cpm.mcScale")).setBounds(new Box(5, 190, 0, 0)));
			Spinner spinnerS = new Spinner(gui);
			spinnerS.setBounds(new Box(5, 200, 70, 18));
			editor.setMCScale.add(f -> {
				spinnerS.setEnabled(f != null);
				if(f != null)spinnerS.setValue(f);
				else spinnerS.setValue(0);
			});
			spinnerS.addChangeListener(() -> editor.setMcScale(spinnerS.getValue()));
			spinnerS.setDp(3);
			addElement(spinnerS);

			Checkbox box = new Checkbox(gui, gui.i18nFormat("label.cpm.mirror"));
			box.setBounds(new Box(80, 200, 70, 18));
			box.setAction(editor::switchMirror);
			editor.setMirror.add(b -> {
				box.setEnabled(b != null);
				if(b != null)box.setSelected(b);
				else box.setSelected(false);
			});
			addElement(box);
		}

		{
			int ys = 220;
			Button modeBtn = new Button(gui, gui.i18nFormat("button.cpm.mode"), editor::switchMode);
			modeBtn.setBounds(new Box(5, ys, 160, 16));

			Spinner spinnerU = new Spinner(gui);
			Spinner spinnerV = new Spinner(gui);
			Spinner spinnerT = new Spinner(gui);
			ColorButton colorBtn = new ColorButton(gui, e, editor::setColor);
			Label lblU = new Label(gui, "U:");
			lblU.setBounds(new Box(5, ys + 20, 50, 18));
			Label lblV = new Label(gui, "V:");
			lblV.setBounds(new Box(60, ys + 20, 50, 18));
			Label lblT = new Label(gui, gui.i18nFormat("label.cpm.texSize"));
			lblT.setBounds(new Box(115, ys + 20, 50, 18));

			spinnerU.setBounds(new Box(5, ys + 30, 50, 18));
			spinnerV.setBounds(new Box(60, ys + 30, 50, 18));
			spinnerT.setBounds(new Box(115, ys + 30, 50, 18));
			colorBtn.setBounds(new Box(5, ys + 20, 160, 16));
			spinnerU.setDp(0);
			spinnerV.setDp(0);
			spinnerT.setDp(0);

			Runnable r = () -> editor.setVec(new Vec3f(spinnerU.getValue(), spinnerV.getValue(), spinnerT.getValue()), VecType.TEXTURE);
			spinnerU.addChangeListener(r);
			spinnerV.addChangeListener(r);
			spinnerT.addChangeListener(r);

			editor.setModeBtn.add(b -> {
				if(b == null) {
					modeBtn.setEnabled(false);
					modeBtn.setText(gui.i18nFormat("button.cpm.mode"));
				} else {
					modeBtn.setEnabled(true);
					modeBtn.setText(b);
				}
			});
			editor.setModePanel.add(t -> {
				boolean color = false, texture = false;
				switch (t) {
				case COLOR:
					color = true;
					break;
				case NULL:
					break;
				case TEX:
					texture = true;
					break;
				default:
					break;
				}
				spinnerU.setVisible(texture);
				spinnerV.setVisible(texture);
				spinnerT.setVisible(texture);
				lblU.setVisible(texture);
				lblV.setVisible(texture);
				lblT.setVisible(texture);
				colorBtn.setVisible(color);
			});
			editor.setTexturePanel.add(v -> {
				if(v != null) {
					spinnerU.setValue(v.x);
					spinnerV.setValue(v.y);
					spinnerT.setValue(v.z);
				}
			});
			editor.setPartColor.add(c -> {
				if(c != null)colorBtn.setColor(c);
			});
			addElement(modeBtn);
			addElement(spinnerU);
			addElement(spinnerV);
			addElement(spinnerT);
			addElement(lblU);
			addElement(lblV);
			addElement(lblT);
			addElement(colorBtn);

			String skinLbl = gui.i18nFormat("label.cpm.skin");
			Label lblS = new Label(gui, skinLbl);
			lblS.setBounds(new Box(5, ys + 50, 40, 18));
			addElement(lblS);
			editor.setSkinEdited.add(b -> {
				if(b)lblS.setText(skinLbl + "*");
				else lblS.setText(skinLbl);
			});

			SkinTextureDisplay skinDisp = new SkinTextureDisplay(gui, editor);
			skinDisp.setBounds(new Box(5, ys + 60, 160, 160));
			addElement(skinDisp);

			Button openSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.skinSettings"), () -> SkinSettingsPopup.showPopup(e));
			openSkinBtn.setBounds(new Box(5, ys + 225, 90, 20));
			addElement(openSkinBtn);

			Button refreshSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.reloadSkin"), editor::reloadSkin);
			refreshSkinBtn.setBounds(new Box(100, ys + 225, 65, 20));
			addElement(refreshSkinBtn);
			editor.setReload.add(f -> {
				refreshSkinBtn.setEnabled(f != null);
				refreshSkinBtn.setTooltip(new Tooltip(e, f != null ? gui.i18nFormat("tooltip.cpm.reloadSkin.file", f) : gui.i18nFormat("tooltip.cpm.reloadSkin.no_file")));
			});
		}
	}

	public static void addVec3(String name, int y, Consumer<Vec3f> consumer, Panel panel, Updater<Vec3f> updater, int dp) {
		IGui gui = panel.getGui();
		Spinner spinnerX = new Spinner(gui);
		Spinner spinnerY = new Spinner(gui);
		Spinner spinnerZ = new Spinner(gui);

		spinnerX.setBounds(new Box(5, y + 10, 50, 18));
		spinnerY.setBounds(new Box(60, y + 10, 50, 18));
		spinnerZ.setBounds(new Box(115, y + 10, 50, 18));
		spinnerX.setDp(dp);
		spinnerY.setDp(dp);
		spinnerZ.setDp(dp);

		Runnable r = () -> consumer.accept(new Vec3f(spinnerX.getValue(), spinnerY.getValue(), spinnerZ.getValue()));
		spinnerX.addChangeListener(r);
		spinnerY.addChangeListener(r);
		spinnerZ.addChangeListener(r);

		panel.addElement(new Label(gui, gui.i18nFormat("label.cpm." + name)).setBounds(new Box(5, y, 0, 0)));
		panel.addElement(spinnerX);
		panel.addElement(spinnerY);
		panel.addElement(spinnerZ);

		updater.add(v -> {
			boolean en = v != null;
			spinnerX.setEnabled(en);
			spinnerY.setEnabled(en);
			spinnerZ.setEnabled(en);

			if(en) {
				spinnerX.setValue(v.x);
				spinnerY.setValue(v.y);
				spinnerZ.setValue(v.z);
			} else {
				spinnerX.setValue(0);
				spinnerY.setValue(0);
				spinnerZ.setValue(0);
			}
		});
	}

	public static enum ModeDisplType {
		NULL, COLOR, TEX
	}
}
