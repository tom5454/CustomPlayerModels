package com.tom.cpm.shared.editor.util;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.tree.TreeElement.TreeSettingElement;

public abstract class UVResizableArea {
	protected final Editor editor;
	protected final TreeElement parent;
	public final TreeSettingElement face;
	public final List<TreeSettingElement> elements;

	public UVResizableArea(Editor editor, TreeElement parent) {
		this.editor = editor;
		this.parent = parent;
		face = face();
		elements = new ArrayList<>();
		for(int i = 0;i<4;i++)
			elements.add(new CornerArea(editor, parent, i));
		for(int i = 0;i<4;i++)
			elements.add(new SideArea(editor, parent, i));
		elements.add(face);
	}

	protected abstract Area getArea();
	protected abstract void setArea(Area v, boolean moveOnly);
	protected abstract void setAreaTemp(Area v);

	protected FaceArea face() {
		return new FaceArea(editor, parent);
	}

	protected SideArea side(int i) {
		return new SideArea(editor, parent, i);
	}

	protected CornerArea corner(int i) {
		return new CornerArea(editor, parent, i);
	}

	public static class Area {
		public int sx, sy, ex, ey;

		public Area(int sx, int sy, int ex, int ey) {
			this.sx = sx;
			this.sy = sy;
			this.ex = ex;
			this.ey = ey;
		}
	}

	public static Vec4f expandBox(Box b, float zoom) {
		if(b.w == 0 || b.h == 0) {
			float sx = b.x;
			float sy = b.y;
			float w = b.w;
			float h = b.h;
			float ex = 2 / zoom;
			if(b.w == 0) {
				sx -= ex;
				w = ex * 2;
			}
			if(b.h == 0) {
				sy -= ex;
				h = ex * 2;
			}
			if(b.w == 0 && b.h == 0) {
				sx -= ex;
				w = ex * 4;
				sy -= ex;
				h = ex * 4;
			}
			return new Vec4f(sx, sy, w, h);
		} else {
			return new Vec4f(b.x, b.y, b.w, b.h);
		}
	}

	public static boolean isHovered(Box b, float x, float y, float zoom) {
		if(b == null)return false;
		if(b.w == 0 || b.h == 0) {
			Vec4f e = expandBox(b, zoom);
			return e.x <= x && e.y <= y && e.x+e.z > x && e.y+e.w > y;
		}
		return b.isInBounds((int) x, (int) y);
	}

	private abstract class BaseArea implements TreeSettingElement {
		protected final Editor editor;
		protected final TreeElement parent;

		public BaseArea(Editor editor, TreeElement parent) {
			this.editor = editor;
			this.parent = parent;
		}

		@Override
		public TreeElement getParent() {
			return parent;
		}

		@Override
		public void drawTexture(IGui gui, int x, int y, float xs, float ys) {
			if(editor.selectedElement == this)TreeSettingElement.super.drawTexture(gui, x, y, xs, ys);
			Box b = getTextureBox();

			Vec4f v = expandBox(b, xs);
			gui.drawRectangle(x + v.x * xs, y + v.y * ys, v.z * xs, v.w * ys, 0xff999999);
		}
	}

	protected class FaceArea extends BaseArea {

		public FaceArea(Editor editor, TreeElement parent) {
			super(editor, parent);
		}

		@Override
		public Vec3f getVec(VecType type) {
			Area f = getArea();
			if(type == VecType.TEXTURE)return new Vec3f(f.sx, f.sy, 1);
			return Vec3f.ZERO;
		}

		@Override
		public void setVec(Vec3f vec, VecType object) {
			if(object == VecType.TEXTURE) {
				Area f = getArea();
				int x = (int) vec.x - f.sx;
				int y = (int) vec.y - f.sy;
				f.sx += x;
				f.sy += y;
				f.ex += x;
				f.ey += y;
				setArea(f, true);
			}
		}

