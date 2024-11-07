package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.model.SkullModel;

@Mixin(SkullModel.class)
public class SkullModelMixin {
	/*public @Shadow ModelPart head;

	@Inject(at = @At("HEAD"), method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", cancellable = true)
	public void renderToBuffer(PoseStack ps, VertexConsumer p_103816_, int p_103817_, int p_103818_, int p_103819_, CallbackInfo cbi) {
		if (head instanceof RedirectModelRendererBase) {
			head.render(ps, p_103816_, p_103817_, p_103818_, p_103819_);
			cbi.cancel();
		}
	}*/
}
