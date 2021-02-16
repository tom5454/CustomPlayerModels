package com.tom.cpm.client.optifine;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.entity.model.anim.ModelUpdater;
import net.optifine.model.ModelSprite;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.cpm.client.CustomRenderTypes;
import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.client.PlayerRenderManager.RedirectModelRendererBase;
import com.tom.cpm.client.Render;
import com.tom.cpm.client.Render.Box;
import com.tom.cpm.client.optifine.proxy.IRenderTypeBufferOF;
import com.tom.cpm.client.optifine.proxy.IVertexBuilderOF;
import com.tom.cpm.client.optifine.proxy.ModelRendererOF;
import com.tom.cpm.client.optifine.proxy.WorldRendererOF;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.ModelRenderManager.ModelPart;
import com.tom.cpm.shared.model.ModelRenderManager.RedirectHolder;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;

public class RedirectRendererOF extends RedirectModelRendererBase implements ModelRendererOF {
	private CubeModelRenderer dispRender;

	public RedirectRendererOF(Model model, RDH holder, Supplier<ModelRenderer> parent, ModelPart part) {
		super(model, holder, parent, part);
		this.dispRender = new CubeModelRenderer();
	}

	private MatrixStack matrixStackIn;
	private IVertexBuilder bufferIn;
	private int packedLightIn, packedOverlayIn;
	private float red, green, blue, alpha;

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
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
	public void renderWithParent(RootModelElement elem, int sel) {
		this.elem = elem;
		this.sel = sel;
		parent.addChild(dispRender);
		parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		parent.childModels.remove(dispRender);
		this.elem = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doRender(RootModelElement elem, int sel) {
		this.elem = elem;
		this.sel = sel;
		matrixStackIn.push();
		translateRotate(matrixStackIn);
		if(sel > 0)drawVanillaOutline(matrixStackIn, bufferIn);
		render(elem, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		List<ModelSprite> spriteList = cpm$spriteList();
		int spriteListSize = spriteList.size();
		for (int ix = 0; ix < spriteListSize; ix++) {
			ModelSprite sprite = spriteList.get(ix);
			sprite.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		}
		matrixStackIn.pop();
		this.elem = null;
	}

	private class CubeModelRenderer extends ModelRenderer {

		public CubeModelRenderer() {
			super(0, 0, 0, 0);
		}

		@Override
		public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
			RedirectRendererOF.this.render(elem, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			if(sel > 0)drawVanillaOutline(matrixStackIn, bufferIn);
		}
	}

	private void drawVanillaOutline(MatrixStack matrixStackIn, IVertexBuilder bufferIn) {
		float f = 0.001f;
		float g = f * 2;
		float scale = 1 / 16.0F;
		if(sel == 2)Render.drawOrigin(matrixStackIn, holder.addDt.getBuffer(CustomRenderTypes.getLinesNoDepth()), 1);
		for(ModelBox b : parent.cubeList) {
			WorldRenderer.drawBoundingBox(matrixStackIn, holder.addDt.getBuffer(CustomRenderTypes.getLinesNoDepth()),
					b.posX1 * scale - f, b.posY1 * scale - f, b.posZ1 * scale - f,
					b.posX2 * scale + g, b.posY2 * scale + g, b.posZ2 * scale + g,
					1, 1, sel == 2 ? 1 : 0, 1);
		}
	}

	private static void translateRotate(RenderedCube rc, MatrixStack matrixStackIn) {
		matrixStackIn.translate(rc.pos.x / 16.0F, rc.pos.y / 16.0F, rc.pos.z / 16.0F);
		if (rc.rotation.z != 0.0F) {
			matrixStackIn.rotate(Vector3f.ZP.rotation(rc.rotation.z));
		}

		if (rc.rotation.y != 0.0F) {
			matrixStackIn.rotate(Vector3f.YP.rotation(rc.rotation.y));
		}

		if (rc.rotation.x != 0.0F) {
			matrixStackIn.rotate(Vector3f.XP.rotation(rc.rotation.x));
		}

	}

	private void render(RenderedCube elem, MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		if(elem.children == null)return;
		for(RenderedCube cube : elem.children) {
			if(!cube.display) {
				continue;
			}
			matrixStackIn.push();
			translateRotate(cube, matrixStackIn);
			float r = red;
			float g = green;
			float b = blue;
			if(cube.color != 0xffffff) {
				r *= ((cube.color & 0xff0000) >> 16) / 255f;
				g *= ((cube.color & 0x00ff00) >> 8 ) / 255f;
				b *= ( cube.color & 0x0000ff       ) / 255f;
			}
			if(cube.useDynamic || cube.renderObject == null) {
				cube.renderObject = createBox(cube);
			}
			int sel = cube.getSelected();
			if(sel == 3) {
				r = 1;
				g = 1;
				b = 1;
			}
			IVertexBuilder buffer = bufferIn;
			if(cube.glow && sel == 0) {
				buffer = holder.addDt.getBuffer(holder.glowType);
			}
			((Box)cube.renderObject).draw(matrixStackIn, buffer, holder.addDt, packedLightIn, packedOverlayIn, r, g, b, alpha);
			holder.addDt.getBuffer(holder.defaultType);
			drawSelect(cube, matrixStackIn, bufferIn, sel);
			render(cube, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			matrixStackIn.pop();
		}
	}

	private void drawSelect(RenderedCube cube, MatrixStack matrixStackIn, IVertexBuilder bufferIn, int sel) {
		if(sel > 0) {
			float f = 0.001f;
			float g = f * 2;
			float scale = 1 / 16f;
			if(sel == 2)Render.drawOrigin(matrixStackIn, holder.addDt.getBuffer(CustomRenderTypes.getLinesNoDepth()), 1);
			Cube c = cube.getCube();
			Render.drawBoundingBox(
					matrixStackIn, holder.addDt.getBuffer(CustomRenderTypes.getLinesNoDepth()),
					c.offset.x * scale - f, c.offset.y * scale - f, c.offset.z * scale - f,
					c.size.x * scale * c.scale.x + g, c.size.y * scale * c.scale.y + g, c.size.z * scale * c.scale.z + g,
					sel == 2 ? 1 : 0.5f, sel == 2 ? 1 : 0.5f, sel == 2 ? 1 : 0, 1
					);
		}
	}

	private Box createBox(RenderedCube elem) {
		Cube c = elem.getCube();
		if(c.texSize == 0) {
			return Render.createColored(
					c.offset.x, c.offset.y, c.offset.z,
					c.size.x * c.scale.x, c.size.y * c.scale.y, c.size.z * c.scale.z,
					c.mcScale, holder.sheetX, holder.sheetY
					);
		} else {
			return Render.createTextured(
					c.offset, c.size, c.scale,
					c.mcScale,
					c.u, c.v, c.texSize, holder.sheetX, holder.sheetY
					);
		}
	}

	@Override
	public ModelRenderer swapIn() {
		if(parent != null)return this;
		parent = parentProvider.get();
		holder.copyModel(parent, this);
		return this;
	}

	@Override
	public ModelRenderer swapOut() {
		if(parent == null)return parentProvider.get();
		ModelRenderer p = parent;
		parent = null;
		return p;
	}

	@Override
	public RedirectHolder<?, ?, ?, ModelRenderer> getHolder() {
		return holder;
	}

	@Override
	public ModelRenderer getParent() {
		return parent;
	}

	@Override
	public ModelPart getPart() {
		return part;
	}

	@Override
	public void addSprite(float posX, float posY, float posZ, int sizeX, int sizeY, int sizeZ, float sizeAdd) {
		if(parent != null)
			((ModelRendererOF)parent).addSprite(posX, posY, posZ, sizeX, sizeY, sizeZ, sizeAdd);
	}
}
