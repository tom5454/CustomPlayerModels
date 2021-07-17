package com.tom.cpm.shared.editor.anim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.animation.InterpolatorChannel;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.render.VanillaModelPart;

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
				if(comp.type == ElementType.ROOT_PART) {
					PartValues val = ((VanillaModelPart) comp.typeData).getDefaultSize(anim.editor.skinType);
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
		ActionBuilder ab = anim.editor.action("setAnim", "label.cpm.position");
		if(dt == null) {
			dt = new Data(elem);
			ab.addToMap(components, elem, dt);
		}
		ab.updateValueOp(dt, dt.pos, v, (a, b) -> a.pos = b);
		ab.execute();
	}

	public void setRot(ModelElement elem, Vec3f v) {
		Data dt = components.get(elem);
		ActionBuilder ab = anim.editor.action("setAnim", "label.cpm.rotation");
		if(dt == null) {
			dt = new Data(elem);
			ab.addToMap(components, elem, dt);
		}
		ab.updateValueOp(dt, dt.rot, v, (a, b) -> a.rot = b);
		ab.execute();
	}

	public void setColor(ModelElement elem, int rgb) {
		Data dt = components.get(elem);
		ActionBuilder ab = anim.editor.action("setAnim", "label.cpm.rotation");
		if(dt == null) {
			dt = new Data(elem);
			ab.addToMap(components, elem, dt);
		}
		int r = ((rgb & 0xff0000) >> 16);
		int g = (rgb & 0x00ff00) >> 8;
		int b =  rgb & 0x0000ff;
		ab.updateValueOp(dt, dt.color, new Vec3f(r, g, b), (a, v) -> a.color = v);
		ab.execute();
	}

	public void switchVis(ModelElement elem) {
		Data dt = components.get(elem);
		ActionBuilder ab = anim.editor.action("setAnim", "label.cpm.hidden_effect");
		if(dt == null) {
			dt = new Data(elem);
			ab.addToMap(components, elem, dt);
		}
		ab.updateValueOp(dt, dt.show, !dt.show, (a, b) -> a.show = b);
		ab.execute();
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
			Editor.walkElements(anim.editor.elements, elem -> {
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

	public void copy(AnimFrame from) {
		from.components.forEach((e, dt) -> components.put(e, new Data(dt)));
	}

	public void clearSelectedData(ModelElement me) {
		components.remove(me);
	}

	public static float[] toArray(EditorAnim anim, ModelElement elem, InterpolatorChannel channel) {
		float[] data = new float[anim.getFrames().size()];
		for (int i = 0; i < anim.getFrames().size(); i++) {
			AnimFrame frm = anim.getFrames().get(i);
			IElem dt = frm.getData(elem);
			if(dt == null) {
				if(anim.add)data[i] = 0;
				else data[i] = elem.part(channel);
			} else data[i] = dt.part(channel);
		}
		return data;
	}
}
