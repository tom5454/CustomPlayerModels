package com.tom.cpm.client;

import static org.lwjgl.opengl.GL11.GL_QUADS;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class Render {

	public static void drawTexturedCube(double x, double y, double z, double w, double h, double d){
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder t = tes.getBuffer();
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
}
