package com.tom.cpm.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

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
	private Map<PlayerModel<AbstractClientPlayerEntity>, RedirectHolder> holders = new HashMap<>();
	private AnimationEngine animEngine = new AnimationEngine();

	public PlayerRenderManager(ModelDefinitionLoader loader) {
		this.loader = loader;
	}

	public void bindModel(PlayerModel<AbstractClientPlayerEntity> model, IRenderTypeBuffer bufferIn, ModelDefinition def, Predicate<Object> unbindRule) {
		holders.computeIfAbsent(model, RedirectHolder::new).swapIn(def, unbindRule, bufferIn);
	}

	public void unbindModel(PlayerModel<AbstractClientPlayerEntity> model) {
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

	public void bindSkin(PlayerModel<AbstractClientPlayerEntity> model, CallbackInfoReturnable<ResourceLocation> cbi) {
		holders.computeIfAbsent(model, RedirectHolder::new).bindTexture(cbi);
	}

	public boolean isBound(PlayerModel<AbstractClientPlayerEntity> model) {
		return holders.computeIfAbsent(model, RedirectHolder::new).swappedIn;
	}

	private static class RedirectHolder {
		private final PlayerModel<AbstractClientPlayerEntity> model;
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
		private IRenderTypeBuffer buffer;
		private ResourceLocation boundSkin;

		public RedirectHolder(PlayerModel<AbstractClientPlayerEntity> model) {
			this.model = model;
			bipedHead         = new RedirectModelRenderer(this, () -> model.bipedHead        , PlayerModelParts.HEAD);
			bipedBody         = new RedirectModelRenderer(this, () -> model.bipedBody        , PlayerModelParts.BODY);
			bipedRightArm     = new RedirectModelRenderer(this, () -> model.bipedRightArm    , PlayerModelParts.RIGHT_ARM);
			bipedLeftArm      = new RedirectModelRenderer(this, () -> model.bipedLeftArm     , PlayerModelParts.LEFT_ARM);
			bipedRightLeg     = new RedirectModelRenderer(this, () -> model.bipedRightLeg    , PlayerModelParts.RIGHT_LEG);
			bipedLeftLeg      = new RedirectModelRenderer(this, () -> model.bipedLeftLeg     , PlayerModelParts.LEFT_LEG);

			bipedHeadwear     = new RedirectModelRenderer(this, () -> model.bipedHeadwear    , null);
			bipedLeftArmwear  = new RedirectModelRenderer(this, () -> model.bipedLeftArmwear , null);
			bipedRightArmwear = new RedirectModelRenderer(this, () -> model.bipedRightArmwear, null);
			bipedLeftLegwear  = new RedirectModelRenderer(this, () -> model.bipedLeftLegwear , null);
			bipedRightLegwear = new RedirectModelRenderer(this, () -> model.bipedRightLegwear, null);
			bipedBodyWear     = new RedirectModelRenderer(this, () -> model.bipedBodyWear    , null);
		}

		public void bindTexture(CallbackInfoReturnable<ResourceLocation> cbi) {
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
			boundSkin = cbi.getReturnValue();
		}

		public void swapIn(ModelDefinition def, Predicate<Object> unbindRule, IRenderTypeBuffer bufferIn) {
			this.def = def;
			this.unbindRule = unbindRule;
			this.buffer = bufferIn;
			if(swappedIn)return;
			model.bipedHead         = bipedHead        .swapIn();
			model.bipedBody         = bipedBody        .swapIn();
			model.bipedRightArm     = bipedRightArm    .swapIn();
			model.bipedLeftArm      = bipedLeftArm     .swapIn();
			model.bipedRightLeg     = bipedRightLeg    .swapIn();
			model.bipedLeftLeg      = bipedLeftLeg     .swapIn();

			model.bipedHeadwear     = bipedHeadwear    .swapIn();
			model.bipedLeftArmwear  = bipedLeftArmwear .swapIn();
			model.bipedRightArmwear = bipedRightArmwear.swapIn();
			model.bipedLeftLegwear  = bipedLeftLegwear .swapIn();
			model.bipedRightLegwear = bipedRightLegwear.swapIn();
			model.bipedBodyWear     = bipedBodyWear    .swapIn();
			this.swappedIn = true;
		}

		public void swapOut() {
			this.def = null;
			this.unbindRule = null;
			this.buffer = null;
			this.boundSkin = null;
			if(!swappedIn)return;
			model.bipedHead         = bipedHead        .swapOut();
			model.bipedBody         = bipedBody        .swapOut();
			model.bipedRightArm     = bipedRightArm    .swapOut();
			model.bipedLeftArm      = bipedLeftArm     .swapOut();
			model.bipedRightLeg     = bipedRightLeg    .swapOut();
			model.bipedLeftLeg      = bipedLeftLeg     .swapOut();

			model.bipedHeadwear     = bipedHeadwear    .swapOut();
			model.bipedLeftArmwear  = bipedLeftArmwear .swapOut();
			model.bipedRightArmwear = bipedRightArmwear.swapOut();
			model.bipedLeftLegwear  = bipedLeftLegwear .swapOut();
			model.bipedRightLegwear = bipedRightLegwear.swapOut();
			model.bipedBodyWear     = bipedBodyWear    .swapOut();
			swappedIn = false;
		}
	}

	private static class RedirectModelRenderer extends ModelRenderer {
		private final RedirectHolder holder;
		private final PlayerModelParts part;
		private final Supplier<ModelRenderer> parentProvider;
		private ModelRenderer parent;
		private PlayerModelElement elem;
		private CubeModelRenderer dispRender;
		private int sel;

		public RedirectModelRenderer(RedirectHolder holder, Supplier<ModelRenderer> parent, PlayerModelParts part) {
			super(holder.model);
			this.holder = holder;
			holder.model.modelRenderers.remove(this);
			this.parentProvider = parent;
			this.part = part;
			this.dispRender = new CubeModelRenderer();
		}

		@Override
		public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
			if(holder.def != null) {
				if(part == null) {
					if(holder.unbindRule != null && holder.unbindRule.test(this))
						holder.swapOut();
					return;
				}
				elem = holder.def.getModelElementFor(part);
				sel = elem.getSelected();
				float px = this.rotationPointX;
				float py = this.rotationPointY;
				float pz = this.rotationPointZ;
				float rx = this.rotateAngleX;
				float ry = this.rotateAngleY;
				float rz = this.rotateAngleZ;
				boolean skipTransform = holder.unbindRule != null;
				if(holder.bipedLeftArm == this && holder.model.leftArmPose.ordinal() > 2) {
					skipTransform = true;
				}
				if(holder.bipedRightArm == this && holder.model.rightArmPose.ordinal() > 2) {
					skipTransform = true;
				}
				if(!skipTransform) {
					if(elem.forcePos) {
						this.rotationPointX = elem.pos.x;
						this.rotationPointY = elem.pos.y;
						this.rotationPointZ = elem.pos.z;
						this.rotateAngleX = elem.rotation.x;
						this.rotateAngleY = elem.rotation.y;
						this.rotateAngleZ = elem.rotation.z;
					} else {
						this.rotationPointX += elem.pos.x;
						this.rotationPointY += elem.pos.y;
						this.rotationPointZ += elem.pos.z;
						this.rotateAngleX += elem.rotation.x;
						this.rotateAngleY += elem.rotation.y;
						this.rotateAngleZ += elem.rotation.z;
					}
				}
				if(elem.doDisplay()) {
					copyModel(this, parent);
					parent.addChild(dispRender);
					parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
					parent.rotationPointX = px;
					parent.rotationPointY = py;
					parent.rotationPointZ = pz;
					parent.rotateAngleX = rx;
					parent.rotateAngleY = ry;
					parent.rotateAngleZ = rz;
					parent.childModels.remove(dispRender);
				} else {
					if(holder.def.isEditor()) {
						//parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, 0.5f);
					}
					matrixStackIn.push();
					translateRotate(matrixStackIn);
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

		private class CubeModelRenderer extends ModelRenderer {
			public CubeModelRenderer() {
				super(holder.model);
				holder.model.modelRenderers.remove(this);
			}

			@Override
			public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
				RedirectModelRenderer.this.render(elem, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
				if(sel > 0)drawVanillaOutline(matrixStackIn, bufferIn);
			}
		}

		private void drawVanillaOutline(MatrixStack matrixStackIn, IVertexBuilder bufferIn) {
			float f = 0.001f;
			float g = f * 2;
			float scale = 1 / 16.0F;
			if(sel == 2)Render.drawOrigin(matrixStackIn, holder.buffer.getBuffer(CustomRenderTypes.getLinesNoDepth()), 1);
			for(ModelBox b : parent.cubeList) {
				WorldRenderer.drawBoundingBox(matrixStackIn, holder.buffer.getBuffer(CustomRenderTypes.getLinesNoDepth()),
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
				boolean newBuffer = false;
				if(cube.glow && sel == 0) {
					buffer = holder.buffer.getBuffer(RenderType.getEyes(holder.boundSkin));
					newBuffer = true;
				}
				((Box)cube.renderObject).draw(matrixStackIn, buffer, holder.buffer, packedLightIn, packedOverlayIn, r, g, b, alpha);
				if(newBuffer) {
					holder.buffer.getBuffer(RenderType.getEntityTranslucent(holder.boundSkin));
				}
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
				if(sel == 2)Render.drawOrigin(matrixStackIn, holder.buffer.getBuffer(CustomRenderTypes.getLinesNoDepth()), 1);
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

		private ModelRenderer swapIn() {
			if(parent != null) {
				//new Exception("Double swapping?").printStackTrace();
				return this;
			}
			parent = parentProvider.get();
			copyModel(parent, this);
			return this;
		}

		private ModelRenderer swapOut() {
			if(parent == null) {
				//new Exception("Double swapping?").printStackTrace();
				return parentProvider.get();
			}
			ModelRenderer p = parent;
			parent = null;
			return p;
		}

		private static void copyModel(ModelRenderer s, ModelRenderer d) {
			d.rotationPointX = s.rotationPointX;
			d.rotationPointY = s.rotationPointY;
			d.rotationPointZ = s.rotationPointZ;
			d.rotateAngleX   = s.rotateAngleX  ;
			d.rotateAngleY   = s.rotateAngleY  ;
			d.rotateAngleZ   = s.rotateAngleZ  ;
			d.showModel      = s.showModel     ;
		}
	}

	public static boolean unbindHand(Object v) {
		RedirectModelRenderer rd = (RedirectModelRenderer) v;
		return rd.parent == rd.holder.bipedLeftArmwear.parent || rd.parent == rd.holder.bipedRightArmwear.parent;
	}

	/*@Override
	public void buildHiddenModelPart(IGui gui, Editor editor, ModelElement elem, PlayerModelParts part) {
		PlayerRenderer rp = Minecraft.getInstance().getRenderManager().getSkinMap().get(editor.skinType == 1 ? "default" : "slim");
		PlayerModel<AbstractClientPlayerEntity> p = rp.getEntityModel();
		p.isChild = false;
		p.leftArmPose = ArmPose.EMPTY;
		p.rightArmPose = ArmPose.EMPTY;
		p.setVisible(true);
		p.bipedHeadwear.showModel = false;
		p.bipedBodyWear.showModel = false;
		p.bipedLeftLegwear.showModel = false;
		p.bipedRightLegwear.showModel = false;
		p.bipedLeftArmwear.showModel = false;
		p.bipedRightArmwear.showModel = false;
		p.isSneak = false;
		p.swingProgress = 0;
		p.isSitting = false;
		p.setRotationAngles(new FakePlayer(), 0, 0, 0, 0, 0);
		ModelRenderer mp = getPart(p, part);
		ModelBox box = mp.cubeList.get(0);
		elem.size = new Vec3f(box.posX2 - box.posX1, box.posY2 - box.posY1, box.posZ2 - box.posZ1);
		elem.offset = new Vec3f(box.posX1, box.posY1, box.posZ1);
		elem.pos = new Vec3f();
		elem.rotation = new Vec3f();
		elem.texture = true;
	}

	private ModelRenderer getPart(PlayerModel<AbstractClientPlayerEntity> p, PlayerModelParts part) {
		switch (part) {
		case BODY:
			return p.bipedBody;
		case HEAD:
			return p.bipedHead;
		case LEFT_ARM:
			return p.bipedLeftArm;
		case LEFT_LEG:
			return p.bipedLeftLeg;
		case RIGHT_ARM:
			return p.bipedRightArm;
		case RIGHT_LEG:
			return p.bipedRightLeg;
		default:
			return null;
		}
	}*/

	@Override
	public AnimationEngine getAnimationEngine() {
		return animEngine;
	}
}
