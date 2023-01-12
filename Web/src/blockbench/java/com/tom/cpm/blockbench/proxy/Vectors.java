package com.tom.cpm.blockbench.proxy;

import com.tom.cpl.math.Vec2f;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec3i;

import elemental2.core.JsArray;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

public class Vectors {

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Array_$$")
	public static class JsVec3 extends JsArray<Float> {
		public JsVec3() {}

		@JsProperty(name = "$$array_0_$$")
		public float x;

		@JsProperty(name = "$$array_1_$$")
		public float y;

		@JsProperty(name = "$$array_2_$$")
		public float z;

		@JsOverlay
		public static JsVec3 make(Vec3f v) {
			JsVec3 ve = new JsVec3();
			ve.x = v.x;
			ve.y = v.y;
			ve.z = v.z;
			return ve;
		}

		@JsOverlay
		public static JsVec3 make(float x, float y, float z) {
			JsVec3 ve = new JsVec3();
			ve.x = x;
			ve.y = y;
			ve.z = z;
			return ve;
		}

		@JsOverlay
		public static JsVec3 make(Vec3i v) {
			JsVec3 ve = new JsVec3();
			ve.x = v.x;
			ve.y = v.y;
			ve.z = v.z;
			return ve;
		}

		@JsOverlay
		public final Vec3f toVecF() {
			return new Vec3f(x, y, z);
		}

		@JsOverlay
		public final Vec3i toVecI() {
			return new Vec3i((int) x, (int) y, (int) z);
		}

		public native void V3_add(JsVec3 origin);
		public native boolean allEqual(int i);

		@JsOverlay
		public final boolean hasValues(int v) {
			int i = 0;
			if(x != 0)i++;
			if(y != 0)i++;
			if(z != 0)i++;
			return i > v;
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Array_$$")
	public static class JsVec2 extends JsArray<Float> {
		public JsVec2() {}

		@JsProperty(name = "$$array_0_$$")
		public float x;

		@JsProperty(name = "$$array_1_$$")
		public float y;

		@JsOverlay
		public static JsVec2 make(Vec2f v) {
			JsVec2 ve = new JsVec2();
			ve.x = v.x;
			ve.y = v.y;
			return ve;
		}

		@JsOverlay
		public static JsVec2 make(Vec2i v) {
			JsVec2 ve = new JsVec2();
			ve.x = v.x;
			ve.y = v.y;
			return ve;
		}

		@JsOverlay
		public static JsVec2 make(float x, float y) {
			JsVec2 ve = new JsVec2();
			ve.x = x;
			ve.y = y;
			return ve;
		}

		@JsOverlay
		public final Vec2f toVecF() {
			return new Vec2f(x, y);
		}

		@JsOverlay
		public final Vec2i toVecI() {
			return new Vec2i(x, y);
		}
	}
}
