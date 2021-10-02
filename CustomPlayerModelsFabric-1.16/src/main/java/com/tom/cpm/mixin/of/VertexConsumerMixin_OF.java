package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

import com.tom.cpm.client.optifine.proxy.VertexConsumerOF;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin_OF extends VertexConsumerOF {

	@Shadow default VertexConsumerProvider.Immediate getRenderTypeBuffer() { return null; }

	@Override
	default VertexConsumerProvider.Immediate cpm$getRenderTypeBuffer() {
		return getRenderTypeBuffer();
	}
}
