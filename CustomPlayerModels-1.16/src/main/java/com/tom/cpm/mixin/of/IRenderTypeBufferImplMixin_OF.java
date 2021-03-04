package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.cpm.client.optifine.proxy.IRenderTypeBufferOF;

@Mixin(IRenderTypeBuffer.Impl.class)
public abstract class IRenderTypeBufferImplMixin_OF implements IRenderTypeBufferOF {

	@Shadow public abstract IVertexBuilder getBuffer(ResourceLocation textureLocation, IVertexBuilder def);

	@Override
	public IVertexBuilder cpm$getBuffer(ResourceLocation textureLocation, IVertexBuilder def) {
		return getBuffer(textureLocation, def);
	}

}
