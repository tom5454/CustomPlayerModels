package com.tom.cpm.blockbench.proxy;

import java.util.function.Consumer;

import com.tom.cpl.util.Direction;
import com.tom.cpm.blockbench.format.CubeData;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec2;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec3;
import com.tom.cpm.blockbench.proxy.three.ThreeMesh;
import com.tom.ugwt.client.JsArrayE;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Cube_$$")
public class Cube extends OutlinerElement {
	public String name;
	public JsVec3 origin, rotation, from, to;
	public JsVec2 uv_offset;
	public Group parent;
	public CubeFaces faces;
	public boolean mirror_uv, visibility, export, box_uv;
	public int autouv;
	public float inflate;
	public static JsArrayE<Cube> all;

	public ThreeMesh mesh;

	@JsProperty(name = "cpm_glow")
	public boolean glow;

	@JsProperty(name = "cpm_recolor")
	public int recolor;

	@JsProperty(name = "cpm_extrude")
	public boolean extrude;

	public static NodePreviewController preview_controller;

	public Cube(CubeProperties ctr) {}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class CubeProperties {
		public String name;
		public JsVec3 origin, rotation, from, to;
		public JsVec2 uv_offset;
		public float inflate;
		public boolean mirror_uv, visibility;
	}

	public native void extend(CubeProperties ctr);
	public native Cube addTo(Group gr);
	public native Cube init();
	public native void applyTexture(Texture tex, boolean all);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class CubeFaces {
		public CubeFace up, down, north, south, east, west;

		@JsOverlay
		public final void forEach(Consumer<CubeFace> c) {
			c.accept(up);
			c.accept(down);
			c.accept(north);
			c.accept(south);
			c.accept(east);
			c.accept(west);
		}

		//TODO
		@JsOverlay
		public final CubeFace getFace(Direction dir) {
			switch (dir) {
			case DOWN: return down;
			case EAST: return west;//!
			case NORTH: return north;
			case SOUTH: return south;
			case UP: return up;
			case WEST: return east;//!
			default: throw new RuntimeException();
			}
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class CubeFace {
		public FaceUV uv;
		public int rotation;
		public boolean autoUV;
		public String texture;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Array_$$")
	public static class FaceUV {
		public FaceUV() {}

		@JsProperty(name = "$$array_0_$$")
		public float sx;

		@JsProperty(name = "$$array_1_$$")
		public float sy;

		@JsProperty(name = "$$array_2_$$")
		public float ex;

		@JsProperty(name = "$$array_3_$$")
		public float ey;

		@JsOverlay
		public static FaceUV make(float sx, float sy, float ex, float ey) {
			FaceUV f = new FaceUV();
			f.sx = sx;
			f.sy = sy;
			f.ex = ex;
			f.ey = ey;
			return f;
		}

		@JsOverlay
		public final FaceUV mul(float x, float y) {
			sx *= x;
			ex *= x;
			sy *= y;
			ey *= y;
			return this;
		}

		@JsOverlay
		public final boolean isEmpty() {
			return sx == 0 && sy == 0 && ex == 0 && ey == 0;
		}
	}

	@JsProperty(name = "cpm_dataCache")
	private CubeData data;

	@JsOverlay
	public final CubeData getData() {
		if(data == null)data = new CubeData(this);
		return data;
	}
}
