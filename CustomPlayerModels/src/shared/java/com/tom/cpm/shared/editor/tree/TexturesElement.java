package com.tom.cpm.shared.editor.tree;

import java.util.function.Consumer;

import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;

public class TexturesElement implements TreeElement {
	private Editor editor;

	public TexturesElement(Editor editor) {
		this.editor = editor;
	}

	@Override
	public String getName() {
		return editor.ui.i18nFormat("label.cpm.tree.texture");
	}

	@Override
	public void getTreeElements(Consumer<TreeElement> c) {
		editor.textures.values().stream().filter(ETextures::isEditable).forEach(c);
	}
}
