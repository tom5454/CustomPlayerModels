package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.skin.TextureProvider;

public class ModelPartTexture implements IModelPart, IResolvedModelPart {
	private TextureProvider image;
	private TextureSheetType texType;

	public ModelPartTexture(IOHelper in, ModelDefinition def) throws IOException {
		texType = in.readEnum(TextureSheetType.VALUES);
		image = new TextureProvider(in, def);
	}

	public ModelPartTexture(Editor editor, TextureSheetType tex) {
		texType = tex;
		this.image = editor.textures.get(tex).provider;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void apply(ModelDefinition def) {
		def.setTexture(texType, image);
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.writeEnum(texType);
		image.write(dout);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.TEXTURE;
	}

	@Override
	public String toString() {
		return "Texture: " + texType;
	}
}
