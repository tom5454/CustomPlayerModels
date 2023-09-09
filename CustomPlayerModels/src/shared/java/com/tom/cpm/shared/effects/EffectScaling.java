package com.tom.cpm.shared.effects;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.ScaleData;
import com.tom.cpm.shared.util.ScalingOptions;

public class EffectScaling implements IRenderEffect {
	private Map<ScalingOptions, Float> scaling;

	public EffectScaling() {
		scaling = new EnumMap<>(ScalingOptions.class);
	}

	public EffectScaling(Map<ScalingOptions, Float> scaling) {
		this.scaling = scaling;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		while(in.available() > 0) {
			ScalingOptions opt = in.readEnum(ScalingOptions.VALUES);
			float v = in.readFloat2();
			if(opt != null)
				scaling.put(opt, v);
		}
	}

	@Override
	public void write(IOHelper out) throws IOException {
		for (Entry<ScalingOptions, Float> e : scaling.entrySet()) {
			if(e.getValue() != 0 && e.getValue() != 1) {
				out.writeEnum(e.getKey());
				out.writeFloat2(e.getValue());
			}
		}
	}

	@Override
	public void apply(ModelDefinition def) {
		def.setScale(new ScaleData(scaling));
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.SCALING;
	}

	@Override
	public String toString() {
		return scaling.entrySet().stream().map(e -> e.getKey().name() + ": " + e.getValue()).collect(Collectors.joining("\n", "Scaling:\n", "\n"));
	}
}
