package com.tom.cpm.shared.editor.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.gui.Keybinds;

public class PartSearchPopup extends PopupPanel {
	private TextField search;
	private Editor editor;
	private List<ModelElement> searchIndex = new ArrayList<>();
	private List<ModelElement> filteredElems = new ArrayList<>();
	private List<TreeElement> treeOpen;
	private String prev = "";

	public PartSearchPopup(IGui gui, Editor editor) {
		super(gui);

		this.editor = editor;
		editor.elements.forEach(this::walk);
		this.treeOpen = editor.treeHandler.getOpenElements();

		String info = gui.i18nFormat("label.cpm.findElement.info", Keybinds.TREE_PREV.getSetKey(gui), Keybinds.TREE_NEXT.getSetKey(gui));

		int w = Math.min(Math.max(gui.textWidth(info) + 12, 160), gui.getFrame().getBounds().w / 2);

		search = new TextField(gui) {

			@Override
			public void draw(MouseEvent event, float partialTicks) {
				super.draw(event, partialTicks);

				if (filteredElems.isEmpty() && !getText().isEmpty()) {
					Box b = getBounds();
					gui.drawRectangle(b.x, b.y, b.w, b.h, 0xffff0000);
				}
			}
		};
		search.setBounds(new Box(5, 5, w - 10, 20));
		search.setEventListener(this::updateSearch);
		search.setFocused(true);
		addElement(search);

		addElement(new Label(gui, info).setBounds(new Box(5, 30, 0, 0)));

		setBounds(new Box(0, 0, w, 45));
	}

	private void walk(ModelElement me) {
		searchIndex.add(me);
		me.children.forEach(this::walk);
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.findElement");
	}

	private void updateSearch() {
		String text = search.getText();
		if (text.isEmpty() || prev.equals(text))return;
		prev = text;

		Pattern m = null;
		try {
			m = Pattern.compile(text.toLowerCase(), Pattern.CASE_INSENSITIVE);
		} catch (Throwable ignore) {
			try {
				m = Pattern.compile(Pattern.quote(text.toLowerCase()), Pattern.CASE_INSENSITIVE);
			} catch (Throwable __) {
				return;
			}
		}
		filteredElems.clear();
		for (ModelElement me : searchIndex) {
			if (m.matcher(me.name.toLowerCase()).find()) {
				filteredElems.add(me);
			}
		}

		if (!filteredElems.isEmpty() && !filteredElems.contains(editor.getSelectedElement())) {
			editor.treeHandler.setOpenElements(treeOpen);
			editor.selectedElement = filteredElems.get(0);
			editor.updateGui();
		}
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if (Keybinds.TREE_PREV.isPressed(gui, event)) {
			int i = filteredElems.indexOf(editor.getSelectedElement());
			if(i < 1)return;
			i--;
			editor.treeHandler.setOpenElements(treeOpen);
			editor.selectedElement = filteredElems.get(i);
			editor.updateGui();
			event.consume();
		} else if (Keybinds.TREE_NEXT.isPressed(gui, event)) {
			int i = filteredElems.indexOf(editor.getSelectedElement());
			if(i == -1)return;
			i++;
			if(i >= filteredElems.size())return;
			editor.treeHandler.setOpenElements(treeOpen);
			editor.selectedElement = filteredElems.get(i);
			editor.updateGui();
			event.consume();
		} else if(event.keyCode == gui.getKeyCodes().KEY_ESCAPE) {
			close();
			event.consume();
		}
		super.keyPressed(event);
	}
}
