package com.tom.cpm.client;

import java.util.function.BiFunction;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

// TODO move into main source
public class DeferredBuffer implements BiFunction<SubmitNodeCollector, RenderType, VertexConsumer> {
	private PoseStack dummy = new PoseStack();

	@Override
	public VertexConsumer apply(SubmitNodeCollector t, RenderType u) {
		t.submitCustomGeometry(dummy, u, (__, vc) -> {

		});
		return null;
	}

}
