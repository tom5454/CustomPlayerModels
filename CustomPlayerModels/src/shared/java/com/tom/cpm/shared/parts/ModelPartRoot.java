package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.RootModelType;

public class ModelPartRoot implements IModelPart, IResolvedModelPart {
	private int id;
	private RootModelType type;
	public ModelPartRoot(IOHelper din, ModelDefinitionLoader loader) throws IOException {
		id = din.readVarInt();
		type = din.readEnum(RootModelType.VALUES);
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.writeVarInt(id);
		dout.writeEnum(type);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.MODEL_ROOT;
	}

	@Override
	public void apply(ModelDefinition def) {
		RootModelElement elem = def.getModelElementFor(PlayerModelParts.CUSTOM_PART);
		for(RenderedCube rc : elem.children) {
			if(rc.getId() == id) {
				elem.children.remove(rc);
				RootModelElement e = def.addRoot(id, type);
				e.children.addAll(rc.children);
				rc.children.forEach(p -> p.setParent(e));
				break;
			}
		}
	}
}
