package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;

public class EffectRemoveArmorOffset implements IRenderEffect {
	private boolean remove;

	public EffectRemoveArmorOffset() {
	}

	public EffectRemoveArmorOffset(boolean hide) {
		this.remove = hide;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		remove = in.readBoolean();
	}

	@Override
	public void write(IOHelper out) throws IOException {
		out.writeBoolean(remove);
	}

	@Override
	public void apply(ModelDefinition def) {
		def.removeArmorOffset = remove;
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.REMOVE_ARMOR_OFFSET;
	}

}
