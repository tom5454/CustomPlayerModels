package com.tom.cpm.shared.editor.anim;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.animation.InterpolatorChannel;
import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.tree.TreeElement.VecType;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public class AnimFrame {
	private final Map<ModelElement, FrameData> components = new HashMap<>();
	private EditorAnim anim;
	public AnimFrame(EditorAnim anim) {
		this.anim = anim;
	}

	public AnimFrame(AnimFrame cpy) {
		this.anim = cpy.anim;
		for (Entry<ModelElement, FrameData> e : cpy.components.entrySet()) {
			components.put(e.getKey(), new FrameData(e.getValue()));
		}
	}

	public class FrameData implements IElem {
		private Vec3f pos, rot, color, scale;
		private boolean show = true;
		private ModelElement comp;

		public FrameData(ModelElement comp) {
			this.comp = comp;
			if(!anim.add) {
				if(comp.type == ElementType.ROOT_PART) {
					PartValues val = ((VanillaModelPart) comp.typeData).getDefaultSize(anim.editor.skinType);
					pos = val.getPos();
					rot = new Vec3f();
					scale = new Vec3f(1, 1, 1);
				} else {
					pos = new Vec3f(comp.pos);
					rot = new Vec3f(comp.rotation);
					scale = new Vec3f(1, 1, 1);
				}
			} else {
				pos = new Vec3f();
				rot = new Vec3f();
				scale = new Vec3f(1, 1, 1);
			}
			int r = (comp.rgb & 0xff0000) >> 16;
			int g = (comp.rgb & 0x00ff00) >> 8;
			int b =  comp.rgb & 0x0000ff;
			color = new Vec3f(r, g, b);
			show = !comp.hidden;
		}

		public FrameData(FrameData cpy) {
			this.comp = cpy.comp;
			pos = new Vec3f(cpy.pos);
			rot = new Vec3f(cpy.rot);
			color = new Vec3f(cpy.color);
			scale = new Vec3f(cpy.scale);
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

		@Override
		public Vec3f getScale() {
			return scale;
		}

		private void apply() {
			comp.rc.setRotation(anim.add,
					(float) Math.toRadians(rot.x),
					(float) Math.toRadians(rot.y),
					(float) Math.toRadians(rot.z)
					);
			comp.rc.setPosition(anim.add, pos.x, pos.y, pos.z);
			comp.rc.setColor(color.x, color.y, color.z);
			comp.rc.setRenderScale(anim.add, scale.x, scale.y, scale.z);
			comp.rc.display = show;
		}

		public boolean hasChanges() {
			return hasPosChanges() || hasRotChanges() || hasColorChanges() || hasVisChanges() || hasScaleChanges();
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

		public boolean hasScaleChanges() {
			return Math.abs(scale.x - 1) > 0.01f || Math.abs(scale.y - 1) > 0.01f || Math.abs(scale.z - 1) > 0.01f;
		}

		public boolean hasColorChanges() {
			if(!comp.texture || comp.recolor) {
				int rgb = (((int) color.x) << 16) | (((int) color.y) << 8) | ((int) color.z);
				if((comp.rgb & 0xffffff) != (rgb & 0xffffff))return true;
			}
			return false;
		}

		public boolean hasVisChanges() {
			return comp.hidden == show;
		}

		public void setPos(Vec3f pos) {
			this.pos = pos;
		}

		public void setRot(Vec3f rot) {
			this.rot = rot;
		}

		public void setColor(Vec3f color) {
			this.color = color;
		}

		public void setShow(boolean show) {
			this.show = show;
		}

		public void setScale(Vec3f v) {
			this.scale = v;
		}
	}

	public void setPos(ModelElement elem, Vec3f v) {
		FrameData dt = components.get(elem);
		ActionBuilder ab = anim.editor.action("setAnim", "label.cpm.position");
		if(dt == null) {
			dt = new FrameData(elem);
			ab.addToMap(components, elem, dt);
		}
		ab.updateValueOp(dt, dt.pos, v, (a, b) -> a.pos = b);
		ab.execute();
	}

	public void setRot(ModelElement elem, Vec3f v) {
		FrameData dt = components.get(elem);
		ActionBuilder ab = anim.editor.action("setAnim", "label.cpm.rotation");
		if(dt == null) {
			dt = new FrameData(elem);
			ab.addToMap(components, elem, dt);
		}
		ab.updateValueOp(dt, dt.rot, v, (a, b) -> a.rot = b);
		ab.execute();
	}

	public void setScale(ModelElement elem, Vec3f v) {
		FrameData dt = components.get(elem);
		ActionBuilder ab = anim.editor.action("setAnim", "label.cpm.render_scale");
		if(dt == null) {
			dt = new FrameData(elem);
			ab.addToMap(components, elem, dt);
		}
		ab.updateValueOp(dt, dt.scale, v, (a, b) -> a.scale = b);
		ab.execute();
	}

	public void setColor(ModelElement elem, int rgb) {
		FrameData dt = components.get(elem);
		ActionBuilder ab = anim.editor.action("setAnim", "label.cpm.rotation");
		if(dt == null) {
			dt = new FrameData(elem);
			ab.addToMap(components, elem, dt);
		}
		int r = ((rgb & 0xff0000) >> 16);
		int g = (rgb & 0x00ff00) >> 8;
		int b =  rgb & 0x0000ff;
		ab.updateValueOp(dt, dt.color, new Vec3f(r, g, b), (a, v) -> a.color = v);
		ab.execute();
	}

	public void switchVis(ModelElement elem) {
		FrameData dt = components.get(elem);
		ActionBuilder ab = anim.editor.action("setAnim", "label.cpm.hidden_effect");
		if(dt == null) {
			dt = new FrameData(elem);
			ab.addToMap(components, elem, dt);
		}
		ab.updateValueOp(dt, dt.show, !dt.show, (a, b) -> a.show = b);
		ab.execute();
	}

	public IElem getData(ModelElement modelElement) {
		return components.get(modelElement);
	}

	public void apply() {
		components.values().forEach(FrameData::apply);
		if(applyDrag)draggingElem.apply();
	}

	public boolean getVisible(ModelElement component) {
		if(!components.containsKey(component))return !component.hidden && component.rc.doDisplay();
		return components.get(component).show;
	}

	public Stream<ModelElement> getAllElements() {
		return components.keySet().stream();
	}

	public Stream<ModelElement> getAllElementsFiltered() {
		return components.entrySet().stream().filter(e -> e.getValue().hasChanges()).map(Entry::getKey);
	}

	public boolean hasPosChanges(ModelElement me) {
		FrameData data = components.get(me);
		if(data == null)return false;
		return data.hasPosChanges();
	}

	public boolean hasRotChanges(ModelElement me) {
		FrameData data = components.get(me);
		if(data == null)return false;
		return data.hasRotChanges();
	}

	public boolean hasColorChanges(ModelElement me) {
		FrameData data = components.get(me);
		if(data == null)return false;
		return data.hasColorChanges();
	}

	public boolean hasVisChanges(ModelElement me) {
		FrameData data = components.get(me);
		if(data == null)return false;
		return data.hasVisChanges();
	}

	public boolean hasScaleChanges(ModelElement me) {
		FrameData data = components.get(me);
		if(data == null)return false;
		return data.hasScaleChanges();
	}

	public void copy(AnimFrame from) {
		from.components.forEach((e, dt) -> components.put(e, new FrameData(dt)));
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
				if(anim.add)data[i] = channel.defaultValue;
				else data[i] = elem.part(channel);
			} else data[i] = dt.part(channel);
		}
		return data;
	}

	public Map<ModelElement, FrameData> getComponents() {
		return components;
	}

	public EditorAnim getAnim() {
		return anim;
	}

	public FrameData makeData(ModelElement elem) {
		FrameData d = new FrameData(elem);
		components.put(elem, d);
		return d;
	}

	private FrameData draggingElem;
	private boolean applyDrag;
	public void beginDrag(ModelElement elem) {
		draggingElem = components.get(elem);
		if(draggingElem == null) {
			draggingElem = new FrameData(elem);
			applyDrag = true;
		}
	}

	public void endDrag() {
		applyDrag = false;
		draggingElem = null;
	}

	public void dragVal(VecType type, Vec3f vec) {
		switch (type) {
		case POSITION:
			draggingElem.setPos(vec);
			break;

		case ROTATION:
			draggingElem.setRot(vec);
			break;

		default:
			break;
		}
	}
}
