package com.tom.cpm.shared.editor.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.GuiElement;
import com.tom.cpm.shared.gui.elements.PopupMenu;
import com.tom.cpm.shared.math.Vec2i;

public class ModelElementsTree extends GuiElement {
	private Editor editor;
	private TreeElement root;
	private Map<Integer, TreeElement> map = new HashMap<>();
	private TreeElement moveElem;

	public ModelElementsTree(IGui gui, Editor editor) {
		super(gui);
		this.editor = editor;
		this.root = new TreeElement();
		this.root.display = "Root";
	}

	@Override
	public void mouseClick(MouseEvent evt) {
		if(evt.isConsumed())return;
		if(bounds.isInBounds(evt.x, evt.y)) {
			int yp = (evt.y - bounds.y) / 10;
			TreeElement elem = map.get(yp);
			if(elem != null) {
				if(evt.btn == 1) {
					if(elem.modelElement != null && (elem.modelElement.type == ElementType.NORMAL || moveElem != null)) {
						PopupMenu popup = new PopupMenu(gui);
						popup.addButton(moveElem != null ? gui.i18nFormat("button.cpm.tree.put") : gui.i18nFormat("button.cpm.tree.move"), () -> {
							if(moveElem != null) {
								if(moveElem.modelElement != elem.modelElement)
									editor.moveElement(moveElem.modelElement, elem.modelElement);
								moveElem = null;
							} else moveElem = elem;
						});
						Vec2i p = evt.getPos();
						popup.display(editor.gui, p.x, p.y);
					}
				} else {
					if(evt.x < 5 + elem.depth * 5 && elem != root && !elem.children.isEmpty()) {
						elem.showChildren = !elem.showChildren;
					} else {
						editor.selectedElement = elem.modelElement;
					}
				}
			} else {
				editor.selectedElement = null;
			}
			editor.updateGui();
			evt.consume();
		}
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		map.clear();
		drawTree(mouseX, mouseY, 0, new int[1], root);
	}

	private void drawTree(int mouseX, int mouseY, int x, int[] y, TreeElement e) {
		int yp = y[0]++;
		int textColor = 0xffffffff;
		if(moveElem != null && e.modelElement == moveElem.modelElement) {
			gui.drawBox(x * 5 - 1, yp * 10 - 1, bounds.w - 1, 12, 0xff000000);
		}
		if(e.modelElement == editor.selectedElement) {
			gui.drawBox(x * 5, yp * 10, bounds.w, 10, 0xff6666ff);
		}
		if(e.modelElement != null && !e.modelElement.show) {
			textColor = 0xffaaaaaa;
		}
		if(mouseX > bounds.x && mouseY > yp * 10 && mouseY < (yp+1) * 10) {
			textColor = 0xffffff00;
		}
		map.put(yp, e);
		gui.drawText(x * 5 + 3, yp * 10, e.display, textColor);
		e.depth = x;
		if(e.showChildren) {
			if(e != root && !e.children.isEmpty())gui.drawTexture(x * 5 - 5, yp * 10, 8, 8, 24, 8, "editor");
			for (TreeElement i : e.children) {
				drawTree(mouseX, mouseY, x + 1, y, i);
			}
		} else {
			if(e != root && !e.children.isEmpty())gui.drawTexture(x * 5 - 5, yp * 10, 8, 8, 24, 0, "editor");
		}
	}

	public void updateTree() {
		List<TreeElement> old = new ArrayList<>(root.children);
		root.children.clear();
		root.showChildren = true;
		editor.elements.forEach(e -> {
			if(e.parent == null) {
				TreeElement t = new TreeElement();
				t.display = e.name;
				t.modelElement = e;
				TreeElement oldTree = find(old, e);
				if(oldTree != null)t.showChildren = oldTree.showChildren;
				root.children.add(t);
				walkChildren(t, oldTree != null ? oldTree.children : null);
			}
		});
	}

	private TreeElement find(List<TreeElement> list, ModelElement elem) {
		for (TreeElement e : list) {
			if(e.modelElement == elem)return e;
			else {
				TreeElement d = find(e.children, elem);
				if(d != null)return d;
			}
		}
		return null;
	}

	private void walkChildren(TreeElement p, List<TreeElement> old) {
		for (ModelElement e : p.modelElement.children) {
			TreeElement t = new TreeElement();
			t.display = e.name;
			t.modelElement = e;
			p.children.add(t);
			TreeElement oldTree = null;
			if(old != null)oldTree = find(old, e);
			if(oldTree != null)t.showChildren = oldTree.showChildren;
			walkChildren(t, oldTree != null ? oldTree.children : null);
		}
	}

	private class TreeElement {
		private String display;
		private ModelElement modelElement;
		private List<TreeElement> children = new ArrayList<>();
		private boolean showChildren;
		private int depth;
	}
}
