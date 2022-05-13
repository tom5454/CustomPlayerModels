package com.tom.cpl.render;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.TriangleBoundingBox;
import com.tom.cpl.math.Vec2f;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.util.Image;

public class VirtualTriangleRenderer {
	public static final Vec4f WHITE = new Vec4f(1, 1, 1, 1);

	public static class Triangle {
		public Vec4f[] verts;
		public Vec2f[] uvs;
		public Vec4f[] colors;
		public boolean culled;

		public Triangle(Vec3f a, Vec3f b, Vec3f c, Vec2f aUV, Vec2f bUV, Vec2f cUV, Vec4f ac, Vec4f bc, Vec4f cc) {
			verts = new Vec4f[3];
			verts[0] = new Vec4f(a, 1);
			verts[1] = new Vec4f(b, 1);
			verts[2] = new Vec4f(c, 1);
			uvs = new Vec2f[3];
			uvs[0] = aUV;
			uvs[1] = bUV;
			uvs[2] = cUV;
			colors = new Vec4f[3];
			colors[0] = ac;
			colors[1] = bc;
			colors[2] = cc;
		}

		public Triangle(TriangleBoundingBox.Triangle tri) {
			verts = tri.verts;
			uvs = tri.uvs;
			culled = tri.culled;
			colors = new Vec4f[3];
			colors[0] = WHITE;
			colors[1] = WHITE;
			colors[2] = WHITE;
		}
	}

