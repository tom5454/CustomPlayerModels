package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.IOHelper.ObjectBlock;

public interface IModelPart extends ObjectBlock<ModelPartType> {
	IResolvedModelPart resolve() throws IOException;
	@Override
	void write(IOHelper dout) throws IOException;
	@Override
	ModelPartType getType();

	@FunctionalInterface
	public static interface Factory {
		IModelPart create(IOHelper in, ModelDefinition def) throws IOException;
	}
}
