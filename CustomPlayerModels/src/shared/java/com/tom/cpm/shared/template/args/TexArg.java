package com.tom.cpm.shared.template.args;

import java.io.IOException;
import java.util.Map;

import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.template.Template.IArg;

public class TexArg implements IArg {
	public int u, v, texSize;

	@Override
	public String getType() {
		return "tex";
	}

	@Override
	public void write(Map<String, Object> map) {
		map.put("u", u);
		map.put("v", v);
		map.put("ts", texSize);
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.writeByte(texSize);
		dout.write(u);
		dout.write(v);
	}

	@Override
	public void load(Map<String, Object> map) {
		u = ((Number)map.get("u")).intValue();
		v = ((Number)map.get("v")).intValue();
		texSize = ((Number)map.get("ts")).intValue();
	}

	@Override
	public void load(IOHelper din) throws IOException {
		texSize = din.readByte();
		u = din.read();
		v = din.read();
	}

	@Override
	public void init(Map<String, Object> map) {
	}

	@Override
	public void apply(RenderedCube cube) {
		if(texSize != 0) {
			cube.getCube().u += u * texSize;
			cube.getCube().v += v * texSize;
			cube.getCube().texSize *= texSize;
		}
	}

	@Override
	public void export(Map<String, Object> map) {
	}

	@Override
	public void apply(Map<String, Object> map) {
	}

	public boolean isTextureMerged() {
		return texSize != 0;
	}
}
