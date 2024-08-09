package com.tom.cpm.shared.editor.elements;

import com.tom.cpl.math.Rotation;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.CombinedListView;
import com.tom.cpl.util.FlatListView;
import com.tom.cpl.util.ListView;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.template.TemplateArgHandler.ArgElem;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.render.PlayerModelSetup;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public enum ElementType {
	NORMAL(new ElementBuilder() {

		@Override
		public void buildElement(Editor editor, ModelElement elem, Object typeData) {
			elem.offset = new Vec3f();
			elem.pos = new Vec3f();
			elem.size = new Vec3f(1, 1, 1);
			elem.rotation = new Vec3f();
			elem.scale = new Vec3f(1, 1, 1);
			elem.meshScale = new Vec3f(1, 1, 1);
			elem.textureSize = 1;
			elem.name = editor.ui.i18nFormat("label.cpm.newCube");
			elem.rc = new RenderedCube(elem) {
				@Override
				public boolean doDisplay() {
					return elem.showInEditor;
				}

				@Override
				public ElementSelectMode getSelected() {
					if(editor.selectedElement != null && editor.selectedElement.isSelected(editor, elem))return ElementSelectMode.SELECTED;
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
		}

		@Override
		public void preRenderUpdate(ModelElement elem) {
			elem.rc.recolor = elem.recolor;
			elem.texSize = elem.texture ? (elem.mirror ? -elem.textureSize : elem.textureSize) : 0;
			elem.rc.reset();
			elem.rc.display = elem.showInEditor && (!elem.editor.applyAnim || !elem.hidden) && !elem.editor.definition.outlineOnly;
			elem.rc.rotation = new Rotation(elem.rotation, true);
			elem.rc.glow = elem.glow;
			elem.rc.singleTex = elem.singleTex;
			elem.rc.faceUVs = elem.faceUV;
			elem.rc.extrude = elem.extrude;
			elem.rc.itemRenderer = elem.itemRenderer;
		}

	}),
	ROOT_PART(new ElementBuilder(){

		@Override
		public void buildElement(Editor editor, ModelElement elem, Object typeData) {
			VanillaModelPart type = (VanillaModelPart) typeData;
			elem.name = "";
			elem.pos = new Vec3f();
			elem.rotation = new Vec3f();
			elem.rc = new RootModelElement(type, editor.definition) {
				@Override
				public boolean doDisplay() {
					return !elem.hidden;
				}

				@Override
				public ElementSelectMode getSelected() {
					if(editor.selectedElement != null && editor.selectedElement.isSelected(editor, elem))return ElementSelectMode.SELECTED;
					if(elem.parent != null) {
						ElementSelectMode ps = elem.parent.rc.getSelected();
						if(ps == ElementSelectMode.SEL_CHILDREN || ps == ElementSelectMode.SELECTED)
							return ElementSelectMode.SEL_CHILDREN;
					}
					return ElementSelectMode.NULL;
				}

				@Override
				public boolean renderPart() {
					return elem.showInEditor;
				}
			};
			elem.rc.setCube(elem);
			elem.rc.pos = new Vec3f();
			elem.rc.rotation = new Rotation();
			elem.rc.children = new CombinedListView<>(new ListView<>(elem.children, m -> m.rc), new FlatListView<>(editor.templates, t -> t.getForPart(type).stream()));
			elem.storeID = type.getId(elem.rc);
			PlayerModelSetup.initDefaultPose((RootModelElement) elem.rc, type);
		}

		@Override
		public void preRenderUpdate(ModelElement elem) {
			RootModelElement e = (RootModelElement) elem.rc;
			e.posN = new Vec3f(elem.pos);
			e.rotN = new Rotation(elem.rotation, true);
			e.reset();
			e.disableVanilla = elem.disableVanillaAnim;
		}
	}),
	;
	private final ElementBuilder elementBuilder;
	private ElementType(ElementBuilder elementBuilder) {
		this.elementBuilder = elementBuilder;
	}

	public void buildElement(Editor e, ModelElement elem, Object typeData) {
		elementBuilder.buildElement(e, elem, typeData);
	}

	public void preRenderUpdate(ModelElement elem) {
		elementBuilder.preRenderUpdate(elem);
	}

	private static interface ElementBuilder {
		void buildElement(Editor e, ModelElement elem, Object typeData);
		void preRenderUpdate(ModelElement elem);
	}
}
