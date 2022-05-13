package com.tom.cpl.math;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.tom.cpl.render.VertexBuffer;

public class TriangleBoundingBox {
	private List<Triangle> triangles = new ArrayList<>();
	private float depth = Float.NEGATIVE_INFINITY;
	private Vec2f hoverPointer = new Vec2f();
	private static final Vec3f NULL = new Vec3f(-1, -1, Float.NEGATIVE_INFINITY);
	private boolean noCull;

	public class Triangle {
		public Vec4f[] verts;
		public Vec2f[] uvs;
		public boolean culled;

		public Triangle(Vec3f a, Vec3f b, Vec3f c, Vec2f aUV, Vec2f bUV, Vec2f cUV) {
			verts = new Vec4f[3];
			verts[0] = new Vec4f(a, 1);
			verts[1] = new Vec4f(b, 1);
			verts[2] = new Vec4f(c, 1);
			uvs = new Vec2f[3];
			uvs[0] = aUV;
			uvs[1] = bUV;
			uvs[2] = cUV;
		}

		public void transform(Mat4f matr) {
			for (int i = 0; i < verts.length; i++) {
				verts[i].transform(matr);
			}
		}

		public void finishTransform(int w, int h) {
			Vec3f line1 = new Vec3f();
			Vec3f line2 = new Vec3f();
			line1.x = this.verts[1].x - this.verts[0].x;
			line1.y = this.verts[1].y - this.verts[0].y;
			line1.z = this.verts[1].z - this.verts[0].z;

			line2.x = this.verts[2].x - this.verts[0].x;
			line2.y = this.verts[2].y - this.verts[0].y;
			line2.z = this.verts[2].z - this.verts[0].z;

			Vec3f normal = new Vec3f();
			normal.x = line1.y * line2.z - line1.z * line2.y;
			normal.y = line1.z * line2.x - line1.x * line2.z;
			normal.z = line1.x * line2.y - line1.y * line2.x;
			normal.normalize();

			double normalDot = normal.x * (this.verts[0].x) +
					normal.y * (this.verts[0].y) +
					normal.z * (this.verts[0].z);

			if(normalDot > 0 && !noCull) {
				culled = true;
				return;
			}

			for (int i = 0; i < verts.length; i++) {
				Vec4f vec4f = verts[i];
				vec4f.x = ( vec4f.x + 1) * 0.5f * w;//-
				vec4f.y = (-vec4f.y + 1) * 0.5f * h;
			}
		}

