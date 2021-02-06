package com.tom.cpm.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;

import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.client.Render.Box;
import com.tom.cpm.shared.IPlayerRenderManager;
import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PlayerModelElement;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.skin.SkinProvider;

public class PlayerRenderManager implements IPlayerRenderManager {
	private final ModelDefinitionLoader loader;
	private Map<PlayerEntityModel<AbstractClientPlayerEntity>, RedirectHolder> holders = new HashMap<>();
	private AnimationEngine animEngine = new AnimationEngine();

	public PlayerRenderManager(ModelDefinitionLoader loader) {
		this.loader = loader;
	}

	public void bindModel(PlayerEntityModel<AbstractClientPlayerEntity> model, VertexConsumerProvider buffer, ModelDefinition def, Predicate<Object> unbindRule) {
		holders.computeIfAbsent(model, RedirectHolder::new).swapIn(def, unbindRule, buffer);
	}

	public void unbindModel(PlayerEntityModel<AbstractClientPlayerEntity> model) {
		RedirectHolder h = holders.get(model);
		if(h != null)h.swapOut();
	}

	@Override
	public void cleanupRenderedCube(RenderedCube cube) {
		if(cube.renderObject != null) {
			cube.renderObject = null;
		}
	}

	@Override
	public ModelDefinitionLoader getLoader() {
		return loader;
	}

	public void bindSkin(PlayerEntityModel<AbstractClientPlayerEntity> model, CallbackInfoReturnable<Identifier> cbi) {
		holders.computeIfAbsent(model, RedirectHolder::new).bindTexture(cbi);
	}

	public boolean isBound(PlayerEntityModel<AbstractClientPlayerEntity> model) {
		return holders.computeIfAbsent(model, RedirectHolder::new).swappedIn;
	}

	private static class RedirectHolder {
		private final PlayerEntityModel<AbstractClientPlayerEntity> model;
		private final RedirectModelRenderer bipedHead;
		private final RedirectModelRenderer bipedBody;
		private final RedirectModelRenderer bipedRightArm;
		private final RedirectModelRenderer bipedLeftArm;
		private final RedirectModelRenderer bipedRightLeg;
		private final RedirectModelRenderer bipedLeftLeg;

		private final RedirectModelRenderer bipedHeadwear;
		private final RedirectModelRenderer bipedLeftArmwear;
		private final RedirectModelRenderer bipedRightArmwear;
		private final RedirectModelRenderer bipedLeftLegwear;
		private final RedirectModelRenderer bipedRightLegwear;
		private final RedirectModelRenderer bipedBodyWear;

		private ModelDefinition def;
		private Predicate<Object> unbindRule;
		private boolean swappedIn;
		private int sheetX, sheetY;
		private VertexConsumerProvider buffer;
		private Identifier skinTex;

		public RedirectHolder(PlayerEntityModel<AbstractClientPlayerEntity> model) {
			this.model = model;
			bipedHead         = new RedirectModelRenderer(this, () -> model.head        , PlayerModelParts.HEAD);
			bipedBody         = new RedirectModelRenderer(this, () -> model.torso        , PlayerModelParts.BODY);
			bipedRightArm     = new RedirectModelRenderer(this, () -> model.rightArm    , PlayerModelParts.RIGHT_ARM);
			bipedLeftArm      = new RedirectModelRenderer(this, () -> model.leftArm     , PlayerModelParts.LEFT_ARM);
			bipedRightLeg     = new RedirectModelRenderer(this, () -> model.rightLeg    , PlayerModelParts.RIGHT_LEG);
			bipedLeftLeg      = new RedirectModelRenderer(this, () -> model.leftLeg     , PlayerModelParts.LEFT_LEG);

			bipedHeadwear     = new RedirectModelRenderer(this, () -> model.helmet    , null);
			bipedLeftArmwear  = new RedirectModelRenderer(this, () -> model.leftSleeve , null);
			bipedRightArmwear = new RedirectModelRenderer(this, () -> model.rightSleeve, null);
			bipedLeftLegwear  = new RedirectModelRenderer(this, () -> model.leftPantLeg , null);
			bipedRightLegwear = new RedirectModelRenderer(this, () -> model.rightPantLeg, null);
			bipedBodyWear     = new RedirectModelRenderer(this, () -> model.jacket    , null);
		}

