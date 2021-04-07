package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;

public class ModelPartCloneable implements IModelPart, IResolvedModelPart {

	public ModelPartCloneable(IOHelper is, ModelDefinitionLoader loader) throws IOException {
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.CLONEABLE;
	}

	@Override
	public void apply(ModelDefinition def) {
		def.cloneable = true;
	}
}
