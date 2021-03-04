package com.tom.cpm.client;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.model.HumanoidHeadModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.client.Render.Box;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.ModelRenderManager;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RenderedCube.ElementSelectMode;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.skin.TextureProvider;

public class PlayerRenderManager extends ModelRenderManager<IRenderTypeBuffer, CallbackInfoReturnable<ResourceLocation>, ModelRenderer, Model> {

	public PlayerRenderManager(ModelDefinitionLoader loader) {
		super(loader);
		setFactory(new RedirectHolderFactory<IRenderTypeBuffer, CallbackInfoReturnable<ResourceLocation>, ModelRenderer>() {

			@SuppressWarnings("unchecked")
			@Override
			public <M> RedirectHolder<M, IRenderTypeBuffer, CallbackInfoReturnable<ResourceLocation>, ModelRenderer> create(
					M model) {
				if(model instanceof PlayerModel) {
					return (RedirectHolder<M, IRenderTypeBuffer, CallbackInfoReturnable<ResourceLocation>, ModelRenderer>)
							new RedirectHolderPlayer(PlayerRenderManager.this, (PlayerModel<AbstractClientPlayerEntity>) model);
				} else if(model instanceof HumanoidHeadModel) {
					return (RedirectHolder<M, IRenderTypeBuffer, CallbackInfoReturnable<ResourceLocation>, ModelRenderer>)
							new RedirectHolderSkull(PlayerRenderManager.this, (HumanoidHeadModel) model);
				}
				return null;
			}
		});
		setRedirectFactory(new RedirectRendererFactory<Model, CallbackInfoReturnable<ResourceLocation>, ModelRenderer>() {

			@Override
			public RedirectRenderer<ModelRenderer> create(Model model,
					RedirectHolder<Model, ?, CallbackInfoReturnable<ResourceLocation>, ModelRenderer> access,
					Supplier<ModelRenderer> modelPart, ModelPart part) {
				return new RedirectModelRenderer(model, (RDH) access, modelPart, part);
			}
		});
		setVis(m -> m.showModel, (m, v) -> m.showModel = v);
		setModelPosGetters(m -> m.rotationPointX, m -> m.rotationPointY, m -> m.rotationPointZ);
		setModelRotGetters(m -> m.rotateAngleX, m -> m.rotateAngleY, m -> m.rotateAngleZ);
		setModelSetters((m, x, y, z) -> {
			m.rotationPointX = x;
			m.rotationPointY = y;
			m.rotationPointZ = z;
		}, (m, x, y, z) -> {
			m.rotateAngleX = x;
			m.rotateAngleY = y;
			m.rotateAngleZ = z;
		});
	}

	@Override
	public void cleanupRenderedCube(RenderedCube cube) {
		if(cube.renderObject != null) {
			cube.renderObject = null;
		}
	}

	public static abstract class RDH extends ModelRenderManager.RedirectHolder<Model, IRenderTypeBuffer, CallbackInfoReturnable<ResourceLocation>, ModelRenderer> {
		public ResourceLocation boundSkin;
		public RenderType glowType, defaultType;

		public RDH(
				ModelRenderManager<IRenderTypeBuffer, CallbackInfoReturnable<ResourceLocation>, ModelRenderer, Model> mngr,
				Model model) {
			super(mngr, model);
		}

		@Override
		public void bindTexture(CallbackInfoReturnable<ResourceLocation> cbi) {
			if(def == null)return;
			TextureProvider skin = def.getSkinOverride();
			if(skin != null && skin.texture != null) {
				skin.bind();
				cbi.setReturnValue(DynTexture.getBoundLoc());
				sheetX = skin.getSize().x;
				sheetY = skin.getSize().y;
			} else {
				sheetX = 64;
				sheetY = 64;
			}
			boundSkin = cbi.getReturnValue();
			defaultType = RenderType.getEntityTranslucent(boundSkin);
			glowType = RenderType.getEyes(boundSkin);
		}

		@Override
		public void swapOut0() {
			this.boundSkin = null;
			defaultType = null;
			glowType = null;
		}

		@Override
		public void swapIn0() {}
	}

	private static class RedirectHolderPlayer extends RDH {
		private RedirectRenderer<ModelRenderer> bipedHead;
		private RedirectRenderer<ModelRenderer> bipedLeftArm;
		private RedirectRenderer<ModelRenderer> bipedRightArm;
		private RedirectRenderer<ModelRenderer> bipedLeftArmwear;
		private RedirectRenderer<ModelRenderer> bipedRightArmwear;

