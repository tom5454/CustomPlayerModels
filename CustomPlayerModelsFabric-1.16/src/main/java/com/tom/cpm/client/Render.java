package com.tom.cpm.client;

import static org.lwjgl.opengl.GL11.GL_QUADS;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

import com.tom.cpm.shared.math.Vec3f;

public class Render {

	public static void drawTexturedCube(MatrixStack mst, float x, float y, float z, float w, float h, float d){
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder t = tes.getBuffer();
		Matrix4f m = mst.peek().getModel();
		t.begin(GL_QUADS, VertexFormats.POSITION_TEXTURE);
		t.vertex(m, x + w, y, z).texture(1, 1).next();
		t.vertex(m, x, y, z).texture(0, 1).next();
		t.vertex(m, x, y + h, z).texture(0, 0).next();
		t.vertex(m, x + w, y + h, z).texture(1, 0).next();
		tes.draw();

		t.begin(GL_QUADS, VertexFormats.POSITION_TEXTURE);
		t.vertex(m, x, y, z + d).texture(1, 1).next();
		t.vertex(m, x + w, y, z + d).texture(0, 1).next();
		t.vertex(m, x + w, y + h, z + d).texture(0, 0).next();
		t.vertex(m, x, y + h, z + d).texture(1, 0).next();
		tes.draw();

		t.begin(GL_QUADS, VertexFormats.POSITION_TEXTURE);
		t.vertex(m, x + w, y, z + d).texture(1, 1).next();
		t.vertex(m, x + w, y, z).texture(0, 1).next();
		t.vertex(m, x + w, y + h, z).texture(0, 0).next();
		t.vertex(m, x + w, y + h, z + d).texture(1, 0).next();
		tes.draw();

		t.begin(GL_QUADS, VertexFormats.POSITION_TEXTURE);
		t.vertex(m, x, y, z).texture(1, 1).next();
		t.vertex(m, x, y, z + d).texture(0, 1).next();
		t.vertex(m, x, y + h, z + d).texture(0, 0).next();
		t.vertex(m, x, y + h, z).texture(1, 0).next();
		tes.draw();

		t.begin(GL_QUADS, VertexFormats.POSITION_TEXTURE);
		t.vertex(m, x + w, y, z).texture(1, 1).next();
		t.vertex(m, x + w, y, z + d).texture(0, 1).next();
		t.vertex(m, x, y, z + d).texture(0, 0).next();
		t.vertex(m, x, y, z).texture(1, 0).next();
		tes.draw();

		t.begin(GL_QUADS, VertexFormats.POSITION_TEXTURE);
		t.vertex(m, x + w, y + h, z + d).texture(1, 1).next();
		t.vertex(m, x + w, y + h, z).texture(0, 1).next();
		t.vertex(m, x, y + h, z).texture(0, 0).next();
		t.vertex(m, x, y + h, z + d).texture(1, 0).next();
		tes.draw();
	}

	public static void drawBoundingBox(MatrixStack matrixStackIn, VertexConsumer buffer, float x, float y, float z,
			float w, float h, float d, float r, float g, float b, float a) {
		WorldRenderer.drawBox(matrixStackIn, buffer, x, y, z, x+w, y+h, z+d, r, g, b, a);
	}

