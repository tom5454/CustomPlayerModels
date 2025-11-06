package com.tom.cpm.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.cpl.render.DirectBuffer;

public class VBuffer extends DirectBuffer<VertexConsumer> {
	private int light, overlay;
	private Pose pose;

	public VBuffer(VertexConsumer buffer, int light, int overlay, PoseStack matrixStackIn) {
		super(buffer);
		this.light = light;
		this.overlay = overlay;
		pose = matrixStackIn.last().copy();
	}

	public VBuffer(VertexConsumer buffer, int light, int overlay, Pose poseIn) {
		super(buffer);
		this.light = light;
		this.overlay = overlay;
		this.pose = poseIn;
	}

	@Override
	protected void pushVertex(float x, float y, float z, float red, float green,
			float blue, float alpha, float u, float v, float nx, float ny, float nz) {
		buffer.addVertex(pose, x, y, z);
		buffer.setColor(red, green, blue, alpha);
		buffer.setUv(u, v);
		buffer.setOverlay(overlay);
		buffer.setLight(light);
		buffer.setNormal(pose, nx, ny, nz);
	}

	@Override
	public void finish() {}
}
