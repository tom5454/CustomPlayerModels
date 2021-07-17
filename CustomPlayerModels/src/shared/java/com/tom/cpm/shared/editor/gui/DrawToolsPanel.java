package com.tom.cpm.shared.editor.gui;

import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ButtonIcon;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Slider;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.ElementGroup;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorTool;
import com.tom.cpm.shared.editor.gui.popup.ColorButton;
import com.tom.cpm.shared.editor.gui.popup.SkinSettingsPopup;

public class DrawToolsPanel extends Panel {
	private Editor editor;

	public DrawToolsPanel(EditorGui e, int x, int y, int w, int h) {
		super(e.getGui());
		this.editor = e.getEditor();
		if(w < 70) {
			x -= (70 - w);
			w = 70;
		}
		setBounds(new Box(x, y, w, h));
		setBackgroundColor(gui.getColors().panel_background);

		Button openSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.skinSettings"), () -> SkinSettingsPopup.showPopup(e));
		openSkinBtn.setBounds(new Box(5, 30, w - 10, 20));
		addElement(openSkinBtn);

		Button refreshSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.reloadSkin"), editor::reloadSkin);
		refreshSkinBtn.setBounds(new Box(5, 5, w - 10, 20));
		addElement(refreshSkinBtn);
		editor.setReload.add(f -> {
			refreshSkinBtn.setEnabled(f != null);
			refreshSkinBtn.setTooltip(new Tooltip(e, f != null ? gui.i18nFormat("tooltip.cpm.reloadSkin.file", f) : gui.i18nFormat("tooltip.cpm.reloadSkin.no_file")));
		});

		ColorButton colorBtn = new ColorButton(gui, e, c -> {
			editor.penColor = c;
			editor.setPenColor.accept(c);
		});
		editor.setPenColor.add(colorBtn::setColor);
		colorBtn.setColor(editor.penColor);
		colorBtn.setBounds(new Box(5, 55, w - 10, 20));
		addElement(colorBtn);

		for(EditorTool tool : EditorTool.VALUES) {
			ButtonIcon button = new ButtonIcon(gui, "editor", tool.ordinal() * 16, 32, () -> {
				editor.drawMode = tool;
				editor.setTool.accept(tool);
			});
			button.setBounds(new Box(5 + 25 * tool.ordinal(), 80, 20, 20));
			editor.setTool.add(tool.setEnabled(button));
			addElement(button);
		}

		{
			ElementGroup<EditorTool, GuiElement> group = new ElementGroup<>(GuiElement::setVisible);
			editor.setTool.add(group);

			Slider sizeSlider = new Slider(gui, gui.i18nFormat("label.cpm.brushSize", editor.brushSize));
			group.addElement(EditorTool.PEN, sizeSlider);
			group.addElement(EditorTool.RUBBER, sizeSlider);
			sizeSlider.setAction(() -> {
				editor.brushSize = Math.max(1, (int) (sizeSlider.getValue() * 10));
				sizeSlider.setText(gui.i18nFormat("label.cpm.brushSize", editor.brushSize));
			});
			sizeSlider.setBounds(new Box(5, 105, w - 10, 20));
			//addElement(sizeSlider);//TODO
		}

		editor.setTool.accept(editor.drawMode);
	}
}
