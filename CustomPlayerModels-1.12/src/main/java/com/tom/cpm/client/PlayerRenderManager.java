package com.tom.cpm.client;

import static com.tom.cpm.client.PlayerModelSetup.scale;

import java.util.function.Supplier;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelHumanoidHead;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;

import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.ModelRenderManager;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.model.render.VanillaModelPart;
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
					VanillaModelPart part) {
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
			renderTypes.put(RenderMode.NORMAL, new NativeRenderType(RetroGL.texture(), 0));
			renderTypes.put(RenderMode.GLOW, new NativeRenderType(RetroGL.eyes(), 1));
			renderTypes.put(RenderMode.OUTLINE, new NativeRenderType(RetroGL.linesNoDepth(), 2));
			renderTypes.put(RenderMode.COLOR, new NativeRenderType(RetroGL.color(), 0));
		}

		@Override protected void bindTexture(Void cbi, TextureSheetType tex) {}
		@Override public void swapOut0() {}
		@Override public void swapIn0() {}
	}

	private static class RedirectModelRenderer extends ModelRenderer implements RedirectRenderer<ModelRenderer> {
		private final RDH holder;
		private final VanillaModelPart part;
		private final Supplier<ModelRenderer> parentProvider;
		private ModelRenderer parent;
		private VBuffers buffers;

		public RedirectModelRenderer(RDH holder, Supplier<ModelRenderer> parent, VanillaModelPart part) {
			super(holder.model);
			this.holder = holder;
			holder.model.boxList.remove(this);
			this.parentProvider = parent;
			this.part = part;
		}

		@Override
		public void render(float scale) {
			this.buffers = new VBuffers(RetroGL::buffer);
			render();
			buffers.finishAll();
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
		public VanillaModelPart getPart() {
			return part;
		}

		@Override
		public void renderParent() {
			parent.render(scale);
		}

		@Override
		public VBuffers getVBuffers() {
			return buffers;
		}

		@Override
		public Vec4f getColor() {
			return RetroGL.getColor();
		}
	}
}
