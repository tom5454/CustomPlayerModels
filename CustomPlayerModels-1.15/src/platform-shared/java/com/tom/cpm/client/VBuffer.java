package com.tom.cpm.client;

import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.cpl.render.DirectBuffer;

public class VBuffer extends DirectBuffer<IVertexBuilder> {
	private int light, overlay;
	private Matrix4f mat4;
	private Matrix3f mat3;

	public VBuffer(IVertexBuilder buffer, int light, int overlay, MatrixStack matrixStackIn) {
		super(buffer);
		this.light = light;
		this.overlay = overlay;
		mat4 = matrixStackIn.last().pose();
		mat3 = matrixStackIn.last().normal();
	}

	@Override
	protected void pushVertex(float x, float y, float z, float red, float green,
			float blue, float alpha, float u, float v, float nx, float ny, float nz) {
		buffer.vertex(mat4, x, y, z);
		buffer.color(red, green, blue, alpha);
		buffer.uv(u, v);
		buffer.overlayCoords(overlay);
		buffer.uv2(light);
		buffer.normal(mat3, nx, ny, nz);
		buffer.endVertex();
	}

	@Override
	public void finish() {}
}
