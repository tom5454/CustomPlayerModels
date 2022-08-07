package com.tom.cpm.shared.model.render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.util.Direction;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorTool;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.tree.TreeElement.TreeSettingElement;
import com.tom.cpm.shared.editor.util.UVResizableArea;
import com.tom.cpm.shared.io.IOHelper;

public class PerFaceUV {
	public Map<Direction, Face> faces = new HashMap<>();

	public PerFaceUV() {
		for (Direction d : Direction.VALUES) {
			faces.put(d, new Face());
		}
	}

	public PerFaceUV(ModelElement el) {
		float w = el.size.x;
		float h = el.size.y;
		float d = el.size.z;

		int ts = Math.abs(el.textureSize);
		int dx = MathHelper.ceil(w * ts);
		int dy = MathHelper.ceil(h * ts);
		int dz = MathHelper.ceil(d * ts);

		int f4 = el.u;
		int f5 = el.u + dz;
		int f6 = el.u + dz + dx;
		int f7 = el.u + dz + dx + dx;
		int f8 = el.u + dz + dx + dz;
		int f9 = el.u + dz + dx + dz + dx;
		int f10 = el.v;
		int f11 = el.v + dz;
		int f12 = el.v + dz + dy;

		faces.put(Direction.UP,    new Face(f5, f10, f6, f11));
		faces.put(Direction.DOWN,  new Face(f6, f11, f7, f10));
		faces.put(Direction.WEST,  new Face(f4, f11, f5, f12));
		faces.put(Direction.NORTH, new Face(f5, f11, f6, f12));
		faces.put(Direction.EAST,  new Face(f6, f11, f8, f12));
		faces.put(Direction.SOUTH, new Face(f8, f11, f9, f12));
	}

	public PerFaceUV(PerFaceUV pfUV) {
		pfUV.faces.forEach((d, f) -> faces.put(d, new Face(f)));
	}

