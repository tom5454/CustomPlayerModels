package com.tom.cpm.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;

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
	private Map<ModelPlayer, RedirectHolder> holders = new HashMap<>();
	private AnimationEngine animEngine = new AnimationEngine();

	public PlayerRenderManager(ModelDefinitionLoader loader) {
		this.loader = loader;
	}

	public void bindModel(ModelPlayer model, ModelDefinition def, Predicate<Object> unbindRule) {
		holders.computeIfAbsent(model, RedirectHolder::new).swapIn(def, unbindRule);
	}

	public void unbindModel(ModelPlayer model) {
		RedirectHolder h = holders.get(model);
		if(h != null)h.swapOut();
	}

	@Override
	public void cleanupRenderedCube(RenderedCube cube) {
		if(cube.renderObject != null) {
			((DisplayList)cube.renderObject).free();
			cube.renderObject = null;
		}
	}

	@Override
	public ModelDefinitionLoader getLoader() {
		return loader;
	}

	private static class DisplayList {
		private int list;

		public DisplayList() {
			this.list = GLAllocation.generateDisplayLists(1);
		}

		public void call() {
			GlStateManager.callList(list);
		}

		public void free() {
			GLAllocation.deleteDisplayLists(list);
		}
	}

	private static class RedirectHolder {
		private final ModelPlayer model;
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
		private boolean skinBound, swappedIn;
		private int sheetX, sheetY;

		public RedirectHolder(ModelPlayer model) {
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

		public void swapIn(ModelDefinition def, Predicate<Object> unbindRule) {
			this.def = def;
			this.unbindRule = unbindRule;
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
			this.skinBound = false;
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
			holder.model.boxList.remove(this);
			this.parentProvider = parent;
			this.part = part;
			this.dispRender = new CubeModelRenderer();
		}

		@Override
		public void render(float scale) {
			if(holder.def != null) {
				if(!holder.skinBound) {
					SkinProvider skin = holder.def.getSkinOverride();
					if(skin != null) {
						if(!holder.def.isEditor())skin.bind();
						holder.sheetX = skin.getSize().x;
						holder.sheetY = skin.getSize().y;
					} else {
						holder.sheetX = 64;
						holder.sheetY = 64;
					}
					holder.skinBound = true;
				}
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
				boolean skipTransform = false;
				if((holder.bipedLeftArm == this || holder.bipedRightArm == this) && holder.model.aimedBow) {
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
					parent.render(scale);
					parent.rotationPointX = px;
					parent.rotationPointY = py;
					parent.rotationPointZ = pz;
					parent.rotateAngleX = rx;
					parent.rotateAngleY = ry;
					parent.rotateAngleZ = rz;
					parent.childModels.remove(dispRender);
				} else {
					if(holder.def.isEditor()) {
						//GlStateManager.color(1, 1, 1, 0.5f);
						//parent.render(scale);
						//GlStateManager.color(1, 1, 1, 1);
					}
					if(sel > 0)drawSelectionOutline(scale);
					GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);
					if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
						if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F) {
							render(elem, scale);
						} else {
							GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
							render(elem, scale);
							GlStateManager.translate(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);
						}
					} else {
						GlStateManager.pushMatrix();
						GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);
						GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

						if (this.rotateAngleZ != 0.0F)
							GlStateManager.rotate(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);

						if (this.rotateAngleY != 0.0F)
							GlStateManager.rotate(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);

						if (this.rotateAngleX != 0.0F)
							GlStateManager.rotate(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);

						render(elem, scale);
						GlStateManager.popMatrix();
					}
					GlStateManager.translate(-this.offsetX, -this.offsetY, -this.offsetZ);
					this.rotationPointX = px;
					this.rotationPointY = py;
					this.rotationPointZ = pz;
					this.rotateAngleX = rx;
					this.rotateAngleY = ry;
					this.rotateAngleZ = rz;
				}
				elem = null;
			} else {
				copyModel(this, parent);
				parent.render(scale);
			}
			if(holder.unbindRule != null && holder.unbindRule.test(this))
				holder.swapOut();
		}

		private class CubeModelRenderer extends ModelRenderer {
			public CubeModelRenderer() {
				super(holder.model);
				holder.model.boxList.remove(this);
			}

			@Override
			public void render(float scale) {
				RedirectModelRenderer.this.render(elem, scale);
				if(sel > 0)drawVanillaOutline(scale);
			}
		}

		private void drawSelectionOutline(float scale) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);
			GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

			if (this.rotateAngleZ != 0.0F)
				GlStateManager.rotate(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);

			if (this.rotateAngleY != 0.0F)
				GlStateManager.rotate(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);

			if (this.rotateAngleX != 0.0F)
				GlStateManager.rotate(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);

			drawVanillaOutline(scale);

			GlStateManager.popMatrix();
		}

		private void drawVanillaOutline(float scale) {
			GlStateManager.disableDepth();
			GlStateManager.disableTexture2D();

			GlStateManager.enableCull();
			if(sel == 2)Render.drawOrigin(1);
			GlStateManager.disableCull();

			float f = 0.001f;
			float g = f * 2;
			for(ModelBox b : parent.cubeList) {
				Render.drawCubeOutline(b.posX1 * scale - f, b.posY1 * scale - f, b.posZ1 * scale - f, (b.posX2 - b.posX1)  * scale + g, (b.posY2 - b.posY1) * scale + g, (b.posZ2 - b.posZ1) * scale + g, 1, 1, sel == 2 ? 1 : 0);
			}
			GlStateManager.enableTexture2D();
			GlStateManager.enableDepth();
		}

		private void render(RenderedCube elem, float scale) {
			if(elem.children == null)return;
			for(RenderedCube cube : elem.children) {
				if(!cube.display)continue;
				if(cube.useDynamic || cube.renderObject == null) {
					if(!cube.useDynamic) {
						cube.renderObject = new DisplayList();
						GL11.glNewList(((DisplayList)cube.renderObject).list, 4864);
					} else {
						GlStateManager.pushMatrix();
						GlStateManager.translate(cube.pos.x * scale, cube.pos.y * scale, cube.pos.z * scale);

						if (cube.rotation.z != 0.0F)
							GlStateManager.rotate(cube.rotation.z * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);

						if (cube.rotation.y != 0.0F)
							GlStateManager.rotate(cube.rotation.y * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);

						if (cube.rotation.x != 0.0F)
							GlStateManager.rotate(cube.rotation.x * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);

						if(cube.color != 0xffffff) {
							float r = ((cube.color & 0xff0000) >> 16) / 255f;
							float g = ((cube.color & 0x00ff00) >> 8 ) / 255f;
							float b = ( cube.color & 0x0000ff       ) / 255f;
							GlStateManager.color(r, g, b, 1);
						}
					}

					drawBox(cube, scale);
					if(cube.useDynamic) {
						drawSelect(cube, scale);
						render(cube, scale);
					}

					if(!cube.useDynamic) {
						GL11.glEndList();
					} else {
						GlStateManager.color(1, 1, 1, 1);
						GlStateManager.popMatrix();
						continue;
					}
				}
				if (cube.rotation.x == 0.0F && cube.rotation.y == 0.0F && cube.rotation.z == 0.0F) {
					if (cube.pos.x == 0.0F && cube.pos.y == 0.0F && cube.pos.z == 0.0F) {
						callList(cube, scale);
						render(cube, scale);
					} else {
						GlStateManager.translate(cube.pos.x * scale, cube.pos.y * scale, cube.pos.z * scale);
						callList(cube, scale);
						render(cube, scale);
						GlStateManager.translate(-cube.pos.x * scale, -cube.pos.y * scale, -cube.pos.z * scale);
					}
				} else {
					GlStateManager.pushMatrix();
					GlStateManager.translate(cube.pos.x * scale, cube.pos.y * scale, cube.pos.z * scale);

					if (cube.rotation.z != 0.0F)
						GlStateManager.rotate(cube.rotation.z * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);

					if (cube.rotation.y != 0.0F)
						GlStateManager.rotate(cube.rotation.y * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);

					if (cube.rotation.x != 0.0F)
						GlStateManager.rotate(cube.rotation.x * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);

					callList(cube, scale);
					render(cube, scale);

					GlStateManager.popMatrix();
				}
			}
		}

		private void callList(RenderedCube cube, float scale) {
			if(cube.color != 0xffffff) {
				float r = ((cube.color & 0xff0000) >> 16) / 255f;
				float g = ((cube.color & 0x00ff00) >> 8 ) / 255f;
				float b = ( cube.color & 0x0000ff       ) / 255f;
				GlStateManager.color(r, g, b, 1);
			}
			((DisplayList)cube.renderObject).call();
			if(cube.color != 0xffffff)GlStateManager.color(1, 1, 1, 1);
			drawSelect(cube, scale);
		}

		private void drawSelect(RenderedCube cube, float scale) {
			int sel = cube.getSelected();

			if(sel > 0) {
				float f = 0.001f;
				float g = f * 2;
				Cube c = cube.getCube();
				GlStateManager.disableDepth();
				GlStateManager.disableTexture2D();
				if(sel == 2)Render.drawOrigin(1);
				Render.drawCubeOutline(
						c.offset.x * scale - f, c.offset.y * scale - f, c.offset.z * scale - f,
						c.size.x * scale * c.scale.x + g, c.size.y * scale * c.scale.y + g, c.size.z * scale * c.scale.z + g,
						sel == 2 ? 1 : 0.5f, sel == 2 ? 1 : 0.5f, sel == 2 ? 1 : 0);
				GlStateManager.enableTexture2D();
				GlStateManager.enableDepth();
			}
		}

		private void drawBox(RenderedCube elem, float scale) {
			Cube c = elem.getCube();
			if(elem.glow)GlStateManager.disableLighting();
			if(c.texSize == 0) {
				GlStateManager.disableTexture2D();
				Render.drawSingleColorCube(
						c.offset.x * scale, c.offset.y * scale, c.offset.z * scale,
						c.size.x * scale * c.scale.x, c.size.y * scale * c.scale.y, c.size.z * scale * c.scale.z,
						c.mcScale);
				GlStateManager.enableTexture2D();
			} else {
				Render.drawTexturedCube(
						c.offset, c.size, c.scale,
						c.mcScale,
						c.u, c.v, c.texSize, holder.sheetX, holder.sheetY,
						scale);
			}
			if(elem.glow)GlStateManager.enableLighting();
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
				new Exception("Double swapping?").printStackTrace();
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
			d.isHidden       = s.isHidden      ;
			d.offsetX        = s.offsetX       ;
			d.offsetY        = s.offsetY       ;
			d.offsetZ        = s.offsetZ       ;
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