		public Vec3f isHovered(Vec2i s) {
			if(culled)return NULL;
			Vec2i a = new Vec2i(verts[0].x, verts[0].y);
			Vec2i b = new Vec2i(verts[1].x, verts[1].y);
			Vec2i c = new Vec2i(verts[2].x, verts[2].y);

			int as_x = s.x-a.x;
			int as_y = s.y-a.y;

			boolean s_ab = (b.x-a.x)*as_y-(b.y-a.y)*as_x > 0;

			if((c.x-a.x)*as_y-(c.y-a.y)*as_x > 0 == s_ab) return NULL;

			if((c.x-b.x)*(s.y-b.y)-(c.y-b.y)*(s.x-b.x) > 0 != s_ab) return NULL;

			Vec2f p1 = new Vec2f(this.verts[0]);
			Vec2f p2 = new Vec2f(this.verts[1]);
			Vec2f p3 = new Vec2f(this.verts[2]);

			Vec3f uv1 = new Vec3f(uvs[0], verts[0].z);
			Vec3f uv2 = new Vec3f(uvs[1], verts[1].z);
			Vec3f uv3 = new Vec3f(uvs[2], verts[2].z);

			if (p2.y < p1.y) {
				swap(p1, p2);
				swap(uv1, uv2);
			}

			if (p3.y < p1.y) {
				swap(p1, p3);
				swap(uv1, uv3);
			}

			if (p3.y < p2.y) {
				swap(p3, p2);
				swap(uv3, uv2);
			}

			float dy1 = p2.y - p1.y;
			float dx1 = p2.x - p1.x;
			float du1 = uv2.x - uv1.x;
			float dv1 = uv2.y - uv1.y;
			float dw1 = uv2.z - uv1.z;

			float dy2 = p3.y - p1.y;
			float dx2 = p3.x - p1.x;
			float du2 = uv3.x - uv1.x;
			float dv2 = uv3.y - uv1.y;
			float dw2 = uv3.z - uv1.z;

			float dax_step = 0, dbx_step = 0,
					du1_step = 0, dv1_step = 0,
					du2_step = 0, dv2_step = 0,
					dw1_step = 0, dw2_step = 0
					;

			if (dy1 != 0) dax_step = dx1 / Math.abs(dy1);
			if (dy2 != 0) dbx_step = dx2 / Math.abs(dy2);

			if (dy1 != 0) du1_step = du1 / Math.abs(dy1);
			if (dy1 != 0) dv1_step = dv1 / Math.abs(dy1);
			if (dy1 != 0) dw1_step = dw1 / Math.abs(dy1);

			if (dy2 != 0) du2_step = du2 / Math.abs(dy2);
			if (dy2 != 0) dv2_step = dv2 / Math.abs(dy2);
			if (dy2 != 0) dw2_step = dw2 / Math.abs(dy2);

			if (dy1 != 0 && s.y > p1.y - 1 && s.y < p2.y + 1) {
				int ax = (int) (p1.x + (s.y - p1.y) * dax_step);
				int bx = (int) (p1.x + (s.y - p1.y) * dbx_step);

				Vec3f tex_s = new Vec3f(
						uv1.x + (s.y - p1.y) * du1_step,
						uv1.y + (s.y - p1.y) * dv1_step,
						uv1.z + (s.y - p1.y) * dw1_step
						);

				Vec3f tex_e = new Vec3f(
						uv1.x + (s.y - p1.y) * du2_step,
						uv1.y + (s.y - p1.y) * dv2_step,
						uv1.z + (s.y - p1.y) * dw2_step
						);

				if (ax > bx) {
					int v = ax;
					ax = bx;
					bx = v;

					swap(tex_s, tex_e);
				}

				float tstep = 1.0f / (bx - ax);

				if(s.x > ax - 2 && s.x < bx + 2) {
					float t = tstep * (s.x - ax);

					float tex_u = (1.0f - t) * tex_s.x + t * tex_e.x;
					float tex_v = (1.0f - t) * tex_s.y + t * tex_e.y;
					float tex_w = (1.0f - t) * tex_s.z + t * tex_e.z;

					return new Vec3f(tex_u, tex_v, tex_w);
				}
			}

			dy1 = p3.y - p2.y;
			dx1 = p3.x - p2.x;
			du1 = uv3.x - uv2.x;
			dv1 = uv3.y - uv2.y;
			dw1 = uv3.z - uv2.z;

			if (dy1 != 0) dax_step = dx1 / Math.abs(dy1);
			if (dy2 != 0) dbx_step = dx2 / Math.abs(dy2);

			du1_step = 0;
			dv1_step = 0;
			dw1_step = 0;

			if (dy1 != 0) du1_step = du1 / Math.abs(dy1);
			if (dy1 != 0) dv1_step = dv1 / Math.abs(dy1);
			if (dy1 != 0) dw1_step = dw1 / Math.abs(dy1);

			if (dy1 != 0 && s.y > p2.y - 1 && s.y < p3.y + 1) {
				int ax = (int) (p2.x + (s.y - p2.y) * dax_step);
				int bx = (int) (p1.x + (s.y - p1.y) * dbx_step);

				Vec3f tex_s = new Vec3f(
						uv2.x + (s.y - p2.y) * du1_step,
						uv2.y + (s.y - p2.y) * dv1_step,
						uv2.z + (s.y - p2.y) * dw1_step
						);

				Vec3f tex_e = new Vec3f(
						uv1.x + (s.y - p1.y) * du2_step,
						uv1.y + (s.y - p1.y) * dv2_step,
						uv1.z + (s.y - p1.y) * dw2_step
						);

				if (ax > bx) {
					int v = ax;
					ax = bx;
					bx = v;

					swap(tex_s, tex_e);
				}

				float tstep = 1.0f / (bx - ax);

				if(s.x > ax - 2 && s.x < bx + 2) {
					float t = tstep * (s.x - ax);

					float tex_u = (1.0f - t) * tex_s.x + t * tex_e.x;
					float tex_v = (1.0f - t) * tex_s.y + t * tex_e.y;
					float tex_w = (1.0f - t) * tex_s.z + t * tex_e.z;

					return new Vec3f(tex_u, tex_v, tex_w);
				}
			}

			return NULL;
		}
	}

