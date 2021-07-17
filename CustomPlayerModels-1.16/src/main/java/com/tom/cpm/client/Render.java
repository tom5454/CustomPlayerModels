package com.tom.cpm.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

public class Render {
	public static void drawTexturedCube(MatrixStack mst, float x, float y, float z, float w, float h, float d){
		RenderType rt = CustomRenderTypes.getTexCutout(new ResourceLocation("cpm", "textures/gui/base.png"));
		IVertexBuilder t = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(rt);
		Matrix4f m = mst.getLast().getMatrix();
		t.pos(m, x + w, y, z).tex(1, 1).endVertex();
		t.pos(m, x, y, z).tex(0, 1).endVertex();
		t.pos(m, x, y + h, z).tex(0, 0).endVertex();
		t.pos(m, x + w, y + h, z).tex(1, 0).endVertex();

		t.pos(m, x, y, z + d).tex(1, 1).endVertex();
		t.pos(m, x + w, y, z + d).tex(0, 1).endVertex();
		t.pos(m, x + w, y + h, z + d).tex(0, 0).endVertex();
		t.pos(m, x, y + h, z + d).tex(1, 0).endVertex();

		t.pos(m, x + w, y, z + d).tex(1, 1).endVertex();
		t.pos(m, x + w, y, z).tex(0, 1).endVertex();
		t.pos(m, x + w, y + h, z).tex(0, 0).endVertex();
		t.pos(m, x + w, y + h, z + d).tex(1, 0).endVertex();

		t.pos(m, x, y, z).tex(1, 1).endVertex();
		t.pos(m, x, y, z + d).tex(0, 1).endVertex();
		t.pos(m, x, y + h, z + d).tex(0, 0).endVertex();
		t.pos(m, x, y + h, z).tex(1, 0).endVertex();

		t.pos(m, x + w, y, z).tex(1, 1).endVertex();
		t.pos(m, x + w, y, z + d).tex(0, 1).endVertex();
		t.pos(m, x, y, z + d).tex(0, 0).endVertex();
		t.pos(m, x, y, z).tex(1, 0).endVertex();

		t.pos(m, x + w, y + h, z + d).tex(1, 1).endVertex();
		t.pos(m, x + w, y + h, z).tex(0, 1).endVertex();
		t.pos(m, x, y + h, z).tex(0, 0).endVertex();
		t.pos(m, x, y + h, z + d).tex(1, 0).endVertex();

		Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(rt);
	}
}
