package com.tom.cpm.client;

import java.util.function.Supplier;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.client.renderer.entity.model.HumanoidHeadModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.entity.LivingEntity;

import com.mojang.blaze3d.platform.GlStateManager;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.VBuffers;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.render.ModelRenderManager;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.retro.RedirectHolderRetro;

public class PlayerRenderManager extends ModelRenderManager<Void, Void, RendererModel, Model> {
	public static final float scale = 0.0625F;

	public PlayerRenderManager() {
		setFactory(new RedirectHolderFactory<Void, Void, RendererModel>() {

			@SuppressWarnings("unchecked")
			@Override
			public <M> RedirectHolder<?, Void, Void, RendererModel> create(
					M model, String arg) {
				if("api".equals(arg) && model instanceof BipedModel) {
					return new RedirectHolderApi(PlayerRenderManager.this, (BipedModel<LivingEntity>) model);
				} else if(model instanceof PlayerModel) {
					return new RedirectHolderPlayer(PlayerRenderManager.this, (PlayerModel<LivingEntity>) model);
				} else if(model instanceof HumanoidHeadModel) {
					return new RedirectHolderSkull(PlayerRenderManager.this, (HumanoidHeadModel) model);
				} else if(model instanceof ElytraModel) {
					return new RedirectHolderElytra(PlayerRenderManager.this, (ElytraModel<LivingEntity>) model);
				} else if(model instanceof BipedModel && "armor1".equals(arg)) {
					return new RedirectHolderArmor1(PlayerRenderManager.this, (BipedModel<LivingEntity>) model);
				} else if(model instanceof BipedModel && "armor2".equals(arg)) {
					return new RedirectHolderArmor2(PlayerRenderManager.this, (BipedModel<LivingEntity>) model);
				}
				return null;
			}
		});
		setRedirectFactory(new RedirectRendererFactory<Model, Void, RendererModel>() {

			@Override
			public RedirectRenderer<RendererModel> create(Model model,
					RedirectHolder<Model, ?, Void, RendererModel> access,
					Supplier<RendererModel> modelPart, VanillaModelPart part) {
				return new RedirectModelRenderer((RDH) access, modelPart, part);
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
	}

	public static abstract class RDH extends RedirectHolderRetro<Model, RendererModel> {

		public RDH(
				ModelRenderManager<Void, Void, RendererModel, Model> mngr,
				Model model) {
			super(mngr, model);
		}
	}

	private static class RedirectHolderPlayer extends RDH {
		private RedirectRenderer<RendererModel> head;

		public RedirectHolderPlayer(PlayerRenderManager mngr, PlayerModel<LivingEntity> model) {
			super(mngr, model);
			head = registerHead(new Field<>(    () -> model.head    , v -> model.head     = v, PlayerModelParts.HEAD));
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
		private RedirectRenderer<RendererModel> head;

		public RedirectHolderApi(PlayerRenderManager mngr, BipedModel<LivingEntity> model) {
			super(mngr, model);
			head = registerHead(new Field<>(    () -> model.head    , v -> model.head     = v, PlayerModelParts.HEAD));
			register(new Field<>(() -> model.body    , v -> model.body     = v, PlayerModelParts.BODY));
			register(new Field<>(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
			register(new Field<>(() -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
			register(new Field<>(() -> model.rightLeg, v -> model.rightLeg = v, PlayerModelParts.RIGHT_LEG));
			register(new Field<>(() -> model.leftLeg , v -> model.leftLeg  = v, PlayerModelParts.LEFT_LEG));

			register(new Field<>(() -> model.hat        , v -> model.hat         = v, null)).setCopyFrom(head);
			if(model instanceof PlayerModel) {
				PlayerModel<LivingEntity> pm = (PlayerModel<LivingEntity>) model;
				register(new Field<>(() -> pm.leftSleeve , v -> pm.leftSleeve  = v, null));
				register(new Field<>(() -> pm.rightSleeve, v -> pm.rightSleeve = v, null));
				register(new Field<>(() -> pm.leftPants  , v -> pm.leftPants   = v, null));
				register(new Field<>(() -> pm.rightPants , v -> pm.rightPants  = v, null));
				register(new Field<>(() -> pm.jacket     , v -> pm.jacket      = v, null));

				register(new Field<>(() -> pm.cloak, v -> pm.cloak = v, RootModelType.CAPE));
			}
		}

		@Override
		protected boolean isInGui() {
			return true;
		}
	}

	private static class RedirectHolderSkull extends RDH {

		public RedirectHolderSkull(PlayerRenderManager mngr, HumanoidHeadModel model) {
			super(mngr, model);

			register(new Field<>(() -> model.head, v -> model.head = v, PlayerModelParts.HEAD));
			register(new Field<>(() -> model.hat , v -> model.hat  = v, null));
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

		public RedirectHolderArmor1(PlayerRenderManager mngr, BipedModel<LivingEntity> model) {
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

		public RedirectHolderArmor2(PlayerRenderManager mngr, BipedModel<LivingEntity> model) {
			super(mngr, model);

			register(new Field<>(() -> model.body,     v -> model.body     = v, RootModelType.ARMOR_LEGGINGS_BODY));
			register(new Field<>(() -> model.rightLeg, v -> model.rightLeg = v, RootModelType.ARMOR_RIGHT_LEG));
			register(new Field<>(() -> model.leftLeg,  v -> model.leftLeg  = v, RootModelType.ARMOR_LEFT_LEG));
		}

	}

	public static class RedirectModelRenderer extends RendererModel implements RedirectRenderer<RendererModel> {
		protected final RDH holder;
		protected final VanillaModelPart part;
		protected final Supplier<RendererModel> parentProvider;
		protected RendererModel parent;
		protected VBuffers buffers;

		public RedirectModelRenderer(RDH holder, Supplier<RendererModel> parent, VanillaModelPart part) {
			super(holder.model);
			this.part = part;
			this.holder = holder;
			this.parentProvider = parent;
		}

		@Override
		public VBuffers getVBuffers() {
			return buffers;
		}

		@Override
		public RendererModel swapIn() {
			if(parent != null)return this;
			parent = parentProvider.get();
			holder.copyModel(parent, this);
			return this;
		}

		@Override
		public RendererModel swapOut() {
			if(parent == null)return parentProvider.get();
			RendererModel p = parent;
			parent = null;
			return p;
		}

		@Override
		public RedirectHolder<?, ?, ?, RendererModel> getHolder() {
			return holder;
		}

		@Override
		public RendererModel getParent() {
			return parent;
		}

		@Override
		public VanillaModelPart getPart() {
			return part;
		}

		@Override
		public Vec4f getColor() {
			return RetroGL.getColor();
		}

		@Override
		public void translateTo(float scale) {
			MatrixStack.Entry e = getPartTransform();
			if(e != null) {
				multiplyStacks(e);
			} else
				super.translateTo(scale);
		}

		@Override
		public void render(float scale) {
			this.buffers = new VBuffers(RetroGL::buffer);
			render();
			buffers.finishAll();
		}

		@Override
		public void renderParent() {
			parent.render(scale);
		}
	}


	public static void multiplyStacks(MatrixStack.Entry e) {
		e.getMatrix().multiplyNative(GlStateManager::multMatrix);
	}
}
