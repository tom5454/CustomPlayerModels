package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpl.math.MathHelper;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;

public class ModelPartScale implements IModelPart, IResolvedModelPart {
	private float scale;

	public ModelPartScale(IOHelper in, ModelDefinitionLoader loader) throws IOException {
		this(in);
	}

	public ModelPartScale(IOHelper in) throws IOException {
		scale = in.readByte() / 10f;
	}

	public ModelPartScale(float scale) {
		this.scale = scale;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.writeByte(MathHelper.clamp((int) (scale * 10), Byte.MIN_VALUE, Byte.MAX_VALUE));
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.SCALE;
	}

	@Override
	public void apply(ModelDefinition def) {
		def.setScale(this);
	}

	public float getScale() {
		if(scale > 10)return 0;
		else if(scale < 0.05f)return 0;
		return scale;
	}
}
