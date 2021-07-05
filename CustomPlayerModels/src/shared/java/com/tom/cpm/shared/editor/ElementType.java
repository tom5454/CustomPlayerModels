package com.tom.cpm.shared.editor;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.CombinedListView;
import com.tom.cpl.util.FlatListView;
import com.tom.cpl.util.ListView;
import com.tom.cpm.shared.editor.template.TemplateArgHandler.ArgElem;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public enum ElementType {
	NORMAL(new ElementBuilder() {

		@Override
		public void buildElement(IGui gui, Editor editor, ModelElement elem, Object typeData) {
			elem.offset = new Vec3f();
			elem.pos = new Vec3f();
			elem.size = new Vec3f(1, 1, 1);
			elem.rotation = new Vec3f();
			elem.scale = new Vec3f(1, 1, 1);
			elem.textureSize = 1;
			elem.name = "New Cube";
			elem.rc = new RenderedCube(elem) {
				@Override
				public boolean doDisplay() {
					return elem.show;
				}

				@Override
				public ElementSelectMode getSelected() {
					if(editor.renderPaint) {
						return ElementSelectMode.PAINT_MODE;
					}
					if(editor.selectedElement == elem)return ElementSelectMode.SELECTED;
					if(editor.selectedElement instanceof ArgElem && ((ArgElem) editor.selectedElement).elem == elem)
						return ElementSelectMode.SEL_ONLY;
					if(elem.parent != null) {
						ElementSelectMode ps = elem.parent.rc.getSelected();
						if(ps == ElementSelectMode.SEL_CHILDREN || ps == ElementSelectMode.SELECTED)
							return ElementSelectMode.SEL_CHILDREN;
					}
					return ElementSelectMode.NULL;
				}
			};
			elem.rc.children = new ListView<>(elem.children, m -> m.rc);
			elem.rc.useDynamic = true;
		}

		@Override
		public void preRenderUpdate(ModelElement elem) {
			elem.rc.recolor = elem.recolor;
			elem.texSize = elem.texture ? (elem.mirror ? -elem.textureSize : elem.textureSize) : 0;
			elem.rc.reset();
			elem.rc.display = elem.show;
			elem.rc.rotation = new Vec3f((float) Math.toRadians(elem.rotation.x), (float) Math.toRadians(elem.rotation.y), (float) Math.toRadians(elem.rotation.z));
			elem.rc.glow = elem.glow;
		}

	}),
	ROOT_PART(new ElementBuilder(){

		@Override
		public void buildElement(IGui gui, Editor editor, ModelElement elem, Object typeData) {
			VanillaModelPart type = (VanillaModelPart) typeData;
			elem.name = gui.i18nFormat("label.cpm.elem." + type.getName());
			elem.pos = new Vec3f();
			elem.rotation = new Vec3f();
			elem.rc = new RootModelElement(type, editor.definition) {
				@Override
				public boolean doDisplay() {
					return elem.show;
				}

				@Override
				public ElementSelectMode getSelected() {
					if(editor.selectedElement == elem)return ElementSelectMode.SELECTED;
					if(elem.parent != null) {
						ElementSelectMode ps = elem.parent.rc.getSelected();
						if(ps == ElementSelectMode.SEL_CHILDREN || ps == ElementSelectMode.SELECTED)
							return ElementSelectMode.SEL_CHILDREN;
					}
					return ElementSelectMode.NULL;
				}
			};
			elem.rc.pos = new Vec3f();
			elem.rc.rotation = new Vec3f();
			elem.rc.children = new CombinedListView<>(new ListView<>(elem.children, m -> m.rc), new FlatListView<>(editor.templates, t -> t.getForPart(type).stream()));
			elem.storeID = type.getId(elem.rc);
		}

		@Override
		public void preRenderUpdate(ModelElement elem) {
			((RootModelElement)elem.rc).posN = new Vec3f(elem.pos);
			((RootModelElement)elem.rc).rotN = new Vec3f((float) Math.toRadians(elem.rotation.x), (float) Math.toRadians(elem.rotation.y), (float) Math.toRadians(elem.rotation.z));
		}
	}),
	;
	private final ElementBuilder elementBuilder;
	private ElementType(ElementBuilder elementBuilder) {
		this.elementBuilder = elementBuilder;
	}

	public void buildElement(IGui gui, Editor e, ModelElement elem, Object typeData) {
		elementBuilder.buildElement(gui, e, elem, typeData);
	}

	public void preRenderUpdate(ModelElement elem) {
		elementBuilder.preRenderUpdate(elem);
	}

	private static interface ElementBuilder {
		void buildElement(IGui gui, Editor e, ModelElement elem, Object typeData);
		void preRenderUpdate(ModelElement elem);
	}
}
