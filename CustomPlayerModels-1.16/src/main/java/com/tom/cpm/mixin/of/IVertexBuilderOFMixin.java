package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer.Impl;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.cpm.client.optifine.proxy.IVertexBuilderOF;

@Mixin(IVertexBuilder.class)
public interface IVertexBuilderOFMixin extends IVertexBuilderOF {

	@Shadow default IRenderTypeBuffer.Impl getRenderTypeBuffer() { return null; }

	@Override
	default Impl cpm$getRenderTypeBuffer() {
		return getRenderTypeBuffer();
	}
}
