package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpl.math.MathHelper;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.ScaleData;

@Deprecated
public class EffectPlayerScale implements IRenderEffect {
	private float eyeHeight;
	private float hitboxW;
	private float hitboxH;

	public EffectPlayerScale() {
	}

	public EffectPlayerScale(float eyeHeight, float hitboxW, float hitboxH) {
		this.eyeHeight = eyeHeight;
		this.hitboxW = hitboxW;
		this.hitboxH = hitboxH;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		eyeHeight = in.readShort() / 100f;
		hitboxW = in.readShort() / 100f;
		hitboxH = in.readShort() / 100f;
	}

	@Override
	public void write(IOHelper out) throws IOException {
		out.writeShort(MathHelper.clamp((int) (eyeHeight * 100), 0, Short.MAX_VALUE));
		out.writeShort(MathHelper.clamp((int) (hitboxW * 100), 0, Short.MAX_VALUE));
		out.writeShort(MathHelper.clamp((int) (hitboxH * 100), 0, Short.MAX_VALUE));
	}

	@Override
	public void apply(ModelDefinition def) {
		if(def.getScale() != null) {
			ScaleData d = def.getScale();
			d.setScale(eyeHeight, hitboxW, hitboxH);
		}
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.PLAYER_SCALE;
	}
}
