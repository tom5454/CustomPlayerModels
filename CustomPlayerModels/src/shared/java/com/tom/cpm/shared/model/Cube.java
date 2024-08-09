package com.tom.cpm.shared.model;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.io.IOHelper;

public class Cube {
	public static final int HAS_MESH    = 1 << 0;
	public static final int HAS_TEXTURE = 1 << 1;
	public static final int HIDDEN      = 1 << 2;
	public static final int MESH_SCALED = 1 << 3;
	public static final int UV_SCALED   = 1 << 4;
	public static final int MC_SCALED   = 1 << 5;
	public static final int SCALED      = 1 << 6;

	public Vec3f offset;
	public Vec3f rotation;
	public Vec3f pos;
	public Vec3f size;
	public Vec3f scale;
	public Vec3f meshScale;
	public int parentId;
	public int id;
	public int rgb;
	public int u, v, texSize;
	public float mcScale;
	public boolean hidden;

	@Deprecated
	public static Cube loadDefinitionCube(IOHelper din) throws IOException {
		Cube c = new Cube();
		c.size = din.readVec3ub();
		c.pos = din.readVec6b();
		c.offset = din.readVec6b();
		c.rotation = din.readAngle();
		c.meshScale = new Vec3f(1, 1, 1);
		c.scale = new Vec3f(1, 1, 1);
		c.parentId = din.readVarInt();
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

	public static Cube loadDefinitionCubeV2(IOHelper din) throws IOException {
		Cube c = new Cube();
		byte flags = din.readByte();
		c.parentId = din.readVarInt();
		c.pos = din.readVarVec3();
		c.rotation = din.readAngle();
		c.hidden = (flags & HIDDEN) != 0;
		if ((flags & HAS_MESH) != 0) {
			c.size = din.readVarVec3();
			c.offset = din.readVarVec3();

			if ((flags & HAS_TEXTURE) != 0) {
				c.texSize = (flags & UV_SCALED) != 0 ? din.readByte() : 1;
				c.u = din.readVarInt();
				c.v = din.readVarInt();
			} else {
				int ch2 = din.read();
				int ch3 = din.read();
				int ch4 = din.read();
				if ((ch2 | ch3 | ch4) < 0)
					throw new EOFException();
				c.rgb = ((0xff << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
			}

			if ((flags & MC_SCALED) != 0) {
				c.mcScale = din.readFloat2();
			}
		} else {
			c.size = new Vec3f();
			c.offset = new Vec3f();
		}

		if ((flags & MESH_SCALED) != 0) {
			c.meshScale = din.readVarVec3();
		} else {
			c.meshScale = new Vec3f(1, 1, 1);
		}

		if ((flags & SCALED) != 0) {
			c.scale = din.readVarVec3();
		} else {
			c.scale = new Vec3f(1, 1, 1);
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
		c.meshScale = new Vec3f((Map<String, Object>) map.get("scale"), new Vec3f(1, 1, 1));
		c.u = ((Number)map.get("u")).intValue();
		c.v = ((Number)map.get("v")).intValue();
		c.rgb = Integer.parseUnsignedInt((String) map.get("color"), 16);
		c.texSize = ((Number)map.get("textureSize")).intValue();
		c.mcScale = ((Number)map.get("mcScale")).floatValue();
		c.parentId = ((Number)map.get("parent")).intValue();
		return c;
	}

	@Deprecated
	public static void saveDefinitionCube(IOHelper dout, Cube cube) throws IOException {
		dout.writeVec3ub(cube.size);
		dout.writeVec6b(cube.pos);
		dout.writeVec6b(cube.offset);
		Vec3f rot = new Vec3f(cube.rotation);
		if(rot.x < 0 || rot.x > 360 || rot.y < 0 || rot.y > 360 || rot.z < 0 || rot.z > 360) {
			while(rot.x < 0)   rot.x += 360;
			while(rot.x >= 360)rot.x -= 360;
			while(rot.y < 0)   rot.y += 360;
			while(rot.y >= 360)rot.y -= 360;
			while(rot.z < 0)   rot.z += 360;
			while(rot.z >= 360)rot.z -= 360;
		}
		dout.writeAngle(rot);
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

	public static void saveDefinitionCubeV2(IOHelper dout, Cube cube) throws IOException {
		boolean hasMesh = !cube.size.epsilon(0.001f);
		boolean texture = hasMesh && cube.texSize != 0;
		boolean hidden = cube.hidden;
		boolean mesh_scaled = cube.meshScale != null && !cube.meshScale.sub(1).epsilon(0.001f);
		boolean scaled = cube.scale != null && !cube.scale.sub(1).epsilon(0.001f);
		boolean uv_scaled = texture && cube.texSize != 1;
		boolean mc_scaled = hasMesh && Math.abs(cube.mcScale) > 0.0001f;
		int flags = 0;
		boolean[] flagsArray = new boolean[] {hasMesh, texture, hidden, mesh_scaled, uv_scaled, mc_scaled, scaled};
		for (int i = flagsArray.length - 1;i >= 0;i--)
			flags = (flags << 1) | (flagsArray[i] ? 1 : 0);

		dout.writeByte(flags);
		dout.writeVarInt(cube.parentId);
		dout.writeVarVec3(cube.pos);
		Vec3f rot = new Vec3f(cube.rotation);
		ActionBuilder.limitVec(rot, 0, 360, true);
		dout.writeAngle(rot);
		if (hasMesh) {
			dout.writeVarVec3(cube.size);
			dout.writeVarVec3(cube.offset);

			if (texture) {
				if (uv_scaled)dout.writeByte(cube.texSize);
				dout.writeVarInt(cube.u);
				dout.writeVarInt(cube.v);
			} else {
				dout.write((cube.rgb >>> 16) & 0xFF);
				dout.write((cube.rgb >>>  8) & 0xFF);
				dout.write((cube.rgb >>>  0) & 0xFF);
			}

			if (mc_scaled) {
				dout.writeFloat2(cube.mcScale);
			}
		}

		if (mesh_scaled) {
			dout.writeVarVec3(cube.meshScale);
		}

		if (scaled) {
			dout.writeVarVec3(cube.scale);
		}
	}

	public static void saveTemplateCube(Map<String, Object> map, Cube cube) throws IOException {
		map.put("offset", cube.offset.toMap());
		map.put("pos", cube.pos.toMap());
		map.put("rotation", cube.rotation.toMap());
		map.put("size", cube.size.toMap());
		if (cube.meshScale != null)map.put("scale", cube.meshScale.toMap());
		map.put("u", cube.u);
		map.put("v", cube.v);
		map.put("color", Integer.toHexString(cube.rgb));
		map.put("mcScale", cube.mcScale);
		map.put("textureSize", cube.texSize);
		map.put("parent", cube.parentId);
	}

	public static List<RenderedCube> resolveCubesV2(List<Cube> cubes) {
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

	public static List<RenderedCube> resolveCubes(List<Cube> cubes) {
		Map<Integer, RenderedCube> r = new HashMap<>();
		for (Cube cube : cubes) {
			boolean h = cube.hidden;
			cube.hidden = false;
			RenderedCube rc = new RenderedCube(cube);
			rc.getCube().hidden = h;
			r.put(cube.id, rc);
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
