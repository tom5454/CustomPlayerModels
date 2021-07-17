package com.tom.cpm.shared.model.render;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.math.BoundingBox;
import com.tom.cpl.math.Mat3f;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpm.shared.model.render.PerFaceUV.Dir;
import com.tom.cpm.shared.model.render.PerFaceUV.Face;

public class BoxRender {

	public static class PositionTextureVertex {
		public final Vec3f position;
		public final float textureU;
		public final float textureV;

		public PositionTextureVertex(float x, float y, float z, float texU, float texV) {
			this(new Vec3f(x, y, z), texU, texV);
		}

		public PositionTextureVertex setTextureUV(float texU, float texV) {
			return new PositionTextureVertex(this.position, texU, texV);
		}

		public PositionTextureVertex(Vec3f posIn, float texU, float texV) {
			this.position = posIn;
			this.textureU = texU;
			this.textureV = texV;
		}
	}

	public static class TexBox implements Mesh {
		private TexturedQuad[] quadList;

		public TexBox(TexturedQuad[] quadList) {
			this.quadList = quadList;
		}

		@Override
		public void draw(MatrixStack matrixStackIn, VertexBuffer bufferIn, float red, float green, float blue, float alpha) {
			Mat4f matrix4f = matrixStackIn.getLast().getMatrix();
			Mat3f matrix3f = matrixStackIn.getLast().getNormal();

			for(TexturedQuad modelrenderer$texturedquad : quadList) {
				Vec3f vector3f = modelrenderer$texturedquad.normal.copy();
				vector3f.transform(matrix3f);
				float f = vector3f.x;
				float f1 = vector3f.y;
				float f2 = vector3f.z;

				for(int i = 0; i < 4; ++i) {
					PositionTextureVertex modelrenderer$positiontexturevertex = modelrenderer$texturedquad.vertexPositions[i];
					float f3 = modelrenderer$positiontexturevertex.position.x / 16.0F;
					float f4 = modelrenderer$positiontexturevertex.position.y / 16.0F;
					float f5 = modelrenderer$positiontexturevertex.position.z / 16.0F;
					Vec4f vector4f = new Vec4f(f3, f4, f5, 1.0F);
					vector4f.transform(matrix4f);
					bufferIn.addVertex(vector4f.x, vector4f.y, vector4f.z, red, green, blue, alpha, modelrenderer$positiontexturevertex.textureU, modelrenderer$positiontexturevertex.textureV, f, f1, f2);
				}
			}
		}

		@Override
		public RenderMode getLayer() {
			return RenderMode.NORMAL;
		}

		@Override
		public void free() {
		}
	}

	public static class TexturedQuad {
		public final PositionTextureVertex[] vertexPositions;
		public final Vec3f normal;

		public TexturedQuad(PositionTextureVertex[] positionsIn, Face f, float texWidth, float texHeight, Vec3f directionIn) {
			this.vertexPositions = positionsIn;
			for (int i = 0; i < positionsIn.length; i++) {
				positionsIn[i] = positionsIn[i].setTextureUV(f.getVertexU(i) / texWidth, f.getVertexV(i) /texHeight);
			}
			this.normal = new Vec3f(directionIn);
		}

		public TexturedQuad(PositionTextureVertex[] positionsIn, float u1, float v1, float u2, float v2, float texWidth, float texHeight, boolean mirrorIn, Vec3f directionIn) {
			this.vertexPositions = positionsIn;
			positionsIn[0] = positionsIn[0].setTextureUV(u2 / texWidth, v1 / texHeight);
			positionsIn[1] = positionsIn[1].setTextureUV(u1 / texWidth, v1 / texHeight);
			positionsIn[2] = positionsIn[2].setTextureUV(u1 / texWidth, v2 / texHeight);
			positionsIn[3] = positionsIn[3].setTextureUV(u2 / texWidth, v2 / texHeight);
			if (mirrorIn) {
				int i = positionsIn.length;

				for(int j = 0; j < i / 2; ++j) {
					PositionTextureVertex modelrenderer$positiontexturevertex = positionsIn[j];
					positionsIn[j] = positionsIn[i - 1 - j];
					positionsIn[i - 1 - j] = modelrenderer$positiontexturevertex;
				}
			}

			this.normal = new Vec3f(directionIn);
			if (mirrorIn) {
				this.normal.mul(-1.0F, 1.0F, 1.0F);
			}
		}
	}

