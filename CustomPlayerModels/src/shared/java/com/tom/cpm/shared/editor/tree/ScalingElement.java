package com.tom.cpm.shared.editor.tree;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.ModeDisplayType;
import com.tom.cpm.shared.util.ScalingOptions;

public class ScalingElement implements TreeElement {
	private static boolean editPopupShown = false;
	private List<TreeElement> options;
	private Editor editor;
	public Map<ScalingOptions, Float> scaling = new EnumMap<>(ScalingOptions.class);
	public Vec3f pos, rotation, scale;
	public boolean enabled;

	public ScalingElement(Editor editor) {
		this.editor = editor;

		pos = new Vec3f();
		rotation = new Vec3f();
		scale = new Vec3f();

		options = new ArrayList<>();
		for(ScalingOptions opt : ScalingOptions.VALUES) {
			options.add(new ValElem(opt));
		}
		options.add(new ModelScale());
	}

	@Override
	public String getName() {
		return editor.ui.i18nFormat("label.cpm.effect.scaling");
	}

	@Override
	public void getTreeElements(Consumer<TreeElement> c) {
		options.forEach(c);
	}

	@Override
	public Tooltip getTooltip(IGui gui) {
		return new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.effect.scaling"));
	}

	private class OptionElem implements TreeElement {
		protected String name;

		public OptionElem(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return editor.ui.i18nFormat("label.cpm.tree.scaling." + name);
		}

		@Override
		public Tooltip getTooltip(IGui gui) {
			return new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.tree.scaling." + name));
		}
	}

	private class ValElem extends OptionElem {
		private ScalingOptions opt;

		public ValElem(ScalingOptions opt) {
			super(opt.name().toLowerCase(Locale.ROOT));
			this.opt = opt;
		}

		@Override
		public void updateGui() {
			if(opt == ScalingOptions.ENTITY)editor.applyScaling = true;
			editor.setModePanel.accept(ModeDisplayType.VALUE);
			editor.setValue.accept(scaling.getOrDefault(opt, 1F));
		}

		@Override
		public void setValue(float value) {
			editor.action("set", "label.cpm.tree.scaling." + name).
			addToMap(scaling, opt, value).execute();
		}

		@Override
		public Tooltip getTooltip(IGui gui) {
			String tooltip = gui.i18nFormat("tooltip.cpm.tree.scaling." + name);
			if(opt != ScalingOptions.ENTITY)tooltip = gui.i18nFormat("tooltip.cpm.tree.scaling.serverRequired", tooltip);
			if(!opt.getDefualtEnabled())tooltip = gui.i18nFormat("tooltip.cpm.tree.scaling.disabledByDefault", tooltip, name);
			return new Tooltip(gui.getFrame(), tooltip);
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
			if(editor.elements.stream().anyMatch(e -> !e.hidden) && !editPopupShown) {
				editPopupShown = true;
				editor.ui.displayMessagePopup(editor.ui.i18nFormat("label.cpm.info"), editor.ui.i18nFormat("label.cpm.warnEditTransform"));
			}
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
		scaling.clear();
		enabled = false;
		pos = new Vec3f();
		rotation = new Vec3f();
		scale = new Vec3f();
	}

	public boolean hasTransform() {
		return pos.x != 0 || pos.y != 0 || pos.z != 0 || rotation.x != 0 || rotation.y != 0 || rotation.z != 0 ||
				scale.x != 0 || scale.y != 0 || scale.z != 0 || scale.x != 1 || scale.y != 1 || scale.z != 1;
	}

	public float getScale() {
		return getScale(ScalingOptions.ENTITY);
	}

	public float getScale(ScalingOptions opt) {
		float v = scaling.getOrDefault(opt, 1F);
		if(v == 0)return 1F;
		return v;
	}
}
