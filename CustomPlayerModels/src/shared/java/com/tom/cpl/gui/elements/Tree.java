package com.tom.cpl.gui.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.MouseEvent;

public class Tree<T> extends GuiElement {
	private TreeElement root;
	private Map<Integer, TreeElement> map = new HashMap<>();
	private TreeModel<T> model;
	private Frame frame;

	public Tree(Frame gui, TreeModel<T> model) {
		super(gui.getGui());
		this.frame = gui;
		this.root = new TreeElement();
		this.root.display = "Root";
		this.model = model;
	}

	@Override
	public void mouseClick(MouseEvent evt) {
		if(evt.isConsumed())return;
		if(bounds.isInBounds(evt.x, evt.y)) {
			int yp = (evt.y - bounds.y) / 10;
			TreeElement elem = map.get(yp);
			if(elem != null) {
				if(evt.btn == 0 && evt.x < 5 + elem.depth * 5 && elem != root && !elem.children.isEmpty()) {
					elem.showChildren = !elem.showChildren;
				} else {
					model.onClick(evt, elem.value);
				}
			} else {
				model.onClick(evt, null);
			}
			model.treeUpdated();
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
		int textColor = gui.getColors().button_text_color;
		int bg = e.value == null ? 0 : model.bgColor(e.value);
		if(bg != 0) {
			gui.drawBox(x * 5, yp * 10, bounds.w, 10, bg);
		}
		int txtc = e.value == null ? 0 : model.textColor(e.value);
		if(txtc != 0) {
			textColor = txtc;
		}
		if(mouseX > bounds.x && mouseY > yp * 10 && mouseY < (yp+1) * 10) {
			textColor = gui.getColors().button_text_hover;
			Tooltip tt = model.getTooltip(e.value);
			if(tt != null)tt.set();
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
		model.getElements(null, e -> {
			TreeElement t = new TreeElement();
			t.display = model.getName(e);
			t.value = e;
			TreeElement oldTree = find(old, e);
			if(oldTree != null)t.showChildren = oldTree.showChildren;
			root.children.add(t);
			model.refresh(e);
			walkChildren(t, oldTree != null ? oldTree.children : null);
		});
	}

	private TreeElement find(List<TreeElement> list, T elem) {
		for (TreeElement e : list) {
			if(e.value == elem)return e;
			else {
				TreeElement d = find(e.children, elem);
				if(d != null)return d;
			}
		}
		return null;
	}

	private void walkChildren(TreeElement p, List<TreeElement> old) {
		model.getElements(p.value, e -> {
			TreeElement t = new TreeElement();
			t.display = model.getName(e);
			t.value = e;
			p.children.add(t);
			TreeElement oldTree = null;
			if(old != null)oldTree = find(old, e);
			if(oldTree != null)t.showChildren = oldTree.showChildren;
			walkChildren(t, oldTree != null ? oldTree.children : null);
		});
	}

	private class TreeElement {
		private String display;
		private T value;
		private List<TreeElement> children = new ArrayList<>();
		private boolean showChildren;
		private int depth;
	}

	public static abstract class TreeModel<T> {
		protected abstract int textColor(T val);
		protected abstract void getElements(T parent, Consumer<T> c);
		protected abstract int bgColor(T val);
		protected abstract void treeUpdated();
		protected abstract void onClick(MouseEvent evt, T elem);
		protected abstract String getName(T elem);
		protected abstract Tooltip getTooltip(T elem);
		protected abstract void refresh(T elem);
	}
}