		public RedirectHolderPlayer(PlayerRenderManager mngr, PlayerModel<AbstractClientPlayerEntity> model) {
			super(mngr, model);
			bipedHead = register(new Field<>(() -> model.bipedHead        , v -> model.bipedHead         = v, PlayerModelParts.HEAD));
			register(new Field<>(() -> model.bipedBody        , v -> model.bipedBody         = v, PlayerModelParts.BODY));
			bipedRightArm = register(new Field<>(() -> model.bipedRightArm    , v -> model.bipedRightArm     = v, PlayerModelParts.RIGHT_ARM));
			bipedLeftArm = register(new Field<>(() -> model.bipedLeftArm     , v -> model.bipedLeftArm      = v, PlayerModelParts.LEFT_ARM));
			register(new Field<>(() -> model.bipedRightLeg    , v -> model.bipedRightLeg     = v, PlayerModelParts.RIGHT_LEG));
			register(new Field<>(() -> model.bipedLeftLeg     , v -> model.bipedLeftLeg      = v, PlayerModelParts.LEFT_LEG));

			register(new Field<>(() -> model.bipedHeadwear    , v -> model.bipedHeadwear     = v, null)).setCopyFrom(bipedHead);
			bipedLeftArmwear = register(new Field<>(() -> model.bipedLeftArmwear , v -> model.bipedLeftArmwear  = v, null));
			bipedRightArmwear = register(new Field<>(() -> model.bipedRightArmwear, v -> model.bipedRightArmwear = v, null));
			register(new Field<>(() -> model.bipedLeftLegwear , v -> model.bipedLeftLegwear  = v, null));
			register(new Field<>(() -> model.bipedRightLegwear, v -> model.bipedRightLegwear = v, null));
			register(new Field<>(() -> model.bipedBodyWear    , v -> model.bipedBodyWear     = v, null));
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean skipTransform(RedirectRenderer<ModelRenderer> part) {
			PlayerModel<AbstractClientPlayerEntity> model = (PlayerModel<AbstractClientPlayerEntity>) this.model;
			boolean skipTransform = false;
			if(bipedLeftArm == part && model.leftArmPose.ordinal() > 2) {
				skipTransform = true;
			}
			if(bipedRightArm == part && model.rightArmPose.ordinal() > 2) {
				skipTransform = true;
			}
			return skipTransform;
		}
	}

	private static class RedirectHolderSkull extends RDH {
		private RedirectRenderer<ModelRenderer> hat;

		public RedirectHolderSkull(PlayerRenderManager mngr, HumanoidHeadModel model) {
			super(mngr, model);

			register(new Field<>(() -> model.field_217105_a, v -> model.field_217105_a = v, PlayerModelParts.HEAD));
			hat = register(new Field<>(() -> model.head        , v -> model.head         = v, null));
		}

	}

	private static class RedirectModelRenderer extends ModelRenderer implements RedirectRenderer<ModelRenderer> {
		private final RDH holder;
		private final ModelPart part;
		private final Supplier<ModelRenderer> parentProvider;
		private ModelRenderer parent;
		private RootModelElement elem;
		private CubeModelRenderer dispRender;

		@SuppressWarnings("unchecked")
		public RedirectModelRenderer(Model model, RDH holder, Supplier<ModelRenderer> parent, ModelPart part) {
			super(model);
			this.part = part;
			this.holder = holder;
			this.parentProvider = parent;
			if(model instanceof PlayerModel)
				((PlayerModel<AbstractClientPlayerEntity>)model).modelRenderers.remove(this);
			this.dispRender = new CubeModelRenderer();
		}

		private MatrixStack matrixStackIn;
		private IVertexBuilder bufferIn;
		private int packedLightIn, packedOverlayIn;
		private float red, green, blue, alpha;

		@Override
		public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
			this.matrixStackIn   = matrixStackIn  ;
			this.bufferIn        = bufferIn       ;
			this.packedLightIn   = packedLightIn  ;
			this.packedOverlayIn = packedOverlayIn;
			this.red             = red            ;
			this.green           = green          ;
			this.blue            = blue           ;
			this.alpha           = alpha          ;
			render();
			this.matrixStackIn = null;
			this.bufferIn = null;
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
			parent.childModels.remove(dispRender);
			this.elem = null;
		}

		@Override
		public void doRender(RootModelElement elem) {
			this.elem = elem;
			matrixStackIn.push();
			translateRotate(matrixStackIn);
			drawVanillaOutline(matrixStackIn, bufferIn);
			render(elem, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			matrixStackIn.pop();
			this.elem = null;
		}

		private class CubeModelRenderer extends ModelRenderer {
			@SuppressWarnings("unchecked")
			public CubeModelRenderer() {
				super(holder.model);
				if(holder.model instanceof PlayerModel)
					((PlayerModel<AbstractClientPlayerEntity>)holder.model).modelRenderers.remove(this);
			}

			@Override
			public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
				RedirectModelRenderer.this.render(elem, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
				drawVanillaOutline(matrixStackIn, bufferIn);
			}
		}

		private void drawVanillaOutline(MatrixStack matrixStackIn, IVertexBuilder bufferIn) {
			if(holder.def.isEditor()) {
				ElementSelectMode sel = elem.getSelected();
				if(sel.isRenderOutline()) {
					float f = 0.001f;
					float g = f * 2;
					float scale = 1 / 16.0F;
					boolean s = sel == ElementSelectMode.SELECTED;
					if(s)Render.drawOrigin(matrixStackIn, holder.addDt.getBuffer(CustomRenderTypes.getLinesNoDepth()), 1);
					for(ModelBox b : parent.cubeList) {
						WorldRenderer.drawBoundingBox(matrixStackIn, holder.addDt.getBuffer(CustomRenderTypes.getLinesNoDepth()),
								b.posX1 * scale - f, b.posY1 * scale - f, b.posZ1 * scale - f,
								b.posX2 * scale + g, b.posY2 * scale + g, b.posZ2 * scale + g,
								1, 1, s ? 1 : 0, 1);
					}
				}
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
				IVertexBuilder buffer = bufferIn;
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

		private void drawSelect(RenderedCube cube, MatrixStack matrixStackIn, IVertexBuilder bufferIn) {
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
	}

	public static boolean unbindHand(Object v) {
		RedirectModelRenderer rd = (RedirectModelRenderer) v;
		RedirectHolderPlayer rdhp = (RedirectHolderPlayer) rd.holder;
		return rd == rdhp.bipedLeftArmwear || rd == rdhp.bipedRightArmwear;
	}

	public static boolean unbindSkull(Object v) {
		RedirectModelRenderer rd = (RedirectModelRenderer) v;
		RedirectHolderSkull rdhs = (RedirectHolderSkull) rd.holder;
		return rd == rdhs.hat;
	}
}
