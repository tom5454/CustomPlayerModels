package com.tom.cpm.client;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.render.model.Cube;
import net.minecraft.client.render.model.ModelBase;
import net.minecraft.client.render.model.ModelBiped;
import net.minecraft.client.render.model.ModelPlayer;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.VBuffers;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.render.ModelRenderManager;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.retro.RedirectHolderRetro;

public class PlayerRenderManager extends ModelRenderManager<Void, Void, Cube, ModelBase> {
	private static final float scale = 0.0625F;

	public PlayerRenderManager() {
		setFactory(new RedirectHolderFactory<Void, Void, Cube>() {

			@Override
			public <M> RedirectHolder<?, Void, Void, Cube> create(
					M model, String arg) {
				if(model instanceof ModelBiped) {
					if ("armor1".equals(arg))
						return new RedirectHolderArmor1(PlayerRenderManager.this, (ModelBiped) model);
					else if("armor2".equals(arg))
						return new RedirectHolderArmor2(PlayerRenderManager.this, (ModelBiped) model);
					else
						return new RedirectHolderPlayer(PlayerRenderManager.this, (ModelBiped) model);
				}
				return null;
			}
		});
		setRedirectFactory(new RedirectRendererFactory<ModelBase, Void, Cube>() {

			@Override
			public RedirectRenderer<Cube> create(ModelBase model,
					RedirectHolder<ModelBase, ?, Void, Cube> access, Supplier<Cube> modelPart,
					VanillaModelPart part) {
				return new RedirectModelPart((RDH) access, modelPart, part);
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
		setRenderPart(new Cube(0, 0));
	}

	private static class RedirectHolderPlayer extends RDH {
		private RedirectRenderer<Cube> head;

		public RedirectHolderPlayer(PlayerRenderManager mngr, ModelBiped model) {
			super(mngr, model);
			head = registerHead(new Field<>(() -> model.bipedHead, v -> model.bipedHead = v, PlayerModelParts.HEAD));
			register(new Field<>(() -> model.bipedBody    , v -> model.bipedBody     = v, PlayerModelParts.BODY));
			register(new Field<>(() -> model.bipedRightArm, v -> model.bipedRightArm = v, PlayerModelParts.RIGHT_ARM));
			register(new Field<>(() -> model.bipedLeftArm , v -> model.bipedLeftArm  = v, PlayerModelParts.LEFT_ARM));
			register(new Field<>(() -> model.bipedRightLeg, v -> model.bipedRightLeg = v, PlayerModelParts.RIGHT_LEG));
			register(new Field<>(() -> model.bipedLeftLeg , v -> model.bipedLeftLeg  = v, PlayerModelParts.LEFT_LEG));

			register(new Field<>(() -> model.bipedHeadOverlay, v -> model.bipedHeadOverlay = v, null)).setCopyFrom(head);

			register(new Field<>(() -> model.bipedCloak,    v -> model.bipedCloak    = v, RootModelType.CAPE));

			if (model instanceof ModelPlayer) {
				ModelPlayer p = (ModelPlayer) model;
				register(new Field<>(() -> p.bipedLeftArmOverlay , v -> p.bipedLeftArmOverlay  = v, null));
				register(new Field<>(() -> p.bipedRightArmOverlay, v -> p.bipedRightArmOverlay = v, null));
				register(new Field<>(() -> p.bipedLeftLegOverlay , v -> p.bipedLeftLegOverlay  = v, null));
				register(new Field<>(() -> p.bipedRightLegOverlay, v -> p.bipedRightLegOverlay = v, null));
				register(new Field<>(() -> p.bipedBodyOverlay    , v -> p.bipedBodyOverlay     = v, null));
			}
		}
	}

	private static class RedirectHolderArmor1 extends RDH {

		public RedirectHolderArmor1(PlayerRenderManager mngr, ModelBiped model) {
			super(mngr, model);

			register(new Field<>(() -> model.bipedHead,     v -> model.bipedHead     = v, RootModelType.ARMOR_HELMET));
			register(new Field<>(() -> model.bipedBody,     v -> model.bipedBody     = v, RootModelType.ARMOR_BODY));
			register(new Field<>(() -> model.bipedRightArm, v -> model.bipedRightArm = v, RootModelType.ARMOR_RIGHT_ARM));
			register(new Field<>(() -> model.bipedLeftArm,  v -> model.bipedLeftArm  = v, RootModelType.ARMOR_LEFT_ARM));
			register(new Field<>(() -> model.bipedRightLeg, v -> model.bipedRightLeg = v, RootModelType.ARMOR_RIGHT_FOOT));
			register(new Field<>(() -> model.bipedLeftLeg,  v -> model.bipedLeftLeg  = v, RootModelType.ARMOR_LEFT_FOOT));

			register(new Field<>(() -> model.bipedHeadOverlay, v -> model.bipedHeadOverlay = v, null));
		}

	}

	private static class RedirectHolderArmor2 extends RDH {

		public RedirectHolderArmor2(PlayerRenderManager mngr, ModelBiped model) {
			super(mngr, model);

			register(new Field<>(() -> model.bipedBody,     v -> model.bipedBody     = v, RootModelType.ARMOR_LEGGINGS_BODY));
			register(new Field<>(() -> model.bipedRightLeg, v -> model.bipedRightLeg = v, RootModelType.ARMOR_RIGHT_LEG));
			register(new Field<>(() -> model.bipedLeftLeg,  v -> model.bipedLeftLeg  = v, RootModelType.ARMOR_LEFT_LEG));
		}

	}

	private abstract static class RDH extends RedirectHolderRetro<ModelBase, Cube> {

		public RDH(ModelRenderManager<Void, Void, Cube, ModelBase> mngr, ModelBase model) {
			super(mngr, model);
		}
	}

	private static class RedirectModelPart extends Cube implements RedirectRenderer<Cube> {
		private final RDH holder;
		private final VanillaModelPart part;
		private final Supplier<Cube> parentProvider;
		private Cube parent;
		private VBuffers buffers;

		public RedirectModelPart(RDH holder, Supplier<Cube> parent, VanillaModelPart part) {
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
		public Cube swapIn() {
			if(parent != null) {
				return this;
			}
			parent = parentProvider.get();
			copyModel(parent, this);
			return this;
		}

		@Override
		public Cube swapOut() {
			if(parent == null) {
				return parentProvider.get();
			}
			Cube p = parent;
			parent = null;
			return p;
		}

		private static void copyModel(Cube s, Cube d) {
			d.rotationPointX = s.rotationPointX;
			d.rotationPointY = s.rotationPointY;
			d.rotationPointZ = s.rotationPointZ;
			d.rotateAngleX   = s.rotateAngleX  ;
			d.rotateAngleY   = s.rotateAngleY  ;
			d.rotateAngleZ   = s.rotateAngleZ  ;
			d.showModel      = s.showModel     ;
		}

		@Override
		public RedirectHolder<?, ?, ?, Cube> getHolder() {
			return holder;
		}

		@Override
		public Cube getParent() {
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
		public void postRender(float scale) {
			MatrixStack.Entry e = getPartTransform();
			if(e != null) {
				multiplyStacks(e);
			} else {
				if (this.rotateAngleX != 0.0f || this.rotateAngleY != 0.0f || this.rotateAngleZ != 0.0f) {
					GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
					if (this.rotateAngleZ != 0.0f) {
						GL11.glRotatef(this.rotateAngleZ * 57.29578f, 0.0f, 0.0f, 1.0f);
					}
					if (this.rotateAngleY != 0.0f) {
						GL11.glRotatef(this.rotateAngleY * 57.29578f, 0.0f, 1.0f, 0.0f);
					}
					if (this.rotateAngleX != 0.0f) {
						GL11.glRotatef(this.rotateAngleX * 57.29578f, 1.0f, 0.0f, 0.0f);
					}
				} else if (this.rotationPointX != 0.0f || this.rotationPointY != 0.0f || this.rotationPointZ != 0.0f) {
					GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
				}
			}
		}
	}

	public static void multiplyStacks(MatrixStack.Entry e) {
		e.getMatrix().multiplyNative(GL11::glMultMatrix);
	}
}
