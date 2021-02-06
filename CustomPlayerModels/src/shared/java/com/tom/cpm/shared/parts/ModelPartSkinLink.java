package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;

public class ModelPartSkinLink extends ModelPartLink {

	public ModelPartSkinLink(IOHelper in, ModelDefinitionLoader loader) throws IOException {
		super(in, loader);
	}

	public ModelPartSkinLink(Link link) {
		super(link);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.SKIN_LINK;
	}

	@Override
	protected IModelPart load(IOHelper din, ModelDefinitionLoader loader) throws IOException {
		return new ModelPartSkin(din, loader);
	}

}
