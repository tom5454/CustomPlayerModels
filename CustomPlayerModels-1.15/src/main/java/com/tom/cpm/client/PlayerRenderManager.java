package com.tom.cpm.client;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.HumanoidHeadModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.render.ModelRenderManager;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.model.render.VanillaModelPart;
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
					Supplier<ModelRenderer> modelPart, VanillaModelPart part) {
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

	public static abstract class RDH extends ModelRenderManager.RedirectHolder<Model, IRenderTypeBuffer, CallbackInfoReturnable<ResourceLocation>, ModelRenderer> {
		public ResourceLocation boundSkin;

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
			renderTypes.put(RenderMode.NORMAL, new NativeRenderType(RenderType.getEntityTranslucent(boundSkin), 0));
			renderTypes.put(RenderMode.GLOW, new NativeRenderType(RenderType.getEyes(boundSkin), 1));
			renderTypes.put(RenderMode.OUTLINE, new NativeRenderType(CustomRenderTypes.getLinesNoDepth(), 2));
			renderTypes.put(RenderMode.COLOR, new NativeRenderType(CustomRenderTypes.getEntityColorTranslucentCull(), 0));
		}

		@Override
		public void swapOut0() {
			this.boundSkin = null;
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
		private final VanillaModelPart part;
		private final Supplier<ModelRenderer> parentProvider;
		private ModelRenderer parent;
		private VBuffers buffers;

		@SuppressWarnings("unchecked")
		public RedirectModelRenderer(Model model, RDH holder, Supplier<ModelRenderer> parent, VanillaModelPart part) {
			super(model);
			this.part = part;
			this.holder = holder;
			this.parentProvider = parent;
			if(model instanceof PlayerModel)
				((PlayerModel<AbstractClientPlayerEntity>)model).modelRenderers.remove(this);
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
			this.buffers = new VBuffers(rt -> new VBuffer(holder.addDt.getBuffer(rt.getNativeType()), packedLightIn, packedOverlayIn, matrixStackIn));
			render();
			holder.addDt.getBuffer(holder.renderTypes.get(RenderMode.NORMAL).getNativeType());
			this.matrixStackIn = null;
			this.bufferIn = null;
		}

		@Override
		public void renderParent() {
			parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
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
		public VanillaModelPart getPart() {
			return part;
		}

		@Override
		public VBuffers getVBuffers() {
			return buffers;
		}
	}
}
