package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;

@Deprecated
public class EffectDisableVanilla implements IRenderEffect {
	private int id;

	public EffectDisableVanilla() {
	}

	public EffectDisableVanilla(int id) {
		this.id = id;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		id = in.readVarInt();
	}

	@Override
	public void write(IOHelper out) throws IOException {
		out.writeVarInt(id);
	}

	@Override
	public void apply(ModelDefinition def) {
		RenderedCube cube = def.getElementById(id);
		if(cube != null && cube instanceof RootModelElement) {
			((RootModelElement) cube).disableVanilla = true;
		}
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.DISABLE_VANILLA;
	}

	@Override
	public String toString() {
		return "DisableVanilla " + id;
	}
}
