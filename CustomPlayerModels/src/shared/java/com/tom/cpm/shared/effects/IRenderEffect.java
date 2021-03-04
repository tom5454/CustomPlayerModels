package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;

public interface IRenderEffect {
	void load(IOHelper in) throws IOException;
	void write(IOHelper out) throws IOException;

	void apply(ModelDefinition def);
	RenderEffects getEffect();
}
