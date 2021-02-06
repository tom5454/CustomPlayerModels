package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;

public class ModelPartSkinType implements IModelPart, IResolvedModelPart {
	private int type;
	public ModelPartSkinType(IOHelper din, ModelDefinitionLoader loader) throws IOException {
		this.type = din.read();
	}

	public ModelPartSkinType(int type) {
		this.type = type;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.write(type);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.SKIN_TYPE;
	}

	public int getSkinType() {
		return type;
	}
}
