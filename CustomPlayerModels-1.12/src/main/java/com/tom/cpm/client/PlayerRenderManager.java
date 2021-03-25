package com.tom.cpm.client;

import static com.tom.cpm.client.PlayerModelSetup.scale;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelHumanoidHead;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;

import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.ModelRenderManager;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RenderedCube.ElementSelectMode;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.skin.TextureProvider;

public class PlayerRenderManager extends ModelRenderManager<Void, Void, ModelRenderer, ModelBase> {

	public PlayerRenderManager(ModelDefinitionLoader loader) {
		super(loader);
		setFactory(new RedirectHolderFactory<Void, Void, ModelRenderer>() {

			@SuppressWarnings("unchecked")
			@Override
			public <M> RedirectHolder<M, Void, Void, ModelRenderer> create(
					M model) {
				if(model instanceof ModelPlayer) {
					return (RedirectHolder<M, Void, Void, ModelRenderer>)
							new RedirectHolderPlayer(PlayerRenderManager.this, (ModelPlayer) model);
				} else if(model instanceof ModelHumanoidHead) {
					return (RedirectHolder<M, Void, Void, ModelRenderer>)
							new RedirectHolderSkull(PlayerRenderManager.this, (ModelHumanoidHead) model);
				}
				return null;
			}
		});
		setRedirectFactory(new RedirectRendererFactory<ModelBase, Void, ModelRenderer>() {

			@Override
			public RedirectRenderer<ModelRenderer> create(ModelBase model,
					RedirectHolder<ModelBase, ?, Void, ModelRenderer> access, Supplier<ModelRenderer> modelPart,
					ModelPart part) {
				return new RedirectModelRenderer((RDH) access, modelPart, part);
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
			((DisplayList)cube.renderObject).free();
			cube.renderObject = null;
		}
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

	private static class RedirectHolderPlayer extends RDH {
		private RedirectRenderer<ModelRenderer> bipedHead;
		private RedirectRenderer<ModelRenderer> bipedLeftArm;
		private RedirectRenderer<ModelRenderer> bipedRightArm;
		private RedirectRenderer<ModelRenderer> bipedLeftArmwear;
		private RedirectRenderer<ModelRenderer> bipedRightArmwear;

		public RedirectHolderPlayer(PlayerRenderManager mngr, ModelPlayer model) {
			super(mngr, model);
			bipedHead = register(new Field<>(() -> model.bipedHead        , v -> model.bipedHead         = v, PlayerModelParts.HEAD), p -> !((PlayerProfile)p).hasPlayerHead);
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

		@Override
		public boolean skipTransform(RedirectRenderer<ModelRenderer> part) {
			ModelPlayer model = (ModelPlayer) this.model;
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

		public RedirectHolderSkull(PlayerRenderManager mngr, ModelHumanoidHead model) {
			super(mngr, model);

			register(new Field<>(() -> model.skeletonHead, v -> model.skeletonHead = v, PlayerModelParts.HEAD));
			hat = register(new Field<>(() -> model.head, v -> model.head = v, null));
		}
	}

	private abstract static class RDH extends ModelRenderManager.RedirectHolder<ModelBase, Void, Void, ModelRenderer> {

		public RDH(ModelRenderManager<Void, Void, ModelRenderer, ModelBase> mngr, ModelBase model) {
			super(mngr, model);
		}

		@Override
		protected void bindSkin() {
			TextureProvider skin = def.getSkinOverride();
			if(skin != null) {
				if(!def.isEditor())skin.bind();
				sheetX = skin.getSize().x;
				sheetY = skin.getSize().y;
			} else {
				sheetX = 64;
				sheetY = 64;
			}
		}

		@Override protected void bindTexture(Void cbi) {}
		@Override public void swapOut0() {}
		@Override public void swapIn0() {}
	}

	private static class RedirectModelRenderer extends ModelRenderer implements RedirectRenderer<ModelRenderer> {
		private final RDH holder;
		private final ModelPart part;
		private final Supplier<ModelRenderer> parentProvider;
		private ModelRenderer parent;
		private RootModelElement elem;
		private CubeModelRenderer dispRender;

		public RedirectModelRenderer(RDH holder, Supplier<ModelRenderer> parent, ModelPart part) {
			super(holder.model);
			this.holder = holder;
			holder.model.boxList.remove(this);
			this.parentProvider = parent;
			this.part = part;
			this.dispRender = new CubeModelRenderer();
		}

		@Override
		public void render(float scale) {
			render();
		}

		private class CubeModelRenderer extends ModelRenderer {
			public CubeModelRenderer() {
				super(holder.model);
				holder.model.boxList.remove(this);
			}

			@Override
			public void render(float scale) {
				RedirectModelRenderer.this.render(elem, scale);
				if(holder.def.isEditor() && elem.getSelected().isRenderOutline())drawVanillaOutline(scale);
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
			ElementSelectMode sel = elem.getSelected();
			if(sel == ElementSelectMode.SELECTED)Render.drawOrigin(1);
			GlStateManager.disableCull();

			float f = 0.001f;
			float g = f * 2;
			for(ModelBox b : parent.cubeList) {
				Render.drawCubeOutline(b.posX1 * scale - f, b.posY1 * scale - f, b.posZ1 * scale - f, (b.posX2 - b.posX1)  * scale + g, (b.posY2 - b.posY1) * scale + g, (b.posZ2 - b.posZ1) * scale + g, 1, 1, sel == ElementSelectMode.SELECTED ? 1 : 0);
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
						GlStateManager.glNewList(((DisplayList)cube.renderObject).list, 4864);
					} else {
						GlStateManager.pushMatrix();
						GlStateManager.translate(cube.pos.x * scale, cube.pos.y * scale, cube.pos.z * scale);

						if (cube.rotation.z != 0.0F)
							GlStateManager.rotate(cube.rotation.z * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);

						if (cube.rotation.y != 0.0F)
							GlStateManager.rotate(cube.rotation.y * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);

						if (cube.rotation.x != 0.0F)
							GlStateManager.rotate(cube.rotation.x * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);

						if(cube.color != 0xffffff && (!holder.def.isEditor() || cube.getSelected().applyColor())) {
							float r = ((cube.color & 0xff0000) >> 16) / 255f;
							float g = ((cube.color & 0x00ff00) >> 8 ) / 255f;
							float b = ( cube.color & 0x0000ff       ) / 255f;
							GlStateManager.color(r, g, b, 1);
						}
					}

					drawBox(cube, scale);
					if(cube.useDynamic) {
						if(holder.def.isEditor())drawSelect(cube, scale);
						render(cube, scale);
					}

					if(!cube.useDynamic) {
						GlStateManager.glEndList();
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
			float lx = OpenGlHelper.lastBrightnessX, ly = OpenGlHelper.lastBrightnessY;
			if(cube.glow) {
				GlStateManager.enableBlend();
				GlStateManager.disableAlpha();
				GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
				GlStateManager.depthMask(true);
				int i = 0xF0;
				int j = i % 65536;
				int k = i / 65536;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
				Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
			}
			((DisplayList)cube.renderObject).call();
			if(cube.color != 0xffffff)GlStateManager.color(1, 1, 1, 1);
			if(cube.glow) {
				Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lx, ly);
				GlStateManager.disableBlend();
				GlStateManager.enableAlpha();
			}
		}

		private void drawSelect(RenderedCube cube, float scale) {
			ElementSelectMode sel = cube.getSelected();

			if(sel.isRenderOutline()) {
				float f = 0.001f;
				float g = f * 2;
				Cube c = cube.getCube();
				GlStateManager.disableDepth();
				GlStateManager.disableTexture2D();
				boolean s = sel == ElementSelectMode.SELECTED;
				if(s)Render.drawOrigin(1);
				Render.drawCubeOutline(
						c.offset.x * scale - f, c.offset.y * scale - f, c.offset.z * scale - f,
						c.size.x * scale * c.scale.x + g, c.size.y * scale * c.scale.y + g, c.size.z * scale * c.scale.z + g,
						s ? 1 : 0.5f, s ? 1 : 0.5f, s ? 1 : 0);
				GlStateManager.enableTexture2D();
				GlStateManager.enableDepth();
			}
		}

		private void drawBox(RenderedCube elem, float scale) {
			Cube c = elem.getCube();
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
		}

		@Override
		public ModelRenderer swapIn() {
			if(parent != null) {
				//new Exception("Double swapping?").printStackTrace();
				return this;
			}
			parent = parentProvider.get();
			copyModel(parent, this);
			return this;
		}

		@Override
		public ModelRenderer swapOut() {
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
		public void renderParent() {
			parent.render(scale);
		}

		@Override
		public void renderWithParent(RootModelElement elem) {
			this.elem = elem;
			parent.addChild(dispRender);
			parent.render(scale);
			parent.childModels.remove(dispRender);
			this.elem = null;
		}

		@Override
		public void doRender(RootModelElement elem) {
			this.elem = elem;
			if(holder.def.isEditor() && elem.getSelected().isRenderOutline())drawSelectionOutline(scale);
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
			this.elem = null;
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
