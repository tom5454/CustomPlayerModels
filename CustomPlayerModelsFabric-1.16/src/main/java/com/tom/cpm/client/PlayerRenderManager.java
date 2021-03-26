package com.tom.cpm.client;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.model.Model;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.model.SkullOverlayEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;

import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.client.Render.Box;
import com.tom.cpm.client.optifine.OptifineTexture;
import com.tom.cpm.client.optifine.RedirectRendererOF;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.ModelRenderManager;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RenderedCube.ElementSelectMode;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.skin.TextureProvider;

public class PlayerRenderManager extends ModelRenderManager<VertexConsumerProvider, CallbackInfoReturnable<Identifier>, net.minecraft.client.model.ModelPart, Model> {

	public PlayerRenderManager(ModelDefinitionLoader loader) {
		super(loader);

		setFactory(new RedirectHolderFactory<VertexConsumerProvider, CallbackInfoReturnable<Identifier>, net.minecraft.client.model.ModelPart>() {

			@SuppressWarnings("unchecked")
			@Override
			public <M> RedirectHolder<M, VertexConsumerProvider, CallbackInfoReturnable<Identifier>, net.minecraft.client.model.ModelPart> create(
					M model) {
				if(model instanceof PlayerEntityModel) {
					return (RedirectHolder<M, VertexConsumerProvider, CallbackInfoReturnable<Identifier>, net.minecraft.client.model.ModelPart>)
							new RedirectHolderPlayer(PlayerRenderManager.this, (PlayerEntityModel<AbstractClientPlayerEntity>) model);
				} else if(model instanceof SkullOverlayEntityModel) {
					return (RedirectHolder<M, VertexConsumerProvider, CallbackInfoReturnable<Identifier>, net.minecraft.client.model.ModelPart>)
							new RedirectHolderSkull(PlayerRenderManager.this, (SkullOverlayEntityModel) model);
				}
				return null;
			}
		});
		setRedirectFactory(new RedirectRendererFactory<Model, CallbackInfoReturnable<Identifier>, net.minecraft.client.model.ModelPart>() {

			@Override
			public RedirectRenderer<net.minecraft.client.model.ModelPart> create(Model model,
					RedirectHolder<Model, ?, CallbackInfoReturnable<Identifier>, net.minecraft.client.model.ModelPart> access,
					Supplier<net.minecraft.client.model.ModelPart> modelPart, ModelPart part) {
				return CustomPlayerModelsClient.optifineLoaded ?
						new RedirectRendererOF(model, (RDH) access, modelPart, part) :
							new RedirectModelRendererVanilla(model, (RDH) access, modelPart, part);
			}
		});
		setVis(m -> m.visible, (m, v) -> m.visible = v);
		setModelPosGetters(m -> m.pivotX, m -> m.pivotY, m -> m.pivotZ);
		setModelRotGetters(m -> m.pitch, m -> m.yaw, m -> m.roll);
		setModelSetters((m, x, y, z) -> {
			m.pivotX = x;
			m.pivotY = y;
			m.pivotZ = z;
		}, (m, x, y, z) -> {
			m.pitch = x;
			m.yaw = y;
			m.roll = z;
		});
	}

	@Override
	public void cleanupRenderedCube(RenderedCube cube) {
		if(cube.renderObject != null) {
			cube.renderObject = null;
		}
	}

	public static abstract class RDH extends ModelRenderManager.RedirectHolder<Model, VertexConsumerProvider, CallbackInfoReturnable<Identifier>, net.minecraft.client.model.ModelPart> {
		public Identifier boundSkin;
		public RenderLayer glowType, defaultType;

		public RDH(
				ModelRenderManager<VertexConsumerProvider, CallbackInfoReturnable<Identifier>, net.minecraft.client.model.ModelPart, Model> mngr,
				Model model) {
			super(mngr, model);
		}