		public void bindTexture(CallbackInfoReturnable<Identifier> cbi) {
			if(def == null)return;
			SkinProvider skin = def.getSkinOverride();
			if(skin != null && skin.texture != null) {
				skin.bind();
				cbi.setReturnValue(DynTexture.getBoundLoc());
				sheetX = skin.getSize().x;
				sheetY = skin.getSize().y;
			} else {
				sheetX = 64;
				sheetY = 64;
			}
			skinTex = cbi.getReturnValue();
		}

		public void swapIn(ModelDefinition def, Predicate<Object> unbindRule, VertexConsumerProvider buf) {
			this.def = def;
			this.unbindRule = unbindRule;
			this.buffer = buf;
			if(swappedIn)return;
			model.head         = bipedHead        .swapIn();
			model.torso         = bipedBody        .swapIn();
			model.rightArm     = bipedRightArm    .swapIn();
			model.leftArm      = bipedLeftArm     .swapIn();
			model.rightLeg     = bipedRightLeg    .swapIn();
			model.leftLeg      = bipedLeftLeg     .swapIn();

			model.helmet     = bipedHeadwear    .swapIn();
			model.leftSleeve  = bipedLeftArmwear .swapIn();
			model.rightSleeve = bipedRightArmwear.swapIn();
			model.leftPantLeg  = bipedLeftLegwear .swapIn();
			model.rightPantLeg = bipedRightLegwear.swapIn();
			model.jacket     = bipedBodyWear    .swapIn();
			this.swappedIn = true;
		}

		public void swapOut() {
			this.def = null;
			this.unbindRule = null;
			this.buffer = null;
			skinTex = null;
			if(!swappedIn)return;
			model.head         = bipedHead        .swapOut();
			model.torso         = bipedBody        .swapOut();
			model.rightArm     = bipedRightArm    .swapOut();
			model.leftArm      = bipedLeftArm     .swapOut();
			model.rightLeg     = bipedRightLeg    .swapOut();
			model.leftLeg      = bipedLeftLeg     .swapOut();

			model.helmet     = bipedHeadwear    .swapOut();
			model.leftSleeve  = bipedLeftArmwear .swapOut();
			model.rightSleeve = bipedRightArmwear.swapOut();
			model.leftPantLeg  = bipedLeftLegwear .swapOut();
			model.rightPantLeg = bipedRightLegwear.swapOut();
			model.jacket     = bipedBodyWear    .swapOut();
			swappedIn = false;
		}
	}

	private static class RedirectModelRenderer extends ModelPart {
		private final RedirectHolder holder;
		private final PlayerModelParts part;
		private final Supplier<ModelPart> parentProvider;
		private ModelPart parent;
		private PlayerModelElement elem;
		private CubeModelRenderer dispRender;
		private int sel;

		public RedirectModelRenderer(RedirectHolder holder, Supplier<ModelPart> parent, PlayerModelParts part) {
			super(holder.model);
			this.holder = holder;
			holder.model.parts.remove(this);
			this.parentProvider = parent;
			this.part = part;
			this.dispRender = new CubeModelRenderer();
		}

