package com.tom.cpm.client;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.util.ResourceLocation;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.render.ModelRenderManager;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureProvider;

public class PlayerRenderManager extends ModelRenderManager<Void, Void, ModelRenderer, ModelBase> {
	private static final float scale = 0.0625F;

	public PlayerRenderManager() {
		setFactory(new RedirectHolderFactory<Void, Void, ModelRenderer>() {

			@Override
			public <M> RedirectHolder<?, Void, Void, ModelRenderer> create(
					M model, String arg) {
				if(model instanceof ModelBiped) {
					return new RedirectHolderPlayer(PlayerRenderManager.this, (ModelBiped) model);
				} else if(model instanceof ModelSkeletonHead) {
					return new RedirectHolderSkull(PlayerRenderManager.this, (ModelSkeletonHead) model);
				} else if(model instanceof ModelBiped && "armor1".equals(arg)) {
					return new RedirectHolderArmor1(PlayerRenderManager.this, (ModelBiped) model);
				} else if(model instanceof ModelBiped && "armor2".equals(arg)) {
					return new RedirectHolderArmor2(PlayerRenderManager.this, (ModelBiped) model);
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
		private RedirectRenderer<ModelRenderer> bipedLeftArm;
		private RedirectRenderer<ModelRenderer> bipedRightArm;
		private RedirectRenderer<ModelRenderer> bipedLeftArmwear;
		private RedirectRenderer<ModelRenderer> bipedRightArmwear;

		public RedirectHolderPlayer(PlayerRenderManager mngr, ModelBiped model) {
			super(mngr, model);
			register(new Field<>(() -> model.bipedHead        , v -> model.bipedHead         = v, PlayerModelParts.HEAD), p -> !((PlayerProfile)p).hasPlayerHead);
			register(new Field<>(() -> model.bipedBody        , v -> model.bipedBody         = v, PlayerModelParts.BODY));
			bipedRightArm = register(new Field<>(() -> model.bipedRightArm    , v -> model.bipedRightArm     = v, PlayerModelParts.RIGHT_ARM));
			bipedLeftArm = register(new Field<>(() -> model.bipedLeftArm     , v -> model.bipedLeftArm      = v, PlayerModelParts.LEFT_ARM));
			register(new Field<>(() -> model.bipedRightLeg    , v -> model.bipedRightLeg     = v, PlayerModelParts.RIGHT_LEG));
			register(new Field<>(() -> model.bipedLeftLeg     , v -> model.bipedLeftLeg      = v, PlayerModelParts.LEFT_LEG));

			register(new Field<>(() -> model.bipedHeadwear    , v -> model.bipedHeadwear     = v, null));

			//register(new Field<>(() -> model.bipedCloak       , v -> model.bipedCloak     = v, RootModelType.CAPE));
		}

		@Override
		public boolean skipTransform(RedirectRenderer<ModelRenderer> part) {
			ModelBiped model = (ModelBiped) this.model;
			boolean skipTransform = false;
			if(bipedRightArm == part && model.aimedBow) {
				skipTransform = true;
			}
			return skipTransform;
		}
	}

	private static class RedirectHolderSkull extends RDH {

		public RedirectHolderSkull(PlayerRenderManager mngr, ModelSkeletonHead model) {
			super(mngr, model);

			register(new Field<>(() -> model.skeletonHead, v -> model.skeletonHead = v, PlayerModelParts.HEAD));
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

	private abstract static class RDH extends ModelRenderManager.RedirectHolder<ModelBase, Void, Void, ModelRenderer> {
		private TextureProvider skin;

		public RDH(ModelRenderManager<Void, Void, ModelRenderer, ModelBase> mngr, ModelBase model) {
			super(mngr, model);
		}

		@Override
		protected void bindSkin() {
			ResourceLocation texID = null;
			if(skin != null) {
				skin.bind();
				texID = DynTexture.getBoundLoc();
			}
			renderTypes.put(RenderMode.NORMAL, new NativeRenderType(RetroGL.texture(texID), 0));
			renderTypes.put(RenderMode.GLOW, new NativeRenderType(RetroGL.eyes(texID), 1));
			renderTypes.put(RenderMode.OUTLINE, new NativeRenderType(RetroGL.linesNoDepth(), 2));
			renderTypes.put(RenderMode.COLOR, new NativeRenderType(RetroGL.color(), 0));
		}

		@Override
		protected void bindTexture(Void cbi, TextureProvider skin) {
			this.skin = skin;
			skinBound = false;
		}

		@Override
		public void swapOut0() {
			skin = null;
		}

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
				return this;
			}
			parent = parentProvider.get();
			copyModel(parent, this);
			return this;
		}

		@Override
		public ModelRenderer swapOut() {
			if(parent == null) {
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

		@Override
		public boolean noReset() {
			return RetroGL.renderCallLoc == RetroGL.HURT_OVERLAY_LOC;
		}

		@Override
		public void postRender(float scale) {
			MatrixStack.Entry e = getPartTransform();
			if(e != null) {
				multiplyStacks(e);
			} else
				super.postRender(scale);
		}
	}

	private static final FloatBuffer BUF_FLOAT_16 = BufferUtils.createFloatBuffer(16);
	public static void multiplyStacks(MatrixStack.Entry e) {
		BUF_FLOAT_16.clear();
		e.getMatrix().store(BUF_FLOAT_16);
		BUF_FLOAT_16.rewind();
		GL11.glMultMatrix(BUF_FLOAT_16);
	}
}