	public static Box createColored(float x, float y, float z, float w, float h, float d, float delta, int sheetSizeX, int sheetSizeY) {
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
			quadList[2] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex}, f5, f10, f6, f11, sheetSizeX, sheetSizeY, false, Direction.DOWN);
			quadList[3] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex5}, f6, f11, f7, f10, sheetSizeX, sheetSizeY, false, Direction.UP);
			quadList[1] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex2}, f4, f11, f5, f12, sheetSizeX, sheetSizeY, false, Direction.WEST);
			quadList[4] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex1}, f5, f11, f6, f12, sheetSizeX, sheetSizeY, false, Direction.NORTH);
			quadList[0] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex5}, f6, f11, f8, f12, sheetSizeX, sheetSizeY, false, Direction.EAST);
			quadList[5] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex5, modelrenderer$positiontexturevertex6}, f8, f11, f9, f12, sheetSizeX, sheetSizeY, false, Direction.SOUTH);
		}

		return new ColorBox(quadList);
	}

	public static class ColorBox implements Box {
		private TexturedQuad[] quadList;

		public ColorBox(TexturedQuad[] quadList) {
			this.quadList = quadList;
		}

		@Override
		public void draw(MatrixStack matrixStackIn, VertexConsumer bufferIn, VertexConsumerProvider buf, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
			Matrix4f matrix4f = matrixStackIn.peek().getModel();
			Matrix3f matrix3f = matrixStackIn.peek().getNormal();

			VertexConsumer buffer = buf.getBuffer(CustomRenderTypes.getEntityColorTranslucentCull());

			for(TexturedQuad modelrenderer$texturedquad : quadList) {
				Vector3f vector3f = modelrenderer$texturedquad.normal.copy();
				vector3f.transform(matrix3f);
				float f = vector3f.getX();
				float f1 = vector3f.getY();
				float f2 = vector3f.getZ();

				for(int i = 0; i < 4; ++i) {
					PositionTextureVertex modelrenderer$positiontexturevertex = modelrenderer$texturedquad.vertexPositions[i];
					float f3 = modelrenderer$positiontexturevertex.position.getX() / 16.0F;
					float f4 = modelrenderer$positiontexturevertex.position.getY() / 16.0F;
					float f5 = modelrenderer$positiontexturevertex.position.getZ() / 16.0F;
					Vector4f vector4f = new Vector4f(f3, f4, f5, 1.0F);
					vector4f.transform(matrix4f);
					buffer.vertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), red, green, blue, alpha, modelrenderer$positiontexturevertex.textureU, modelrenderer$positiontexturevertex.textureV, packedOverlayIn, packedLightIn, f, f1, f2);
				}
			}
		}
	}

	public static Box createTextured(Vec3f pos, Vec3f size, Vec3f sc, float delta, int texU, int texV, int texSize, int sheetSizeX, int sheetSizeY) {
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
			quadList[2] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex}, f5, f10, f6, f11, sheetSizeX, sheetSizeY, texSize < 0, Direction.DOWN);
			quadList[3] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex5}, f6, f11, f7, f10, sheetSizeX, sheetSizeY, texSize < 0, Direction.UP);
			quadList[1] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex2}, f4, f11, f5, f12, sheetSizeX, sheetSizeY, texSize < 0, Direction.WEST);
			quadList[4] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex1}, f5, f11, f6, f12, sheetSizeX, sheetSizeY, texSize < 0, Direction.NORTH);
			quadList[0] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex5}, f6, f11, f8, f12, sheetSizeX, sheetSizeY, texSize < 0, Direction.EAST);
			quadList[5] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex5, modelrenderer$positiontexturevertex6}, f8, f11, f9, f12, sheetSizeX, sheetSizeY, texSize < 0, Direction.SOUTH);
		}

		return new TexBox(quadList);
	}

	public static interface Box {
		public void draw(MatrixStack matrixStackIn, VertexConsumer bufferIn, VertexConsumerProvider buf, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha);
	}
	public static class TexBox implements Box {
		private TexturedQuad[] quadList;

		public TexBox(TexturedQuad[] quadList) {
			this.quadList = quadList;
		}

		@Override
		public void draw(MatrixStack matrixStackIn, VertexConsumer bufferIn, VertexConsumerProvider buf, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
			Matrix4f matrix4f = matrixStackIn.peek().getModel();
			Matrix3f matrix3f = matrixStackIn.peek().getNormal();

			for(TexturedQuad modelrenderer$texturedquad : quadList) {
				Vector3f vector3f = modelrenderer$texturedquad.normal.copy();
				vector3f.transform(matrix3f);
				float f = vector3f.getX();
				float f1 = vector3f.getY();
				float f2 = vector3f.getZ();

				for(int i = 0; i < 4; ++i) {
					PositionTextureVertex modelrenderer$positiontexturevertex = modelrenderer$texturedquad.vertexPositions[i];
					float f3 = modelrenderer$positiontexturevertex.position.getX() / 16.0F;
					float f4 = modelrenderer$positiontexturevertex.position.getY() / 16.0F;
					float f5 = modelrenderer$positiontexturevertex.position.getZ() / 16.0F;
					Vector4f vector4f = new Vector4f(f3, f4, f5, 1.0F);
					vector4f.transform(matrix4f);
					bufferIn.vertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), red, green, blue, alpha, modelrenderer$positiontexturevertex.textureU, modelrenderer$positiontexturevertex.textureV, packedOverlayIn, packedLightIn, f, f1, f2);
				}
			}
		}
	}

	static class PositionTextureVertex {
		public final Vector3f position;
		public final float textureU;
		public final float textureV;

		public PositionTextureVertex(float x, float y, float z, float texU, float texV) {
			this(new Vector3f(x, y, z), texU, texV);
		}

		public PositionTextureVertex setTextureUV(float texU, float texV) {
			return new PositionTextureVertex(this.position, texU, texV);
		}

		public PositionTextureVertex(Vector3f posIn, float texU, float texV) {
			this.position = posIn;
			this.textureU = texU;
			this.textureV = texV;
		}
	}

	static class TexturedQuad {
		public final PositionTextureVertex[] vertexPositions;
		public final Vector3f normal;

		public TexturedQuad(PositionTextureVertex[] positionsIn, float u1, float v1, float u2, float v2, float texWidth, float texHeight, boolean mirrorIn, Direction directionIn) {
			this.vertexPositions = positionsIn;
			float f = 0.0F / texWidth;
			float f1 = 0.0F / texHeight;
			positionsIn[0] = positionsIn[0].setTextureUV(u2 / texWidth - f, v1 / texHeight + f1);
			positionsIn[1] = positionsIn[1].setTextureUV(u1 / texWidth + f, v1 / texHeight + f1);
			positionsIn[2] = positionsIn[2].setTextureUV(u1 / texWidth + f, v2 / texHeight - f1);
			positionsIn[3] = positionsIn[3].setTextureUV(u2 / texWidth - f, v2 / texHeight - f1);
			if (mirrorIn) {
				int i = positionsIn.length;

				for(int j = 0; j < i / 2; ++j) {
					PositionTextureVertex modelrenderer$positiontexturevertex = positionsIn[j];
					positionsIn[j] = positionsIn[i - 1 - j];
					positionsIn[i - 1 - j] = modelrenderer$positiontexturevertex;
				}
			}

			this.normal = directionIn.getUnitVector();
			if (mirrorIn) {
				this.normal.multiplyComponentwise(-1.0F, 1.0F, 1.0F);
			}

		}
	}

	public static void drawOrigin(MatrixStack matrixStackIn, VertexConsumer bufferIn, float len) {
		Matrix4f matrix4f = matrixStackIn.peek().getModel();
		bufferIn.vertex(matrix4f, 0, 0, 0).color(0f, 1, 0, 1).next();
		bufferIn.vertex(matrix4f, 0, len, 0).color(0f, 1, 0, 1).next();
		bufferIn.vertex(matrix4f, 0, 0, 0).color(1f, 0, 0, 1).next();
		bufferIn.vertex(matrix4f, len, 0, 0).color(1f, 0, 0, 1).next();
		bufferIn.vertex(matrix4f, 0, 0, 0).color(0f, 0, 1, 1).next();
		bufferIn.vertex(matrix4f, 0, 0, len).color(0f, 0, 1, 1).next();
	}
}
