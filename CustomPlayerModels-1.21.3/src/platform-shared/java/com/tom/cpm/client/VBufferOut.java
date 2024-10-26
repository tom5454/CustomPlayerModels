package com.tom.cpm.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.cpm.shared.model.render.BatchedBuffers.BufferOutput;

public class VBufferOut implements BufferOutput<VertexConsumer> {
	private int light, overlay;
	private Pose pose;

	public VBufferOut(int light, int overlay, PoseStack matrixStackIn) {
		this.light = light;
		this.overlay = overlay;
		pose = matrixStackIn.last().copy();
	}

	@Override
	public void push(VertexConsumer buffer, float x, float y, float z, float red, float green,
			float blue, float alpha, float u, float v, float nx, float ny, float nz) {
		buffer.addVertex(pose, x, y, z);
		buffer.setColor(red, green, blue, alpha);
		buffer.setUv(u, v);
		buffer.setOverlay(overlay);
		buffer.setLight(light);
		buffer.setNormal(pose, nx, ny, nz);
	}
}
