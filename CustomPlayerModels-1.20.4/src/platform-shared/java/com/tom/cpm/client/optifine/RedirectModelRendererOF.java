package com.tom.cpm.client.optifine;

import java.util.function.Supplier;

import net.minecraft.client.model.geom.ModelPart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.client.PlayerRenderManager.RedirectModelRendererVanilla;
import com.tom.cpm.shared.model.render.VanillaModelPart;

public class RedirectModelRendererOF extends RedirectModelRendererVanilla {

	public RedirectModelRendererOF(RDH holder, Supplier<ModelPart> parent, VanillaModelPart part) {
		super(holder, parent, part);
	}

	public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, boolean updateModel) {
		render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
}
