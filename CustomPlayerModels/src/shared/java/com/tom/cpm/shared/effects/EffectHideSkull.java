package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;

public class EffectHideSkull implements IRenderEffect {
	private boolean hide;

	public EffectHideSkull() {
	}

	public EffectHideSkull(boolean hide) {
		this.hide = hide;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		hide = in.readBoolean();
	}

	@Override
	public void write(IOHelper out) throws IOException {
		out.writeBoolean(hide);
	}

	@Override
	public void apply(ModelDefinition def) {
		def.hideHeadIfSkull = hide;
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.HIDE_SKULL;
	}

}
