package com.tom.cpm.client.optifine.proxy;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.Identifier;

public interface VertexConsumerProviderOF {
	VertexConsumer cpm$getBuffer(Identifier textureLocation, VertexConsumer def);
	RenderLayer cpm$getLastRenderType();
}
