package com.tom.cpm.shared.editor;

import static com.tom.cpm.shared.model.render.BoxRender.*;

import java.util.function.BiFunction;

import com.tom.cpl.math.BoundingBox;
import com.tom.cpl.math.Mat3f;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Rotation;
import com.tom.cpl.math.TriangleBoundingBox;
import com.tom.cpl.math.TriangleBoundingBox.BoxBuilder;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.tree.VecType;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.render.ModelRenderManager;

public class EditorRenderer {

	public static class Bounds {
		public TriangleBoundingBox bb;
		public Runnable drawHover;
		public boolean isHovered;
		public TreeElement elem;
		public BoundType type = BoundType.CLICK;
	}

	public static enum BoundType {
		CLICK,
		DRAG_X,
		DRAG_Y,
		DRAG_Z,
		DRAG_NX,
		DRAG_NY,
		DRAG_NZ,
		DRAG_PANE,
	}

	public static void drawDragArrows(MatrixStack matrixStackIn, VertexBuffer bufferIn, BiFunction<EditorRenderer.BoundType, BoxBuilder, VertexBuffer[]> boxConsumer, float x, float y, float z, float len, float size, float alpha) {
		float m = 1 / 16f;
		BoxBuilder builderX = TriangleBoundingBox.builder();
		BoxBuilder builderY = TriangleBoundingBox.builder();
		BoxBuilder builderZ = TriangleBoundingBox.builder();
		BoundingBox bx = new BoundingBox(x,        y - size, z - size, x + len,  y + size, z + size).mul(m);
		BoundingBox by = new BoundingBox(x - size, y,        z - size, x + size, y + len,  z + size).mul(m);
		BoundingBox bz = new BoundingBox(x - size, y - size, z,        x + size, y + size, z + len ).mul(m);
		fillBoundingBox(matrixStackIn, builderX, bx, 1, 0, 0, alpha);
		fillBoundingBox(matrixStackIn, builderY, by, 0, 1, 0, alpha);
		fillBoundingBox(matrixStackIn, builderZ, bz, 0, 0, 1, alpha);

		float f1 = len - (2*size) / 3f;
		float f2 = len - size / 3f;
		float f3 = len - size;
		float f4 = len;
		float s1 = size / 3;
		float s2 = s1 * 2;
		float s3 = size;
		BoundingBox bx1 = new BoundingBox(x,      y - s1, z - s1, x + f4, y + s1, z + s1).mul(m);
		BoundingBox by1 = new BoundingBox(x - s1, y,      z - s1, x + s1, y + f4, z + s1).mul(m);
		BoundingBox bz1 = new BoundingBox(x - s1, y - s1, z,      x + s1, y + s1, z + f4).mul(m);
		BoundingBox bx2 = new BoundingBox(x + f1, y - s2, z - s2, x + f2, y + s2, z + s2).mul(m);
		BoundingBox by2 = new BoundingBox(x - s2, y + f1, z - s2, x + s2, y + f2, z + s2).mul(m);
		BoundingBox bz2 = new BoundingBox(x - s2, y - s2, z + f1, x + s2, y + s2, z + f2).mul(m);
		BoundingBox bx3 = new BoundingBox(x + f3, y - s3, z - s3, x + f1, y + s3, z + s3).mul(m);
		BoundingBox by3 = new BoundingBox(x - s3, y + f3, z - s3, x + s3, y + f1, z + s3).mul(m);
		BoundingBox bz3 = new BoundingBox(x - s3, y - s3, z + f3, x + s3, y + s3, z + f1).mul(m);

		VertexBuffer[] xb = boxConsumer.apply(EditorRenderer.BoundType.DRAG_X, builderX);
		drawBoundingBox(matrixStackIn, bufferIn, bx1, 0.5f, 0, 0, alpha);
		drawBoundingBox(matrixStackIn, bufferIn, bx2, 0.5f, 0, 0, alpha);
		drawBoundingBox(matrixStackIn, bufferIn, bx3, 0.5f, 0, 0, alpha);

		drawBoundingBox(matrixStackIn, xb[1], bx1, 1, 0, 0, alpha);
		drawBoundingBox(matrixStackIn, xb[1], bx2, 1, 0, 0, alpha);
		drawBoundingBox(matrixStackIn, xb[1], bx3, 1, 0, 0, alpha);

		fillBoundingBox(matrixStackIn, xb[0], bx1, 1, 0, 0, alpha);
		fillBoundingBox(matrixStackIn, xb[0], bx2, 1, 0, 0, alpha);
		fillBoundingBox(matrixStackIn, xb[0], bx3, 1, 0, 0, alpha);

		VertexBuffer[] yb = boxConsumer.apply(EditorRenderer.BoundType.DRAG_Y, builderY);
		drawBoundingBox(matrixStackIn, bufferIn, by1, 0, 0.5f, 0, alpha);
		drawBoundingBox(matrixStackIn, bufferIn, by2, 0, 0.5f, 0, alpha);
		drawBoundingBox(matrixStackIn, bufferIn, by3, 0, 0.5f, 0, alpha);

		drawBoundingBox(matrixStackIn, yb[1], by1, 0, 1, 0, alpha);
		drawBoundingBox(matrixStackIn, yb[1], by2, 0, 1, 0, alpha);
		drawBoundingBox(matrixStackIn, yb[1], by3, 0, 1, 0, alpha);

		fillBoundingBox(matrixStackIn, yb[0], by1, 0, 1, 0, alpha);
		fillBoundingBox(matrixStackIn, yb[0], by2, 0, 1, 0, alpha);
		fillBoundingBox(matrixStackIn, yb[0], by3, 0, 1, 0, alpha);

		VertexBuffer[] zb = boxConsumer.apply(EditorRenderer.BoundType.DRAG_Z, builderZ);
		drawBoundingBox(matrixStackIn, bufferIn, bz1, 0, 0, 0.5f, alpha);
		drawBoundingBox(matrixStackIn, bufferIn, bz2, 0, 0, 0.5f, alpha);
		drawBoundingBox(matrixStackIn, bufferIn, bz3, 0, 0, 0.5f, alpha);

		drawBoundingBox(matrixStackIn, zb[1], bz1, 0, 0, 1, alpha);
		drawBoundingBox(matrixStackIn, zb[1], bz2, 0, 0, 1, alpha);
		drawBoundingBox(matrixStackIn, zb[1], bz3, 0, 0, 1, alpha);

		fillBoundingBox(matrixStackIn, zb[0], bz1, 0, 0, 1, alpha);
		fillBoundingBox(matrixStackIn, zb[0], bz2, 0, 0, 1, alpha);
		fillBoundingBox(matrixStackIn, zb[0], bz3, 0, 0, 1, alpha);
	}

