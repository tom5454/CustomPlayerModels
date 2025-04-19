package com.tom.cpm.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;

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
					return new RedirectHolderApi(PlayerRenderManager.this, (HumanoidModel<HumanoidRenderState>) model);
				} else if(CustomPlayerModelsClient.vrLoaded && VRPlayerRenderer.isVRPlayer(model)) {
					return VRPlayerRenderer.createVRPlayer(PlayerRenderManager.this, model);
				} else if(model instanceof PlayerModel) {
					return new RedirectHolderPlayer(PlayerRenderManager.this, (PlayerModel) model);
				} else if(model instanceof SkullModel) {
					return new RedirectHolderSkull(PlayerRenderManager.this, (SkullModel) model);
				} else if(model instanceof PlayerCapeModel) {
					return new RedirectHolderCape(PlayerRenderManager.this, (PlayerCapeModel<PlayerRenderState>) model);
				} else if(model instanceof ElytraModel) {
					return new RedirectHolderElytra(PlayerRenderManager.this, (ElytraModel) model);
				} else if(model instanceof HumanoidModel && "armor1".equals(arg)) {
					return new RedirectHolderArmor1(PlayerRenderManager.this, (HumanoidModel<HumanoidRenderState>) model);
				} else if(model instanceof HumanoidModel && "armor2".equals(arg)) {
					return new RedirectHolderArmor2(PlayerRenderManager.this, (HumanoidModel<HumanoidRenderState>) model);
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
						new RedirectModelRendererOF((RDH<?>) access, modelPart, part) :
							new RedirectModelRendererVanilla((RDH<?>) access, modelPart, part);
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

	public static abstract class RDH<M extends Model> extends ModelRenderManager.RedirectHolder<Model, MultiBufferSource, ModelTexture, ModelPart> {
		public List<Supplier<ModelPart>> renderedParts = new ArrayList<>();
		private ModelPart rootPart;
		private RootModelPart rootRenderer;

		public RDH(
				ModelRenderManager<MultiBufferSource, ModelTexture, ModelPart, Model> mngr,
				M model) {
			super(mngr, model);
			batch = new BatchedBuffers<>(this, MultiBufferSource::getBuffer);
			rootPart = getRoot();
			rootRenderer = new RootModelPart(this);
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
			setRoot(rootPart);
		}

		@Override
		public void swapIn0() {
			setRoot(rootRenderer);
		}

		protected Field<ModelPart> createRendered(Supplier<ModelPart> get, Consumer<ModelPart> set, VanillaModelPart part) {
			renderedParts.add(get);
			return new Field<>(get, set, part);
		}

		protected Field<ModelPart> create2ndLayer(Supplier<ModelPart> get, Consumer<ModelPart> set, RedirectRenderer<ModelPart> layer2) {
			if (layer2 instanceof RedirectModelRendererBase r) {
				r.layer2 = get.get();
			}
			return new Field<>(get, set, null);
		}

		protected abstract ModelPart getRoot();
		protected abstract void setRoot(ModelPart part);

		@SuppressWarnings("unchecked")
		protected M model() {
			return (M) model;
		}
	}

	private static class RedirectHolderPlayer extends RDH<PlayerModel> {
		private RedirectRenderer<ModelPart> head;
		private RedirectRenderer<ModelPart> body;
		private RedirectRenderer<ModelPart> leftArm;
		private RedirectRenderer<ModelPart> rightArm;
		private RedirectRenderer<ModelPart> leftLeg;
		private RedirectRenderer<ModelPart> rightLeg;

		public RedirectHolderPlayer(PlayerRenderManager mngr, PlayerModel model) {
			super(mngr, model);
			head = registerHead(createRendered(() -> model.head, v -> model.head = v, PlayerModelParts.HEAD));
			body = register(createRendered(() -> model.body    , v -> model.body     = v, PlayerModelParts.BODY));
			rightArm = register(createRendered(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
			leftArm = register(createRendered(() -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
			rightLeg = register(createRendered(() -> model.rightLeg, v -> model.rightLeg = v, PlayerModelParts.RIGHT_LEG));
			leftLeg = register(createRendered(() -> model.leftLeg , v -> model.leftLeg  = v, PlayerModelParts.LEFT_LEG));

			register(create2ndLayer(() -> model.hat        , v -> model.hat         = v, head));
			register(create2ndLayer(() -> model.leftSleeve , v -> model.leftSleeve  = v, leftArm));
			register(create2ndLayer(() -> model.rightSleeve, v -> model.rightSleeve = v, rightArm));
			register(create2ndLayer(() -> model.leftPants  , v -> model.leftPants   = v, leftLeg));
			register(create2ndLayer(() -> model.rightPants , v -> model.rightPants  = v, rightLeg));
			register(create2ndLayer(() -> model.jacket     , v -> model.jacket      = v, body));
		}

		@Override
		protected ModelPart getRoot() {
			return model().root;
		}

		@Override
		protected void setRoot(ModelPart part) {
			model().root = part;
		}
	}

	private static class RedirectHolderApi extends RDH<HumanoidModel<? extends HumanoidRenderState>> {
		private RedirectRenderer<ModelPart> head;

		public RedirectHolderApi(PlayerRenderManager mngr, HumanoidModel<? extends HumanoidRenderState> model) {
			super(mngr, model);
			head = registerHead(createRendered(() -> model.head, v -> model.head = v, PlayerModelParts.HEAD));
			register(createRendered(() -> model.body    , v -> model.body     = v, PlayerModelParts.BODY));
			register(createRendered(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
			register(createRendered(() -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
			register(createRendered(() -> model.rightLeg, v -> model.rightLeg = v, PlayerModelParts.RIGHT_LEG));
			register(createRendered(() -> model.leftLeg , v -> model.leftLeg  = v, PlayerModelParts.LEFT_LEG));

			register(new Field<>(() -> model.hat        , v -> model.hat         = v, null)).setCopyFrom(head);

			if(model instanceof PlayerModel) {
				PlayerModel mp = (PlayerModel) model;
				register(new Field<>(() -> mp.leftSleeve , v -> mp.leftSleeve  = v, null));
				register(new Field<>(() -> mp.rightSleeve, v -> mp.rightSleeve = v, null));
				register(new Field<>(() -> mp.leftPants  , v -> mp.leftPants   = v, null));
				register(new Field<>(() -> mp.rightPants , v -> mp.rightPants  = v, null));
				register(new Field<>(() -> mp.jacket     , v -> mp.jacket      = v, null));
			}
		}

		@Override
		protected ModelPart getRoot() {
			return model().root;
		}

		@Override
		protected void setRoot(ModelPart part) {
			model().root = part;
		}
	}

	private static class RedirectHolderSkull extends RDH<SkullModel> {

		public RedirectHolderSkull(PlayerRenderManager mngr, SkullModel model) {
			super(mngr, model);

			register(createRendered(() -> model.head, v -> model.head = v, PlayerModelParts.HEAD));
		}

		@Override
		protected ModelPart getRoot() {
			return model().root;
		}

		@Override
		protected void setRoot(ModelPart part) {
			model().root = part;
		}
	}

	private static class RedirectHolderElytra extends RDH<ElytraModel> {

		public RedirectHolderElytra(PlayerRenderManager mngr, ElytraModel model) {
			super(mngr, model);

			register(createRendered(() -> model.rightWing, v -> model.rightWing = v, RootModelType.ELYTRA_RIGHT));
			register(createRendered(() -> model.leftWing,  v -> model.leftWing  = v, RootModelType.ELYTRA_LEFT));
		}

		@Override
		protected ModelPart getRoot() {
			return model().root;
		}

		@Override
		protected void setRoot(ModelPart part) {
			model().root = part;
		}
	}

	private static class RedirectHolderArmor1 extends RDH<HumanoidModel<HumanoidRenderState>> {

		public RedirectHolderArmor1(PlayerRenderManager mngr, HumanoidModel<HumanoidRenderState> model) {
			super(mngr, model);

			register(createRendered(() -> model.head,     v -> model.head     = v, RootModelType.ARMOR_HELMET));
			register(createRendered(() -> model.body,     v -> model.body     = v, RootModelType.ARMOR_BODY));
			register(createRendered(() -> model.rightArm, v -> model.rightArm = v, RootModelType.ARMOR_RIGHT_ARM));
			register(createRendered(() -> model.leftArm,  v -> model.leftArm  = v, RootModelType.ARMOR_LEFT_ARM));
			register(createRendered(() -> model.rightLeg, v -> model.rightLeg = v, RootModelType.ARMOR_RIGHT_FOOT));
			register(createRendered(() -> model.leftLeg,  v -> model.leftLeg  = v, RootModelType.ARMOR_LEFT_FOOT));

			register(new Field<>(() -> model.hat , v -> model.hat  = v, null));
		}

		@Override
		protected ModelPart getRoot() {
			return model().root;
		}

		@Override
		protected void setRoot(ModelPart part) {
			model().root = part;
		}
	}

	private static class RedirectHolderArmor2 extends RDH<HumanoidModel<HumanoidRenderState>> {

		public RedirectHolderArmor2(PlayerRenderManager mngr, HumanoidModel<HumanoidRenderState> model) {
			super(mngr, model);

			register(createRendered(() -> model.body,     v -> model.body     = v, RootModelType.ARMOR_LEGGINGS_BODY));
			register(createRendered(() -> model.rightLeg, v -> model.rightLeg = v, RootModelType.ARMOR_RIGHT_LEG));
			register(createRendered(() -> model.leftLeg,  v -> model.leftLeg  = v, RootModelType.ARMOR_LEFT_LEG));
		}

		@Override
		protected ModelPart getRoot() {
			return model().root;
		}

		@Override
		protected void setRoot(ModelPart part) {
			model().root = part;
		}
	}

	private static class RedirectHolderCape extends RDH<PlayerCapeModel<PlayerRenderState>> {

		public RedirectHolderCape(PlayerRenderManager mngr, PlayerCapeModel<PlayerRenderState> model) {
			super(mngr, model);

			register(createRendered(() -> model.cape,     v -> model.cape     = v, RootModelType.CAPE));
		}

		@Override
		protected ModelPart getRoot() {
			return model().root;
		}

		@Override
		protected void setRoot(ModelPart part) {
			model().root = part;
		}
	}

	public static class RootModelPart extends ModelPart {
		private RDH<?> holder;

		public RootModelPart(RDH<?> holder) {
			super(Collections.emptyList(), Collections.emptyMap());
			this.holder = holder;
		}

		@Override
		public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k) {
			poseStack.pushPose();
			this.translateAndRotate(poseStack);
			for (final Supplier<ModelPart> modelPart : holder.renderedParts) {
				modelPart.get().render(poseStack, vertexConsumer, i, j, k);
			}
			poseStack.popPose();
		}
	}

	public static abstract class RedirectModelRendererBase extends ModelPart implements RedirectRenderer<ModelPart> {
		protected final RDH<?> holder;
		protected final VanillaModelPart part;
		protected final Supplier<ModelPart> parentProvider;
		protected ModelPart parent;
		protected VBuffers buffers;
		protected ModelPart layer2;

		public RedirectModelRendererBase(RDH<?> holder, Supplier<ModelPart> parent, VanillaModelPart part) {
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
			resetPose();
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

		protected int color;

		@Override
		public Vec4f getColor() {
			return new Vec4f(ARGB.red(color) / 255f, ARGB.green(color) / 255f, ARGB.blue(color) / 255f,
					ARGB.alpha(color) / 255f);
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

		public RedirectModelRendererVanilla(RDH<?> holder, Supplier<ModelPart> parent, VanillaModelPart part) {
			super(holder, parent, part);
		}

		private PoseStack matrixStackIn;
		private VertexConsumer bufferIn;
		private int packedLightIn, packedOverlayIn;

		@Override
		public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color) {
			if(!holder.renderTypes.isInitialized()) {
				holder.copyModel(this, parent);
				parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
				holder.logWarning();
				return;
			}
			this.matrixStackIn   = matrixStackIn  ;
			this.bufferIn        = bufferIn       ;
			this.packedLightIn   = packedLightIn  ;
			this.packedOverlayIn = packedOverlayIn;
			this.color = color;
			this.buffers = holder.nextBatch(() -> new VBufferOut(packedLightIn, packedOverlayIn, matrixStackIn), bufferIn);
			render();
			this.matrixStackIn = null;
			this.bufferIn = null;
		}

		@Override
		public void renderParent() {
			boolean lv = false;
			if (layer2 != null) {
				lv = layer2.visible;
				layer2.visible = false;
			}
			parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
			if (layer2 != null) {
				layer2.visible = lv;
			}
		}
	}

	public static void multiplyStacks(MatrixStack.Entry e, PoseStack stack) {
		stack.last().pose().mul(new Matrix4f().setTransposed(e.getMatrixArray()));
		stack.last().normal().mul(new Matrix3f().set(e.getNormalArray3()).transpose());
	}
}
