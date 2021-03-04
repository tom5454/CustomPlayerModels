package com.tom.cpm.shared.template.args;

import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.math.MathHelper;

public class Float1Arg extends ArgBase {
	private float value;

	@Override
	public String getType() {
		return "float1";
	}

	@Override
	public void write(IOHelper h) throws IOException {
		h.write(MathHelper.clamp((int) (value * 10), 0, 255));
	}

	@Override
	public void load(IOHelper h) throws IOException {
		value = h.read() / 10f;
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