		@Override
		public void bindTexture(CallbackInfoReturnable<Identifier> cbi) {
			if(def == null)return;
			TextureProvider skin = def.getSkinOverride();
			if(skin != null && skin.texture != null) {
				skin.bind();
				OptifineTexture.applyOptifineTexture(cbi.getReturnValue(), skin);
				cbi.setReturnValue(DynTexture.getBoundLoc());
				sheetX = skin.getSize().x;
				sheetY = skin.getSize().y;
			} else {
				sheetX = 64;
				sheetY = 64;
			}
			boundSkin = cbi.getReturnValue();
			defaultType = RenderLayer.getEntityTranslucent(boundSkin);
			glowType = RenderLayer.getEyes(boundSkin);
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
		private RedirectRenderer<net.minecraft.client.model.ModelPart> bipedHead;
		private RedirectRenderer<net.minecraft.client.model.ModelPart> bipedLeftArm;
		private RedirectRenderer<net.minecraft.client.model.ModelPart> bipedRightArm;
		private RedirectRenderer<net.minecraft.client.model.ModelPart> bipedLeftArmwear;
		private RedirectRenderer<net.minecraft.client.model.ModelPart> bipedRightArmwear;

		public RedirectHolderPlayer(PlayerRenderManager mngr, PlayerEntityModel<AbstractClientPlayerEntity> model) {
			super(mngr, model);
			bipedHead = register(new Field<>(() -> model.head        , v -> model.head         = v, PlayerModelParts.HEAD), p -> !((PlayerProfile)p).hasPlayerHead);
			register(new Field<>(() -> model.torso       , v -> model.torso        = v, PlayerModelParts.BODY));
			bipedRightArm = register(new Field<>(() -> model.rightArm    , v -> model.rightArm     = v, PlayerModelParts.RIGHT_ARM));
			bipedLeftArm = register(new Field<>(() -> model.leftArm     , v -> model.leftArm      = v, PlayerModelParts.LEFT_ARM));
			register(new Field<>(() -> model.rightLeg    , v -> model.rightLeg     = v, PlayerModelParts.RIGHT_LEG));
			register(new Field<>(() -> model.leftLeg     , v -> model.leftLeg      = v, PlayerModelParts.LEFT_LEG));

			register(new Field<>(() -> model.helmet      , v -> model.helmet       = v, null)).setCopyFrom(bipedHead);
			bipedLeftArmwear = register(new Field<>(() -> model.leftSleeve  , v -> model.leftSleeve   = v, null));
			bipedRightArmwear = register(new Field<>(() -> model.rightSleeve , v -> model.rightSleeve  = v, null));
			register(new Field<>(() -> model.leftPantLeg , v -> model.leftPantLeg  = v, null));
			register(new Field<>(() -> model.rightPantLeg, v -> model.rightPantLeg = v, null));
			register(new Field<>(() -> model.jacket      , v -> model.jacket       = v, null));
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean skipTransform(RedirectRenderer<net.minecraft.client.model.ModelPart> part) {
			PlayerEntityModel<AbstractClientPlayerEntity> model = (PlayerEntityModel<AbstractClientPlayerEntity>) this.model;
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
		private RedirectRenderer<net.minecraft.client.model.ModelPart> hat;

		public RedirectHolderSkull(PlayerRenderManager mngr, SkullOverlayEntityModel model) {
			super(mngr, model);

			register(new Field<>(() -> model.skull, v -> model.skull = v, PlayerModelParts.HEAD));
			hat = register(new Field<>(() -> model.skullOverlay        , v -> model.skullOverlay         = v, null));
		}

	}

	public static abstract class RedirectModelRendererBase extends net.minecraft.client.model.ModelPart implements RedirectRenderer<net.minecraft.client.model.ModelPart> {
		public final RDH holder;
		public final ModelPart part;
		public final Supplier<net.minecraft.client.model.ModelPart> parentProvider;
		public net.minecraft.client.model.ModelPart parent;
		public RootModelElement elem;

		public RedirectModelRendererBase(Model model, RDH holder, Supplier<net.minecraft.client.model.ModelPart> parent, ModelPart part) {
			super(0, 0, 0, 0);
			this.holder = holder;
			this.parentProvider = parent;
			this.part = part;
		}
	}

	private static class RedirectModelRendererVanilla extends RedirectModelRendererBase {
		private CubeModelRenderer dispRender;

		public RedirectModelRendererVanilla(Model model, RDH holder, Supplier<net.minecraft.client.model.ModelPart> parent, ModelPart part) {
			super(model, holder, parent, part);
			this.dispRender = new CubeModelRenderer();
		}

		private MatrixStack matrixStackIn;
		private VertexConsumer bufferIn;
		private int packedLightIn, packedOverlayIn;
		private float red, green, blue, alpha;

		@Override
		public void render(MatrixStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
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

		private class CubeModelRenderer extends net.minecraft.client.model.ModelPart {
			public CubeModelRenderer() {
				super(0, 0, 0, 0);
			}

			@Override
			public void render(MatrixStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
				RedirectModelRendererVanilla.this.render(elem, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
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
			if(elem.children == null || holder.defaultType == null)return;
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
				return this;
			}
			parent = parentProvider.get();
			holder.copyModel(parent, this);
			return this;
		}

		@Override
		public net.minecraft.client.model.ModelPart swapOut() {
			if(parent == null) {
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

		@Override
		public void doRender(RootModelElement elem) {
			this.elem = elem;
			matrixStackIn.push();
			rotate(matrixStackIn);
			drawVanillaOutline(matrixStackIn, bufferIn);
			render(elem, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			matrixStackIn.pop();
			this.elem = null;
		}
	}

	public static boolean unbindHand(Object v) {
		RedirectModelRendererBase rd = (RedirectModelRendererBase) v;
		RedirectHolderPlayer rdhp = (RedirectHolderPlayer) rd.holder;
		return rd == rdhp.bipedLeftArmwear || rd == rdhp.bipedRightArmwear;
	}

	public static boolean unbindSkull(Object v) {
		RedirectModelRendererBase rd = (RedirectModelRendererBase) v;
		RedirectHolderSkull rdhs = (RedirectHolderSkull) rd.holder;
		return rd == rdhs.hat;
	}
}
