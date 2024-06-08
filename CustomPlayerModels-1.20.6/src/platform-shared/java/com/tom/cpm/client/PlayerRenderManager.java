package com.tom.cpm.client;

import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.VBuffers;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.client.optifine.OptifineTexture;
import com.tom.cpm.client.optifine.RedirectModelRendererOF;
import com.tom.cpm.client.vr.VRPlayerRenderer;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.BatchedBuffers;
import com.tom.cpm.shared.model.render.ModelRenderManager;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureProvider;

public class PlayerRenderManager extends ModelRenderManager<MultiBufferSource, ModelTexture, ModelPart, Model> {
	public static final Function<ResourceLocation, RenderType> armor = RenderType::armorCutoutNoCull;
	public static final Function<ResourceLocation, RenderType> entity = RenderType::entityTranslucent;

	public PlayerRenderManager() {
		setFactory(new RedirectHolderFactory<MultiBufferSource, ModelTexture, ModelPart>() {

			@SuppressWarnings("unchecked")
			@Override
			public <M> RedirectHolder<?, MultiBufferSource, ModelTexture, ModelPart> create(
					M model, String arg) {
				if ("api".equals(arg) && model instanceof HumanoidModel) {
					return new RedirectHolderApi(PlayerRenderManager.this, (HumanoidModel<LivingEntity>) model);
				} else if(CustomPlayerModelsClient.vrLoaded && VRPlayerRenderer.isVRPlayer(model)) {
					return VRPlayerRenderer.createVRPlayer(PlayerRenderManager.this, model);
				} else if(model instanceof PlayerModel) {
					return new RedirectHolderPlayer(PlayerRenderManager.this, (PlayerModel<LivingEntity>) model);
				} else if(model instanceof SkullModel) {
					return new RedirectHolderSkull(PlayerRenderManager.this, (SkullModel) model);
				} else if(model instanceof ElytraModel) {
					return new RedirectHolderElytra(PlayerRenderManager.this, (ElytraModel<LivingEntity>) model);
				} else if(model instanceof HumanoidModel && "armor1".equals(arg)) {
					return new RedirectHolderArmor1(PlayerRenderManager.this, (HumanoidModel<LivingEntity>) model);
				} else if(model instanceof HumanoidModel && "armor2".equals(arg)) {
					return new RedirectHolderArmor2(PlayerRenderManager.this, (HumanoidModel<LivingEntity>) model);
				}
				return null;
			}
		});
		setRedirectFactory(new RedirectRendererFactory<Model, ModelTexture, ModelPart>() {

			@Override
			public RedirectRenderer<ModelPart> create(Model model,
					RedirectHolder<Model, ?, ModelTexture, ModelPart> access,
					Supplier<ModelPart> modelPart, VanillaModelPart part) {
				return CustomPlayerModelsClient.optifineLoaded ?
						new RedirectModelRendererOF((RDH) access, modelPart, part) :
							new RedirectModelRendererVanilla((RDH) access, modelPart, part);
			}
		});
		setVis(m -> m.visible, (m, v) -> m.visible = v);
		setModelPosGetters(m -> m.x, m -> m.y, m -> m.z);
		setModelRotGetters(m -> m.xRot, m -> m.yRot, m -> m.zRot);
		setModelSetters((m, x, y, z) -> {
			m.x = x;
			m.y = y;
			m.z = z;
		}, (m, x, y, z) -> {
			m.xRot = x;
			m.yRot = y;
			m.zRot = z;
		});
		setRenderPart(new ModelPart(Collections.emptyList(), Collections.emptyMap()));
	}

	public static abstract class RDH extends ModelRenderManager.RedirectHolder<Model, MultiBufferSource, ModelTexture, ModelPart> {

		public RDH(
				ModelRenderManager<MultiBufferSource, ModelTexture, ModelPart, Model> mngr,
				Model model) {
			super(mngr, model);
			batch = new BatchedBuffers<>(this, MultiBufferSource::getBuffer);
		}

		@Override
		public void setupRenderSystem(ModelTexture cbi, TextureSheetType tex) {
			CustomPlayerModelsClient.mc.renderBuilder.build(renderTypes, cbi);
		}

		@Override
		protected void bindTexture(ModelTexture cbi, TextureProvider skin) {
			skin.bind();
			OptifineTexture.applyOptifineTexture(cbi.getTexture(), skin);
			cbi.setTexture(DynTexture.getBoundLoc());
		}

		@Override
		public void swapOut0() {
		}

		@Override
		public void swapIn0() {}
	}

	private static class RedirectHolderPlayer extends RDH {
		private RedirectRenderer<ModelPart> head;