	public static void drawDragBoxes(MatrixStack matrixStackIn, VertexBuffer bufferIn, BiFunction<EditorRenderer.BoundType, BoxBuilder, VertexBuffer[]> boxConsumer, float x, float y, float z, float len, float size, float alpha) {
		float m = 1 / 16f;
		BoxBuilder builderPX = TriangleBoundingBox.builder();
		BoxBuilder builderPY = TriangleBoundingBox.builder();
		BoxBuilder builderPZ = TriangleBoundingBox.builder();
		BoxBuilder builderNX = TriangleBoundingBox.builder();
		BoxBuilder builderNY = TriangleBoundingBox.builder();
		BoxBuilder builderNZ = TriangleBoundingBox.builder();
		BoundingBox bpx = new BoundingBox(x,        y - size, z - size, x + len,  y + size, z + size).mul(m);
		BoundingBox bpy = new BoundingBox(x - size, y,        z - size, x + size, y + len,  z + size).mul(m);
		BoundingBox bpz = new BoundingBox(x - size, y - size, z,        x + size, y + size, z + len ).mul(m);
		BoundingBox bnx = new BoundingBox(x,        y - size, z - size, x - len,  y + size, z + size).mul(m);
		BoundingBox bny = new BoundingBox(x - size, y,        z - size, x + size, y - len,  z + size).mul(m);
		BoundingBox bnz = new BoundingBox(x - size, y - size, z,        x + size, y + size, z - len ).mul(m);
		fillBoundingBox(matrixStackIn, builderPX, bpx, 1, 0, 0, alpha);
		fillBoundingBox(matrixStackIn, builderPY, bpy, 0, 1, 0, alpha);
		fillBoundingBox(matrixStackIn, builderPZ, bpz, 0, 0, 1, alpha);
		fillBoundingBox(matrixStackIn, builderNX, bnx, 1, 0, 0, alpha);
		fillBoundingBox(matrixStackIn, builderNY, bny, 0, 1, 0, alpha);
		fillBoundingBox(matrixStackIn, builderNZ, bnz, 0, 0, 1, alpha);

		float f1 = len - size / 3f;
		float f2 = len;
		float s1 = size / 3;
		float s2 = size;
		BoundingBox bpx1 = new BoundingBox(x,      y - s1, z - s1, x + f2, y + s1, z + s1).mul(m);
		BoundingBox bpy1 = new BoundingBox(x - s1, y,      z - s1, x + s1, y + f2, z + s1).mul(m);
		BoundingBox bpz1 = new BoundingBox(x - s1, y - s1, z,      x + s1, y + s1, z + f2).mul(m);
		BoundingBox bpx2 = new BoundingBox(x + f1, y - s2, z - s2, x + f2, y + s2, z + s2).mul(m);
		BoundingBox bpy2 = new BoundingBox(x - s2, y + f1, z - s2, x + s2, y + f2, z + s2).mul(m);
		BoundingBox bpz2 = new BoundingBox(x - s2, y - s2, z + f1, x + s2, y + s2, z + f2).mul(m);
		BoundingBox bnx1 = new BoundingBox(x,      y - s1, z - s1, x - f2, y + s1, z + s1).mul(m);
		BoundingBox bny1 = new BoundingBox(x - s1, y,      z - s1, x + s1, y - f2, z + s1).mul(m);
		BoundingBox bnz1 = new BoundingBox(x - s1, y - s1, z,      x + s1, y + s1, z - f2).mul(m);
		BoundingBox bnx2 = new BoundingBox(x - f1, y - s2, z - s2, x - f2, y + s2, z + s2).mul(m);
		BoundingBox bny2 = new BoundingBox(x - s2, y - f1, z - s2, x + s2, y - f2, z + s2).mul(m);
		BoundingBox bnz2 = new BoundingBox(x - s2, y - s2, z - f1, x + s2, y + s2, z - f2).mul(m);

		VertexBuffer[] pxb = boxConsumer.apply(EditorRenderer.BoundType.DRAG_X, builderPX);
		drawBoundingBox(matrixStackIn, bufferIn, bpx1, 0.5f, 0, 0, alpha);
		drawBoundingBox(matrixStackIn, bufferIn, bpx2, 0.5f, 0, 0, alpha);

		drawBoundingBox(matrixStackIn, pxb[1], bpx1, 1, 0, 0, alpha);
		drawBoundingBox(matrixStackIn, pxb[1], bpx2, 1, 0, 0, alpha);

		fillBoundingBox(matrixStackIn, pxb[0], bpx1, 1, 0, 0, alpha);
		fillBoundingBox(matrixStackIn, pxb[0], bpx2, 1, 0, 0, alpha);

		VertexBuffer[] pyb = boxConsumer.apply(EditorRenderer.BoundType.DRAG_Y, builderPY);
		drawBoundingBox(matrixStackIn, bufferIn, bpy1, 0, 0.5f, 0, alpha);
		drawBoundingBox(matrixStackIn, bufferIn, bpy2, 0, 0.5f, 0, alpha);

		drawBoundingBox(matrixStackIn, pyb[1], bpy1, 0, 1, 0, alpha);
		drawBoundingBox(matrixStackIn, pyb[1], bpy2, 0, 1, 0, alpha);

		fillBoundingBox(matrixStackIn, pyb[0], bpy1, 0, 1, 0, alpha);
		fillBoundingBox(matrixStackIn, pyb[0], bpy2, 0, 1, 0, alpha);

		VertexBuffer[] pzb = boxConsumer.apply(EditorRenderer.BoundType.DRAG_Z, builderPZ);
		drawBoundingBox(matrixStackIn, bufferIn, bpz1, 0, 0, 0.5f, alpha);
		drawBoundingBox(matrixStackIn, bufferIn, bpz2, 0, 0, 0.5f, alpha);

		drawBoundingBox(matrixStackIn, pzb[1], bpz1, 0, 0, 1, alpha);
		drawBoundingBox(matrixStackIn, pzb[1], bpz2, 0, 0, 1, alpha);

		fillBoundingBox(matrixStackIn, pzb[0], bpz1, 0, 0, 1, alpha);
		fillBoundingBox(matrixStackIn, pzb[0], bpz2, 0, 0, 1, alpha);

		VertexBuffer[] nxb = boxConsumer.apply(EditorRenderer.BoundType.DRAG_NX, builderNX);
		drawBoundingBox(matrixStackIn, bufferIn, bnx1, 0.5f, 0, 0, alpha);
		drawBoundingBox(matrixStackIn, bufferIn, bnx2, 0.5f, 0, 0, alpha);

		drawBoundingBox(matrixStackIn, nxb[1], bnx1, 1, 0, 0, alpha);
		drawBoundingBox(matrixStackIn, nxb[1], bnx2, 1, 0, 0, alpha);

		fillBoundingBox(matrixStackIn, nxb[0], bnx1, 1, 0, 0, alpha);
		fillBoundingBox(matrixStackIn, nxb[0], bnx2, 1, 0, 0, alpha);

		VertexBuffer[] nyb = boxConsumer.apply(EditorRenderer.BoundType.DRAG_NY, builderNY);
		drawBoundingBox(matrixStackIn, bufferIn, bny1, 0, 0.5f, 0, alpha);
		drawBoundingBox(matrixStackIn, bufferIn, bny2, 0, 0.5f, 0, alpha);

		drawBoundingBox(matrixStackIn, nyb[1], bny1, 0, 1, 0, alpha);
		drawBoundingBox(matrixStackIn, nyb[1], bny2, 0, 1, 0, alpha);

		fillBoundingBox(matrixStackIn, nyb[0], bny1, 0, 1, 0, alpha);
		fillBoundingBox(matrixStackIn, nyb[0], bny2, 0, 1, 0, alpha);

		VertexBuffer[] nzb = boxConsumer.apply(EditorRenderer.BoundType.DRAG_NZ, builderNZ);
		drawBoundingBox(matrixStackIn, bufferIn, bnz1, 0, 0, 0.5f, alpha);
		drawBoundingBox(matrixStackIn, bufferIn, bnz2, 0, 0, 0.5f, alpha);

		drawBoundingBox(matrixStackIn, nzb[1], bnz1, 0, 0, 1, alpha);
		drawBoundingBox(matrixStackIn, nzb[1], bnz2, 0, 0, 1, alpha);

		fillBoundingBox(matrixStackIn, nzb[0], bnz1, 0, 0, 1, alpha);
		fillBoundingBox(matrixStackIn, nzb[0], bnz2, 0, 0, 1, alpha);
	}