	private static void plot(Triangle tri, Image img, Vec2i cursor, Image bound) {
		if(tri.culled)return;
		boolean isH = false;//!Float.isInfinite(tri.isHovered(evt).z);

		triangle(img, (int) tri.verts[0].x, (int) tri.verts[0].y,
				(int) tri.verts[1].x, (int) tri.verts[1].y,
				(int) tri.verts[2].x, (int) tri.verts[2].y, isH ? 0xffffff00 : 0xff00ffff);

		if(true)return;

		Vec2i p1 = new Vec2i(tri.verts[0]);
		Vec2i p2 = new Vec2i(tri.verts[1]);
		Vec2i p3 = new Vec2i(tri.verts[2]);

		Vec3f uv1 = new Vec3f(tri.uvs[0], tri.verts[0].z);
		Vec3f uv2 = new Vec3f(tri.uvs[1], tri.verts[1].z);
		Vec3f uv3 = new Vec3f(tri.uvs[2], tri.verts[2].z);

		Vec4f c1 = new Vec4f(tri.colors[0]);
		Vec4f c2 = new Vec4f(tri.colors[1]);
		Vec4f c3 = new Vec4f(tri.colors[2]);

		if (p2.y < p1.y) {
			swap(p1, p2);
			swap(uv1, uv2);
			swap(c1, c2);
		}

		if (p3.y < p1.y) {
			swap(p1, p3);
			swap(uv1, uv3);
			swap(c1, c3);
		}

		if (p3.y < p2.y) {
			swap(p3, p2);
			swap(uv3, uv2);
			swap(c3, c2);
		}

		int dy1 = p2.y - p1.y;
		int dx1 = p2.x - p1.x;
		float du1 = uv2.x - uv1.x;
		float dv1 = uv2.y - uv1.y;
		float dw1 = uv2.z - uv1.z;

		float dr1 = c2.x - c1.x;
		float dg1 = c2.y - c1.y;
		float db1 = c2.z - c1.z;
		float da1 = c2.w - c1.w;

		int dy2 = p3.y - p1.y;
		int dx2 = p3.x - p1.x;
		float du2 = uv3.x - uv1.x;
		float dv2 = uv3.y - uv1.y;
		float dw2 = uv3.z - uv1.z;

		float dr2 = c3.x - c1.x;
		float dg2 = c3.y - c1.y;
		float db2 = c3.z - c1.z;
		float da2 = c3.w - c1.w;

		float tex_u, tex_v, tex_w, c_r, c_g, c_b, c_a;

		float dax_step = 0, dbx_step = 0,
				du1_step = 0, dv1_step = 0,
				du2_step = 0, dv2_step = 0,
				dw1_step = 0, dw2_step = 0,
				dr1_step = 0, dg1_step = 0, db1_step = 0, da1_step = 0,
				dr2_step = 0, dg2_step = 0, db2_step = 0, da2_step = 0
				;

		if (dy1 != 0) dax_step = dx1 / (float) Math.abs(dy1);
		if (dy2 != 0) dbx_step = dx2 / (float) Math.abs(dy2);

		if (dy1 != 0) du1_step = du1 / Math.abs(dy1);
		if (dy1 != 0) dv1_step = dv1 / Math.abs(dy1);
		if (dy1 != 0) dw1_step = dw1 / Math.abs(dy1);

		if (dy1 != 0) dr1_step = dr1 / Math.abs(dy1);
		if (dy1 != 0) dg1_step = dg1 / Math.abs(dy1);
		if (dy1 != 0) db1_step = db1 / Math.abs(dy1);
		if (dy1 != 0) da1_step = da1 / Math.abs(dy1);

		if (dy2 != 0) du2_step = du2 / Math.abs(dy2);
		if (dy2 != 0) dv2_step = dv2 / Math.abs(dy2);
		if (dy2 != 0) dw2_step = dw2 / Math.abs(dy2);

		if (dy2 != 0) dr2_step = dr2 / Math.abs(dy2);
		if (dy2 != 0) dg2_step = dg2 / Math.abs(dy2);
		if (dy2 != 0) db2_step = db2 / Math.abs(dy2);
		if (dy2 != 0) da2_step = da2 / Math.abs(dy2);

		if (dy1 != 0) {
			for (int i = p1.y; i <= p2.y; i++) {
				int ax = (int) (p1.x + (i - p1.y) * dax_step);
				int bx = (int) (p1.x + (i - p1.y) * dbx_step);

				Vec3f tex_s = new Vec3f(
						uv1.x + (i - p1.y) * du1_step,
						uv1.y + (i - p1.y) * dv1_step,
						uv1.z + (i - p1.y) * dw1_step
						);

				Vec3f tex_e = new Vec3f(
						uv1.x + (i - p1.y) * du2_step,
						uv1.y + (i - p1.y) * dv2_step,
						uv1.z + (i - p1.y) * dw2_step
						);

				Vec4f c_s = new Vec4f(
						c1.x + (i - p1.y) * dr1_step,
						c1.y + (i - p1.y) * dg1_step,
						c1.z + (i - p1.y) * db1_step,
						c1.w + (i - p1.y) * da1_step
						);

				Vec4f c_e = new Vec4f(
						c1.x + (i - p1.y) * dr2_step,
						c1.y + (i - p1.y) * dg2_step,
						c1.z + (i - p1.y) * db2_step,
						c1.w + (i - p1.y) * da2_step
						);

				if (ax > bx) {
					int v = ax;
					ax = bx;
					bx = v;

					Vec3f u = tex_s;
					tex_s = tex_e;
					tex_e = u;

					Vec4f t = c_s;
					c_s = c_e;
					c_e = t;
				}

				tex_u = tex_s.x;
				tex_v = tex_s.y;
				tex_w = tex_s.z;

				c_r = c_s.x;
				c_g = c_s.y;
				c_b = c_s.z;
				c_a = c_s.w;

				float tstep = 1.0f / (bx - ax);
				float t = 0.0f;

				for (int j = ax; j < bx; j++) {
					tex_u = (1.0f - t) * tex_s.x + t * tex_e.x;
					tex_v = (1.0f - t) * tex_s.y + t * tex_e.y;
					tex_w = (1.0f - t) * tex_s.z + t * tex_e.z;

					c_r = (1.0f - t) * c_s.x + t * c_e.x;
					c_g = (1.0f - t) * c_s.y + t * c_e.y;
					c_b = (1.0f - t) * c_s.z + t * c_e.z;
					c_a = (1.0f - t) * c_s.w + t * c_e.w;

					if(j > 0 && i > 0 && j < img.getWidth() && i < img.getHeight())
						img.setRGB(j, i, sample(tex_u, tex_v, bound, c_r, c_g, c_b, c_a));
					t += tstep;
				}
			}
		}

		dy1 = p3.y - p2.y;
		dx1 = p3.x - p2.x;
		du1 = uv3.x - uv2.x;
		dv1 = uv3.y - uv2.y;
		dw1 = uv3.z - uv2.z;

		dr1 = c3.x - c2.x;
		dg1 = c3.y - c2.y;
		db1 = c3.z - c2.z;
		da1 = c3.w - c2.w;

		if (dy1 != 0) dax_step = dx1 / (float)Math.abs(dy1);
		if (dy2 != 0) dbx_step = dx2 / (float)Math.abs(dy2);

		du1_step = 0;
		dv1_step = 0;
		dw1_step = 0;
		dr1_step = 0;
		dg1_step = 0;
		db1_step = 0;
		da1_step = 0;

		if (dy1 != 0) du1_step = du1 / Math.abs(dy1);
		if (dy1 != 0) dv1_step = dv1 / Math.abs(dy1);
		if (dy1 != 0) dw1_step = dw1 / Math.abs(dy1);

		if (dy1 != 0) dr1_step = dr1 / Math.abs(dy1);
		if (dy1 != 0) dg1_step = dg1 / Math.abs(dy1);
		if (dy1 != 0) db1_step = db1 / Math.abs(dy1);
		if (dy1 != 0) da1_step = da1 / Math.abs(dy1);

		if (dy1 != 0) {
			for (int i = p2.y; i <= p3.y; i++) {
				int ax = (int) (p2.x + (i - p2.y) * dax_step);
				int bx = (int) (p1.x + (i - p1.y) * dbx_step);

				Vec3f tex_s = new Vec3f(
						uv2.x + (i - p2.y) * du1_step,
						uv2.y + (i - p2.y) * dv1_step,
						uv2.z + (i - p2.y) * dw1_step
						);

				Vec3f tex_e = new Vec3f(
						uv1.x + (i - p1.y) * du2_step,
						uv1.y + (i - p1.y) * dv2_step,
						uv1.z + (i - p1.y) * dw2_step
						);

				Vec4f c_s = new Vec4f(
						c2.x + (i - p2.y) * dr1_step,
						c2.y + (i - p2.y) * dg1_step,
						c2.z + (i - p2.y) * db1_step,
						c2.w + (i - p2.y) * da1_step
						);

				Vec4f c_e = new Vec4f(
						c1.x + (i - p1.y) * dr2_step,
						c1.y + (i - p1.y) * dg2_step,
						c1.z + (i - p1.y) * db2_step,
						c1.w + (i - p1.y) * da2_step
						);

				if (ax > bx) {
					int v = ax;
					ax = bx;
					bx = v;

					Vec3f u = tex_s;
					tex_s = tex_e;
					tex_e = u;

					Vec4f t = c_s;
					c_s = c_e;
					c_e = t;
				}

				tex_u = tex_s.x;
				tex_v = tex_s.y;
				tex_w = tex_s.z;

				c_r = c_s.x;
				c_g = c_s.y;
				c_b = c_s.z;
				c_a = c_s.w;

				float tstep = 1.0f / (bx - ax);
				float t = 0.0f;

				for (int j = ax; j < bx; j++) {
					tex_u = (1.0f - t) * tex_s.x + t * tex_e.x;
					tex_v = (1.0f - t) * tex_s.y + t * tex_e.y;
					tex_w = (1.0f - t) * tex_s.z + t * tex_e.z;

					c_r = (1.0f - t) * c_s.x + t * c_e.x;
					c_g = (1.0f - t) * c_s.y + t * c_e.y;
					c_b = (1.0f - t) * c_s.z + t * c_e.z;
					c_a = (1.0f - t) * c_s.w + t * c_e.w;

					if(j > 0 && i > 0 && j < img.getWidth() && i < img.getHeight())
						img.setRGB(j, i, sample(tex_u, tex_v, bound, c_r, c_g, c_b, c_a));
					t += tstep;
				}
			}
		}
	}

