package com.tom.cpm.shared.model;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.io.IOHelper;

public class Cube {
	public Vec3f offset;
	public Vec3f rotation;
	public Vec3f pos;
	public Vec3f size;
	public Vec3f scale;
	public int parentId;
	public int id;
	public int rgb;
	public int u, v, texSize;
	public float mcScale;

	public static Cube loadDefinitionCube(IOHelper din) throws IOException {
		Cube c = new Cube();
		loadCubePos(c, din);
		int tex = din.readByte();
		if(tex == 0) {
			int ch2 = din.read();
			int ch3 = din.read();
			int ch4 = din.read();
			if ((ch2 | ch3 | ch4) < 0)
				throw new EOFException();
			c.rgb = ((0xff << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
		} else {
			c.texSize = tex;
			c.u = din.read();
			c.v = din.read();
		}
		return c;
	}

	@SuppressWarnings("unchecked")
	public static Cube loadDefinitionCube(Map<String, Object> map) {
		Cube c = new Cube();
		c.offset = new Vec3f((Map<String, Object>) map.get("offset"), new Vec3f());
		c.pos = new Vec3f((Map<String, Object>) map.get("pos"), new Vec3f());
		c.rotation = new Vec3f((Map<String, Object>) map.get("rotation"), new Vec3f());
		c.size = new Vec3f((Map<String, Object>) map.get("size"), new Vec3f(1, 1, 1));
		c.scale = new Vec3f((Map<String, Object>) map.get("scale"), new Vec3f(1, 1, 1));
		c.u = ((Number)map.get("u")).intValue();
		c.v = ((Number)map.get("v")).intValue();
		c.rgb = Integer.parseUnsignedInt((String) map.get("color"), 16);
		c.texSize = ((Number)map.get("textureSize")).intValue();
		c.mcScale = ((Number)map.get("mcScale")).floatValue();
		c.parentId = ((Number)map.get("parent")).intValue();
		return c;
	}

	public static void saveDefinitionCube(IOHelper dout, Cube cube) throws IOException {
		dout.writeVec3ub(cube.size);
		dout.writeVec6b(cube.pos);
		dout.writeVec6b(cube.offset);
		dout.writeAngle(cube.rotation);
		dout.writeVarInt(cube.parentId);
		dout.writeByte(cube.texSize);
		if(cube.texSize == 0) {
			dout.write((cube.rgb >>> 16) & 0xFF);
			dout.write((cube.rgb >>>  8) & 0xFF);
			dout.write((cube.rgb >>>  0) & 0xFF);
		} else {
			dout.write(cube.u);
			dout.write(cube.v);
		}
	}

	public static void saveDefinitionCube(Map<String, Object> map, Cube cube) throws IOException {
		map.put("offset", cube.offset.toMap());
		map.put("pos", cube.pos.toMap());
		map.put("rotation", cube.rotation.toMap());
		map.put("size", cube.size.toMap());
		map.put("scale", cube.scale.toMap());
		map.put("u", cube.u);
		map.put("v", cube.v);
		map.put("color", Integer.toHexString(cube.rgb));
		map.put("mcScale", cube.mcScale);
		map.put("textureSize", cube.texSize);
		map.put("parent", cube.parentId);
	}

	public static Cube loadTemplateCube(IOHelper din, int[] colors) throws IOException {
		Cube c = new Cube();
		loadCubePos(c, din);
		c.rgb = colors[din.read()];
		return c;
	}

	private static void loadCubePos(Cube c, IOHelper din) throws IOException {
		c.size = din.readVec3ub();
		c.pos = din.readVec6b();
		c.offset = din.readVec6b();
		c.rotation = din.readAngle();
		c.scale = new Vec3f(1, 1, 1);
		c.parentId = din.readVarInt();
	}

	public static List<RenderedCube> resolveCubes(List<Cube> cubes) {
		Map<Integer, RenderedCube> r = new HashMap<>();
		for (Cube cube : cubes) {
			r.put(cube.id, new RenderedCube(cube));
		}
		for (Cube c : cubes) {
			if(c.parentId < 10)continue;
			RenderedCube cube = r.get(c.id);
			RenderedCube parent = r.get(c.parentId);
			cube.setParent(parent);
			parent.addChild(cube);
		}
		return new ArrayList<>(r.values());
	}

	public static Cube newFakeCube() {
		Cube c = new Cube();
		c.offset = new Vec3f();
		c.size = new Vec3f();
		c.pos = new Vec3f();
		c.rotation = new Vec3f();
		c.parentId = PlayerModelParts.CUSTOM_PART.ordinal();
		return c;
	}
}
