package com.tom.cpm.shared.editor.gui;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.ButtonIcon;
import com.tom.cpm.shared.gui.elements.Panel;
import com.tom.cpm.shared.math.Box;

public class TreePanel extends Panel {

	public TreePanel(IGui gui, EditorGui e, int width, int height) {
		super(gui);
		setBounds(new Box(width - 150, 0, 150, height));
		setBackgroundColor(gui.getColors().panel_background);
		Editor editor = e.getEditor();

		ModelElementsTree tree = new ModelElementsTree(gui, editor);
		tree.setBounds(new Box(0, 0, 150, height - 20));
		addElement(tree);
		editor.updateTree.add(tree::updateTree);

		ButtonIcon newBtn = new ButtonIcon(gui, "editor", 0, 16, editor::addNew);
		newBtn.setBounds(new Box(5, height - 24, 18, 18));
		addElement(newBtn);
		editor.setAddEn.add(newBtn::setEnabled);

		ButtonIcon delBtn = new ButtonIcon(gui, "editor", 14, 16, editor::deleteSel);
		delBtn.setBounds(new Box(25, height - 24, 18, 18));
		addElement(delBtn);
		editor.setDelEn.add(delBtn::setEnabled);

		ButtonIcon visBtn = new ButtonIcon(gui, "editor", 42, 16, editor::switchVis);
		visBtn.setBounds(new Box(45, height - 24, 18, 18));
		addElement(visBtn);
		editor.setVis.add(b -> {
			if(b == null) {
				visBtn.setEnabled(false);
				visBtn.setU(42);
			} else {
				visBtn.setEnabled(true);
				visBtn.setU(b ? 42 : 28);
			}
		});
	}

}
