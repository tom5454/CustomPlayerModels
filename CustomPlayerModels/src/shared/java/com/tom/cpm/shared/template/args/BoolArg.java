package com.tom.cpm.shared.template.args;

import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;

public class BoolArg extends ArgBase {
	private boolean value;

	@Override
	public String getType() {
		return "bool";
	}

	@Override
	public void write(IOHelper h) throws IOException {
		h.writeBoolean(value);
	}

	@Override
	public void load(IOHelper h) throws IOException {
		value = h.readBoolean();
	}

	@Override
	public Object toJson() {
		return value;
	}

	@Override
	public void fromJson(Object v) {
		value = (boolean) v;
	}

}
