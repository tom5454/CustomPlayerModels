package com.tom.cpm.shared.model.render;

import java.util.function.Supplier;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpm.shared.gui.panel.ViewportPanelBase3d;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.builtin.VanillaPartRenderer;
import com.tom.cpm.shared.model.builtin.VanillaPlayerModel;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.PlayerModelLayer;

public class GuiModelRenderManager extends ModelRenderManager<VBuffers, ViewportPanelBase3d, VanillaPartRenderer, VanillaPlayerModel> {
	public static final String PLAYER = "player";

	public GuiModelRenderManager() {
		setFactory(new RedirectHolderFactory<VBuffers, ViewportPanelBase3d, VanillaPartRenderer>() {

			@Override
			public <M> RedirectHolder<?, VBuffers, ViewportPanelBase3d, VanillaPartRenderer> create(
					M model, String arg) {
				if(model instanceof VanillaPlayerModel) {
					return new RedirectHolderPlayer(GuiModelRenderManager.this, (VanillaPlayerModel) model, arg);
				}
				return null;
			}
		});
		setRedirectFactory(new RedirectRendererFactory<VanillaPlayerModel, ViewportPanelBase3d, VanillaPartRenderer>() {

			@Override
			public RedirectRenderer<VanillaPartRenderer> create(VanillaPlayerModel model,
					RedirectHolder<VanillaPlayerModel, ?, ViewportPanelBase3d, VanillaPartRenderer> access,
					Supplier<VanillaPartRenderer> modelPart, VanillaModelPart part) {
				return new RedirectPartRenderer((RedirectHolderPlayer) access, modelPart, part);
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

	@Override
	public void unbindModel(VanillaPlayerModel model) {
		RedirectHolder<VanillaPlayerModel, VBuffers, ViewportPanelBase3d, VanillaPartRenderer> h = holders.remove(model);
		if(h != null)h.swapOut();
	}

	public static class RedirectHolderPlayer extends ModelRenderManager.RedirectHolder<VanillaPlayerModel, VBuffers, ViewportPanelBase3d, VanillaPartRenderer> {

		public RedirectHolderPlayer(ModelRenderManager<VBuffers, ViewportPanelBase3d, VanillaPartRenderer, VanillaPlayerModel> mngr,
				VanillaPlayerModel model, String arg) {
			super(mngr, model);

			if(arg.equals(PLAYER)) {
				register(new Field<>(() -> model.head    , v -> model.head     = v, PlayerModelParts.HEAD));
				register(new Field<>(() -> model.body    , v -> model.body     = v, PlayerModelParts.BODY));
				register(new Field<>(() -> model.rightArm, v -> model.rightArm = v, PlayerModelParts.RIGHT_ARM));
				register(new Field<>(() -> model.leftArm , v -> model.leftArm  = v, PlayerModelParts.LEFT_ARM));
				register(new Field<>(() -> model.rightLeg, v -> model.rightLeg = v, PlayerModelParts.RIGHT_LEG));
				register(new Field<>(() -> model.leftLeg , v -> model.leftLeg  = v, PlayerModelParts.LEFT_LEG));
			} else if(arg.equals(PlayerModelLayer.CAPE.name())) {
				register(new Field<>(() -> model.cape    , v -> model.cape     = v, RootModelType.CAPE));
			} else if(arg.equals(PlayerModelLayer.ELYTRA.name())) {
				register(new Field<>(() -> model.elytraRight, v -> model.elytraRight = v, RootModelType.ELYTRA_RIGHT));
				register(new Field<>(() -> model.elytraLeft,  v -> model.elytraLeft  = v, RootModelType.ELYTRA_LEFT));
			} else if(arg.equals(PlayerModelLayer.HELMET.name())) {
				register(new Field<>(() -> model.armorHelmet, v -> model.armorHelmet = v, RootModelType.ARMOR_HELMET));
			} else if(arg.equals(PlayerModelLayer.BODY.name())) {
				register(new Field<>(() -> model.armorBody,     v -> model.armorBody     = v, RootModelType.ARMOR_BODY));
				register(new Field<>(() -> model.armorRightArm, v -> model.armorRightArm = v, RootModelType.ARMOR_RIGHT_ARM));
				register(new Field<>(() -> model.armorLeftArm,  v -> model.armorLeftArm  = v, RootModelType.ARMOR_LEFT_ARM));
			} else if(arg.equals(PlayerModelLayer.BOOTS.name())) {
				register(new Field<>(() -> model.armorRightFoot, v -> model.armorRightFoot = v, RootModelType.ARMOR_RIGHT_FOOT));
				register(new Field<>(() -> model.armorLeftFoot,  v -> model.armorLeftFoot  = v, RootModelType.ARMOR_LEFT_FOOT));
			} else if(arg.equals(PlayerModelLayer.LEGS.name())) {
				register(new Field<>(() -> model.armorLeggingsBody, v -> model.armorLeggingsBody = v, RootModelType.ARMOR_LEGGINGS_BODY));
				register(new Field<>(() -> model.armorRightLeg,     v -> model.armorRightLeg     = v, RootModelType.ARMOR_RIGHT_LEG));
				register(new Field<>(() -> model.armorLeftLeg,      v -> model.armorLeftLeg      = v, RootModelType.ARMOR_LEFT_LEG));
			} else {
				throw new RuntimeException("Undefined argument: " + arg);
			}
		}

		@Override
		protected void swapIn0() {
		}

		@Override
		protected void swapOut0() {
		}

		@Override
		protected void setupRenderSystem(ViewportPanelBase3d cbi, TextureSheetType tex) {
			cbi.putRenderTypes(renderTypes);
		}

		@Override
		protected void bindTexture(ViewportPanelBase3d cbi, TextureProvider skin) {
			skin.bind();
			cbi.load();
		}

		@Override
		protected void bindDefaultTexture(ViewportPanelBase3d cbi, TextureSheetType tex) {
			cbi.load(tex.name().toLowerCase());
		}

		@Override
		protected boolean isInGui() {
			return true;
		}
	}

	public static class RedirectPartRenderer extends VanillaPartRenderer implements RedirectRenderer<VanillaPartRenderer> {
		protected final RedirectHolderPlayer holder;
		protected final VanillaModelPart part;
		protected final Supplier<VanillaPartRenderer> parentProvider;
		protected VanillaPartRenderer parent;

		private MatrixStack stack;
		private VertexBuffer buf;
		private VBuffers buffers;

		public RedirectPartRenderer(RedirectHolderPlayer holder, Supplier<VanillaPartRenderer> parent,
				VanillaModelPart part) {
			this.part = part;
			this.holder = holder;
			this.parentProvider = parent;
		}

		@Override
		public VanillaPartRenderer swapIn() {
			if(parent != null)return this;
			parent = parentProvider.get();
			holder.copyModel(parent, this);
			return this;
		}

		@Override
		public VanillaPartRenderer swapOut() {
			if(parent == null)return parentProvider.get();
			VanillaPartRenderer p = parent;
			parent = null;
			return p;
		}

		@Override
		public RedirectHolder<?, ?, ?, VanillaPartRenderer> getHolder() {
			return holder;
		}

		@Override
		public VanillaPartRenderer getParent() {
			return parent;
		}

		@Override
		public VanillaModelPart getPart() {
			return part;
		}

		@Override
		public void renderParent() {
			parent.render(stack, buf);
		}

		@Override
		public VBuffers getVBuffers() {
			return buffers;
		}

		@Override
		public Vec4f getColor() {
			return new Vec4f(1, 1, 1, 1);
		}

		@Override
		public void render(MatrixStack stack, VertexBuffer buf) {
			this.stack = stack;
			this.buf = buf;
			this.buffers = holder.addDt.normal(buf).transform(stack);
			render();
			this.buffers = null;
			this.stack = null;
			this.buf = null;
		}

		@Override
		public void doRender0(RootModelElement elem, boolean doRender) {
			MatrixStack stack = new MatrixStack();
			stack.push();
			holder.transform(stack, holder.def.getScale());
			translateRotate(stack);
			VBuffers buf = getVBuffers();
			Vec4f color = getColor();
			render(elem, stack, buf, color.x, color.y, color.z, color.w, true, true);
			stack.pop();
		}

		@Override
		public void translateRotatePart(MatrixStack matrixStackIn) {
			MatrixStack.Entry e = getPartTransform();
			if(e != null) {
				matrixStackIn.setLast(e);
			} else
				super.translateRotatePart(matrixStackIn);
		}

		public void renderParent(VertexBuffer buf) {
			parent.render(new MatrixStack(), buf);
		}
	}
}
