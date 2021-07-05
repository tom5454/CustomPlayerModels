package com.tom.cpm.client;

import static org.lwjgl.opengl.GL11.GL_QUADS;

import com.tom.cpm.client.RetroGL.RetroTessellator;

public class Render {

	public static void drawTexturedCube(double x, double y, double z, double w, double h, double d){
		RetroTessellator t = RetroGL.tessellator;

		t.begin(GL_QUADS);
		t.pos(x + w, y, z).tex(1, 1).endVertex();
		t.pos(x, y, z).tex(0, 1).endVertex();
		t.pos(x, y + h, z).tex(0, 0).endVertex();
		t.pos(x + w, y + h, z).tex(1, 0).endVertex();
		t.draw();

		t.begin(GL_QUADS);
		t.pos(x, y, z + d).tex(1, 1).endVertex();
		t.pos(x + w, y, z + d).tex(0, 1).endVertex();
		t.pos(x + w, y + h, z + d).tex(0, 0).endVertex();
		t.pos(x, y + h, z + d).tex(1, 0).endVertex();
		t.draw();

		t.begin(GL_QUADS);
		t.pos(x + w, y, z + d).tex(1, 1).endVertex();
		t.pos(x + w, y, z).tex(0, 1).endVertex();
		t.pos(x + w, y + h, z).tex(0, 0).endVertex();
		t.pos(x + w, y + h, z + d).tex(1, 0).endVertex();
		t.draw();

		t.begin(GL_QUADS);
		t.pos(x, y, z).tex(1, 1).endVertex();
		t.pos(x, y, z + d).tex(0, 1).endVertex();
		t.pos(x, y + h, z + d).tex(0, 0).endVertex();
		t.pos(x, y + h, z).tex(1, 0).endVertex();
		t.draw();

		t.begin(GL_QUADS);
		t.pos(x + w, y, z).tex(1, 1).endVertex();
		t.pos(x + w, y, z + d).tex(0, 1).endVertex();
		t.pos(x, y, z + d).tex(0, 0).endVertex();
		t.pos(x, y, z).tex(1, 0).endVertex();
		t.draw();

		t.begin(GL_QUADS);
		t.pos(x + w, y + h, z + d).tex(1, 1).endVertex();
		t.pos(x + w, y + h, z).tex(0, 1).endVertex();
		t.pos(x, y + h, z).tex(0, 0).endVertex();
		t.pos(x, y + h, z + d).tex(1, 0).endVertex();
		t.draw();
	}
}
