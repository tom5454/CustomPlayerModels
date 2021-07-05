package com.tom.cpm.client;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

import com.tom.cpl.render.DirectBuffer;

public class VBuffer extends DirectBuffer<VertexConsumer> {
	private int light, overlay;
	private Matrix4f mat4;
	private Matrix3f mat3;

	public VBuffer(VertexConsumer buffer, int light, int overlay, MatrixStack matrixStackIn) {
		super(buffer);
		this.light = light;
		this.overlay = overlay;
		mat4 = matrixStackIn.peek().getModel();
		mat3 = matrixStackIn.peek().getNormal();
	}

	@Override
	protected void pushVertex(float x, float y, float z, float red, float green,
			float blue, float alpha, float u, float v, float nx, float ny, float nz) {
		buffer.vertex(mat4, x, y, z);
		buffer.color(red, green, blue, alpha);
		buffer.texture(u, v);
		buffer.overlay(overlay);
		buffer.light(light);
		buffer.normal(mat3, nx, ny, nz);
		buffer.next();
	}

	@Override
	public void finish() {}
}