		@Override
		public void render(MatrixStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
			if(holder.def != null) {
				if(part == null) {
					if(holder.unbindRule != null && holder.unbindRule.test(this))
						holder.swapOut();
					return;
				}
				elem = holder.def.getModelElementFor(part);
				sel = elem.getSelected();
				float px = this.pivotX;
				float py = this.pivotY;
				float pz = this.pivotZ;
				float rx = this.pitch;
				float ry = this.yaw;
				float rz = this.roll;
				boolean skipTransform = false;
				if(holder.bipedLeftArm == this && holder.model.leftArmPose.ordinal() > 2) {
					skipTransform = true;
				}
				if(holder.bipedRightArm == this && holder.model.rightArmPose.ordinal() > 2) {
					skipTransform = true;
				}
				if(!skipTransform) {
					if(elem.forcePos) {
						this.pivotX = elem.pos.x;
						this.pivotY = elem.pos.y;
						this.pivotZ = elem.pos.z;
						this.pitch  = elem.rotation.x;
						this.yaw    = elem.rotation.y;
						this.roll   = elem.rotation.z;
					} else {
						this.pivotX += elem.pos.x;
						this.pivotY += elem.pos.y;
						this.pivotZ += elem.pos.z;
						this.pitch  += elem.rotation.x;
						this.yaw    += elem.rotation.y;
						this.roll   += elem.rotation.z;
					}
				}
				if(elem.doDisplay()) {
					copyModel(this, parent);
					parent.addChild(dispRender);
					parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
					parent.pivotX = px;
					parent.pivotY = py;
					parent.pivotZ = pz;
					parent.pitch  = rx;
					parent.yaw    = ry;
					parent.roll   = rz;
					parent.children.remove(dispRender);
				} else {
					if(holder.def.isEditor()) {
						//GlStateManager.color(1, 1, 1, 0.5f);
						//parent.render(scale);
						//GlStateManager.color(1, 1, 1, 1);
					}
					matrixStackIn.push();
					rotate(matrixStackIn);
					if(sel > 0)drawVanillaOutline(matrixStackIn, bufferIn);
					render(elem, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
					matrixStackIn.pop();
				}
				elem = null;
			} else {
				copyModel(this, parent);
				parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
			if(holder.unbindRule != null && holder.unbindRule.test(this))
				holder.swapOut();
		}

		private class CubeModelRenderer extends ModelPart {
			public CubeModelRenderer() {
				super(holder.model);
				holder.model.parts.remove(this);
			}

			@Override
			public void render(MatrixStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
				RedirectModelRenderer.this.render(elem, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
				if(sel > 0)drawVanillaOutline(matrixStackIn, bufferIn);
			}
		}

		private void drawVanillaOutline(MatrixStack matrixStackIn, VertexConsumer bufferIn) {
			float f = 0.001f;
			float g = f * 2;
			float scale = 1 / 16.0F;
			for(Cuboid b : parent.cuboids) {
				WorldRenderer.drawBox(
						matrixStackIn, holder.buffer.getBuffer(CustomRenderTypes.getLinesNoDepth()),
						b.minX * scale - f, b.minY * scale - f, b.minZ * scale - f,
						b.maxX * scale + g, b.maxY * scale + g, b.maxZ * scale + g,
						1, 1, sel == 2 ? 1 : 0, 1);
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
				boolean newBuffer = false;
				if(cube.glow && sel == 0) {
					buffer = holder.buffer.getBuffer(RenderLayer.getEyes(holder.skinTex));
					newBuffer = true;
				}
				((Box)cube.renderObject).draw(matrixStackIn, buffer, holder.buffer, packedLightIn, packedOverlayIn, r, g, b, alpha);
				if(newBuffer) {
					holder.buffer.getBuffer(RenderLayer.getEntityTranslucent(holder.skinTex));
				}
				drawSelect(cube, matrixStackIn, bufferIn);
				render(cube, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
				matrixStackIn.pop();
			}
		}

		private void drawSelect(RenderedCube cube, MatrixStack matrixStackIn, VertexConsumer bufferIn) {
			int sel = cube.getSelected();

			if(sel > 0) {
				float f = 0.001f;
				float g = f * 2;
				float scale = 1 / 16f;
				Cube c = cube.getCube();
				Render.drawBoundingBox(
						matrixStackIn, holder.buffer.getBuffer(CustomRenderTypes.getLinesNoDepth()),
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

		private ModelPart swapIn() {
			if(parent != null) {
				//new Exception("Double swapping?").printStackTrace();
				return this;
			}
			parent = parentProvider.get();
			copyModel(parent, this);
			return this;
		}

		private ModelPart swapOut() {
			if(parent == null) {
				//new Exception("Double swapping?").printStackTrace();
				return parentProvider.get();
			}
			ModelPart p = parent;
			parent = null;
			return p;
		}

		private static void copyModel(ModelPart s, ModelPart d) {
			d.pivotX = s.pivotX;
			d.pivotY = s.pivotY;
			d.pivotZ = s.pivotZ;
			d.yaw   = s.yaw  ;
			d.pitch   = s.pitch  ;
			d.roll   = s.roll  ;
			d.visible      = s.visible     ;
		}
	}

	public static boolean unbindHand(Object v) {
		RedirectModelRenderer rd = (RedirectModelRenderer) v;
		return rd.parent == rd.holder.bipedLeftArmwear.parent || rd.parent == rd.holder.bipedRightArmwear.parent;
	}

	@Override
	public AnimationEngine getAnimationEngine() {
		return animEngine;
	}
}
