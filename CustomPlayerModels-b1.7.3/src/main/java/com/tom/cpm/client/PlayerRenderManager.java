package com.tom.cpm.client;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.VBuffers;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.render.ModelRenderManager;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.retro.RedirectHolderRetro;

public class PlayerRenderManager extends ModelRenderManager<Void, Void, ModelPart, EntityModel> {
	private static final float scale = 0.0625F;

	public PlayerRenderManager() {
		setFactory(new RedirectHolderFactory<Void, Void, ModelPart>() {

			@Override
			public <M> RedirectHolder<?, Void, Void, ModelPart> create(
					M model, String arg) {
				if(model instanceof BipedEntityModel) {
					if ("armor1".equals(arg))
						return new RedirectHolderArmor1(PlayerRenderManager.this, (BipedEntityModel) model);
					else if("armor2".equals(arg))
						return new RedirectHolderArmor2(PlayerRenderManager.this, (BipedEntityModel) model);
					else
						return new RedirectHolderPlayer(PlayerRenderManager.this, (BipedEntityModel) model);
				}
				return null;
			}
		});
		setRedirectFactory(new RedirectRendererFactory<EntityModel, Void, ModelPart>() {

			@Override
			public RedirectRenderer<ModelPart> create(EntityModel model,
					RedirectHolder<EntityModel, ?, Void, ModelPart> access, Supplier<ModelPart> modelPart,
					VanillaModelPart part) {
				return new RedirectModelPart((RDH) access, modelPart, part);
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
		setRenderPart(new ModelPart(0, 0));
	}

	private static class RedirectHolderPlayer extends RDH {
		private RedirectRenderer<ModelPart> head;

		public RedirectHolderPlayer(PlayerRenderManager mngr, BipedEntityModel model) {
			super(mngr, model);
			head = registerHead(new Field<>(() -> model.head, v -> model.head = v, PlayerModelParts.HEAD));
			register(new Field<>(() -> model.body    , v -> model.body     = v, PlayerModelParts.BODY));
			register(new Field<>(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
			register(new Field<>(() -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
			register(new Field<>(() -> model.rightLeg, v -> model.rightLeg = v, PlayerModelParts.RIGHT_LEG));
			register(new Field<>(() -> model.leftLeg , v -> model.leftLeg  = v, PlayerModelParts.LEFT_LEG));

			register(new Field<>(() -> model.hat, v -> model.hat = v, null)).setCopyFrom(head);

			register(new Field<>(() -> model.cape,    v -> model.cape    = v, RootModelType.CAPE));
		}
	}

	private static class RedirectHolderArmor1 extends RDH {

		public RedirectHolderArmor1(PlayerRenderManager mngr, BipedEntityModel model) {
			super(mngr, model);

			register(new Field<>(() -> model.head,     v -> model.head     = v, RootModelType.ARMOR_HELMET));
			register(new Field<>(() -> model.body,     v -> model.body     = v, RootModelType.ARMOR_BODY));
			register(new Field<>(() -> model.rightArm, v -> model.rightArm = v, RootModelType.ARMOR_RIGHT_ARM));
			register(new Field<>(() -> model.leftArm,  v -> model.leftArm  = v, RootModelType.ARMOR_LEFT_ARM));
			register(new Field<>(() -> model.rightLeg, v -> model.rightLeg = v, RootModelType.ARMOR_RIGHT_FOOT));
			register(new Field<>(() -> model.leftLeg,  v -> model.leftLeg  = v, RootModelType.ARMOR_LEFT_FOOT));

			register(new Field<>(() -> model.hat, v -> model.hat = v, null));
		}

	}

	private static class RedirectHolderArmor2 extends RDH {

		public RedirectHolderArmor2(PlayerRenderManager mngr, BipedEntityModel model) {
			super(mngr, model);

			register(new Field<>(() -> model.body,     v -> model.body     = v, RootModelType.ARMOR_LEGGINGS_BODY));
			register(new Field<>(() -> model.rightLeg, v -> model.rightLeg = v, RootModelType.ARMOR_RIGHT_LEG));
			register(new Field<>(() -> model.leftLeg,  v -> model.leftLeg  = v, RootModelType.ARMOR_LEFT_LEG));
		}

	}

	private abstract static class RDH extends RedirectHolderRetro<EntityModel, ModelPart> {

		public RDH(ModelRenderManager<Void, Void, ModelPart, EntityModel> mngr, EntityModel model) {
			super(mngr, model);
		}
	}

	private static class RedirectModelPart extends ModelPart implements RedirectRenderer<ModelPart> {
		private final RDH holder;
		private final VanillaModelPart part;
		private final Supplier<ModelPart> parentProvider;
		private ModelPart parent;
		private VBuffers buffers;

		public RedirectModelPart(RDH holder, Supplier<ModelPart> parent, VanillaModelPart part) {
			super(0, 0);
			this.holder = holder;
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
		public ModelPart swapIn() {
			if(parent != null) {
				return this;
			}
			parent = parentProvider.get();
			copyModel(parent, this);
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

		private static void copyModel(ModelPart s, ModelPart d) {
			d.pivotX = s.pivotX;
			d.pivotY = s.pivotY;
			d.pivotZ = s.pivotZ;
			d.pitch   = s.pitch  ;
			d.yaw   = s.yaw  ;
			d.roll   = s.roll  ;
			d.visible      = s.visible     ;
			d.hidden       = s.hidden      ;
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

		@Override
		public void transform(float scale) {
			MatrixStack.Entry e = getPartTransform();
			if(e != null) {
				multiplyStacks(e);
			} else {
				if (this.pitch == 0.0F && this.yaw == 0.0F && this.roll == 0.0F) {
					if (this.pivotX != 0.0F || this.pivotY != 0.0F || this.pivotZ != 0.0F) {
						GL11.glTranslatef(this.pivotX * scale, this.pivotY * scale, this.pivotZ * scale);
					}
				} else {
					GL11.glTranslatef(this.pivotX * scale, this.pivotY * scale, this.pivotZ * scale);
					if (this.roll != 0.0F) {
						GL11.glRotatef(this.roll * 57.295776F, 0.0F, 0.0F, 1.0F);
					}

					if (this.yaw != 0.0F) {
						GL11.glRotatef(this.yaw * 57.295776F, 0.0F, 1.0F, 0.0F);
					}

					if (this.pitch != 0.0F) {
						GL11.glRotatef(this.pitch * 57.295776F, 1.0F, 0.0F, 0.0F);
					}
				}
			}
		}
	}

	public static void multiplyStacks(MatrixStack.Entry e) {
		e.getMatrix().multiplyNative(GL11::glMultMatrix);
	}
}
