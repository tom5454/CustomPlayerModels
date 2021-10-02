package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;

import com.tom.cpm.client.optifine.proxy.VertexConsumerProviderOF;

@Mixin(VertexConsumerProvider.Immediate.class)
public abstract class VertexConsumerProviderImmediateMixin_OF implements VertexConsumerProviderOF {

	@Shadow public abstract VertexConsumer getBuffer(Identifier textureLocation, VertexConsumer def);

	@Override
	public VertexConsumer cpm$getBuffer(Identifier textureLocation, VertexConsumer def) {
		return getBuffer(textureLocation, def);
	}

}
