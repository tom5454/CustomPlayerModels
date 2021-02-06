package com.tom.cpm.shared.editor.anim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.util.PlayerPartValues;
import com.tom.cpm.shared.editor.util.ValueOp;
import com.tom.cpm.shared.math.Vec3f;
import com.tom.cpm.shared.model.PlayerModelParts;

public class AnimFrame {
	private final Map<ModelElement, Data> components = new HashMap<>();
	private EditorAnim anim;
	public AnimFrame(EditorAnim anim) {
		this.anim = anim;
	}

	public AnimFrame(AnimFrame cpy) {
		this.anim = cpy.anim;
		for (Entry<ModelElement, Data> e : cpy.components.entrySet()) {
			components.put(e.getKey(), new Data(e.getValue()));
		}
	}

	private class Data implements IElem {
		private Vec3f pos, rot, color;
		private boolean show = true;
		private ModelElement comp;

		public Data(ModelElement comp) {
			this.comp = comp;
			if(!anim.add) {
				if(comp.type == ElementType.PLAYER_PART) {
					PlayerPartValues val = PlayerPartValues.getFor((PlayerModelParts) comp.typeData, anim.editor.skinType);
					pos = val.getPos();
					rot = new Vec3f();
				} else {
					pos = new Vec3f(comp.pos);
					rot = new Vec3f(comp.rotation);
				}
			} else {
				pos = new Vec3f();
				rot = new Vec3f();
			}
			int r = (comp.rgb & 0xff0000) >> 16;
			int g = (comp.rgb & 0x00ff00) >> 8;
			int b =  comp.rgb & 0x0000ff;
			color = new Vec3f(r, g, b);
			show = comp.show;
		}

		public Data(Data cpy) {
			this.comp = cpy.comp;
			pos = new Vec3f(cpy.pos);
			rot = new Vec3f(cpy.rot);
			color = new Vec3f(cpy.color);
			show = cpy.show;
		}

		@Override
		public Vec3f getPosition() {
			return pos;
		}

		@Override
		public Vec3f getRotation() {
			return rot;
		}

		@Override
		public Vec3f getColor() {
			return color;
		}

		@Override
		public boolean isVisible() {
			return show;
		}

		private void apply() {
			comp.rc.setRotation(anim.add,
					(float) Math.toRadians(rot.x),
					(float) Math.toRadians(rot.y),
					(float) Math.toRadians(rot.z)
					);
			comp.rc.setPosition(anim.add, pos.x, pos.y, pos.z);
			comp.rc.setColor(color.x, color.y, color.z);
			comp.rc.display = show;
		}

		public boolean hasChanges() {
			return hasPosChanges() || hasRotChanges() || hasColorChanges() || hasVisChanges();
		}

		public boolean hasPosChanges() {
			if(anim.add) {
				return Math.abs(pos.x) > 0.01f || Math.abs(pos.y) > 0.01f || Math.abs(pos.z) > 0.01f;
			} else {
				return Math.abs(pos.x - comp.pos.x) > 0.01f || Math.abs(pos.y - comp.pos.y) > 0.01f || Math.abs(pos.z - comp.pos.z) > 0.01f;
			}
		}

		public boolean hasRotChanges() {
			if(anim.add) {
				return Math.abs(rot.x) > 0.01f || Math.abs(rot.y) > 0.01f || Math.abs(rot.z) > 0.01f;
			} else {
				return Math.abs(rot.x - comp.rotation.x) > 0.01f || Math.abs(rot.y - comp.rotation.y) > 0.01f || Math.abs(rot.z - comp.rotation.z) > 0.01f;
			}
		}

		public boolean hasColorChanges() {
			if(!comp.texture || comp.recolor) {
				int rgb = (((int) color.x) << 16) | (((int) color.y) << 8) | ((int) color.z);
				if((comp.rgb & 0xffffff) != (rgb & 0xffffff))return true;
			}
			return false;
		}

		public boolean hasVisChanges() {
			return comp.show != show;
		}
	}

	public void setPos(ModelElement elem, Vec3f v) {
		Data dt = components.get(elem);
		if(dt == null) {
			anim.editor.addUndo(() -> components.remove(elem));
			dt = components.computeIfAbsent(elem, Data::new);
			final Data d = dt;
			anim.editor.currentOp = () -> components.put(elem, d);
		} else {
			anim.editor.addUndo(new ValueOp<>(dt, dt.pos, (a, b) -> a.pos = b));
			anim.editor.currentOp = null;
		}
		dt.pos = v;
		anim.editor.appendCurrentOp(new ValueOp<>(dt, dt.pos, (a, b) -> a.pos = b));
		anim.editor.markDirty();
	}

