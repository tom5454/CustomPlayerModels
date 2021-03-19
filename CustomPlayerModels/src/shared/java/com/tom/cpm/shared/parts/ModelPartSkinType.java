package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.SkinType;

public class ModelPartSkinType implements IModelPart, IResolvedModelPart {
	private SkinType type;
	public ModelPartSkinType(IOHelper din, ModelDefinitionLoader loader) throws IOException {
		int t = din.read();
		this.type = t >= SkinType.VALUES.length ? SkinType.UNKNOWN : SkinType.VALUES[t];
	}

	public ModelPartSkinType(SkinType type) {
		this.type = type;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.write(type.ordinal());
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.SKIN_TYPE;
	}

	public SkinType getSkinType() {
		return type;
	}

	@Override
	public String toString() {
		return "Skin Type: " + type.getName();
	}
}
