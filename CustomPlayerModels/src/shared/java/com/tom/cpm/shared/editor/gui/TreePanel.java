package com.tom.cpm.shared.editor.gui;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeybindHandler;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.elements.ButtonIcon;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Tree;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.gui.Keybinds;

public class TreePanel extends Panel {
	private Tree<TreeElement> tree;
	private Editor editor;

	public TreePanel(IGui gui, EditorGui e, int width, int height, boolean enableMod) {
		super(gui);
		setBounds(new Box(width - 150, 0, 150, height));
		setBackgroundColor(gui.getColors().panel_background);
		editor = e.getEditor();

		ScrollPanel treePanel = new ScrollPanel(gui);
		treePanel.setBounds(new Box(0, 0, 150, height - 30));
		addElement(treePanel);

		tree = new Tree<>(e, editor.treeHandler);
		editor.updateGui.add(tree::updateTree);

		Panel tp = new Panel(gui);
		treePanel.setDisplay(tp);
		tp.addElement(tree);

		tree.setSizeUpdate(s -> {
			int w = Math.max(s.x, 146);
			int h = Math.max(s.y, height - 34);
			tp.setBounds(new Box(0, 0, w, h));
			tree.setBounds(new Box(0, 0, w, h));
		});

		if(enableMod) {
			ButtonIcon newBtn = new ButtonIcon(gui, "editor", 0, 16, editor::addNew);
			newBtn.setBounds(new Box(5, height - 24, 18, 18));
			addElement(newBtn);
			editor.setAddEn.add(newBtn::setEnabled);

			ButtonIcon delBtn = new ButtonIcon(gui, "editor", 14, 16, editor::deleteSel);
			delBtn.setBounds(new Box(25, height - 24, 18, 18));
			addElement(delBtn);
			editor.setDelEn.add(delBtn::setEnabled);
		}

		ButtonIcon visBtn = new ButtonIcon(gui, "editor", 42, 16, editor::switchVis);
		visBtn.setBounds(new Box(enableMod ? 45 : 5, height - 24, 18, 18));
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

	@Override
	public void keyPressed(KeyboardEvent event) {
		KeybindHandler h = editor.frame.getKeybindHandler();
		h.registerKeybind(Keybinds.TREE_UP, () -> {
			editor.selectedElement = tree.findUp(editor.selectedElement);
			editor.updateGui();
		});
		h.registerKeybind(Keybinds.TREE_PREV, () -> {
			editor.selectedElement = tree.findPrev(editor.selectedElement);
			editor.updateGui();
		});
		h.registerKeybind(Keybinds.TREE_DOWN, () -> {
			if(editor.selectedElement == null)
				editor.selectedElement = editor.elements.get(0);
			else
				editor.selectedElement = tree.findDown(editor.selectedElement);
			editor.updateGui();
		});
		h.registerKeybind(Keybinds.TREE_NEXT, () -> {
			if(editor.selectedElement == null)
				editor.selectedElement = editor.elements.get(0);
			else
				editor.selectedElement = tree.findNext(editor.selectedElement);
			editor.updateGui();
		});
		super.keyPressed(event);
	}
}
