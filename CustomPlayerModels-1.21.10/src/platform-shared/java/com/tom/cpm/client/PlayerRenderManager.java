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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
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
import com.tom.cpm.client.vr.VRPlayerRenderer;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.ModelRenderManager;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.Log;

public class PlayerRenderManager extends ModelRenderManager<Void, ModelTexture, ModelPart, Model> {
	public static final Function<ResourceLocation, RenderType> armor = RenderType::armorCutoutNoCull;
	public static final Function<ResourceLocation, RenderType> entity = RenderType::entityTranslucent;

	public PlayerRenderManager() {
		setFactory(new RedirectHolderFactory<Void, ModelTexture, ModelPart>() {

			@SuppressWarnings("unchecked")
			@Override
			public <M> RedirectHolder<?, Void, ModelTexture, ModelPart> create(
					M model, String arg) {
				if ("api".equals(arg) && model instanceof HumanoidModel) {
					return new RedirectHolderApi(PlayerRenderManager.this, (HumanoidModel<HumanoidRenderState>) model);
				} else if(model instanceof PlayerCapeModel) {
					return new RedirectHolderCape(PlayerRenderManager.this, (PlayerCapeModel) model);
				} else if(model instanceof PlayerModel && "armor1".equals(arg)) {
					return new RedirectHolderArmorHelm(PlayerRenderManager.this, (PlayerModel) model);
				} else if(model instanceof PlayerModel && "armor2".equals(arg)) {
					return new RedirectHolderArmorLegs(PlayerRenderManager.this, (PlayerModel) model);
				} else if(model instanceof PlayerModel && "armor3".equals(arg)) {
					return new RedirectHolderArmorChest(PlayerRenderManager.this, (PlayerModel) model);
				} else if(model instanceof PlayerModel && "armor4".equals(arg)) {
					return new RedirectHolderArmorFeet(PlayerRenderManager.this, (PlayerModel) model);
				} else if(CustomPlayerModelsClient.vrLoaded && VRPlayerRenderer.isVRPlayer(model)) {
					return VRPlayerRenderer.createVRPlayer(PlayerRenderManager.this, model);
				} else if(model instanceof PlayerModel) {
					return new RedirectHolderPlayer(PlayerRenderManager.this, (PlayerModel) model);
				} else if(model instanceof SkullModel) {
					return new RedirectHolderSkull(PlayerRenderManager.this, (SkullModel) model);
				} else if(model instanceof ElytraModel) {
					return new RedirectHolderElytra(PlayerRenderManager.this, (ElytraModel) model);
				}
				return null;
			}
		});
		setRedirectFactory(new RedirectRendererFactory<Model, ModelTexture, ModelPart>() {

			@Override
			public RedirectRenderer<ModelPart> create(Model model,
					RedirectHolder<Model, ?, ModelTexture, ModelPart> access,
					Supplier<ModelPart> modelPart, VanillaModelPart part) {
				return new RedirectModelRenderer((RDH<?>) access, modelPart, part);
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

	public static abstract class RDH<M extends Model> extends ModelRenderManager.RedirectHolder<Model, Void, ModelTexture, ModelPart> {
		public List<Supplier<ModelPart>> renderedParts = new ArrayList<>();
		private ModelPart rootPart;
		private RootModelPart rootRenderer;

		public RDH(
				ModelRenderManager<Void, ModelTexture, ModelPart, Model> mngr,
				M model) {
			this(mngr, model, false);
		}

		public RDH(
				ModelRenderManager<Void, ModelTexture, ModelPart, Model> mngr,
				M model, boolean storeStateOnSubmit) {
			super(mngr, model);
			rootPart = getRoot();
			rootRenderer = new RootModelPart(this, storeStateOnSubmit);
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
			if (layer2 instanceof RedirectModelRenderer r) {
				r.layer2 = get.get();
			}
			return new Field<>(get, set, null);
		}

		protected ModelPart getRoot() {
			return model().root;
		}

		protected void setRoot(ModelPart part) {
			model().root = part;
		}

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
			super(mngr, model, true);
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
	}

	private static class RedirectHolderSkull extends RDH<SkullModel> {

		public RedirectHolderSkull(PlayerRenderManager mngr, SkullModel model) {
			super(mngr, model);

			register(createRendered(() -> model.head, v -> model.head = v, PlayerModelParts.HEAD));
		}
	}

	private static class RedirectHolderElytra extends RDH<ElytraModel> {

		public RedirectHolderElytra(PlayerRenderManager mngr, ElytraModel model) {
			super(mngr, model);

			register(createRendered(() -> model.rightWing, v -> model.rightWing = v, RootModelType.ELYTRA_RIGHT));
			register(createRendered(() -> model.leftWing,  v -> model.leftWing  = v, RootModelType.ELYTRA_LEFT));
		}
	}

	private static class RedirectHolderArmorHelm extends RDH<PlayerModel> {

		public RedirectHolderArmorHelm(PlayerRenderManager mngr, PlayerModel model) {
			super(mngr, model);

			var h = register(createRendered(() -> model.head,     v -> model.head     = v, RootModelType.ARMOR_HELMET));
			register(createRendered(() -> model.hat , v -> model.hat  = v, null)).setCopyFrom(h);
		}
	}

	private static class RedirectHolderArmorChest extends RDH<PlayerModel> {

		public RedirectHolderArmorChest(PlayerRenderManager mngr, PlayerModel model) {
			super(mngr, model);

			register(createRendered(() -> model.body,     v -> model.body     = v, RootModelType.ARMOR_BODY));
			register(createRendered(() -> model.rightArm, v -> model.rightArm = v, RootModelType.ARMOR_RIGHT_ARM));
			register(createRendered(() -> model.leftArm,  v -> model.leftArm  = v, RootModelType.ARMOR_LEFT_ARM));
		}
	}

	private static class RedirectHolderArmorLegs extends RDH<PlayerModel> {

		public RedirectHolderArmorLegs(PlayerRenderManager mngr, PlayerModel model) {
			super(mngr, model);

			register(createRendered(() -> model.body,     v -> model.body     = v, RootModelType.ARMOR_LEGGINGS_BODY));
			register(createRendered(() -> model.rightLeg, v -> model.rightLeg = v, RootModelType.ARMOR_RIGHT_LEG));
			register(createRendered(() -> model.leftLeg,  v -> model.leftLeg  = v, RootModelType.ARMOR_LEFT_LEG));
		}
	}

	private static class RedirectHolderArmorFeet extends RDH<PlayerModel> {

		public RedirectHolderArmorFeet(PlayerRenderManager mngr, PlayerModel model) {
			super(mngr, model);

			register(createRendered(() -> model.rightLeg, v -> model.rightLeg = v, RootModelType.ARMOR_RIGHT_FOOT));
			register(createRendered(() -> model.leftLeg,  v -> model.leftLeg  = v, RootModelType.ARMOR_LEFT_FOOT));
		}
	}

	private static class RedirectHolderCape extends RDH<PlayerCapeModel> {

		public RedirectHolderCape(PlayerRenderManager mngr, PlayerCapeModel model) {
			super(mngr, model);

			register(createRendered(() -> model.cape,     v -> model.cape     = v, RootModelType.CAPE));
		}
	}

	public static abstract class CPMModelPart extends ModelPart implements SelfRenderer {

		public CPMModelPart() {
			super(Collections.emptyList(), Collections.emptyMap());
		}

		@Override
		public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k) {
			//Shouldn't be ever called
			Log.error("Render call on CPM part root", new UnsupportedOperationException("Can't directly render CPM part root"));
		}
	}

	public static class RootModelPart extends CPMModelPart {
		private final RDH<?> holder;
		private final boolean storeStateOnSubmit;

		public RootModelPart(RDH<?> holder, boolean storeStateOnSubmit) {
			this.holder = holder;
			this.storeStateOnSubmit = storeStateOnSubmit;
		}

		@Override
		public void submitSelf(RenderCollector collector) {
			for (final Supplier<ModelPart> modelPart : holder.renderedParts) {
				((SelfRenderer) modelPart.get()).submitSelf(collector);
			}

			if (storeStateOnSubmit && holder.model instanceof PlayerModel pm) {
				collector.storeState(pm);
			}
		}
	}

	public static class RedirectModelRenderer extends CPMModelPart implements RedirectRenderer<ModelPart> {
		protected final RDH<?> holder;
		protected final VanillaModelPart part;
		protected final Supplier<ModelPart> parentProvider;
		protected ModelPart parent;
		protected VBuffers buffers;
		protected ModelPart layer2;

		private RenderCollector collector;

		public RedirectModelRenderer(RDH<?> holder, Supplier<ModelPart> parent, VanillaModelPart part) {
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

		@Override
		public void renderParent() {
			// TODO check layer 2
			collector.submitVanilla(parent);
		}

		@Override
		public void submitSelf(RenderCollector collector) {
			this.collector        = collector;
			this.color   = collector.tint();

			this.buffers = VBuffers.record(collector.recordBuffer());
			render();
			this.collector = null;
		}
	}

	public static void multiplyStacks(MatrixStack.Entry e, PoseStack stack) {
		stack.last().pose().mul(new Matrix4f().setTransposed(e.getMatrixArray()));
		stack.last().normal().mul(new Matrix3f().set(e.getNormalArray3()).transpose());
	}
}