	private static int sample(float u, float v, Image bound, float r, float g, float b, float a) {
		if(bound == null) {
			int ri = (int) (r * 0xff) & 0xff;
			int gi = (int) (g * 0xff) & 0xff;
			int bi = (int) (b * 0xff) & 0xff;
			int ai = (int) (a * 0xff) & 0xff;
			return ai << 24 | ri << 16 | gi << 8 | bi;
		}
		int w = bound.getWidth();
		int h = bound.getHeight();
		int x = (MathHelper.floor(u * w) % w + w) % w;
		int y = (MathHelper.floor(v * h) % h + h) % h;

		return bound.getRGB(x, y);
	}

	public static void triangle(Image img, int x0, int y0, int x1, int y1, int x2, int y2, int col){
		line(img, x0, y0, x1, y1, col);
		line(img, x1, y1, x2, y2, col);
		line(img, x2, y2, x0, y0, col);
	}

	public static void line(Image img, int x0, int y0, int x1, int y1, int col){
		int dx =  Math.abs(x1-x0);
		int sx = x0<x1 ? 1 : -1;
		int dy = -Math.abs(y1-y0);
		int sy = y0<y1 ? 1 : -1;
		int err = dx+dy;  /* error value e_xy */
		while (true){   /* loop */
			int ix = MathHelper.clamp(x0, 0, img.getWidth() - 1);
			int iy = MathHelper.clamp(y0, 0, img.getHeight() - 1);
			//if(x0 > 0 && y0 > 0 && x0 < img.getWidth() && y0 < img.getHeight())
			img.setRGB(ix, iy, col);
			if (x0==x1 && y0==y1) break;
			int e2 = 2*err;
			if (e2 >= dy){
				err += dy; /* e_xy+e_x > 0 */
				x0 += sx;
			}
			if (e2 <= dx){ /* e_xy+e_y < 0 */
				err += dx;
				y0 += sy;
			}
		}
	}

