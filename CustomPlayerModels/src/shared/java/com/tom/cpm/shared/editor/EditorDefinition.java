package com.tom.cpm.shared.editor;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.ModelRenderManager.ModelPart;
import com.tom.cpm.shared.model.PartRoot;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.skin.TextureProvider;

public class EditorDefinition extends ModelDefinition {
	private Editor editor;
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
		return editor.renderTexture;
	}
}
