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
		IVertexBuilder t = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(rt);
		Matrix4f m = mst.last().pose();
		t.vertex(m, x + w, y, z).uv(1, 1).endVertex();
		t.vertex(m, x, y, z).uv(0, 1).endVertex();
		t.vertex(m, x, y + h, z).uv(0, 0).endVertex();
		t.vertex(m, x + w, y + h, z).uv(1, 0).endVertex();

		t.vertex(m, x, y, z + d).uv(1, 1).endVertex();
		t.vertex(m, x + w, y, z + d).uv(0, 1).endVertex();
		t.vertex(m, x + w, y + h, z + d).uv(0, 0).endVertex();
		t.vertex(m, x, y + h, z + d).uv(1, 0).endVertex();

		t.vertex(m, x + w, y, z + d).uv(1, 1).endVertex();
		t.vertex(m, x + w, y, z).uv(0, 1).endVertex();
		t.vertex(m, x + w, y + h, z).uv(0, 0).endVertex();
		t.vertex(m, x + w, y + h, z + d).uv(1, 0).endVertex();

		t.vertex(m, x, y, z).uv(1, 1).endVertex();
		t.vertex(m, x, y, z + d).uv(0, 1).endVertex();
		t.vertex(m, x, y + h, z + d).uv(0, 0).endVertex();
		t.vertex(m, x, y + h, z).uv(1, 0).endVertex();

		t.vertex(m, x + w, y, z).uv(1, 1).endVertex();
		t.vertex(m, x + w, y, z + d).uv(0, 1).endVertex();
		t.vertex(m, x, y, z + d).uv(0, 0).endVertex();
		t.vertex(m, x, y, z).uv(1, 0).endVertex();

		t.vertex(m, x + w, y + h, z + d).uv(1, 1).endVertex();
		t.vertex(m, x + w, y + h, z).uv(0, 1).endVertex();
		t.vertex(m, x, y + h, z).uv(0, 0).endVertex();
		t.vertex(m, x, y + h, z + d).uv(1, 0).endVertex();

		Minecraft.getInstance().renderBuffers().bufferSource().endBatch(rt);
	}
}
