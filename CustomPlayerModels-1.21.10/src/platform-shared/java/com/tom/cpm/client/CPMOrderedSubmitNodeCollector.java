package com.tom.cpm.client;

import java.util.List;

import org.joml.Quaternionf;

import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector.ParticleGroupRenderer;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState.LeashState;
import net.minecraft.client.renderer.entity.state.EntityRenderState.ShadowPiece;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.SelfRenderer.RenderCollector;

public class CPMOrderedSubmitNodeCollector implements OrderedSubmitNodeCollector {
	private final OrderedSubmitNodeCollector collector;

	public CPMOrderedSubmitNodeCollector(OrderedSubmitNodeCollector collector) {
		this.collector = collector;
	}

	@Override
	public void submitHitbox(PoseStack p_438906_, EntityRenderState p_439622_, HitboxesRenderState p_439768_) {
		collector.submitHitbox(p_438906_, p_439622_, p_439768_);
	}

	@Override
	public void submitShadow(PoseStack p_439112_, float p_439891_, List<ShadowPiece> p_439759_) {
		collector.submitShadow(p_439112_, p_439891_, p_439759_);
	}

	@Override
	public void submitNameTag(PoseStack p_439537_, Vec3 p_439628_, int p_439109_, Component p_439200_,
			boolean p_439687_, int p_451645_, double p_440364_, CameraRenderState p_451474_) {
		collector.submitNameTag(p_439537_, p_439628_, p_439109_, p_439200_, p_439687_, p_451645_, p_440364_, p_451474_);
	}

	@Override
	public void submitText(PoseStack p_440112_, float p_439364_, float p_439156_, FormattedCharSequence p_438924_,
			boolean p_440612_, DisplayMode p_439113_, int p_440164_, int p_439316_, int p_440620_, int p_440227_) {
		collector.submitText(p_440112_, p_439364_, p_439156_, p_438924_, p_440612_, p_439113_, p_440164_, p_439316_,
				p_440620_, p_440227_);
	}

	@Override
	public void submitFlame(PoseStack p_440539_, EntityRenderState p_440393_, Quaternionf p_439403_) {
		collector.submitFlame(p_440539_, p_440393_, p_439403_);
	}

	@Override
	public void submitLeash(PoseStack p_440528_, LeashState p_440576_) {
		collector.submitLeash(p_440528_, p_440576_);
	}

	@Override
	public <S> void submitModel(Model<? super S> model, S state, PoseStack pose, RenderType type,
			int light, int overlay, int tint, TextureAtlasSprite sprite, int outline,
			CrumblingOverlay p_439871_) {
		if (model.root() instanceof SelfRenderer sr) {
			model.setupAnim(state);
			sr.submitSelf(new RenderCollector(pose, collector, type, light, overlay, tint, outline, sprite, state));
		} else
			collector.submitModel(model, state, pose, type, light, overlay, tint, sprite,
					outline, p_439871_);
	}

	@Override
	public <S> void submitModel(Model<? super S> model, S state, PoseStack pose, RenderType type,
			int light, int overlay, int outline, CrumblingOverlay p_439732_) {
		if (model.root() instanceof SelfRenderer sr) {
			model.setupAnim(state);
			sr.submitSelf(new RenderCollector(pose, collector, type, light, overlay, -1, outline, null, state));
		} else
			collector.submitModel(model, state, pose, type, light, overlay, outline, p_439732_);
	}

	@Override
	public void submitModelPart(ModelPart part, PoseStack pose, RenderType type, int light,
			int overlay, TextureAtlasSprite sprite) {
		if (part instanceof SelfRenderer sr) {
			sr.submitSelf(new RenderCollector(pose, collector, type, light, overlay, -1, 0, sprite, null));
		} else
			collector.submitModelPart(part, pose, type, light, overlay, sprite);
	}

	@Override
	public void submitModelPart(ModelPart part, PoseStack pose, RenderType type, int light,
			int overlay, TextureAtlasSprite sprite, int tint, CrumblingOverlay p_442691_) {
		if (part instanceof SelfRenderer sr) {
			sr.submitSelf(new RenderCollector(pose, collector, type, light, overlay, tint, 0, sprite, null));
		} else
			collector.submitModelPart(part, pose, type, light, overlay, sprite, tint,
					p_442691_);
	}

	@Override
	public void submitModelPart(ModelPart part, PoseStack pose, RenderType type, int light,
			int overlay, TextureAtlasSprite sprite, boolean p_438944_, boolean p_440215_) {
		if (part instanceof SelfRenderer sr) {
			sr.submitSelf(new RenderCollector(pose, collector, type, light, overlay, -1, 0, sprite, null));
		} else
			collector.submitModelPart(part, pose, type, light, overlay, sprite, p_438944_,
					p_440215_);
	}

	@Override
	public void submitModelPart(ModelPart part, PoseStack pose, RenderType type, int light,
			int overlay, TextureAtlasSprite sprite, boolean p_439080_, boolean p_438989_, int tint,
			CrumblingOverlay p_442896_, int outline) {
		if (part instanceof SelfRenderer sr) {
			sr.submitSelf(new RenderCollector(pose, collector, type, light, overlay, tint, outline, sprite, null));
		} else
			collector.submitModelPart(part, pose, type, light, overlay, sprite, p_439080_,
					p_438989_, tint, p_442896_, outline);
	}

	@Override
	public void submitBlock(PoseStack p_438936_, BlockState p_439547_, int p_440601_, int p_440032_, int p_440736_) {
		collector.submitBlock(p_438936_, p_439547_, p_440601_, p_440032_, p_440736_);
	}

	@Override
	public void submitMovingBlock(PoseStack p_439854_, MovingBlockRenderState p_440284_) {
		collector.submitMovingBlock(p_439854_, p_440284_);
	}

	@Override
	public void submitBlockModel(PoseStack p_439157_, RenderType p_440628_, BlockStateModel p_439853_, float p_440368_,
			float p_440148_, float p_440307_, int p_440602_, int p_440529_, int p_440743_) {
		collector.submitBlockModel(p_439157_, p_440628_, p_439853_, p_440368_, p_440148_, p_440307_, p_440602_,
				p_440529_, p_440743_);
	}

	@Override
	public void submitItem(PoseStack p_439086_, ItemDisplayContext p_439900_, int p_439678_, int p_440575_,
			int p_440740_, int[] p_440087_, List<BakedQuad> p_440405_, RenderType p_440525_, FoilType p_438984_) {
		collector.submitItem(p_439086_, p_439900_, p_439678_, p_440575_, p_440740_, p_440087_, p_440405_, p_440525_,
				p_438984_);
	}

	@Override
	public void submitCustomGeometry(PoseStack p_440145_, RenderType p_440184_, CustomGeometryRenderer p_439967_) {
		collector.submitCustomGeometry(p_440145_, p_440184_, p_439967_);
	}

	@Override
	public void submitParticleGroup(ParticleGroupRenderer p_445517_) {
		collector.submitParticleGroup(p_445517_);
	}

	public static class CPMSubmitNodeCollector extends CPMOrderedSubmitNodeCollector implements SubmitNodeCollector {
		private final SubmitNodeCollector collector;

		public CPMSubmitNodeCollector(SubmitNodeCollector collector) {
			super(collector);
			this.collector = collector;
		}

		@Override
		public OrderedSubmitNodeCollector order(int order) {
			return new CPMOrderedSubmitNodeCollector(collector.order(order));
		}
	}
}
