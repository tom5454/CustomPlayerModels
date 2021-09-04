package com.tom.cpm.client.optifine;

import java.util.function.Supplier;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.optifine.entity.model.anim.ModelUpdater;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.cpl.render.VBuffers;
import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.client.PlayerRenderManager.RedirectModelRendererBase;
import com.tom.cpm.client.VBuffer;
import com.tom.cpm.client.optifine.proxy.IRenderTypeBufferOF;
import com.tom.cpm.client.optifine.proxy.IVertexBuilderOF;
import com.tom.cpm.client.optifine.proxy.ModelRendererOF;
import com.tom.cpm.client.optifine.proxy.WorldRendererOF;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public class RedirectRendererOF extends RedirectModelRendererBase implements ModelRendererOF {

	public RedirectRendererOF(RDH holder, Supplier<ModelRenderer> parent, VanillaModelPart part) {
		super(holder, parent, part);
	}

	private MatrixStack matrixStackIn;
	private IVertexBuilder bufferIn;
	private int packedLightIn, packedOverlayIn;

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		if(!holder.renderTypes.isInitialized()) {
			holder.copyModel(this, parent);
			parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			holder.logWarning();
			return;
		}
		RenderType lastRenderType = null;
		IRenderTypeBuffer.Impl renderTypeBuffer = null;
		if (this.cpm$textureLocation() != null) {
			if (((WorldRendererOF) this.cpm$renderGlobal()).getRenderOverlayEyes())
				return;
			renderTypeBuffer = ((IVertexBuilderOF) bufferIn).cpm$getRenderTypeBuffer();
			if (renderTypeBuffer != null) {
				lastRenderType = ((IRenderTypeBufferOF) renderTypeBuffer).cpm$getLastRenderType();
				bufferIn = ((IRenderTypeBufferOF) renderTypeBuffer).cpm$getBuffer(this.cpm$textureLocation(), bufferIn);
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
		/*matrixStackIn.push();
		translateRotate(matrixStackIn);
		List<ModelSprite> spriteList = cpm$spriteList();
		int spriteListSize = spriteList.size();
		for (int ix = 0; ix < spriteListSize; ix++) {
			ModelSprite sprite = spriteList.get(ix);
			sprite.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		}
		matrixStackIn.pop();*/
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
			((ModelRendererOF)parent).addSprite(posX, posY, posZ, sizeX, sizeY, sizeZ, sizeAdd);
	}
}
