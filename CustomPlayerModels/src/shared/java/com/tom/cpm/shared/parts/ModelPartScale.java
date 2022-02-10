package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpl.math.MathHelper;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.ScaleData;

@Deprecated
public class ModelPartScale implements IModelPart, IResolvedModelPart {
	private float scale;

	public ModelPartScale(IOHelper in, ModelDefinition def) throws IOException {
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
		def.setScale(new ScaleData(scale));
	}
}