	public PerFaceUV(JsonMap map) {
		for (Direction d : Direction.VALUES) {
			if(map.containsKey(d.name().toLowerCase()))
				faces.put(d, Face.load(map.getMap(d.name().toLowerCase())));
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("PerFaceUV");
		faces.forEach((d, f) -> {
			b.append("\n\t");
			b.append(d.name());
			b.append(": ");
			b.append(f);
		});
		return b.toString();
	}

	public static class Face {
		public int sx, sy, ex, ey;
		public Rot rotation = Rot.ROT_0;
		public boolean autoUV;
		private UVArea controlElems;

		public Face() {}

		public Face(Face f) {
			sx = f.sx;
			sy = f.sy;
			ex = f.ex;
			ey = f.ey;
			rotation = f.rotation;
			autoUV = f.autoUV;
		}

		private Face(JsonMap m) {
			sx = m.getInt("sx");
			sy = m.getInt("sy");
			ex = m.getInt("ex");
			ey = m.getInt("ey");
			String rot = "ROT_" + m.getString("rot");
			for (Rot r : Rot.VALUES) {
				if(r.name().equals(rot)) {
					rotation = r;
					break;
				}
			}
			autoUV = m.getBoolean("autoUV", false);
		}

		private Face(int sx, int sy, int ex, int ey) {
			this.sx = sx;
			this.sy = sy;
			this.ex = ex;
			this.ey = ey;
			autoUV = true;
		}

		public static Face load(JsonMap m) {
			if(m == null)return null;
			return new Face(m);
		}

		public Map<String, Object> toMap() {
			Map<String, Object> m = new HashMap<>();
			m.put("sx", sx);
			m.put("sy", sy);
			m.put("ex", ex);
			m.put("ey", ey);
			m.put("rot", rotation.name().toLowerCase().substring(4));
			m.put("autoUV", autoUV);
			return m;
		}

		public int getVertexU(int index) {
			int i = this.getVertexRotated(index);
			return i != 0 && i != 1 ? ex : sx;
		}

		public int getVertexV(int index)  {
			int i = this.getVertexRotated(index);
			return i != 0 && i != 3 ? ey : sy;
		}

		private int getVertexRotated(int index)  {
			return (index + this.rotation.ordinal() + 3) % 4;
		}

		public void set(Vec4f v) {
			sx = (int) v.x;
			sy = (int) v.y;
			ex = (int) v.z;
			ey = (int) v.w;
		}

		public Vec4f getVec() {
			return new Vec4f(sx, sy, ex, ey);
		}
	}

	public void readFaces(IOHelper h) throws IOException {
		int hidden = h.read();
		for (Direction dir : Direction.VALUES) {
			if((hidden & (1 << dir.ordinal())) != 0) {
				Face f = new Face();
				f.sx = h.readVarInt();
				f.sy = h.readVarInt();
				f.ex = h.readVarInt();
				f.ey = h.readVarInt();
				f.rotation = h.readEnum(Rot.VALUES);
				faces.put(dir, f);
			}
		}
	}

	public void writeFaces(IOHelper h) throws IOException {
		int hidden = 0;
		for (Direction dir : Direction.VALUES) {
			if(faces.get(dir) != null) {
				hidden |= (1 << dir.ordinal());
			}
		}
		h.write(hidden);
		for (Direction dir : Direction.VALUES) {
			Face face = faces.get(dir);
			if(face != null) {
				h.writeVarInt(face.sx);
				h.writeVarInt(face.sy);
				h.writeVarInt(face.ex);
				h.writeVarInt(face.ey);
				h.writeEnum(face.rotation);
			}
		}
	}

	public Map<String, Object> toMap() {
		Map<String, Object> m = new HashMap<>();
		faces.forEach((d, f) -> m.put(d.name().toLowerCase(), f.toMap()));
		return m;
	}

	public static enum Rot {
		ROT_0, ROT_90, ROT_180, ROT_270
		;
		public static final Rot[] VALUES = values();
	}

	public boolean contains(Direction key) {
		return faces.containsKey(key);
	}

	public Face get(Direction key) {
		return faces.get(key);
	}

	public Vec4f getVec(Direction key) {
		Face f = faces.get(key);
		if(f == null)return new Vec4f(0, 0, 0, 0);
		else return f.getVec();
	}

	public Rot getRot(Direction key) {
		Face f = faces.get(key);
		if(f == null)return Rot.ROT_0;
		else return f.rotation;
	}

	public Boolean isAutoUV(Direction key) {
		Face f = faces.get(key);
		if(f == null)return false;
		else return f.autoUV;
	}

	public List<TreeSettingElement> getDragBoxes(Editor editor, ModelElement parent) {
		List<TreeSettingElement> l = new ArrayList<>();
		List<TreeSettingElement> sel = new ArrayList<>();
		boolean show = editor.drawMode.get() == EditorTool.MOVE_UV;
		for(Direction d : Direction.VALUES) {
			Face f = faces.get(d);
			if(f == null)continue;
			if(f.controlElems == null) {
				f.controlElems = new UVArea(editor, parent, d, f);
			}
			if(show && d == editor.perfaceFaceDir.get()) {
				sel.addAll(f.controlElems.elements);
			} else {
				l.add(f.controlElems.face);
			}
		}
		sel.addAll(l);
		return sel;
	}

	private static class UVArea extends UVResizableArea {
		private Face f;
		private Direction d;

		public UVArea(Editor editor, ModelElement parent, Direction d, Face f) {
			super(editor, parent);
			this.f = f;
			this.d = d;
		}

		@Override
		protected Area getArea() {
			return new Area(f.sx, f.sy, f.ex, f.ey);
		}

		@Override
		protected void setArea(Area area, boolean moveOnly) {
			ActionBuilder ab = editor.action("set", "action.cpm.texUV");
			Vec4f v = new Vec4f(area.sx, area.sy, area.ex, area.ey);
			ab.updateValueOp(f, f.getVec(), v, Face::set);
			if(!moveOnly)ab.updateValueOp(f, f.autoUV, false, (a, b) -> a.autoUV = b);
			ab.onAction(((ModelElement)parent)::markDirty);
			ab.execute();
		}

		@Override
		protected void setAreaTemp(Area area) {
			f.set(new Vec4f(area.sx, area.sy, area.ex, area.ey));
			((ModelElement) parent).markDirty();
		}

		@Override
		protected FaceArea face() {
			return new FaceArea(editor, parent) {

				@Override
				public void onClick(Editor e, MouseEvent evt) {
					super.onClick(e, evt);
					e.perfaceFaceDir.accept(d);
				}
			};
		}
	}
}
