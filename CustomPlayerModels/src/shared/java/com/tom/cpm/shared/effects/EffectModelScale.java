package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpl.math.Rotation;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.ScaleData;

public class EffectModelScale implements IRenderEffect {
	private Vec3f pos, rotation, scale;

	public EffectModelScale() {
	}

	public EffectModelScale(Vec3f pos, Vec3f rotation, Vec3f scale) {
		this.pos = pos;
		this.rotation = rotation;
		this.scale = scale;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		pos = in.readVec6b();
		rotation = in.readAngle();
		scale = in.readVec6b();
	}

	@Override
	public void write(IOHelper out) throws IOException {
		out.writeVec6b(pos);
		out.writeAngle(rotation);
		out.writeVec6b(scale);
	}

	@Override
	public void apply(ModelDefinition def) {
		if(def.getScale() != null) {
			ScaleData d = def.getScale();
			d.setRenderScale(pos, new Rotation(rotation, false), scale);
		}
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.MODEL_SCALE;
	}
}
