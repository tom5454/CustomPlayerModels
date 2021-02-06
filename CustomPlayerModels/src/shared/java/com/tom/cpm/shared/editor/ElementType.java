package com.tom.cpm.shared.editor;

import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.math.Vec3f;
import com.tom.cpm.shared.model.PlayerModelElement;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.util.ListView;

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
				public int getSelected() {
					if(editor.renderPaint && !elem.texture)return 3;
					if(editor.selectedElement == elem)return 2;
					if(elem.parent != null) {
						int ps = elem.parent.rc.getSelected();
						if(ps == 2 || ps == 1)return 1;
					}
					return 0;
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
	PLAYER_PART(new ElementBuilder(){

		@Override
		public void buildElement(IGui gui, Editor editor, ModelElement elem, Object typeData) {
			PlayerModelParts type = (PlayerModelParts) typeData;
			elem.name = gui.i18nFormat("label.cpm.elem." + type.name().toLowerCase());
			elem.pos = new Vec3f();
			elem.rotation = new Vec3f();
			elem.rc = new PlayerModelElement(type) {
				@Override
				public boolean doDisplay() {
					return elem.show;
				}

				@Override
				public int getSelected() {
					if(editor.selectedElement == elem)return 2;
					if(elem.parent != null) {
						int ps = elem.parent.rc.getSelected();
						if(ps == 2 || ps == 1)return 1;
					}
					return 0;
				}
			};
			elem.rc.pos = elem.pos;
			elem.rc.rotation = elem.rotation;
			elem.rc.children = new ListView<>(elem.children, m -> m.rc);
			elem.storeID = type.ordinal();
		}

		@Override
		public void preRenderUpdate(ModelElement elem) {
			elem.rc.pos = new Vec3f(elem.pos);
			elem.rc.rotation = new Vec3f((float) Math.toRadians(elem.rotation.x), (float) Math.toRadians(elem.rotation.y), (float) Math.toRadians(elem.rotation.z));
			((PlayerModelElement)elem.rc).forcePos = false;
		}
	}),
	//TEMPLATE

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