	public static Mesh createTexturedSingle(Vec3f pos, Vec3f size, Vec3f sc, float delta, int texU, int texV, int texSize, int sheetSizeX, int sheetSizeY) {
		float x = pos.x;
		float y = pos.y;
		float z = pos.z;
		float w = size.x;
		float h = size.y;
		float d = size.z;

		int ts = Math.abs(texSize);
		int dx = MathHelper.ceil(w * ts);
		int dy = MathHelper.ceil(h * ts);
		int dz = MathHelper.ceil(d * ts);

		float ex = x + w * sc.x;
		float ey = y + h * sc.y;
		float ez = z + d * sc.z;

		x = x - delta;
		y = y - delta;
		z = z - delta;
		ex = ex + delta;
		ey = ey + delta;
		ez = ez + delta;

		texU *= ts;
		texV *= ts;

		if (texSize < 0) {
			float f3 = ex;
			ex = x;
			x = f3;
		}

		if(ex == x || ey == y || ez == z) {
			TexturedQuad[] quadList = new TexturedQuad[2];
			if(ex == x) {
				ex += 0.001f;
				PositionTextureVertex modelrenderer$positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex = new PositionTextureVertex(ex, y, z, 0.0F, 8.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex1 = new PositionTextureVertex(ex, ey, z, 8.0F, 8.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex2 = new PositionTextureVertex(x, ey, z, 8.0F, 0.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex3 = new PositionTextureVertex(x, y, ez, 0.0F, 0.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex4 = new PositionTextureVertex(ex, y, ez, 0.0F, 8.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex5 = new PositionTextureVertex(ex, ey, ez, 8.0F, 8.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex6 = new PositionTextureVertex(x, ey, ez, 8.0F, 0.0F);
				float tu = texU + dz;
				float tv = texV + dy;
				quadList[1] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex2}, texU, texV, tu, tv, sheetSizeX, sheetSizeY, texSize < 0, Vec3f.NEGATIVE_X);
				quadList[0] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex5}, tu, texV, texU, tv, sheetSizeX, sheetSizeY, texSize < 0,  Vec3f.POSITIVE_X);
			} else if(ey == y) {
				ey += 0.001f;
				PositionTextureVertex modelrenderer$positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex = new PositionTextureVertex(ex, y, z, 0.0F, 8.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex1 = new PositionTextureVertex(ex, ey, z, 8.0F, 8.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex2 = new PositionTextureVertex(x, ey, z, 8.0F, 0.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex3 = new PositionTextureVertex(x, y, ez, 0.0F, 0.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex4 = new PositionTextureVertex(ex, y, ez, 0.0F, 8.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex5 = new PositionTextureVertex(ex, ey, ez, 8.0F, 8.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex6 = new PositionTextureVertex(x, ey, ez, 8.0F, 0.0F);
				float tu = texU + dx;
				float tv = texV + dz;
				quadList[0] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex}, texU, texV, tu, tv, sheetSizeX, sheetSizeY, texSize < 0,  Vec3f.NEGATIVE_Y);
				quadList[1] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex5}, tu, texV, texU, tv, sheetSizeX, sheetSizeY, texSize < 0, Vec3f.POSITIVE_Y);
			} else if(ez == z) {
				ez += 0.001f;
				PositionTextureVertex modelrenderer$positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex = new PositionTextureVertex(ex, y, z, 0.0F, 8.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex1 = new PositionTextureVertex(ex, ey, z, 8.0F, 8.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex2 = new PositionTextureVertex(x, ey, z, 8.0F, 0.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex3 = new PositionTextureVertex(x, y, ez, 0.0F, 0.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex4 = new PositionTextureVertex(ex, y, ez, 0.0F, 8.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex5 = new PositionTextureVertex(ex, ey, ez, 8.0F, 8.0F);
				PositionTextureVertex modelrenderer$positiontexturevertex6 = new PositionTextureVertex(x, ey, ez, 8.0F, 0.0F);
				float tu = texU + dx;
				float tv = texV + dy;
				quadList[0] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex1}, texU, texV, tu, tv, sheetSizeX, sheetSizeY, texSize < 0,  Vec3f.NEGATIVE_Z);
				quadList[1] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex5, modelrenderer$positiontexturevertex6}, tu, texV, texU, tv, sheetSizeX, sheetSizeY, texSize < 0, Vec3f.POSITIVE_Z);
			}
			if(quadList[0] != null)
				return new TexBox(quadList);
		}

		TexturedQuad[] quadList = new TexturedQuad[6];

		PositionTextureVertex modelrenderer$positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
		PositionTextureVertex modelrenderer$positiontexturevertex = new PositionTextureVertex(ex, y, z, 0.0F, 8.0F);
		PositionTextureVertex modelrenderer$positiontexturevertex1 = new PositionTextureVertex(ex, ey, z, 8.0F, 8.0F);
		PositionTextureVertex modelrenderer$positiontexturevertex2 = new PositionTextureVertex(x, ey, z, 8.0F, 0.0F);
		PositionTextureVertex modelrenderer$positiontexturevertex3 = new PositionTextureVertex(x, y, ez, 0.0F, 0.0F);
		PositionTextureVertex modelrenderer$positiontexturevertex4 = new PositionTextureVertex(ex, y, ez, 0.0F, 8.0F);
		PositionTextureVertex modelrenderer$positiontexturevertex5 = new PositionTextureVertex(ex, ey, ez, 8.0F, 8.0F);
		PositionTextureVertex modelrenderer$positiontexturevertex6 = new PositionTextureVertex(x, ey, ez, 8.0F, 0.0F);
		int txS = Math.max(dx, Math.max(dy, dz));
		float tu = texU + txS;
		float tv = texV + txS;
		quadList[2] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex}, texU, texV, tu, tv, sheetSizeX, sheetSizeY, texSize < 0,  Vec3f.NEGATIVE_Y);
		quadList[3] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex5}, texU, texV, tu, tv, sheetSizeX, sheetSizeY, texSize < 0, Vec3f.POSITIVE_Y);
		quadList[1] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex2}, texU, texV, tu, tv, sheetSizeX, sheetSizeY, texSize < 0, Vec3f.NEGATIVE_X);
		quadList[4] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex1}, texU, texV, tu, tv, sheetSizeX, sheetSizeY, texSize < 0,  Vec3f.NEGATIVE_Z);
		quadList[0] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex5}, texU, texV, tu, tv, sheetSizeX, sheetSizeY, texSize < 0,  Vec3f.POSITIVE_X);
		quadList[5] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex5, modelrenderer$positiontexturevertex6}, texU, texV, tu, tv, sheetSizeX, sheetSizeY, texSize < 0, Vec3f.POSITIVE_Z);

		return new TexBox(quadList);
	}

	public static Mesh createTextured(Vec3f pos, Vec3f size, Vec3f sc, float delta, int texU, int texV, int texSize, int sheetSizeX, int sheetSizeY) {
		TexturedQuad[] quadList = new TexturedQuad[6];
		{
			float x = pos.x;
			float y = pos.y;
			float z = pos.z;
			float w = size.x;
			float h = size.y;
			float d = size.z;

			int ts = Math.abs(texSize);
			int dx = MathHelper.ceil(w * ts);
			int dy = MathHelper.ceil(h * ts);
			int dz = MathHelper.ceil(d * ts);

			float f = x + w * sc.x;
			float f1 = y + h * sc.y;
			float f2 = z + d * sc.z;

			x = x - delta;
			y = y - delta;
			z = z - delta;
			f = f + delta;
			f1 = f1 + delta;
			f2 = f2 + delta;

			texU *= ts;
			texV *= ts;

			if (texSize < 0) {
				float f3 = f;
				f = x;
				x = f3;
			}

			PositionTextureVertex modelrenderer$positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
			float f4 = texU;
			float f5 = (float)texU + dz;
			float f6 = (float)texU + dz + dx;
			float f7 = (float)texU + dz + dx + dx;
			float f8 = (float)texU + dz + dx + dz;
			float f9 = (float)texU + dz + dx + dz + dx;
			float f10 = texV;
			float f11 = (float)texV + dz;
			float f12 = (float)texV + dz + dy;
			quadList[2] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex}, f5, f10, f6, f11, sheetSizeX, sheetSizeY, texSize < 0,  Vec3f.NEGATIVE_Y);
			quadList[3] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex5}, f6, f11, f7, f10, sheetSizeX, sheetSizeY, texSize < 0, Vec3f.POSITIVE_Y);
			quadList[1] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex2}, f4, f11, f5, f12, sheetSizeX, sheetSizeY, texSize < 0, Vec3f.NEGATIVE_X);
			quadList[4] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex1}, f5, f11, f6, f12, sheetSizeX, sheetSizeY, texSize < 0,  Vec3f.NEGATIVE_Z);
			quadList[0] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex5}, f6, f11, f8, f12, sheetSizeX, sheetSizeY, texSize < 0,  Vec3f.POSITIVE_X);
			quadList[5] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex5, modelrenderer$positiontexturevertex6}, f8, f11, f9, f12, sheetSizeX, sheetSizeY, texSize < 0, Vec3f.POSITIVE_Z);
		}

		return new TexBox(quadList);
	}

	public static Mesh createTextured(Vec3f pos, Vec3f size, Vec3f sc, float delta, PerFaceUV uv, int texSize, int sheetSizeX, int sheetSizeY) {
		List<TexturedQuad> quadList = new ArrayList<>();
		{
			float x = pos.x;
			float y = pos.y;
			float z = pos.z;
			float w = size.x;
			float h = size.y;
			float d = size.z;

			float f = x + w * sc.x;
			float f1 = y + h * sc.y;
			float f2 = z + d * sc.z;

			x = x - delta;
			y = y - delta;
			z = z - delta;
			f = f + delta;
			f1 = f1 + delta;
			f2 = f2 + delta;

			PositionTextureVertex ptv7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
			PositionTextureVertex ptv = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
			PositionTextureVertex ptv1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
			PositionTextureVertex ptv2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
			PositionTextureVertex ptv3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
			PositionTextureVertex ptv4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
			PositionTextureVertex ptv5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
			PositionTextureVertex ptv6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
			if(uv.contains(Dir.EAST ))quadList.add(new TexturedQuad(new PositionTextureVertex[]{ptv4, ptv, ptv1, ptv5},  uv.get(Dir.EAST ), sheetSizeX, sheetSizeY, Vec3f.POSITIVE_X));
			if(uv.contains(Dir.WEST ))quadList.add(new TexturedQuad(new PositionTextureVertex[]{ptv7, ptv3, ptv6, ptv2}, uv.get(Dir.WEST ), sheetSizeX, sheetSizeY, Vec3f.NEGATIVE_X));
			if(uv.contains(Dir.UP   ))quadList.add(new TexturedQuad(new PositionTextureVertex[]{ptv4, ptv3, ptv7, ptv},  uv.get(Dir.UP   ), sheetSizeX, sheetSizeY, Vec3f.NEGATIVE_Y));
			if(uv.contains(Dir.DOWN ))quadList.add(new TexturedQuad(new PositionTextureVertex[]{ptv1, ptv2, ptv6, ptv5}, uv.get(Dir.DOWN ), sheetSizeX, sheetSizeY, Vec3f.POSITIVE_Y));
			if(uv.contains(Dir.NORTH))quadList.add(new TexturedQuad(new PositionTextureVertex[]{ptv, ptv7, ptv2, ptv1},  uv.get(Dir.NORTH), sheetSizeX, sheetSizeY, Vec3f.NEGATIVE_Z));
			if(uv.contains(Dir.SOUTH))quadList.add(new TexturedQuad(new PositionTextureVertex[]{ptv3, ptv4, ptv5, ptv6}, uv.get(Dir.SOUTH), sheetSizeX, sheetSizeY, Vec3f.POSITIVE_Z));
		}

		return new TexBox(quadList.toArray(new TexturedQuad[0]));
	}

	public static Mesh createColored(float x, float y, float z, float w, float h, float d, float delta, int sheetSizeX, int sheetSizeY) {
		TexturedQuad[] quadList = new TexturedQuad[6];
		{
			float f = x + w;
			float f1 = y + h;
			float f2 = z + d;

			x = x - delta;
			y = y - delta;
			z = z - delta;
			f = f + delta;
			f1 = f1 + delta;
			f2 = f2 + delta;

			PositionTextureVertex modelrenderer$positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
			float f4 = 0;
			float f5 = 1;
			float f6 = 1;
			float f7 = 1;
			float f8 = 1;
			float f9 = 1;
			float f10 = 0;
			float f11 = 1;
			float f12 = 1;
			quadList[2] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex}, f5, f10, f6, f11, sheetSizeX, sheetSizeY, false,  Vec3f.NEGATIVE_Y);
			quadList[3] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex5}, f6, f11, f7, f10, sheetSizeX, sheetSizeY, false, Vec3f.POSITIVE_Y);
			quadList[1] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex2}, f4, f11, f5, f12, sheetSizeX, sheetSizeY, false, Vec3f.NEGATIVE_X);
			quadList[4] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex1}, f5, f11, f6, f12, sheetSizeX, sheetSizeY, false,  Vec3f.NEGATIVE_Z);
			quadList[0] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex5}, f6, f11, f8, f12, sheetSizeX, sheetSizeY, false,  Vec3f.POSITIVE_X);
			quadList[5] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex5, modelrenderer$positiontexturevertex6}, f8, f11, f9, f12, sheetSizeX, sheetSizeY, false, Vec3f.POSITIVE_Z);
		}

		return new ColorBox(quadList);
	}

	public static class ColorBox implements Mesh {
		private TexturedQuad[] quadList;

		public ColorBox(TexturedQuad[] quadList) {
			this.quadList = quadList;
		}

		@Override
		public void draw(MatrixStack matrixStackIn, VertexBuffer bufferIn, float red, float green, float blue, float alpha) {
			Mat4f matrix4f = matrixStackIn.getLast().getMatrix();
			Mat3f matrix3f = matrixStackIn.getLast().getNormal();

			for(TexturedQuad modelrenderer$texturedquad : quadList) {
				Vec3f vector3f = modelrenderer$texturedquad.normal.copy();
				vector3f.transform(matrix3f);
				float f = vector3f.x;
				float f1 = vector3f.y;
				float f2 = vector3f.z;

				for(int i = 0; i < 4; ++i) {
					PositionTextureVertex modelrenderer$positiontexturevertex = modelrenderer$texturedquad.vertexPositions[i];
					float f3 = modelrenderer$positiontexturevertex.position.x / 16.0F;
					float f4 = modelrenderer$positiontexturevertex.position.y / 16.0F;
					float f5 = modelrenderer$positiontexturevertex.position.z / 16.0F;
					Vec4f vector4f = new Vec4f(f3, f4, f5, 1.0F);
					vector4f.transform(matrix4f);
					bufferIn.addVertex(vector4f.x, vector4f.y, vector4f.z, red, green, blue, alpha, modelrenderer$positiontexturevertex.textureU, modelrenderer$positiontexturevertex.textureV, f, f1, f2);
				}
			}
		}

		@Override
		public RenderMode getLayer() {
			return RenderMode.COLOR;
		}

		@Override
		public void free() {
		}
	}

	public static void drawOrigin(MatrixStack matrixStackIn, VertexBuffer bufferIn, float len) {
		Mat4f matrix4f = matrixStackIn.getLast().getMatrix();
		Mat3f matrix3f = matrixStackIn.getLast().getNormal();
		bufferIn.pos(matrix4f, 0, 0, 0).color(0f, 1, 0, 1).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, 0, len, 0).color(0f, 1, 0, 1).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, 0, 0, 0).color(1f, 0, 0, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, len, 0, 0).color(1f, 0, 0, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, 0, 0, 0).color(0f, 0, 1, 1).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
		bufferIn.pos(matrix4f, 0, 0, len).color(0f, 0, 1, 1).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
	}

	public static void drawBoundingBox(MatrixStack matrixStackIn, VertexBuffer bufferIn, BoundingBox bb, float red, float green, float blue, float alpha) {
		Mat4f matrix4f = matrixStackIn.getLast().getMatrix();
		Mat3f matrix3f = matrixStackIn.getLast().getNormal();
		float sx = bb.minX;
		float sy = bb.minY;
		float sz = bb.minZ;
		float ex = bb.maxX;
		float ey = bb.maxY;
		float ez = bb.maxZ;
		bufferIn.pos(matrix4f, sx, sy, sz).color(red, green, blue, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, ex, sy, sz).color(red, green, blue, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, sx, sy, sz).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, sx, ey, sz).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, sx, sy, sz).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
		bufferIn.pos(matrix4f, sx, sy, ez).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
		bufferIn.pos(matrix4f, ex, sy, sz).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, ex, ey, sz).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, ex, ey, sz).color(red, green, blue, alpha).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, sx, ey, sz).color(red, green, blue, alpha).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, sx, ey, sz).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
		bufferIn.pos(matrix4f, sx, ey, ez).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
		bufferIn.pos(matrix4f, sx, ey, ez).color(red, green, blue, alpha).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, sx, sy, ez).color(red, green, blue, alpha).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, sx, sy, ez).color(red, green, blue, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, ex, sy, ez).color(red, green, blue, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, ex, sy, ez).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
		bufferIn.pos(matrix4f, ex, sy, sz).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
		bufferIn.pos(matrix4f, sx, ey, ez).color(red, green, blue, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, ex, ey, ez).color(red, green, blue, alpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, ex, sy, ez).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, ex, ey, ez).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		bufferIn.pos(matrix4f, ex, ey, sz).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
		bufferIn.pos(matrix4f, ex, ey, ez).color(red, green, blue, alpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
	}

}
