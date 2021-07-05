package com.tom.cpm.client;

import static org.lwjgl.opengl.GL11.GL_QUADS;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

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
}
