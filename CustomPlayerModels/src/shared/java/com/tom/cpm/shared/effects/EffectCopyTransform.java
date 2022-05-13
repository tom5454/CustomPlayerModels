package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.CopyTransform;
import com.tom.cpm.shared.model.RenderedCube;

public class EffectCopyTransform implements IRenderEffect {
	private int from, to;
	private short copy;

	public EffectCopyTransform() {
	}

	public EffectCopyTransform(int from, int to, short copy) {
		this.from = from;
		this.to = to;
		this.copy = copy;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		from = in.readVarInt();
		to = in.readVarInt();
		copy = (short) in.readVarInt();
	}

	@Override
	public void write(IOHelper out) throws IOException {
		out.writeVarInt(from);
		out.writeVarInt(to);
		out.writeVarInt(copy);
	}

	@Override
	public void apply(ModelDefinition def) {
		RenderedCube from = def.getElementById(this.from);
		RenderedCube to = def.getElementById(this.to);
		if(from != null && to != null) {
			CopyTransform ct = new CopyTransform(from, to, copy);
			def.getAnimations().register(VanillaPose.GLOBAL, ct);
		}
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.COPY_TRANSFORM;
	}

}