		@Override
		public void setVecTemp(VecType type, Vec3f vec) {
			if(type == VecType.TEXTURE) {
				Area f = getArea();
				int x = (int) vec.x - f.sx;
				int y = (int) vec.y - f.sy;
				f.sx += x;
				f.sy += y;
				f.ex += x;
				f.ey += y;
				setAreaTemp(f);
			}
		}

		@Override
		public Box getTextureBox() {
			Area f = getArea();
			int sx = Math.min(f.sx, f.ex);
			int sy = Math.min(f.sy, f.ey);
			int ex = Math.max(f.sx, f.ex);
			int ey = Math.max(f.sy, f.ey);
			return new Box(sx, sy, ex - sx, ey - sy);
		}
	}

	protected class CornerArea extends BaseArea {
		private int corner;
		public CornerArea(Editor editor, TreeElement parent, int corner) {
			super(editor, parent);
			this.corner = corner;
		}

		@Override
		public Vec3f getVec(VecType type) {
			Area f = getArea();
			if(type == VecType.TEXTURE)return new Vec3f(corner % 2 != 0 ? f.ex : f.sx, corner > 1 ? f.ey : f.sy, 1);
			return Vec3f.ZERO;
		}

		@Override
		public void setVec(Vec3f vec, VecType object) {
			if(object == VecType.TEXTURE) {
				Area f = getArea();
				if(corner % 2 != 0) f.ex = (int) vec.x;
				else f.sx = (int) vec.x;
				if(corner > 1) f.ey = (int) vec.y;
				else f.sy = (int) vec.y;
				setArea(f, false);
			}
		}

		@Override
		public void setVecTemp(VecType type, Vec3f vec) {
			if(type == VecType.TEXTURE) {
				Area f = getArea();
				if(corner % 2 != 0) f.ex = (int) vec.x;
				else f.sx = (int) vec.x;
				if(corner > 1) f.ey = (int) vec.y;
				else f.sy = (int) vec.y;
				setAreaTemp(f);
			}
		}

		@Override
		public Box getTextureBox() {
			Area f = getArea();
			return new Box(
					(corner % 2 != 0 ? f.ex : f.sx),
					(corner > 1 ? f.ey : f.sy),
					0, 0
					);
		}
	}

	protected class SideArea extends BaseArea {
		private int side;

		public SideArea(Editor editor, TreeElement parent, int side) {
			super(editor, parent);
			this.side = side;
		}

		@Override
		public Vec3f getVec(VecType type) {
			Area f = getArea();
			if(type == VecType.TEXTURE)return new Vec3f(side > 1 && side % 2 != 0 ? f.ex : f.sx, side > 1 && side % 2 == 0 ? f.ey : f.sy, 1);
			return Vec3f.ZERO;
		}

		@Override
		public void setVec(Vec3f vec, VecType object) {
			if(object == VecType.TEXTURE) {
				Area f = getArea();
				if(side % 2 == 0) {
					if(side > 1) f.ey = (int) vec.y;
					else f.sy = (int) vec.y;
				} else {
					if(side > 1) f.ex = (int) vec.x;
					else f.sx = (int) vec.x;
				}
				setArea(f, false);
			}
		}

		@Override
		public void setVecTemp(VecType type, Vec3f vec) {
			if(type == VecType.TEXTURE) {
				Area f = getArea();
				if(side % 2 == 0) {
					if(side > 1) f.ey = (int) vec.y;
					else f.sy = (int) vec.y;
				} else {
					if(side > 1) f.ex = (int) vec.x;
					else f.sx = (int) vec.x;
				}
				setAreaTemp(f);
			}
		}

		@Override
		public Box getTextureBox() {
			Area f = getArea();
			return new Box(
					(side > 1 && side % 2 != 0 ? f.ex : f.sx),
					(side > 1 && side % 2 == 0 ? f.ey : f.sy),
					(side % 2 == 0 ? (f.ex - f.sx) : 0),
					(side % 2 != 0 ? (f.ey - f.sy) : 0)
					);
		}
	}
}
