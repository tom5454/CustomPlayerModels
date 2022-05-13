package com.tom.cpm.client;

import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

import com.tom.cpl.math.MatrixStack.Entry;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.VBuffers;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.client.optifine.OptifineTexture;
import com.tom.cpm.client.optifine.proxy.ModelPartOF;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.ModelRenderManager;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureProvider;

public class PlayerRenderManager extends ModelRenderManager<VertexConsumerProvider, ModelTexture, ModelPart, Model> {
	public static final Function<Identifier, RenderLayer> armor = RenderLayer::getArmorCutoutNoCull;
	public static final Function<Identifier, RenderLayer> entity = RenderLayer::getEntityTranslucent;

	public PlayerRenderManager() {
		setFactory(new RedirectHolderFactory<VertexConsumerProvider, ModelTexture, ModelPart>() {

			@SuppressWarnings("unchecked")
			@Override
			public <M> RedirectHolder<?, VertexConsumerProvider, ModelTexture, ModelPart> create(
					M model, String arg) {
				if ("api".equals(arg) && model instanceof BipedEntityModel) {
					return new RedirectHolderApi(PlayerRenderManager.this, (BipedEntityModel<LivingEntity>) model);
				} else if(model instanceof PlayerEntityModel) {
					return new RedirectHolderPlayer(PlayerRenderManager.this, (PlayerEntityModel<AbstractClientPlayerEntity>) model);
				} else if(model instanceof SkullEntityModel) {
					return new RedirectHolderSkull(PlayerRenderManager.this, (SkullEntityModel) model);
				} else if(model instanceof ElytraEntityModel) {
					return new RedirectHolderElytra(PlayerRenderManager.this, (ElytraEntityModel<AbstractClientPlayerEntity>) model);
				} else if(model instanceof BipedEntityModel && "armor1".equals(arg)) {
					return new RedirectHolderArmor1(PlayerRenderManager.this, (BipedEntityModel<AbstractClientPlayerEntity>) model);
				} else if(model instanceof BipedEntityModel && "armor2".equals(arg)) {
					return new RedirectHolderArmor2(PlayerRenderManager.this, (BipedEntityModel<AbstractClientPlayerEntity>) model);
				}
				return null;
			}
		});
		setRedirectFactory(new RedirectRendererFactory<Model, ModelTexture, ModelPart>() {

			@Override
			public RedirectRenderer<ModelPart> create(Model model,
					RedirectHolder<Model, ?, ModelTexture, ModelPart> access,
					Supplier<ModelPart> modelPart, VanillaModelPart part) {
				return new RedirectModelRenderer(model, (RDH) access, modelPart, part);
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

	public static abstract class RDH extends ModelRenderManager.RedirectHolder<Model, VertexConsumerProvider, ModelTexture, ModelPart> {

		public RDH(
				ModelRenderManager<VertexConsumerProvider, ModelTexture, ModelPart, Model> mngr,
				Model model) {
			super(mngr, model);
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
		private RedirectRenderer<ModelPart> bipedHead;

		public RedirectHolderPlayer(PlayerRenderManager mngr, PlayerEntityModel<AbstractClientPlayerEntity> model) {
			super(mngr, model);
			bipedHead = registerHead(new Field<>(() -> model.head, v -> model.head = v, PlayerModelParts.HEAD));
			register(new Field<>(() -> model.body    , v -> model.body     = v, PlayerModelParts.BODY));
			register(new Field<>(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
			register(new Field<>(() -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
			register(new Field<>(() -> model.rightLeg, v -> model.rightLeg = v, PlayerModelParts.RIGHT_LEG));
			register(new Field<>(() -> model.leftLeg , v -> model.leftLeg  = v, PlayerModelParts.LEFT_LEG));

			register(new Field<>(() -> model.hat        , v -> model.hat         = v, null)).setCopyFrom(bipedHead);
			register(new Field<>(() -> model.leftSleeve , v -> model.leftSleeve  = v, null));
			register(new Field<>(() -> model.rightSleeve, v -> model.rightSleeve = v, null));
			register(new Field<>(() -> model.leftPants  , v -> model.leftPants   = v, null));
			register(new Field<>(() -> model.rightPants , v -> model.rightPants  = v, null));
			register(new Field<>(() -> model.jacket     , v -> model.jacket      = v, null));

			register(new Field<>(() -> model.cloak, v -> model.cloak = v, RootModelType.CAPE));
		}
	}

	private static class RedirectHolderApi extends RDH {
		private RedirectRenderer<ModelPart> bipedHead;

		public RedirectHolderApi(PlayerRenderManager mngr, BipedEntityModel<LivingEntity> model) {
			super(mngr, model);
			bipedHead = registerHead(new Field<>(() -> model.head, v -> model.head = v, PlayerModelParts.HEAD));
			register(new Field<>(() -> model.body    , v -> model.body     = v, PlayerModelParts.BODY));
			register(new Field<>(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
			register(new Field<>(() -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
			register(new Field<>(() -> model.rightLeg, v -> model.rightLeg = v, PlayerModelParts.RIGHT_LEG));
			register(new Field<>(() -> model.leftLeg , v -> model.leftLeg  = v, PlayerModelParts.LEFT_LEG));

			register(new Field<>(() -> model.hat        , v -> model.hat         = v, null)).setCopyFrom(bipedHead);
			if(model instanceof PlayerEntityModel) {
				PlayerEntityModel<LivingEntity> mp = (PlayerEntityModel<LivingEntity>) model;
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

		public RedirectHolderSkull(PlayerRenderManager mngr, SkullEntityModel model) {
			super(mngr, model);

			register(new Field<>(() -> model.head, v -> {
				model.root.children.put("head", v);
				model.head = v;
				if(model.root instanceof ModelPartOF)
					((ModelPartOF)model.root).cpm$updateChildModelsList();
			}, PlayerModelParts.HEAD));
		}
	}

	private static class RedirectHolderElytra extends RDH {

		public RedirectHolderElytra(PlayerRenderManager mngr, ElytraEntityModel<AbstractClientPlayerEntity> model) {
			super(mngr, model);

			register(new Field<>(() -> model.rightWing, v -> model.rightWing = v, RootModelType.ELYTRA_RIGHT));
			register(new Field<>(() -> model.leftWing,  v -> model.leftWing  = v, RootModelType.ELYTRA_LEFT));
		}

	}

	private static class RedirectHolderArmor1 extends RDH {

		public RedirectHolderArmor1(PlayerRenderManager mngr, BipedEntityModel<AbstractClientPlayerEntity> model) {
			super(mngr, model);

			register(new Field<>(() -> model.head,      v -> model.head      = v, RootModelType.ARMOR_HELMET));
			register(new Field<>(() -> model.body,      v -> model.body      = v, RootModelType.ARMOR_BODY));
			register(new Field<>(() -> model.rightArm,  v -> model.rightArm  = v, RootModelType.ARMOR_RIGHT_ARM));
			register(new Field<>(() -> model.leftArm,   v -> model.leftArm   = v, RootModelType.ARMOR_LEFT_ARM));
			register(new Field<>(() -> model.rightLeg,  v -> model.rightLeg  = v, RootModelType.ARMOR_RIGHT_FOOT));
			register(new Field<>(() -> model.leftLeg,   v -> model.leftLeg   = v, RootModelType.ARMOR_LEFT_FOOT));
		}

	}

	private static class RedirectHolderArmor2 extends RDH {

		public RedirectHolderArmor2(PlayerRenderManager mngr, BipedEntityModel<AbstractClientPlayerEntity> model) {
			super(mngr, model);

			register(new Field<>(() -> model.body,      v -> model.body      = v, RootModelType.ARMOR_LEGGINGS_BODY));
			register(new Field<>(() -> model.rightLeg,  v -> model.rightLeg  = v, RootModelType.ARMOR_RIGHT_LEG));
			register(new Field<>(() -> model.leftLeg,   v -> model.leftLeg   = v, RootModelType.ARMOR_LEFT_LEG));
		}

	}

	public static abstract class RedirectModelRendererBase extends ModelPart implements RedirectRenderer<ModelPart> {
		protected final RDH holder;
		protected final VanillaModelPart part;
		protected final Supplier<ModelPart> parentProvider;
		protected ModelPart parent;
		protected VBuffers buffers;

		public RedirectModelRendererBase(Model model, RDH holder, Supplier<ModelPart> parent, VanillaModelPart part) {
			super(Collections.emptyList(), Collections.emptyMap());
			this.holder = holder;
			this.parentProvider = parent;
			this.part = part;
		}

		@Override
		public ModelPart swapIn() {
			if(parent != null) {
				return this;
			}
			parent = parentProvider.get();
			holder.copyModel(parent, this);
			return this;
		}

		@Override
		public ModelPart swapOut() {
			if(parent == null) {
				return parentProvider.get();
			}
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

		@Override
		public VBuffers getVBuffers() {
			return buffers;
		}

		protected float red, green, blue, alpha;

		@Override
		public Vec4f getColor() {
			return new Vec4f(red, green, blue, alpha);
		}

		@Override
		public void rotate(MatrixStack matrix) {
			Entry e = getPartTransform();
			if(e != null) {
				multiplyStacks(e, matrix);
			} else
				super.rotate(matrix);
		}
	}

	private static class RedirectModelRenderer extends RedirectModelRendererBase {

		public RedirectModelRenderer(Model model, RDH holder, Supplier<ModelPart> parent, VanillaModelPart part) {
			super(model, holder, parent, part);
		}

		private MatrixStack matrixStackIn;
		private VertexConsumer bufferIn;
		private int packedLightIn, packedOverlayIn;

		@Override
		public void render(MatrixStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
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
			this.buffers = new VBuffers(rt -> new VBuffer(holder.addDt.getBuffer(rt.getNativeType()), packedLightIn, packedOverlayIn, matrixStackIn), new VBuffer(bufferIn, packedLightIn, packedOverlayIn, matrixStackIn));
			render();
			holder.addDt.getBuffer(holder.renderTypes.get(RenderMode.DEFAULT).getNativeType());
			this.matrixStackIn = null;
			this.bufferIn = null;
		}

		@Override
		public void renderParent() {
			parent.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		}
	}

	public static void multiplyStacks(com.tom.cpl.math.MatrixStack.Entry e, MatrixStack stack) {
		stack.peek().getPositionMatrix().multiply(Mat4Access.load(e.getMatrixArray()));
		stack.peek().getNormalMatrix().multiply(new Matrix3f(Mat4Access.load(e.getNormalArray())));
	}

	public static interface Mat4Access {
		void cpm$loadValue(float[] data);

		static Matrix4f load(float[] data) {
			Matrix4f m = new Matrix4f();
			((Mat4Access) (Object) m).cpm$loadValue(data);
			return m;
		}
	}
}
