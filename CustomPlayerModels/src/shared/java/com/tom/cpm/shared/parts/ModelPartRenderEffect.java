package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.effects.IRenderEffect;
import com.tom.cpm.shared.effects.RenderEffects;
import com.tom.cpm.shared.io.IOHelper;

public class ModelPartRenderEffect implements IModelPart, IResolvedModelPart {
	private IRenderEffect effect;

	public ModelPartRenderEffect(IOHelper in, ModelDefinition def) throws IOException {
		RenderEffects ef = in.readEnum(RenderEffects.VALUES);
		if(ef != null) {
			effect = ef.create();
			effect.load(in);
		}
	}

	public ModelPartRenderEffect(IRenderEffect effect) {
		this.effect = effect;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.writeEnum(effect.getEffect());
		effect.write(dout);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.RENDER_EFFECT;
	}

	@Override
	public void apply(ModelDefinition def) {
		if(effect != null) {
			effect.apply(def);
		}
	}

	@Override
	public String toString() {
		return "RenderEffect: " + effect;
	}
}
