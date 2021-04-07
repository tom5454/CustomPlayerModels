package com.tom.cpm.shared.editor;

import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.ModelRenderManager.ModelPart;
import com.tom.cpm.shared.model.PartRoot;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.PaintImageCreator;

public class EditorDefinition extends ModelDefinition {
	private Editor editor;
	private TextureProvider paint = new TextureProvider(PaintImageCreator.createImage(), null) {

		@Override
		public Vec2i getSize() {
			return editor.renderTexture.size;
		}
	};

	public EditorDefinition(Editor editor) {
		this.editor = editor;
	}

	@Override
	public PartRoot getModelElementFor(ModelPart part) {
		PartRoot root = new PartRoot();
		editor.elements.forEach(e -> {
			RootModelElement el = (RootModelElement) e.rc;
			if(el.getPart() == part) {
				root.add(el);
				if(!e.duplicated)root.setMainRoot(el);
			}
		});
		return root;
	}

	@Override
	public boolean isEditor() {
		return true;
	}

	@Override
	public TextureProvider getSkinOverride() {
		return editor.renderPaint ? paint : editor.renderTexture;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		paint.free();
	}

	@Override
	public SkinType getSkinType() {
		return editor.skinType;
	}
}
