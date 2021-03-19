package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.RenderedCube;

public class EffectScale implements IRenderEffect {
	private int id;
	private Vec3f scale;
	private float mcScale;

	public EffectScale() {
	}

	public EffectScale(int id, Vec3f scale, float mcScale) {
		this.id = id;
		this.scale = scale;
		this.mcScale = mcScale;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		id = in.readVarInt();
		mcScale = in.readFloat2();
		scale = in.readVec6b();
	}

	@Override
	public void write(IOHelper out) throws IOException {
		out.writeVarInt(id);
		out.writeFloat2(mcScale);
		out.writeVec6b(scale);
	}

	@Override
	public void apply(ModelDefinition def) {
		RenderedCube cube = def.getElementById(id);
		if(cube != null) {
			cube.getCube().scale = scale;
			cube.getCube().mcScale = mcScale;
		}
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.SCALE;
	}

	@Override
	public String toString() {
		return "Scale [" + id + "] " + scale + "+" + mcScale;
	}

}
