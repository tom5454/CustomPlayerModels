package com.tom.cpm.shared.effects;

import java.io.EOFException;
import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.RenderedCube;

public class EffectColor implements IRenderEffect {
	private int id, color;

	public EffectColor() {
	}

	public EffectColor(int id, int color) {
		this.id = id;
		this.color = color;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		id = in.readVarInt();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		color = ((ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	@Override
	public void write(IOHelper out) throws IOException {
		out.writeVarInt(id);
		out.write((color >>> 16) & 0xFF);
		out.write((color >>>  8) & 0xFF);
		out.write((color >>>  0) & 0xFF);
	}

	@Override
	public void apply(ModelDefinition def) {
		RenderedCube cube = def.getElementById(id);
		if(cube != null) {
			cube.getCube().rgb = color;
			cube.recolor = true;
		}
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.COLOR;
	}

	@Override
	public String toString() {
		return "Color [" + id + "] " + Integer.toHexString(color);
	}
}