	public static BoxBuilder drawDragPane(MatrixStack matrixStackIn, VertexBuffer bufferIn, EditorRenderer.BoundType type, VecType v, RenderedCube rc, float size, Vec3f old) {
		Cube c = rc.getCube();
		switch (v) {
		case OFFSET:
		case SIZE:
			return drawDragPane(matrixStackIn, bufferIn, type, c.size.mul(1 / 2F), c.offset, size, true);

		case POSITION:
		{
			MatrixStack f = matrixStackIn.fork();
			f.pop();
			return drawDragPane(f, bufferIn, type, Vec3f.ZERO, rc.getTransformPosition(), size, true);
		}

		case ROTATION:
		{
			MatrixStack f = matrixStackIn.fork();
			f.pop();
			f.push();
			Vec3f pos = rc.getTransformPosition();
			ModelRenderManager.RedirectRenderer.translateRotate(pos.x, pos.y, pos.z, new Rotation(old, true), f);
			return drawRotationDragPane(f, bufferIn, type, Vec3f.ZERO, new Vec3f(), size, true);
		}

		case SCALE:
		case TEXTURE:
		default:
			break;
		}
		return TriangleBoundingBox.builder();
	}

	public static BoxBuilder drawDragPane(MatrixStack matrixStackIn, VertexBuffer bufferIn, EditorRenderer.BoundType type, Vec3f center, Vec3f value, float size, boolean addValue) {
		BoxBuilder b = TriangleBoundingBox.builder();
		Mat4f matrix4f = matrixStackIn.getLast().getMatrix();
		Mat3f matrix3f = matrixStackIn.getLast().getNormal();
		float x = center.x / 16f;
		float y = center.y / 16f;
		float z = center.z / 16f;
		float vx = value.x / 16f;
		float vy = value.y / 16f;
		float vz = value.z / 16f;
		float m = 4;
		float l = size / 16f;
		float s = size / 16f * m;
		float u = size * m;
		float v = size * m;
		switch (type) {
		case DRAG_X:
		case DRAG_NX:
			if(addValue) {
				y += vy;
				z += vz;
			}
			bufferIn.pos(matrix4f, x - l, y, z).color(1, 0, 0, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
			bufferIn.pos(matrix4f, x + l, y, z).color(1, 0, 0, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();

			b.pos(matrix4f, x - s, y + s, z).tex(-u, -v).endVertex();
			b.pos(matrix4f, x + s, y + s, z).tex( u,  v).endVertex();
			b.pos(matrix4f, x + s, y - s, z).tex( u,  v).endVertex();
			b.pos(matrix4f, x - s, y - s, z).tex(-u, -v).endVertex();

			b.pos(matrix4f, x - s, y, z + s).tex(-u, -v).endVertex();
			b.pos(matrix4f, x + s, y, z + s).tex( u,  v).endVertex();
			b.pos(matrix4f, x + s, y, z - s).tex( u,  v).endVertex();
			b.pos(matrix4f, x - s, y, z - s).tex(-u, -v).endVertex();
			break;

		case DRAG_Y:
		case DRAG_NY:
			if(addValue) {
				x += vx;
				z += vz;
			}
			bufferIn.pos(matrix4f, x, y - l, z).color(0, 1, 0, 1).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
			bufferIn.pos(matrix4f, x, y + l, z).color(0, 1, 0, 1).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();

			b.pos(matrix4f, x - s, y + s, z).tex( u,  v).endVertex();
			b.pos(matrix4f, x + s, y + s, z).tex( u,  v).endVertex();
			b.pos(matrix4f, x + s, y - s, z).tex(-u, -v).endVertex();
			b.pos(matrix4f, x - s, y - s, z).tex(-u, -v).endVertex();

			b.pos(matrix4f, x, y + s, z - s).tex( u,  v).endVertex();
			b.pos(matrix4f, x, y + s, z + s).tex( u,  v).endVertex();
			b.pos(matrix4f, x, y - s, z + s).tex(-u, -v).endVertex();
			b.pos(matrix4f, x, y - s, z - s).tex(-u, -v).endVertex();
			break;

		case DRAG_Z:
		case DRAG_NZ:
			if(addValue) {
				x += vx;
				y += vy;
			}
			bufferIn.pos(matrix4f, x, y, z - l).color(0, 0, 1, 1).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
			bufferIn.pos(matrix4f, x, y, z + l).color(0, 0, 1, 1).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();

			b.pos(matrix4f, x - s, y, z + s).tex( u,  v).endVertex();
			b.pos(matrix4f, x + s, y, z + s).tex( u,  v).endVertex();
			b.pos(matrix4f, x + s, y, z - s).tex(-u, -v).endVertex();
			b.pos(matrix4f, x - s, y, z - s).tex(-u, -v).endVertex();

			b.pos(matrix4f, x, y + s, z - s).tex(-u, -v).endVertex();
			b.pos(matrix4f, x, y + s, z + s).tex( u,  v).endVertex();
			b.pos(matrix4f, x, y - s, z + s).tex( u,  v).endVertex();
			b.pos(matrix4f, x, y - s, z - s).tex(-u, -v).endVertex();
			break;

		case CLICK:
		case DRAG_PANE:
		default:
			break;
		}
		return b;
	}

	public static BoxBuilder drawRotationDragPane(MatrixStack matrixStackIn, VertexBuffer bufferIn, EditorRenderer.BoundType type, Vec3f center, Vec3f value, float size, boolean addValue) {
		BoxBuilder b = TriangleBoundingBox.builder();
		Mat4f matrix4f = matrixStackIn.getLast().getMatrix();
		Mat3f matrix3f = matrixStackIn.getLast().getNormal();
		float x = center.x / 16f;
		float y = center.y / 16f;
		float z = center.z / 16f;
		float vx = value.x / 16f;
		float vy = value.y / 16f;
		float vz = value.z / 16f;
		float m = 4;
		float l = size / 16f;
		float s = size / 16f * m;
		float u = size * m;
		float v = size * m;
		switch (type) {
		case DRAG_X:
		case DRAG_NX:
			if(addValue) {
				y += vy;
				z += vz;
			}
			bufferIn.pos(matrix4f, x - l, y, z).color(1, 0, 0, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
			bufferIn.pos(matrix4f, x + l, y, z).color(1, 0, 0, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();

			b.pos(matrix4f, x, y + s, z - s).tex( u,  v).endVertex();
			b.pos(matrix4f, x, y + s, z + s).tex( u, -v).endVertex();
			b.pos(matrix4f, x, y - s, z + s).tex(-u, -v).endVertex();
			b.pos(matrix4f, x, y - s, z - s).tex(-u,  v).endVertex();
			break;

		case DRAG_Y:
		case DRAG_NY:
			if(addValue) {
				x += vx;
				z += vz;
			}
			bufferIn.pos(matrix4f, x, y - l, z).color(0, 1, 0, 1).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
			bufferIn.pos(matrix4f, x, y + l, z).color(0, 1, 0, 1).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();

			b.pos(matrix4f, x - s, y, z + s).tex(-u,  v).endVertex();
			b.pos(matrix4f, x + s, y, z + s).tex( u,  v).endVertex();
			b.pos(matrix4f, x + s, y, z - s).tex( u, -v).endVertex();
			b.pos(matrix4f, x - s, y, z - s).tex(-u, -v).endVertex();
			break;

		case DRAG_Z:
		case DRAG_NZ:
			if(addValue) {
				x += vx;
				y += vy;
			}
			bufferIn.pos(matrix4f, x, y, z - l).color(0, 0, 1, 1).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
			bufferIn.pos(matrix4f, x, y, z + l).color(0, 0, 1, 1).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();

			b.pos(matrix4f, x - s, y + s, z).tex( u, -v).endVertex();
			b.pos(matrix4f, x + s, y + s, z).tex( u,  v).endVertex();
			b.pos(matrix4f, x + s, y - s, z).tex(-u,  v).endVertex();
			b.pos(matrix4f, x - s, y - s, z).tex(-u, -v).endVertex();
			break;

		case CLICK:
		case DRAG_PANE:
		default:
			break;
		}
		return b;
	}

	public static void drawDrag(MatrixStack matrixStackIn, VertexBuffer buffer, VecType dragType, BiFunction<EditorRenderer.BoundType, BoxBuilder, VertexBuffer[]> boxConsumer, RenderedCube rc, Vec3f old, float sizeW, float sizeL, float alpha) {
		Cube c = rc.getCube();
		switch (dragType) {
		case OFFSET:
			drawDragArrows(matrixStackIn, buffer, boxConsumer,
					(c.offset.x + c.size.x / 2),
					(c.offset.y + c.size.y / 2),
					(c.offset.z + c.size.z / 2),
					sizeL, sizeW, alpha);
			break;

		case POSITION:
		{
			MatrixStack f = matrixStackIn.fork();
			f.pop();
			Vec3f pos = rc.getTransformPosition();
			drawDragArrows(f, buffer, boxConsumer, pos.x, pos.y, pos.z, sizeL, sizeW, alpha);
		}
		break;

		case ROTATION:
		{
			MatrixStack f = matrixStackIn.fork();
			f.pop();
			f.push();
			Vec3f pos = rc.getTransformPosition();
			Vec3f rot = rc.getCube().rotation;
			if(old == null)ModelRenderManager.RedirectRenderer.translateRotate(pos.x, pos.y, pos.z, new Rotation(rot, true), f);
			else ModelRenderManager.RedirectRenderer.translateRotate(pos.x, pos.y, pos.z, new Rotation(old, true), f);
			drawRotation(f, buffer, boxConsumer, rc, Vec3f.POSITIVE_X, EditorRenderer.BoundType.DRAG_X, sizeW, alpha);
			f.push();
			f.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
			drawRotation(f, buffer, boxConsumer, rc, Vec3f.POSITIVE_Z, EditorRenderer.BoundType.DRAG_Z, sizeW, alpha);
			f.pop();
			f.push();
			f.rotate(Vec3f.POSITIVE_Z.getDegreesQuaternion(90));
			drawRotation(f, buffer, boxConsumer, rc, Vec3f.POSITIVE_Y, EditorRenderer.BoundType.DRAG_Y, sizeW, alpha);
			f.pop();
			f.pop();
		}
		break;

		case SCALE:
			break;

		case SIZE:
			drawDragBoxes(matrixStackIn, buffer, boxConsumer,
					(c.offset.x + c.size.x / 2),
					(c.offset.y + c.size.y / 2),
					(c.offset.z + c.size.z / 2),
					sizeL, sizeW, alpha);
			break;

		case TEXTURE:
			break;
		default:
			break;
		}
	}

	private static void drawRotation(MatrixStack f, VertexBuffer buffer, BiFunction<BoundType, BoxBuilder, VertexBuffer[]> boxConsumer, RenderedCube rc, Vec3f color, EditorRenderer.BoundType type, float size, float alpha) {
		BoundingBox bbs = BoundingBox.create(-1 / 32f, -17 / 32f, -3 / 32f, 1 / 16f, 1 / 16f, 3 / 16f).mul(size);
		BoundingBox bbd = BoundingBox.create(-1 / 128f, -8 / 16f, -3 / 32f, 1 / 64f, 1 / 64f, 3 / 16f).mul(size);
		BoxBuilder b = TriangleBoundingBox.builder();
		for(int i = 0;i<18;i++) {
			f.push();
			f.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(i * 20));
			drawBoundingBox(f, buffer, bbd, color.x * 0.7f, color.y * 0.7f, color.z * 0.7f, alpha);
			fillBoundingBox(f, b, bbs, color.x, color.y, color.z, alpha);
			f.pop();
		}
		VertexBuffer[] xb = boxConsumer.apply(type, b);
		for(int i = 0;i<18;i++) {
			f.push();
			f.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(i * 20));
			drawBoundingBox(f, xb[1], bbd, color.x, color.y, color.z, alpha);
			fillBoundingBox(f, xb[0], bbd, color.x, color.y, color.z, alpha);
			f.pop();
		}
	}
}
