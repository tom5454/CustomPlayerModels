package com.tom.cpm.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class Render {

	public static void drawTexturedCube(MatrixStack mst, float x, float y, float z, float w, float h, float d){
		RenderLayer rt = CustomRenderTypes.getTexCutout(new Identifier("cpm", "textures/gui/base.png"));
		VertexConsumer t = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().getBuffer(rt);
		Matrix4f m = mst.peek().getModel();
		t.vertex(m, x + w, y, z).texture(1, 1).next();
		t.vertex(m, x, y, z).texture(0, 1).next();
		t.vertex(m, x, y + h, z).texture(0, 0).next();
		t.vertex(m, x + w, y + h, z).texture(1, 0).next();

		t.vertex(m, x, y, z + d).texture(1, 1).next();
		t.vertex(m, x + w, y, z + d).texture(0, 1).next();
		t.vertex(m, x + w, y + h, z + d).texture(0, 0).next();
		t.vertex(m, x, y + h, z + d).texture(1, 0).next();

		t.vertex(m, x + w, y, z + d).texture(1, 1).next();
		t.vertex(m, x + w, y, z).texture(0, 1).next();
		t.vertex(m, x + w, y + h, z).texture(0, 0).next();
		t.vertex(m, x + w, y + h, z + d).texture(1, 0).next();

		t.vertex(m, x, y, z).texture(1, 1).next();
		t.vertex(m, x, y, z + d).texture(0, 1).next();
		t.vertex(m, x, y + h, z + d).texture(0, 0).next();
		t.vertex(m, x, y + h, z).texture(1, 0).next();

		t.vertex(m, x + w, y, z).texture(1, 1).next();
		t.vertex(m, x + w, y, z + d).texture(0, 1).next();
		t.vertex(m, x, y, z + d).texture(0, 0).next();
		t.vertex(m, x, y, z).texture(1, 0).next();

		t.vertex(m, x + w, y + h, z + d).texture(1, 1).next();
		t.vertex(m, x + w, y + h, z).texture(0, 1).next();
		t.vertex(m, x, y + h, z).texture(0, 0).next();
		t.vertex(m, x, y + h, z + d).texture(1, 0).next();

		MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().draw(rt);
	}
}