	public static void plot(TriangleBoundingBox tbb, Image img, Vec2i cursor, Image bound) {
		tbb.getTriangles().forEach(t -> plot(new Triangle(t), img, cursor, bound));
		if(cursor.x > 0 && cursor.y > 0)
			img.fill(cursor.x - 1, cursor.y - 1, 3, 3, 0xffff0000);
	}

	private static void swap(Vec2i a, Vec2i b) {
		int x = a.x;
		int y = a.y;
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

	private static void swap(Vec4f a, Vec4f b) {
		float x = a.x;
		float y = a.y;
		float z = a.z;
		float w = a.w;
		a.x = b.x;
		a.y = b.y;
		a.z = b.z;
		a.w = b.w;
		b.x = x;
		b.y = y;
		b.z = z;
		b.w = w;
	}

	public static class VirtualBuffer implements VertexBuffer {
		private List<Vertex[]> quads = new ArrayList<>();
		private Vertex current = new Vertex();
		private Vertex[] currentArray = new Vertex[4];
		private int currentV;

		private VirtualBuffer() {
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
			current.r = red;
			current.g = green;
			current.b = blue;
			current.a = alpha;
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
			private float x, y, z, u, v, r, g, b, a;
		}

		public List<Triangle> build() {
			List<Triangle> tris = new ArrayList<>();
			for (Vertex[] v : quads) {
				Vec3f v0 = new Vec3f(v[0].x, v[0].y, v[0].z);
				Vec3f v1 = new Vec3f(v[1].x, v[1].y, v[1].z);
				Vec3f v2 = new Vec3f(v[2].x, v[2].y, v[2].z);
				Vec3f v3 = new Vec3f(v[3].x, v[3].y, v[3].z);

				Vec2f t0 = new Vec2f(v[0].u, v[0].v);
				Vec2f t1 = new Vec2f(v[1].u, v[1].v);
				Vec2f t2 = new Vec2f(v[2].u, v[2].v);
				Vec2f t3 = new Vec2f(v[3].u, v[3].v);

				Vec4f c0 = new Vec4f(v[0].r, v[0].g, v[0].b, v[0].a);
				Vec4f c1 = new Vec4f(v[1].r, v[1].g, v[1].b, v[1].a);
				Vec4f c2 = new Vec4f(v[2].r, v[2].g, v[2].b, v[2].a);
				Vec4f c3 = new Vec4f(v[3].r, v[3].g, v[3].b, v[3].a);
				tris.add(new Triangle(v0, v1, v2, t0, t1, t2, c0, c1, c2));
				tris.add(new Triangle(v0, v2, v3, t0, t2, t3, c0, c2, c3));
			}
			return tris;
		}
	}
}
