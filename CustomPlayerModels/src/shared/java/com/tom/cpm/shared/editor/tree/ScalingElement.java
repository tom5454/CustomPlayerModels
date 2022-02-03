package com.tom.cpm.shared.editor.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.tom.cpl.function.FloatConsumer;
import com.tom.cpl.function.FloatSupplier;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.PosPanel.ModeDisplayType;

public class ScalingElement implements TreeElement {
	private List<TreeElement> options;
	private Editor editor;
	public float entityScaling;
	public float eyeHeight;
	public float hitboxW, hitboxH;
	public Vec3f pos, rotation, scale;

	public ScalingElement(Editor editor) {
		this.editor = editor;

		pos = new Vec3f();
		rotation = new Vec3f();
		scale = new Vec3f();

		options = new ArrayList<>();
		options.add(new EntityElem());
		options.add(new ValElem("eye_height", () -> eyeHeight, v -> eyeHeight = v));
		options.add(new ValElem("hitbox_width", () -> hitboxW, v -> hitboxW = v));
		options.add(new ValElem("hitbox_height", () -> hitboxH, v -> hitboxH = v));
		options.add(new ModelScale());
	}

	@Override
	public String getName() {
		return editor.gui().i18nFormat("label.cpm.effect.scaling");
	}

	@Override
	public void getTreeElements(Consumer<TreeElement> c) {
		options.forEach(c);
	}

	@Override
	public Tooltip getTooltip() {
		return new Tooltip(editor.frame, editor.gui().i18nFormat("tooltip.cpm.effect.scaling"));
	}

	private class OptionElem implements TreeElement {
		protected String name;

		public OptionElem(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return editor.gui().i18nFormat("label.cpm.tree.scaling." + name);
		}

		@Override
		public Tooltip getTooltip() {
			return new Tooltip(editor.frame, editor.gui().i18nFormat("tooltip.cpm.tree.scaling." + name));
		}
	}

	private class EntityElem extends OptionElem {

		public EntityElem() {
			super("entity");
		}

		@Override
		public float getValue() {
			return entityScaling;
		}

		@Override
		public void setValue(float value) {
			entityScaling = value;
			if(entityScaling > 10) {
				entityScaling = 10;
				editor.setValue.accept(entityScaling);
			}
			if(entityScaling < 0.05f) {
				entityScaling = 0.05f;
				editor.setValue.accept(entityScaling);
			}
		}

		@Override
		public void updateGui() {
			editor.applyScaling = true;
			editor.setValue.accept(entityScaling);
			editor.setModePanel.accept(ModeDisplayType.VALUE);
		}
	}

	private class ValElem extends OptionElem {
		private FloatSupplier get;
		private FloatConsumer set;

		public ValElem(String name, FloatSupplier get, FloatConsumer set) {
			super(name);
			this.get = get;
			this.set = set;
		}

		@Override
		public void updateGui() {
			editor.setModePanel.accept(ModeDisplayType.VALUE);
			editor.setValue.accept(get.getAsFloat());
		}

		@Override
		public void setValue(float value) {
			set.accept(value);
			editor.action("set", "label.cpm.tree.scaling." + name).
			updateValueOp(set, get.getAsFloat(), value, FloatConsumer::accept).execute();
		}
	}

	private class ModelScale extends OptionElem {

		public ModelScale() {
			super("model");
		}

		@Override
		public void updateGui() {
			editor.setRot.accept(rotation);
			editor.setPosition.accept(pos);
			editor.setScale.accept(scale);
		}

		@Override
		public void setVec(Vec3f v, VecType object) {
			switch (object) {
			case ROTATION:
				editor.action("set", "label.cpm.rotation").
				updateValueOp(ScalingElement.this, rotation, v, 0, 360, true, (a, b) -> a.rotation = b, editor.setRot).
				execute();
				break;

			case POSITION:
				editor.action("set", "label.cpm.position").
				updateValueOp(ScalingElement.this, pos, v, -Vec3f.MAX_POS, Vec3f.MAX_POS, false, (a, b) -> a.pos = b, editor.setPosition).
				execute();
				break;

			case SCALE:
				editor.action("set", "label.cpm.scale").
				updateValueOp(ScalingElement.this, scale, v, 0, 25, false, (a, b) -> a.scale = b, editor.setScale).
				execute();
				break;

			default:
				break;
			}
		}
	}

	public void reset() {
		entityScaling = 0;
	}

	public boolean hasTransform() {
		return pos.x != 0 || pos.y != 0 || pos.z != 0 || rotation.x != 0 || rotation.y != 0 || rotation.z != 0 ||
				scale.x != 0 || scale.y != 0 || scale.z != 0 || scale.x != 1 || scale.y != 1 || scale.z != 1;
	}
}
