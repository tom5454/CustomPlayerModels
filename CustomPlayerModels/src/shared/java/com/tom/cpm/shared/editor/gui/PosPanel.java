package com.tom.cpm.shared.editor.gui;

import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.UpdaterRegistry.Updater;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.ElementGroup;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.gui.util.TabFocusHandler;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.Effect;
import com.tom.cpm.shared.editor.gui.popup.SkinSettingsPopup;
import com.tom.cpm.shared.editor.tree.TreeElement.VecType;

public class PosPanel extends Panel {
	private TabFocusHandler tabHandler;

	public PosPanel(IGui gui, EditorGui e) {
		super(gui);
		tabHandler = new TabFocusHandler(gui);
		Editor editor = e.getEditor();
		setBounds(new Box(0, 0, 170, 475));
		setBackgroundColor(gui.getColors().panel_background);

		FlowLayout layout = new FlowLayout(this, 4, 1);

		{
			Panel panel = new Panel(gui);
			addElement(panel);
			panel.setBounds(new Box(0, 0, 170, 30));

			panel.addElement(new Label(gui, gui.i18nFormat("label.cpm.name")).setBounds(new Box(5, 0, 0, 0)));
			TextField nameField = new TextField(gui);
			nameField.setBounds(new Box(5, 10, 160, 20));
			editor.updateName.add(t -> {
				nameField.setEnabled(t != null);
				if(t != null)nameField.setText(t);
				else nameField.setText("");
			});
			nameField.setEventListener(() -> editor.setName(nameField.getText()));
			panel.addElement(nameField);
			tabHandler.add(nameField);
		}

		addVec3("size", v -> editor.setVec(v, VecType.SIZE), this, editor.setSize, 1, tabHandler);
		addVec3("offset", v -> editor.setVec(v, VecType.OFFSET), this, editor.setOffset, 2, tabHandler);
		addVec3("rotation", v -> editor.setVec(v, VecType.ROTATION), this, editor.setRot, 1, tabHandler);
		addVec3("position", v -> editor.setVec(v, VecType.POSITION), this, editor.setPosition, 2, tabHandler);
		Panel sc =  addVec3("scale", v -> editor.setVec(v, VecType.SCALE), this, editor.setScale, 2, tabHandler);
		editor.updateGui.add(() -> {
			sc.setVisible(editor.displayAdvScaling);
			layout.reflow();
		});

		{
			Panel panel = new Panel(gui);
			addElement(panel);
			panel.setBounds(new Box(0, 0, 170, 30));

			panel.addElement(new Label(gui, gui.i18nFormat("label.cpm.mcScale")).setBounds(new Box(5, 0, 0, 0)));
			Spinner spinnerS = new Spinner(gui);
			spinnerS.setBounds(new Box(5, 10, 70, 18));
			editor.setMCScale.add(f -> {
				spinnerS.setEnabled(f != null);
				if(f != null)spinnerS.setValue(f);
				else spinnerS.setValue(0);
			});
			spinnerS.addChangeListener(() -> editor.setMcScale(spinnerS.getValue()));
			spinnerS.setDp(3);
			panel.addElement(spinnerS);
			tabHandler.add(spinnerS);

			Checkbox box = new Checkbox(gui, gui.i18nFormat("label.cpm.mirror"));
			box.setBounds(new Box(80, 10, 70, 18));
			box.setAction(() -> editor.switchEffect(Effect.MIRROR));
			editor.setMirror.add(box::updateState);
			panel.addElement(box);
		}
		{
			ElementGroup<ModeDisplayType, GuiElement> group = new ElementGroup<>(GuiElement::setVisible);
			editor.setModePanel.add(group);
			editor.setModePanel.add(layout);
			for (ModeDisplayType mdt : ModeDisplayType.VALUES) {
				Panel p = mdt.factory.apply(e, editor, tabHandler);
				addElement(p);
				group.addElement(mdt, p);
			}
		}
		{
			String skinLbl = gui.i18nFormat("label.cpm.skin");
			Label lblS = new Label(gui, skinLbl);
			lblS.setBounds(new Box(5, 0, 40, 5));
			addElement(lblS);
			editor.setSkinEdited.add(b -> {
				if(b)lblS.setText(skinLbl + "*");
				else lblS.setText(skinLbl);
			});

			TextureDisplay skinDisp = new TextureDisplay(gui, editor);
			skinDisp.setBounds(new Box(5, 0, 160, 160));
			addElement(skinDisp);
		}
		{
			Panel panel = new Panel(gui);
			addElement(panel);
			panel.setBounds(new Box(0, 0, 170, 20));

			Button openSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.skinSettings"), () -> SkinSettingsPopup.showPopup(e));
			openSkinBtn.setBounds(new Box(5, 0, 90, 20));
			panel.addElement(openSkinBtn);

			Button refreshSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.reloadSkin"), editor::reloadSkin);
			refreshSkinBtn.setBounds(new Box(100, 0, 65, 20));
			panel.addElement(refreshSkinBtn);
			editor.setReload.add(f -> {
				refreshSkinBtn.setEnabled(f != null);
				refreshSkinBtn.setTooltip(new Tooltip(e, f != null ? gui.i18nFormat("tooltip.cpm.reloadSkin.file", f) : gui.i18nFormat("tooltip.cpm.reloadSkin.no_file")));
			});
		}
		layout.reflow();
		addElement(tabHandler);
	}

	public static Panel addVec3(String name, Consumer<Vec3f> consumer, Panel panelIn, Updater<Vec3f> updater, int dp, TabFocusHandler tabHandler) {
		IGui gui = panelIn.getGui();
		Panel panel = new Panel(gui);
		panel.setBounds(new Box(0, 0, 170, 30));
		panelIn.addElement(panel);

		Spinner spinnerX = new Spinner(gui);
		Spinner spinnerY = new Spinner(gui);
		Spinner spinnerZ = new Spinner(gui);

		spinnerX.setBounds(new Box(5, 10, 50, 18));
		spinnerY.setBounds(new Box(60, 10, 50, 18));
		spinnerZ.setBounds(new Box(115, 10, 50, 18));
		spinnerX.setDp(dp);
		spinnerY.setDp(dp);
		spinnerZ.setDp(dp);

		Runnable r = () -> consumer.accept(new Vec3f(spinnerX.getValue(), spinnerY.getValue(), spinnerZ.getValue()));
		spinnerX.addChangeListener(r);
		spinnerY.addChangeListener(r);
		spinnerZ.addChangeListener(r);

		panel.addElement(new Label(gui, gui.i18nFormat("label.cpm." + name)).setBounds(new Box(5, 0, 0, 0)));
		panel.addElement(spinnerX);
		panel.addElement(spinnerY);
		panel.addElement(spinnerZ);

		tabHandler.add(spinnerX);
		tabHandler.add(spinnerY);
		tabHandler.add(spinnerZ);

		spinnerX.setBackgroundColor(0xFFFF8888);
		spinnerY.setBackgroundColor(0xFF88FF88);
		spinnerZ.setBackgroundColor(0xFF8888FF);

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
		return panel;
	}
}
