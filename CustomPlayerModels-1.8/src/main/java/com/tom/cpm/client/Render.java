package com.tom.cpm.client;

import static org.lwjgl.opengl.GL11.GL_QUADS;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import com.tom.cpm.shared.math.MathHelper;
import com.tom.cpm.shared.math.Vec3f;

public class Render {
	public static void drawCube(double x, double y, double z, double w, double h, double d){
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer t = tes.getWorldRenderer();
		t.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		t.pos(x + w, y, z).color(1f, 0f, 0f, 1f).endVertex();
		t.pos(x, y, z).color(1f, 0f, 0f, 1f).endVertex();
		t.pos(x, y + h, z).color(1f, 0f, 0f, 1f).endVertex();
		t.pos(x + w, y + h, z).color(1f, 0f, 0f, 1f).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		t.pos(x, y, z + d).color(1f, 0f, 0f, 1f).endVertex();
		t.pos(x + w, y, z + d).color(1f, 0f, 0f, 1f).endVertex();
		t.pos(x + w, y + h, z + d).color(1f, 0f, 0f, 1f).endVertex();
		t.pos(x, y + h, z + d).color(1f, 0f, 0f, 1f).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		t.pos(x + w, y, z + d).color(0f, 1f, 0f, 1f).endVertex();
		t.pos(x + w, y, z).color(0f, 1f, 0f, 1f).endVertex();
		t.pos(x + w, y + h, z).color(0f, 1f, 0f, 1f).endVertex();
		t.pos(x + w, y + h, z + d).color(0f, 1f, 0f, 1f).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		t.pos(x, y, z).color(0f, 1f, 0f, 1f).endVertex();
		t.pos(x, y, z + d).color(0f, 1f, 0f, 1f).endVertex();
		t.pos(x, y + h, z + d).color(0f, 1f, 0f, 1f).endVertex();
		t.pos(x, y + h, z).color(0f, 1f, 0f, 1f).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		t.pos(x + w, y, z).color(0f, 0f, 1f, 1f).endVertex();
		t.pos(x + w, y, z + d).color(0f, 0f, 1f, 1f).endVertex();
		t.pos(x, y, z + d).color(0f, 0f, 1f, 1f).endVertex();
		t.pos(x, y, z).color(0f, 0f, 1f, 1f).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		t.pos(x + w, y + h, z + d).color(0f, 0f, 1f, 1f).endVertex();
		t.pos(x + w, y + h, z).color(0f, 0f, 1f, 1f).endVertex();
		t.pos(x, y + h, z).color(0f, 0f, 1f, 1f).endVertex();
		t.pos(x, y + h, z + d).color(0f, 0f, 1f, 1f).endVertex();
		tes.draw();
	}

