package com.tom.cpm.client.optifine.proxy;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.vertex.IVertexBuilder;

public interface IRenderTypeBufferOF {
	IVertexBuilder cpm$getBuffer(ResourceLocation textureLocation, IVertexBuilder def);
	RenderType cpm$getLastRenderType();
}
