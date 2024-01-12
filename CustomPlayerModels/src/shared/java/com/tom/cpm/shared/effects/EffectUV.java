package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.RenderedCube;

@Deprecated
public class EffectUV implements IRenderEffect {
	private int id;
	private int u, v;

	public EffectUV() {
	}

	public EffectUV(int id, int u, int v) {
		this.id = id;
		this.u = u;
		this.v = v;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		id = in.readVarInt();
		u = in.readVarInt();
		v = in.readVarInt();
	}

	@Override
	public void write(IOHelper out) throws IOException {
		out.writeVarInt(id);
		out.writeVarInt(u);
		out.writeVarInt(v);
	}

	@Override
	public void apply(ModelDefinition def) {
		RenderedCube cube = def.getElementById(id);
		if(cube != null) {
			cube.getCube().u = u;
			cube.getCube().v = v;
		}
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.UV_OVERFLOW;
	}

	@Override
	public String toString() {
		return "UV [" + id + "] UV[" + u + ", " + v + "]";
	}
}
