package com.tom.cpm.shared.editor.tree;

import java.util.function.Consumer;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.gui.elements.Tree.TreeModel;

public abstract interface TreeElement {
	public class ModelTree extends TreeModel<TreeElement> {
		private Editor e;

		public ModelTree(Editor e) {
			this.e = e;
		}

		@Override
		protected int textColor(TreeElement val) {
			return val.textColor();
		}

		@Override
		protected void getElements(TreeElement parent, Consumer<TreeElement> c) {
			if(parent == null) {
				e.elements.forEach(c);
			} else
				parent.getTreeElements(c);
		}

		@Override
		protected int bgColor(TreeElement val) {
			return val.bgColor();
		}

		@Override
		protected void treeUpdated() {
			e.updateGui();
		}

		@Override
		protected void moveElement(TreeElement elem, TreeElement to) {
			to.accept(elem);
		}

		@Override
		protected boolean canMove(TreeElement elem) {
			return elem.canMove();
		}

		@Override
		protected boolean canMoveTo(TreeElement elem, TreeElement to) {
			return to.canAccept(elem);
		}

		@Override
		protected void onClick(TreeElement elem) {
			if(elem == null) {
				e.selectedElement = null;
			} else elem.onClick();
		}

		@Override
		protected String getName(TreeElement elem) {
			return elem.getName();
		}
	}

	public String getName();
	public int textColor();
	public int bgColor();
	public void accept(TreeElement elem);
	public boolean canAccept(TreeElement elem);
	public boolean canMove();
	public void getTreeElements(Consumer<TreeElement> c);
	public void onClick();
}
