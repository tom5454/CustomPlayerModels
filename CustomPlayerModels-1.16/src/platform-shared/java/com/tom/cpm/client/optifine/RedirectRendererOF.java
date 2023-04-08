package com.tom.cpm.client.optifine;

import java.util.function.Supplier;

import net.minecraft.client.renderer.model.ModelRenderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.client.PlayerRenderManager.RedirectModelRendererVanilla;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public class RedirectRendererOF extends RedirectModelRendererVanilla {

	public RedirectRendererOF(RDH holder, Supplier<ModelRenderer> parent, VanillaModelPart part) {
		super(holder, parent, part);
	}

	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, boolean updateModel) {
		render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
}
