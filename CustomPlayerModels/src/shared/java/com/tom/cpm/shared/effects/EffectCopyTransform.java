package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.CopyTransform;
import com.tom.cpm.shared.model.RenderedCube;

public class EffectCopyTransform implements IRenderEffect {
	private int from, to;
	private short copy;
	private float multiply;

	public EffectCopyTransform() {
	}

	public EffectCopyTransform(int from, int to, short copy, float multiply) {
		this.from = from;
		this.to = to;
		this.copy = copy;
		this.multiply = multiply;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		from = in.readVarInt();
		to = in.readVarInt();
		copy = (short) in.readVarInt();
		if ((copy & (1 << 14)) != 0) {
			multiply = in.readFloat();
		} else {
			multiply = 1f;
		}
	}

	@Override
	public void write(IOHelper out) throws IOException {
		out.writeVarInt(from);
		out.writeVarInt(to);
		out.writeVarInt(copy);
		if (Math.abs(multiply - 1f) > 0.01f)
			out.writeFloat(multiply);
	}

	@Override
	public void apply(ModelDefinition def) {
		RenderedCube from = def.getElementById(this.from);
		RenderedCube to = def.getElementById(this.to);
		if(from != null && to != null) {
			def.getAnimations().addCopy(new CopyTransform(from, to, copy, multiply));
		}
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.COPY_TRANSFORM;
	}

}