	public static void drawSingleColorCube(double x, double y, double z, double w, double h, double d, float delta){
		x = x - delta;
		y = y - delta;
		z = z - delta;
		w = w + delta;
		h = h + delta;
		d = d + delta;

		Tessellator tes = Tessellator.getInstance();
		WorldRenderer t = tes.getWorldRenderer();
		t.begin(GL_QUADS, DefaultVertexFormats.POSITION);
		t.pos(x + w, y, z).endVertex();
		t.pos(x, y, z).endVertex();
		t.pos(x, y + h, z).endVertex();
		t.pos(x + w, y + h, z).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION);
		t.pos(x, y, z + d).endVertex();
		t.pos(x + w, y, z + d).endVertex();
		t.pos(x + w, y + h, z + d).endVertex();
		t.pos(x, y + h, z + d).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION);
		t.pos(x + w, y, z + d).endVertex();
		t.pos(x + w, y, z).endVertex();
		t.pos(x + w, y + h, z).endVertex();
		t.pos(x + w, y + h, z + d).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION);
		t.pos(x, y, z).endVertex();
		t.pos(x, y, z + d).endVertex();
		t.pos(x, y + h, z + d).endVertex();
		t.pos(x, y + h, z).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION);
		t.pos(x + w, y, z).endVertex();
		t.pos(x + w, y, z + d).endVertex();
		t.pos(x, y, z + d).endVertex();
		t.pos(x, y, z).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION);
		t.pos(x + w, y + h, z + d).endVertex();
		t.pos(x + w, y + h, z).endVertex();
		t.pos(x, y + h, z).endVertex();
		t.pos(x, y + h, z + d).endVertex();
		tes.draw();
	}

	public static void drawCubeOutline(double x, double y, double z, double w, double h, double d, float r, float g, float b){
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer t = tes.getWorldRenderer();
		t.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

		t.pos(x, y, z).color(r, g, b, 1f).endVertex();
		t.pos(x, y + h, z).color(r, g, b, 1f).endVertex();

		t.pos(x, y, z).color(r, g, b, 1f).endVertex();
		t.pos(x + w, y, z).color(r, g, b, 1f).endVertex();

		t.pos(x, y, z).color(r, g, b, 1f).endVertex();
		t.pos(x, y , z + d).color(r, g, b, 1f).endVertex();

		t.pos(x, y + h, z).color(r, g, b, 1f).endVertex();
		t.pos(x + w, y + h, z).color(r, g, b, 1f).endVertex();

		t.pos(x, y + h, z).color(r, g, b, 1f).endVertex();
		t.pos(x, y + h, z + d).color(r, g, b, 1f).endVertex();

		t.pos(x + w, y, z).color(r, g, b, 1f).endVertex();
		t.pos(x + w, y + h, z).color(r, g, b, 1f).endVertex();

		t.pos(x + w, y, z).color(r, g, b, 1f).endVertex();
		t.pos(x + w, y, z + d).color(r, g, b, 1f).endVertex();

		t.pos(x + w, y, z + d).color(r, g, b, 1f).endVertex();
		t.pos(x + w, y + h, z + d).color(r, g, b, 1f).endVertex();

		t.pos(x + w, y + h, z).color(r, g, b, 1f).endVertex();
		t.pos(x + w, y + h, z + d).color(r, g, b, 1f).endVertex();

		t.pos(x, y + h, z + d).color(r, g, b, 1f).endVertex();
		t.pos(x + w, y + h, z + d).color(r, g, b, 1f).endVertex();

		t.pos(x, y, z + d).color(r, g, b, 1f).endVertex();
		t.pos(x + w, y, z + d).color(r, g, b, 1f).endVertex();

		t.pos(x, y, z + d).color(r, g, b, 1f).endVertex();
		t.pos(x, y + h, z + d).color(r, g, b, 1f).endVertex();

		tes.draw();
	}

	public static void drawTexturedCube(double x, double y, double z, double w, double h, double d){
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer t = tes.getWorldRenderer();
		t.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		t.pos(x + w, y, z).tex(1, 1).endVertex();
		t.pos(x, y, z).tex(0, 1).endVertex();
		t.pos(x, y + h, z).tex(0, 0).endVertex();
		t.pos(x + w, y + h, z).tex(1, 0).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		t.pos(x, y, z + d).tex(1, 1).endVertex();
		t.pos(x + w, y, z + d).tex(0, 1).endVertex();
		t.pos(x + w, y + h, z + d).tex(0, 0).endVertex();
		t.pos(x, y + h, z + d).tex(1, 0).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		t.pos(x + w, y, z + d).tex(1, 1).endVertex();
		t.pos(x + w, y, z).tex(0, 1).endVertex();
		t.pos(x + w, y + h, z).tex(0, 0).endVertex();
		t.pos(x + w, y + h, z + d).tex(1, 0).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		t.pos(x, y, z).tex(1, 1).endVertex();
		t.pos(x, y, z + d).tex(0, 1).endVertex();
		t.pos(x, y + h, z + d).tex(0, 0).endVertex();
		t.pos(x, y + h, z).tex(1, 0).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		t.pos(x + w, y, z).tex(1, 1).endVertex();
		t.pos(x + w, y, z + d).tex(0, 1).endVertex();
		t.pos(x, y, z + d).tex(0, 0).endVertex();
		t.pos(x, y, z).tex(1, 0).endVertex();
		tes.draw();

		t.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		t.pos(x + w, y + h, z + d).tex(1, 1).endVertex();
		t.pos(x + w, y + h, z).tex(0, 1).endVertex();
		t.pos(x, y + h, z).tex(0, 0).endVertex();
		t.pos(x, y + h, z + d).tex(1, 0).endVertex();
		tes.draw();
	}

	public static void drawTexturedCube(Vec3f pos, Vec3f size, Vec3f sc, float delta, int texU, int texV, int texSize, int sheetSizeX, int sheetSizeY, float scale) {
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

		PositionTextureVertex[] vertexPositions = new PositionTextureVertex[8];
		TexturedQuad[] quadList = new TexturedQuad[6];
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

		PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
		PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
		PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
		PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
		PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
		PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
		PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
		PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
		vertexPositions[0] = positiontexturevertex7;
		vertexPositions[1] = positiontexturevertex;
		vertexPositions[2] = positiontexturevertex1;
		vertexPositions[3] = positiontexturevertex2;
		vertexPositions[4] = positiontexturevertex3;
		vertexPositions[5] = positiontexturevertex4;
		vertexPositions[6] = positiontexturevertex5;
		vertexPositions[7] = positiontexturevertex6;
		quadList[0] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, texU + dz + dx, texV + dz, texU + dz + dx + dz, texV + dz + dy, sheetSizeX, sheetSizeY);
		quadList[1] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, texU, texV + dz, texU + dz, texV + dz + dy, sheetSizeX, sheetSizeY);
		quadList[2] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, texU + dz, texV, texU + dz + dx, texV + dz, sheetSizeX, sheetSizeY);
		quadList[3] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, texU + dz + dx, texV + dz, texU + dz + dx + dx, texV, sheetSizeX, sheetSizeY);
		quadList[4] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, texU + dz, texV + dz, texU + dz + dx, texV + dz + dy, sheetSizeX, sheetSizeY);
		quadList[5] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, texU + dz + dx + dz, texV + dz, texU + dz + dx + dz + dx, texV + dz + dy, sheetSizeX, sheetSizeY);

		if (texSize < 0)  {
			for (TexturedQuad texturedquad : quadList) {
				texturedquad.flipFace();
			}
		}

		WorldRenderer t = Tessellator.getInstance().getWorldRenderer();

		for (TexturedQuad texturedquad : quadList)  {
			texturedquad.draw(t, scale);
		}
	}

	public static void drawOrigin(float len) {
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer t = tes.getWorldRenderer();
		t.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

		t.pos(0, 0, 0).color(0f, 1, 0, 1).endVertex();
		t.pos(0, len, 0).color(0f, 1, 0, 1).endVertex();
		t.pos(0, 0, 0).color(1f, 0, 0, 1).endVertex();
		t.pos(len, 0, 0).color(1f, 0, 0, 1).endVertex();
		t.pos(0, 0, 0).color(0f, 0, 1, 1).endVertex();
		t.pos(0, 0, len).color(0f, 0, 1, 1).endVertex();

		tes.draw();
	}
}
