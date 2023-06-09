package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;

public class EffectInvisGlow implements IRenderEffect {

	public EffectInvisGlow() {
	}

	@Override
	public void load(IOHelper in) throws IOException {
	}

	@Override
	public void write(IOHelper out) throws IOException {
	}

	@Override
	public void apply(ModelDefinition def) {
		def.enableInvisGlow = true;
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.INVIS_GLOW;
	}

}