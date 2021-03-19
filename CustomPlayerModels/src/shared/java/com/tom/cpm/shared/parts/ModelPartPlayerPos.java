package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;

public class ModelPartPlayerPos implements IModelPart, IResolvedModelPart {
	public int id;
	public Vec3f pos, rot;

	public ModelPartPlayerPos(IOHelper in, ModelDefinitionLoader loader) throws IOException {
		id = in.readVarInt();
		pos = in.readVec6b();
		rot = in.readAngle();
	}

	public ModelPartPlayerPos(int id, Vec3f pos, Vec3f rot) {
		this.id = id;
		this.pos = pos;
		this.rot = rot;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.writeVarInt(id);
		dout.writeVec6b(pos);
		dout.writeAngle(rot);
	}

	@Override
	public void apply(ModelDefinition def) {
		RenderedCube e = def.getElementById(id);
		if(e instanceof RootModelElement) {
			RootModelElement elem = (RootModelElement) e;
			elem.posN = pos;
			elem.rotN = rot;
		}
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.PLAYER_PARTPOS;
	}

	@Override
	public String toString() {
		return "Root Pos: " + id;
	}
}
