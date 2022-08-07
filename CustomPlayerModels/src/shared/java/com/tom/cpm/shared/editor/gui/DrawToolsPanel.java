package com.tom.cpm.shared.editor.gui;

import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ButtonIcon;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Slider;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.ElementGroup;
import com.tom.cpl.gui.util.FlowLayout;
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

		Panel panel = new Panel(gui);
		panel.setBounds(new Box(0, 5, w, h));
		addElement(panel);
		FlowLayout layout = new FlowLayout(panel, 5, 1);

		Button openSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.skinSettings"), () -> SkinSettingsPopup.showPopup(e));
		openSkinBtn.setBounds(new Box(5, 0, w - 10, 20));
		panel.addElement(openSkinBtn);

		Button refreshSkinBtn = new Button(gui, gui.i18nFormat("button.cpm.reloadSkin"), editor::reloadSkin);
		refreshSkinBtn.setBounds(new Box(5, 0, w - 10, 20));
		panel.addElement(refreshSkinBtn);
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
		colorBtn.setBounds(new Box(5, 0, w - 10, 20));
		panel.addElement(colorBtn);

		Panel toolsPanel = new Panel(gui);

		int tx = 0;
		int ty = 0;
		for(EditorTool tool : EditorTool.VALUES) {
			ButtonIcon button = new ButtonIcon(gui, "editor", tool.ordinal() * 16, 32, () -> editor.drawMode.accept(tool));
			if(25 * (tx + 1) > w) {
				tx = 0;
				ty++;
			}
			button.setBounds(new Box(5 + 25 * (tx++), ty * 25, 20, 20));
			editor.drawMode.add(tool.setEnabled(button));
			toolsPanel.addElement(button);
		}

		toolsPanel.setBounds(new Box(0, 0, w, ty * 25 + 20));
		panel.addElement(toolsPanel);

		{
			ElementGroup<EditorTool, GuiElement> group = new ElementGroup<>(GuiElement::setVisible);
			editor.drawMode.add(group);

			Slider sizeSlider = new Slider(gui, gui.i18nFormat("label.cpm.brushSize", editor.brushSize));
			group.addElement(EditorTool.PEN, sizeSlider);
			group.addElement(EditorTool.RUBBER, sizeSlider);
			sizeSlider.setAction(() -> {
				editor.brushSize = Math.max(1, (int) (sizeSlider.getValue() * 10));
				sizeSlider.setText(gui.i18nFormat("label.cpm.brushSize", editor.brushSize));
			});
			sizeSlider.setBounds(new Box(5, 0, w - 10, 20));
			//panel.addElement(sizeSlider);//TODO

			Slider alphaSlider = new Slider(gui, gui.i18nFormat("label.cpm.brushAlpha", editor.alphaValue));
			alphaSlider.setValue(editor.alphaValue / 255f);
			group.addElement(EditorTool.PEN, alphaSlider);
			group.addElement(EditorTool.FILL, alphaSlider);
			alphaSlider.setAction(() -> {
				editor.alphaValue = Math.max(1, (int) (alphaSlider.getValue() * 255));
				alphaSlider.setText(gui.i18nFormat("label.cpm.brushAlpha", editor.alphaValue));
			});
			alphaSlider.setBounds(new Box(5, 0, w - 10, 20));
			alphaSlider.setTooltip(new Tooltip(editor.frame, gui.i18nFormat("tooltip.cpm.brushAlpha")));
			panel.addElement(alphaSlider);
		}

		layout.reflow();
	}
}
