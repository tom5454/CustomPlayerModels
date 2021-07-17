package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.skin.TextureProvider;

public class ModelPartSkin implements IModelPart, IResolvedModelPart {
	private TextureProvider image;

	public ModelPartSkin(IOHelper in, ModelDefinitionLoader loader) throws IOException {
		image = new TextureProvider(in, 512);
	}

	public ModelPartSkin(Editor editor) {
		this.image = editor.textures.get(TextureSheetType.SKIN).provider;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public TextureProvider getSkin() {
		return image;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		image.write(dout);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.SKIN;
	}
}