	public void setRot(ModelElement elem, Vec3f v) {
		Data dt = components.get(elem);
		if(dt == null) {
			anim.editor.addUndo(() -> components.remove(elem));
			dt = components.computeIfAbsent(elem, Data::new);
			final Data d = dt;
			anim.editor.currentOp = () -> components.put(elem, d);
		} else {
			anim.editor.addUndo(new ValueOp<>(dt, dt.rot, (a, b) -> a.rot = b));
			anim.editor.currentOp = null;
		}
		dt.rot = v;
		anim.editor.appendCurrentOp(new ValueOp<>(dt, dt.rot, (a, b) -> a.rot = b));
		anim.editor.markDirty();
	}

	public void setColor(ModelElement elem, int rgb) {
		Data dt = components.get(elem);
		if(dt == null) {
			anim.editor.addUndo(() -> components.remove(elem));
			dt = components.computeIfAbsent(elem, Data::new);
			final Data d = dt;
			anim.editor.currentOp = () -> components.put(elem, d);
		} else {
			anim.editor.addUndo(new ValueOp<>(dt, dt.color, (a, b) -> a.color = b));
			anim.editor.currentOp = null;
		}
		int r = (rgb & 0xff0000) >> 16;
		int g = (rgb & 0x00ff00) >> 8;
		int b =  rgb & 0x0000ff;
		dt.color = new Vec3f(r, g, b);
		anim.editor.appendCurrentOp(new ValueOp<>(dt, dt.color, (a, c) -> a.color = c));
		anim.editor.markDirty();
	}

	public void switchVis(ModelElement elem) {
		Data dt = components.get(elem);
		if(dt == null) {
			anim.editor.addUndo(() -> components.remove(elem));
			dt = components.computeIfAbsent(elem, Data::new);
			final Data d = dt;
			anim.editor.currentOp = () -> components.put(elem, d);
		} else {
			anim.editor.addUndo(new ValueOp<>(dt, dt.show, (a, b) -> a.show = b));
			anim.editor.currentOp = null;
		}
		dt.show = !dt.show;
		anim.editor.appendCurrentOp(new ValueOp<>(dt, dt.show, (a, c) -> a.show = c));
		anim.editor.markDirty();
	}

	public IElem getData(ModelElement modelElement) {
		return components.get(modelElement);
	}

	public void apply() {
		components.values().forEach(Data::apply);
	}

	public boolean getVisible(ModelElement component) {
		if(!components.containsKey(component))return component.show;
		return components.get(component).show;
	}

	public Stream<ModelElement> getAllElements() {
		return components.keySet().stream();
	}

	public Stream<ModelElement> getAllElementsFiltered() {
		return components.entrySet().stream().filter(e -> e.getValue().hasChanges()).map(Entry::getKey);
	}

	@SuppressWarnings("unchecked")
	public void loadFrom(Map<String, Object> data) {
		List<Map<String, Object>> c = (List<Map<String, Object>>) data.get("components");
		for (Map<String, Object> map : c) {
			long sid = ((Number)map.get("storeID")).longValue();
			anim.editor.walkElements(anim.editor.elements, elem -> {
				if(elem.storeID == sid) {
					Data dt = new Data(elem);
					components.put(elem, dt);
					dt.pos = new Vec3f((Map<String, Object>) map.get("pos"), new Vec3f());
					dt.rot = new Vec3f((Map<String, Object>) map.get("rotation"), new Vec3f());
					int rgb = Integer.parseUnsignedInt((String) map.get("color"), 16);
					int r = (rgb & 0xff0000) >> 16;
					int g = (rgb & 0x00ff00) >> 8;
					int b =  rgb & 0x0000ff;
					dt.color = new Vec3f(r, g, b);
					dt.show = (boolean) map.get("show");
				}
			});
		}
	}

	public Map<String, Object> store() {
		List<Map<String, Object>> c = new ArrayList<>();
		for(Entry<ModelElement, Data> e : components.entrySet()) {
			Map<String, Object> map = new HashMap<>();
			c.add(map);
			map.put("storeID", e.getKey().storeID);
			Data dt = e.getValue();
			map.put("pos", dt.pos.toMap());
			map.put("rotation", dt.rot.toMap());
			int rgb = (((int) dt.color.x) << 16) | (((int) dt.color.y) << 8) | ((int) dt.color.z);
			map.put("color", Integer.toHexString(rgb));
			map.put("show", dt.show);
		}
		Map<String, Object> data = new HashMap<>();
		data.put("components", c);
		return data;
	}

	public boolean hasPosChanges(ModelElement me) {
		Data data = components.get(me);
		if(data == null)return false;
		return data.hasPosChanges();
	}

	public boolean hasRotChanges(ModelElement me) {
		Data data = components.get(me);
		if(data == null)return false;
		return data.hasRotChanges();
	}

	public boolean hasColorChanges(ModelElement me) {
		Data data = components.get(me);
		if(data == null)return false;
		return data.hasColorChanges();
	}

	public boolean hasVisChanges(ModelElement me) {
		Data data = components.get(me);
		if(data == null)return false;
		return data.hasVisChanges();
	}
}
