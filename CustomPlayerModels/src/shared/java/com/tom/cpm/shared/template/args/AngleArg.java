package com.tom.cpm.shared.template.args;

import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.math.MathHelper;

public class AngleArg extends ArgBase {
	private float value;

	@Override
	public String getType() {
		return "angle";
	}

	@Override
	public void write(IOHelper h) throws IOException {
		h.writeShort(MathHelper.clamp((int) (value / 360f * 65535), 0, 65535));
	}

	@Override
	public void load(IOHelper h) throws IOException {
		value = (float) (h.readShort() / 65535f * 2 * Math.PI);
	}

	@Override
	public Object toJson() {
		return value;
	}

	@Override
	public void fromJson(Object v) {
		value = ((Number) v).floatValue();
	}
}