		public RedirectHolderPlayer(PlayerRenderManager mngr, PlayerModel<LivingEntity> model) {
			super(mngr, model);
			head = registerHead(new Field<>(() -> model.head, v -> model.head = v, PlayerModelParts.HEAD));
			register(new Field<>(() -> model.body    , v -> model.body     = v, PlayerModelParts.BODY));
			register(new Field<>(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
			register(new Field<>(() -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
			register(new Field<>(() -> model.rightLeg, v -> model.rightLeg = v, PlayerModelParts.RIGHT_LEG));
			register(new Field<>(() -> model.leftLeg , v -> model.leftLeg  = v, PlayerModelParts.LEFT_LEG));

			register(new Field<>(() -> model.hat        , v -> model.hat         = v, null)).setCopyFrom(head);
			register(new Field<>(() -> model.leftSleeve , v -> model.leftSleeve  = v, null));
			register(new Field<>(() -> model.rightSleeve, v -> model.rightSleeve = v, null));
			register(new Field<>(() -> model.leftPants  , v -> model.leftPants   = v, null));
			register(new Field<>(() -> model.rightPants , v -> model.rightPants  = v, null));
			register(new Field<>(() -> model.jacket     , v -> model.jacket      = v, null));

			register(new Field<>(() -> model.cloak, v -> model.cloak = v, RootModelType.CAPE));
		}
	}

	private static class RedirectHolderApi extends RDH {
		private RedirectRenderer<ModelPart> head;

		public RedirectHolderApi(PlayerRenderManager mngr, HumanoidModel<LivingEntity> model) {
			super(mngr, model);
			head = registerHead(new Field<>(() -> model.head, v -> model.head = v, PlayerModelParts.HEAD));
			register(new Field<>(() -> model.body    , v -> model.body     = v, PlayerModelParts.BODY));
			register(new Field<>(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
			register(new Field<>(() -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
			register(new Field<>(() -> model.rightLeg, v -> model.rightLeg = v, PlayerModelParts.RIGHT_LEG));
			register(new Field<>(() -> model.leftLeg , v -> model.leftLeg  = v, PlayerModelParts.LEFT_LEG));

			register(new Field<>(() -> model.hat        , v -> model.hat         = v, null)).setCopyFrom(head);

			if(model instanceof PlayerModel) {
				PlayerModel<LivingEntity> mp = (PlayerModel<LivingEntity>) model;
				register(new Field<>(() -> mp.leftSleeve , v -> mp.leftSleeve  = v, null));
				register(new Field<>(() -> mp.rightSleeve, v -> mp.rightSleeve = v, null));
				register(new Field<>(() -> mp.leftPants  , v -> mp.leftPants   = v, null));
				register(new Field<>(() -> mp.rightPants , v -> mp.rightPants  = v, null));
				register(new Field<>(() -> mp.jacket     , v -> mp.jacket      = v, null));

				register(new Field<>(() -> mp.cloak, v -> mp.cloak = v, RootModelType.CAPE));
			}
		}
	}

	private static class RedirectHolderSkull extends RDH {

		public RedirectHolderSkull(PlayerRenderManager mngr, SkullModel model) {
			super(mngr, model);

			register(new Field<>(() -> model.head, v -> model.head = v, PlayerModelParts.HEAD));
		}

	}

	private static class RedirectHolderElytra extends RDH {

		public RedirectHolderElytra(PlayerRenderManager mngr, ElytraModel<LivingEntity> model) {
			super(mngr, model);

			register(new Field<>(() -> model.rightWing, v -> model.rightWing = v, RootModelType.ELYTRA_RIGHT));
			register(new Field<>(() -> model.leftWing,  v -> model.leftWing  = v, RootModelType.ELYTRA_LEFT));
		}

	}

	private static class RedirectHolderArmor1 extends RDH {

		public RedirectHolderArmor1(PlayerRenderManager mngr, HumanoidModel<LivingEntity> model) {
			super(mngr, model);

			register(new Field<>(() -> model.head,     v -> model.head     = v, RootModelType.ARMOR_HELMET));
			register(new Field<>(() -> model.body,     v -> model.body     = v, RootModelType.ARMOR_BODY));
			register(new Field<>(() -> model.rightArm, v -> model.rightArm = v, RootModelType.ARMOR_RIGHT_ARM));
			register(new Field<>(() -> model.leftArm,  v -> model.leftArm  = v, RootModelType.ARMOR_LEFT_ARM));
			register(new Field<>(() -> model.rightLeg, v -> model.rightLeg = v, RootModelType.ARMOR_RIGHT_FOOT));
			register(new Field<>(() -> model.leftLeg,  v -> model.leftLeg  = v, RootModelType.ARMOR_LEFT_FOOT));

			register(new Field<>(() -> model.hat , v -> model.hat  = v, null));
		}

	}

	private static class RedirectHolderArmor2 extends RDH {

		public RedirectHolderArmor2(PlayerRenderManager mngr, HumanoidModel<LivingEntity> model) {
			super(mngr, model);

			register(new Field<>(() -> model.body,     v -> model.body     = v, RootModelType.ARMOR_LEGGINGS_BODY));
			register(new Field<>(() -> model.rightLeg, v -> model.rightLeg = v, RootModelType.ARMOR_RIGHT_LEG));
			register(new Field<>(() -> model.leftLeg,  v -> model.leftLeg  = v, RootModelType.ARMOR_LEFT_LEG));
		}

	}

	public static abstract class RedirectModelRendererBase extends ModelPart implements RedirectRenderer<ModelPart> {
		protected final RDH holder;
		protected final VanillaModelPart part;
		protected final Supplier<ModelPart> parentProvider;
		protected ModelPart parent;
		protected VBuffers buffers;

		public RedirectModelRendererBase(RDH holder, Supplier<ModelPart> parent, VanillaModelPart part) {
			super(Collections.emptyList(), Collections.emptyMap());
			this.part = part;
			this.holder = holder;
			this.parentProvider = parent;
		}

		@Override
		public VBuffers getVBuffers() {
			return buffers;
		}

		@Override
		public ModelPart swapIn() {
			if(parent != null)return this;
			parent = parentProvider.get();
			holder.copyModel(parent, this);
			setInitialPose(parent.getInitialPose());
			return this;
		}

		@Override
		public ModelPart swapOut() {
			if(parent == null)return parentProvider.get();
			ModelPart p = parent;
			parent = null;
			return p;
		}

		@Override
		public RedirectHolder<?, ?, ?, ModelPart> getHolder() {
			return holder;
		}

		@Override
		public ModelPart getParent() {
			return parent;
		}

		@Override
		public VanillaModelPart getPart() {
			return part;
		}

		protected float red, green, blue, alpha;

		@Override
		public Vec4f getColor() {
			return new Vec4f(red, green, blue, alpha);
		}

		@Override
		public void translateAndRotate(PoseStack stack) {
			MatrixStack.Entry e = getPartTransform();
			if(e != null) {
				multiplyStacks(e, stack);
			} else
				super.translateAndRotate(stack);
		}

		@Override
		public Cube getRandomCube(RandomSource pRandom) {
			if(parent != null)return parent.getRandomCube(pRandom);
			return super.getRandomCube(pRandom);
		}
	}

	public static class RedirectModelRendererVanilla extends RedirectModelRendererBase {

		public RedirectModelRendererVanilla(RDH holder, Supplier<ModelPart> parent, VanillaModelPart part) {
			super(holder, parent, part);
		}

		private PoseStack matrixStackIn;
		private VertexConsumer bufferIn;
		private int packedLightIn, packedOverlayIn;

		@Override
		public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
			if(!holder.renderTypes.isInitialized()) {
				holder.copyModel(this, parent);
				parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
				holder.logWarning();
				return;
			}
			this.matrixStackIn   = matrixStackIn  ;
			this.bufferIn        = bufferIn       ;
			this.packedLightIn   = packedLightIn  ;
			this.packedOverlayIn = packedOverlayIn;
			this.red             = red            ;
			this.green           = green          ;
			this.blue            = blue           ;
			this.alpha           = alpha          ;
			this.buffers = holder.nextBatch(() -> new VBufferOut(packedLightIn, packedOverlayIn, matrixStackIn), bufferIn);
			//this.buffers = new VBuffers(rt -> new VBuffer(holder.addDt.getBuffer(rt.getNativeType()), packedLightIn, packedOverlayIn, matrixStackIn), new VBuffer(bufferIn, packedLightIn, packedOverlayIn, matrixStackIn));
			render();
			//holder.addDt.getBuffer(holder.renderTypes.get(RenderMode.DEFAULT).getNativeType());
			this.matrixStackIn = null;
			this.bufferIn = null;
		}

		@Override
		public void renderParent() {
			parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		}
	}

	public static void multiplyStacks(MatrixStack.Entry e, PoseStack stack) {
		stack.last().pose().mul(new Matrix4f().setTransposed(e.getMatrixArray()));
		stack.last().normal().mul(new Matrix3f().set(e.getNormalArray3()).transpose());
	}
}
