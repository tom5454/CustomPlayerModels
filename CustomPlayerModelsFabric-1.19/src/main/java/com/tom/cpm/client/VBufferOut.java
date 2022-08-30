package com.tom.cpm.client;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

import com.tom.cpm.shared.model.render.BatchedBuffers.BufferOutput;

public class VBufferOut implements BufferOutput<VertexConsumer> {
	private int light, overlay;
	private Matrix4f mat4;
	private Matrix3f mat3;

	public VBufferOut(int light, int overlay, MatrixStack matrixStackIn) {
		this.light = light;
		this.overlay = overlay;
		mat4 = new Matrix4f(matrixStackIn.peek().getPositionMatrix());
		mat3 = new Matrix3f(matrixStackIn.peek().getNormalMatrix());
	}

	@Override
	public void push(VertexConsumer buffer, float x, float y, float z, float red, float green,
			float blue, float alpha, float u, float v, float nx, float ny, float nz) {
		buffer.vertex(mat4, x, y, z);
		buffer.color(red, green, blue, alpha);
		buffer.texture(u, v);
		buffer.overlay(overlay);
		buffer.light(light);
		buffer.normal(mat3, nx, ny, nz);
		buffer.next();
	}
}
