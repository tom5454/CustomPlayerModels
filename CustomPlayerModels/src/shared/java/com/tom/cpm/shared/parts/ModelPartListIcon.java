package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.skin.TextureProvider;

@Deprecated
public class ModelPartListIcon implements IModelPart, IResolvedModelPart {
	private TextureProvider image;

	public ModelPartListIcon(IOHelper in, ModelDefinition def) throws IOException {
		image = new TextureProvider(in, def);
	}

	public ModelPartListIcon(Editor editor) {
		this.image = editor.textures.get(TextureSheetType.LIST_ICON).provider;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void apply(ModelDefinition def) {
		def.setTexture(TextureSheetType.LIST_ICON, image);
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		image.write(dout);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.LIST_ICON;
	}
}
