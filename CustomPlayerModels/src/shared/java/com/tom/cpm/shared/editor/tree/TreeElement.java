package com.tom.cpm.shared.editor.tree;

import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.elements.Tree.TreeModel;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.Effect;

public abstract interface TreeElement {
	public class ModelTree extends TreeModel<TreeElement> {
		private Editor e;
		private TreeElement moveElem;

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
				e.templates.forEach(c);
				if(e.templateSettings != null)c.accept(e.templateSettings);
				if(e.scalingElem.enabled)c.accept(e.scalingElem);
				c.accept(e.texElem);
			} else
				parent.getTreeElements(c);
		}

		@Override
		protected int bgColor(TreeElement val) {
			int bg = val.bgColor();
			if(bg != 0)return bg;
			if(moveElem != null && moveElem == val)return e.gui().getColors().move_background;
			if(e.selectedElement == val)return e.colors().select_background;
			return 0;
		}

		@Override
		protected void treeUpdated() {
			e.updateGui();
		}

		@Override
		protected void onClick(MouseEvent evt, TreeElement elem) {
			if(evt.btn == 1 && elem != null) {
				PopupMenu popup = new PopupMenu(e.gui(), e.frame);
				if(elem.canMove() || (moveElem != null && elem.canAccept(moveElem))) {
					String btnTxt;
					if(moveElem != null) {
						if(moveElem == elem)btnTxt = e.gui().i18nFormat("button.cpm.tree.cancelMove");
						else btnTxt = e.gui().i18nFormat("button.cpm.tree.put");
					} else btnTxt = e.gui().i18nFormat("button.cpm.tree.move");
					popup.addButton(btnTxt, () -> {
						if(moveElem != null) {
							if(moveElem != elem)
								elem.accept(moveElem);
							moveElem = null;
						} else moveElem = elem;
					});
				}
				elem.populatePopup(popup);
				if(popup.getY() > 0) {
					Vec2i p = evt.getPos();
					popup.display(p.x, p.y);
				}
			} else {
				if(elem != null)elem.onClick(evt);
				e.selectedElement = elem;
			}
		}

		@Override
		protected String getName(TreeElement elem) {
			return elem.getName();
		}

		@Override
		protected Tooltip getTooltip(TreeElement elem) {
			if(elem != null)return elem.getTooltip();
			return null;
		}

		@Override
		protected void refresh(TreeElement elem) {
			elem.onRefreshTree();
		}

		@Override
		protected boolean isSelected(TreeElement elem) {
			return e.selectedElement == elem;
		}
	}

	public String getName();
	public default int textColor() { return 0; }
	public default int bgColor() { return 0; }
	public default void accept(TreeElement elem) { throw new UnsupportedOperationException(); }
	public default boolean canAccept(TreeElement elem) { return false; }
	public default boolean canMove() { return false; }
	public default void getTreeElements(Consumer<TreeElement> c) {}
	public default void onClick(MouseEvent evt) {}
	public default void populatePopup(PopupMenu popup) {}
	public default Tooltip getTooltip() { return null; }

	public static enum VecType {
		SIZE,
		OFFSET,
		ROTATION,
		POSITION,
		SCALE,
		TEXTURE,
		;
		public static final VecType[] MOUSE_EDITOR_TYPES = {SIZE, OFFSET, ROTATION, POSITION};
		public static final VecType[] MOUSE_EDITOR_ANIM_TYPES = {ROTATION, POSITION};
	}

	public default void setVec(Vec3f v, VecType object) {}
	public default void setElemName(String name) {}
	public default String getElemName() { return ""; }
	public default void drawTexture(IGui gui, int x, int y, float xs, float ys) {}
	public default ETextures getTexture() { return null; }
	public default Box getTextureBox() { return null; }
	public default void modeSwitch() {}
	public default void updateGui() {}
	public default void onRefreshTree() {}
	public default void addNew() {}
	public default void delete() {}
	public default void setElemColor(int color) {}
	public default void setMCScale(float scale) {}
	public default void switchVis() {}
	public default void switchEffect(Effect effect) {}
	public default float getValue() { return 0; }
	public default void setValue(float value) {}
}
