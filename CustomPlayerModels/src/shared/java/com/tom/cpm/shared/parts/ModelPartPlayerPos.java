package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.math.Vec3f;
import com.tom.cpm.shared.model.PlayerModelElement;
import com.tom.cpm.shared.model.PlayerModelParts;

public class ModelPartPlayerPos implements IModelPart, IResolvedModelPart {
	public PlayerModelParts part;
	public Vec3f pos, rot;

	public ModelPartPlayerPos(IOHelper in, ModelDefinitionLoader loader) throws IOException {
		part = PlayerModelParts.VALUES[in.readByte()];
		pos = in.readVec6b();
		rot = in.readAngle();
	}

	public ModelPartPlayerPos(PlayerModelParts part, Vec3f pos, Vec3f rot) {
		this.part = part;
		this.pos = pos;
		this.rot = rot;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.writeByte(part.ordinal());
		dout.writeVec6b(pos);
		dout.writeAngle(rot);
	}

	@Override
	public void apply(ModelDefinition def) {
		PlayerModelElement elem = def.getModelElementFor(part);
		elem.posN = pos;
		elem.rotN = rot;
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.PLAYER_PARTPOS;
	}

}
