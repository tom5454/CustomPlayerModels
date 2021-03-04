package com.tom.cpm.shared.template.args;

import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;

public class Float2Arg extends ArgBase {
	private float value;

	@Override
	public String getType() {
		return "float2";
	}

	@Override
	public void write(IOHelper h) throws IOException {
		h.writeFloat2(value);
	}

	@Override
	public void load(IOHelper h) throws IOException {
		value = h.readFloat2();
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
