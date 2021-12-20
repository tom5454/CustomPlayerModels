package com.tom.cpm.shared.editor.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.tom.cpl.function.FloatConsumer;
import com.tom.cpl.function.FloatSupplier;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.PosPanel.ModeDisplayType;

public class ScalingElement implements TreeElement {
	private List<TreeElement> options;
	private Editor editor;
	private Tooltip tt;
	public float entityScaling;

	public ScalingElement(Editor editor) {
		this.editor = editor;
	}

	@Override
	public String getName() {
		return editor.gui().i18nFormat("label.cpm.display.scaling");
	}

	@Override
	public void getTreeElements(Consumer<TreeElement> c) {
		if(options == null) {
			options = new ArrayList<>();
			options.add(new EntityElem());
		}
		options.forEach(c);
	}

	@Override
	public Tooltip getTooltip() {
		if(tt == null)tt = new Tooltip(editor.frame, editor.gui().i18nFormat("tooltip.cpm.display.scaling"));
		return tt;
	}

	private class OptionElem implements TreeElement {
		protected String name;
		private Tooltip tooltip;

		public OptionElem(String name) {
			this.name = name;
			tooltip = new Tooltip(editor.frame, editor.gui().i18nFormat("tooltip.cpm.tree.scaling." + name));
		}

		@Override
		public String getName() {
			return editor.gui().i18nFormat("label.cpm.tree.scaling." + name);
		}

		@Override
		public Tooltip getTooltip() {
			return tooltip;
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

	public void reset() {
		entityScaling = 0;
	}
}
