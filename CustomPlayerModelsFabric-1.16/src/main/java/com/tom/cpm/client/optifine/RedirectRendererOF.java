package com.tom.cpm.client.optifine;

import java.util.function.Supplier;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.optifine.entity.model.anim.ModelUpdater;

import com.tom.cpl.render.VBuffers;
import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.client.PlayerRenderManager.RedirectModelRendererBase;
import com.tom.cpm.client.VBuffer;
import com.tom.cpm.client.optifine.proxy.ModelPartOF;
import com.tom.cpm.client.optifine.proxy.VertexConsumerOF;
import com.tom.cpm.client.optifine.proxy.VertexConsumerProviderOF;
import com.tom.cpm.client.optifine.proxy.WorldRendererOF;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public class RedirectRendererOF extends RedirectModelRendererBase implements ModelPartOF {

	public RedirectRendererOF(RDH holder, Supplier<ModelPart> parent, VanillaModelPart part) {
		super(holder, parent, part);
	}

	private MatrixStack matrixStackIn;
	private VertexConsumer bufferIn;
	private int packedLightIn, packedOverlayIn;

	@Override
	public void render(MatrixStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		if(!holder.renderTypes.isInitialized()) {
			holder.copyModel(this, parent);
			parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			holder.logWarning();
			return;
		}
		RenderLayer lastRenderType = null;
		VertexConsumerProvider.Immediate renderTypeBuffer = null;
		if (this.cpm$textureLocation() != null) {
			if (((WorldRendererOF) this.cpm$renderGlobal()).getRenderOverlayEyes())
				return;
			renderTypeBuffer = ((VertexConsumerOF) bufferIn).cpm$getRenderTypeBuffer();
			if (renderTypeBuffer != null) {
				lastRenderType = ((VertexConsumerProviderOF) renderTypeBuffer).cpm$getLastRenderType();
				bufferIn = ((VertexConsumerProviderOF) renderTypeBuffer).cpm$getBuffer(this.cpm$textureLocation(), bufferIn);
			}
		}
		ModelUpdater mu = cpm$modelUpdater();
		if (mu != null)
			mu.update();
		this.matrixStackIn   = matrixStackIn  ;
		this.bufferIn        = bufferIn       ;
		this.packedLightIn   = packedLightIn  ;
		this.packedOverlayIn = packedOverlayIn;
		this.red             = red            ;
		this.green           = green          ;
		this.blue            = blue           ;
		this.alpha           = alpha          ;
		this.buffers = new VBuffers(rt -> new VBuffer(holder.addDt.getBuffer(rt.getNativeType()), packedLightIn, packedOverlayIn, matrixStackIn), new VBuffer(bufferIn, packedLightIn, packedOverlayIn, matrixStackIn));
		render();
		if (lastRenderType != null)
			renderTypeBuffer.getBuffer(lastRenderType);
		this.matrixStackIn = null;
		this.bufferIn = null;
	}

	@Override
	public void renderParent() {
		parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	@Override
	public void addSprite(float posX, float posY, float posZ, int sizeX, int sizeY, int sizeZ, float sizeAdd) {
		if(parent != null)
			((ModelPartOF)parent).addSprite(posX, posY, posZ, sizeX, sizeY, sizeZ, sizeAdd);
	}
}
