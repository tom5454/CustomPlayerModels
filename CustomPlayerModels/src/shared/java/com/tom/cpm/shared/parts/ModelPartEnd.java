package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;

public class ModelPartEnd implements IModelPart {
	public static final ModelPartEnd END = new ModelPartEnd();

	public ModelPartEnd(IOHelper in, ModelDefinitionLoader loader) throws IOException {}

	private ModelPartEnd() {}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return null;
	}

	@Override
	public void write(IOHelper dout) throws IOException {}

	@Override
	public ModelPartType getType() {
		return ModelPartType.END;
	}
}
