package com.tom.cpm.client.optifine;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.optifine.entity.model.anim.ModelUpdater;
import net.optifine.model.ModelSprite;

import com.tom.cpm.client.CustomRenderTypes;
import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.client.PlayerRenderManager.RedirectModelRendererBase;
import com.tom.cpm.client.Render;
import com.tom.cpm.client.Render.Box;
import com.tom.cpm.client.optifine.proxy.ModelPartOF;
import com.tom.cpm.client.optifine.proxy.VertexConsumerOF;
import com.tom.cpm.client.optifine.proxy.VertexConsumerProviderOF;
import com.tom.cpm.client.optifine.proxy.WorldRendererOF;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.ModelRenderManager.ModelPart;
import com.tom.cpm.shared.model.ModelRenderManager.RedirectHolder;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RenderedCube.ElementSelectMode;
import com.tom.cpm.shared.model.RootModelElement;

public class RedirectRendererOF extends RedirectModelRendererBase implements ModelPartOF {
	private CubeModelRenderer dispRender;

	public RedirectRendererOF(Model model, RDH holder, Supplier<net.minecraft.client.model.ModelPart> parent, ModelPart part) {
		super(model, holder, parent, part);
		this.dispRender = new CubeModelRenderer();
	}

	private MatrixStack matrixStackIn;
	private VertexConsumer bufferIn;
	private int packedLightIn, packedOverlayIn;
	private float red, green, blue, alpha;

	@Override
	public void render(MatrixStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
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
		render();
		if (lastRenderType != null)
			renderTypeBuffer.getBuffer(lastRenderType);
		this.matrixStackIn = null;
		this.bufferIn = null;
	}

	private class CubeModelRenderer extends net.minecraft.client.model.ModelPart {
		public CubeModelRenderer() {
			super(0, 0, 0, 0);
		}

		@Override
		public void render(MatrixStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
			RedirectRendererOF.this.render(elem, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			drawVanillaOutline(matrixStackIn, bufferIn);
		}
	}

	private void drawVanillaOutline(MatrixStack matrixStackIn, VertexConsumer bufferIn) {
		if(holder.def.isEditor()) {
			ElementSelectMode sel = elem.getSelected();
			if(sel.isRenderOutline()) {
				float f = 0.001f;
				float g = f * 2;
				float scale = 1 / 16.0F;
				for(Cuboid b : parent.cuboids) {
					WorldRenderer.drawBox(
							matrixStackIn, holder.addDt.getBuffer(CustomRenderTypes.getLinesNoDepth()),
							b.minX * scale - f, b.minY * scale - f, b.minZ * scale - f,
							b.maxX * scale + g, b.maxY * scale + g, b.maxZ * scale + g,
							1, 1, sel == ElementSelectMode.SELECTED ? 1 : 0, 1);
				}
			}
		}
	}

	private static void translateRotate(RenderedCube rc, MatrixStack matrixStackIn) {
		matrixStackIn.translate(rc.pos.x / 16.0F, rc.pos.y / 16.0F, rc.pos.z / 16.0F);
		if (rc.rotation.z != 0.0F) {
			matrixStackIn.multiply(Vector3f.POSITIVE_Z.getRadialQuaternion(rc.rotation.z));
		}

		if (rc.rotation.y != 0.0F) {
			matrixStackIn.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(rc.rotation.y));
		}

		if (rc.rotation.x != 0.0F) {
			matrixStackIn.multiply(Vector3f.POSITIVE_X.getRadialQuaternion(rc.rotation.x));
		}

	}

	private void render(RenderedCube elem, MatrixStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		if(elem.children == null)return;
		for(RenderedCube cube : elem.children) {
			if(!cube.display)continue;
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
			VertexConsumer buffer = bufferIn;
			if(holder.def.isEditor()) {
				ElementSelectMode sel = cube.getSelected();
				if(!sel.applyColor()) {
					r = 1;
					g = 1;
					b = 1;
				}
				if(cube.glow && sel == ElementSelectMode.NULL) {
					buffer = holder.addDt.getBuffer(holder.glowType);
				}
			} else if(cube.glow) {
				buffer = holder.addDt.getBuffer(holder.glowType);
			}
			((Box)cube.renderObject).draw(matrixStackIn, buffer, holder.addDt, packedLightIn, packedOverlayIn, r, g, b, alpha);
			holder.addDt.getBuffer(holder.defaultType);
			drawSelect(cube, matrixStackIn, bufferIn);
			render(cube, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			matrixStackIn.pop();
		}
	}

	private void drawSelect(RenderedCube cube, MatrixStack matrixStackIn, VertexConsumer bufferIn) {
		if(holder.def.isEditor()) {
			ElementSelectMode sel = cube.getSelected();
			if(sel.isRenderOutline()) {
				float f = 0.001f;
				float g = f * 2;
				float scale = 1 / 16f;
				boolean s = sel == ElementSelectMode.SELECTED;
				if(s)Render.drawOrigin(matrixStackIn, holder.addDt.getBuffer(CustomRenderTypes.getLinesNoDepth()), 1);
				Cube c = cube.getCube();
				Render.drawBoundingBox(
						matrixStackIn, holder.addDt.getBuffer(CustomRenderTypes.getLinesNoDepth()),
						c.offset.x * scale - f, c.offset.y * scale - f, c.offset.z * scale - f,
						c.size.x * scale * c.scale.x + g, c.size.y * scale * c.scale.y + g, c.size.z * scale * c.scale.z + g,
						s ? 1 : 0.5f, s ? 1 : 0.5f, s ? 1 : 0, 1
						);
			}
		}
	}

	private Box createBox(RenderedCube elem) {
		Cube c = elem.getCube();
		if(c.texSize == 0) {
			return Render.createColored(
					c.offset.x, c.offset.y, c.offset.z,
					c.size.x * c.scale.x, c.size.y * c.scale.y, c.size.z * c.scale.z,
					c.mcScale,
					holder.sheetX, holder.sheetY
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
	public net.minecraft.client.model.ModelPart swapIn() {
		if(parent != null) {
			//new Exception("Double swapping?").printStackTrace();
			return this;
		}
		parent = parentProvider.get();
		holder.copyModel(parent, this);
		return this;
	}

	@Override
	public net.minecraft.client.model.ModelPart swapOut() {
		if(parent == null) {
			//new Exception("Double swapping?").printStackTrace();
			return parentProvider.get();
		}
		net.minecraft.client.model.ModelPart p = parent;
		parent = null;
		return p;
	}

	@Override
	public RedirectHolder<?, ?, ?, net.minecraft.client.model.ModelPart> getHolder() {
		return holder;
	}

	@Override
	public net.minecraft.client.model.ModelPart getParent() {
		return parent;
	}

	@Override
	public ModelPart getPart() {
		return part;
	}

	@Override
	public void renderParent() {
		parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	@Override
	public void renderWithParent(RootModelElement elem) {
		this.elem = elem;
		parent.addChild(dispRender);
		parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		parent.children.remove(dispRender);
		this.elem = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doRender(RootModelElement elem) {
		this.elem = elem;
		matrixStackIn.push();
		rotate(matrixStackIn);
		drawVanillaOutline(matrixStackIn, bufferIn);
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

	@Override
	public void addSprite(float posX, float posY, float posZ, int sizeX, int sizeY, int sizeZ, float sizeAdd) {
		if(parent != null)
			((ModelPartOF)parent).addSprite(posX, posY, posZ, sizeX, sizeY, sizeZ, sizeAdd);
	}
}