	private static void swap(Vec2f a, Vec2f b) {
		float x = a.x;
		float y = a.y;
		a.x = b.x;
		a.y = b.y;
		b.x = x;
		b.y = y;
	}

	private static void swap(Vec3f a, Vec3f b) {
		float x = a.x;
		float y = a.y;
		float z = a.z;
		a.x = b.x;
		a.y = b.y;
		a.z = b.z;
		b.x = x;
		b.y = y;
		b.z = z;
	}

	public TriangleBoundingBox() {
	}

	public void transform(Mat4f matr) {
		triangles.forEach(t -> t.transform(matr));
	}

	public void transform(Quaternion q) {
		Mat4f mat = new Mat4f();
		mat.setIdentity();
		mat.mul(q);
		triangles.forEach(t -> t.transform(mat));
	}

	public void finishTransform(int w, int h, Vec2i cursor) {
		triangles.forEach(t -> t.finishTransform(w, h));
		Vec3f f = triangles.stream().map(t -> t.isHovered(cursor)).max(Comparator.comparingDouble(v -> v.z)).orElse(NULL);
		if(Float.isInfinite(f.z))depth = Float.NEGATIVE_INFINITY;
		else {
			depth = f.z;
			hoverPointer.x = f.x;
			hoverPointer.y = f.y;
		}
	}

	public float isHovered() {
		return depth;
	}

	public Vec2f getHoverPointer() {
		return hoverPointer;
	}

	public List<Triangle> getTriangles() {
		return triangles;
	}

	public static BoxBuilder builder() {
		return new BoxBuilder();
	}

	public static class BoxBuilder implements VertexBuffer {
		private List<Vertex[]> quads = new ArrayList<>();
		private Vertex current = new Vertex();
		private Vertex[] currentArray = new Vertex[4];
		private int currentV;

		private BoxBuilder() {
		}

		@Override
		public VertexBuffer pos(float x, float y, float z) {
			current.x = x;
			current.y = y;
			current.z = z;
			return this;
		}

		@Override
		public VertexBuffer tex(float u, float v) {
			current.u = u;
			current.v = v;
			return this;
		}

		@Override
		public VertexBuffer color(float red, float green, float blue, float alpha) {
			return this;
		}

		@Override
		public VertexBuffer normal(float x, float y, float z) {
			return this;
		}

		@Override
		public void endVertex() {
			currentArray[currentV++] = current;
			current = new Vertex();
			if(currentV > 3) {
				quads.add(currentArray);
				currentArray = new Vertex[4];
				currentV = 0;
			}
		}

		@Override
		public void finish() {
		}

		private static class Vertex {
			private float x, y, z, u, v;
		}

		public TriangleBoundingBox build() {
			return build(false);
		}

		public TriangleBoundingBox build(boolean noCull) {
			TriangleBoundingBox b = new TriangleBoundingBox();
			b.noCull = noCull;
			for (Vertex[] v : quads) {
				Vec3f v0 = new Vec3f(v[0].x, v[0].y, v[0].z);
				Vec3f v1 = new Vec3f(v[1].x, v[1].y, v[1].z);
				Vec3f v2 = new Vec3f(v[2].x, v[2].y, v[2].z);
				Vec3f v3 = new Vec3f(v[3].x, v[3].y, v[3].z);

				Vec2f t0 = new Vec2f(v[0].u, v[0].v);
				Vec2f t1 = new Vec2f(v[1].u, v[1].v);
				Vec2f t2 = new Vec2f(v[2].u, v[2].v);
				Vec2f t3 = new Vec2f(v[3].u, v[3].v);

				b.triangles.add(b.new Triangle(v0, v1, v2, t0, t1, t2));
				b.triangles.add(b.new Triangle(v0, v2, v3, t0, t2, t3));
			}
			return b;
		}
	}
}
