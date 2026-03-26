package com.tom.cpm.client;

import java.util.List;

import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector.ParticleGroupRenderer;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState.LeashState;
import net.minecraft.client.renderer.entity.state.EntityRenderState.ShadowPiece;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.SelfRenderer.RenderCollector;

public class CPMOrderedSubmitNodeCollector implements OrderedSubmitNodeCollector {
	private final OrderedSubmitNodeCollector collector;

	public CPMOrderedSubmitNodeCollector(OrderedSubmitNodeCollector collector) {
		this.collector = collector;
	}

	@Override
	public <S> void submitModel(Model<? super S> model, S state, PoseStack pose, RenderType type,
			int light, int overlay, int tint, TextureAtlasSprite sprite, int outline,
			CrumblingOverlay crumblingOverlay) {
		if (model.root() instanceof SelfRenderer sr) {
			model.setupAnim(state);
			sr.submitSelf(new RenderCollector(pose, collector, type, light, overlay, tint, outline, sprite, state));
		} else
			collector.submitModel(model, state, pose, type, light, overlay, tint, sprite,
					outline, crumblingOverlay);
	}

	@Override
	public <S> void submitModel(Model<? super S> model, S state, PoseStack pose, RenderType type,
			int light, int overlay, int outline, CrumblingOverlay crumblingOverlay) {
		if (model.root() instanceof SelfRenderer sr) {
			model.setupAnim(state);
			sr.submitSelf(new RenderCollector(pose, collector, type, light, overlay, -1, outline, null, state));
		} else
			collector.submitModel(model, state, pose, type, light, overlay, outline, crumblingOverlay);
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
			int overlay, TextureAtlasSprite sprite, int tint, CrumblingOverlay crumblingOverlay) {
		if (part instanceof SelfRenderer sr) {
			sr.submitSelf(new RenderCollector(pose, collector, type, light, overlay, tint, 0, sprite, null));
		} else
			collector.submitModelPart(part, pose, type, light, overlay, sprite, tint,
					crumblingOverlay);
	}

	@Override
	public void submitModelPart(ModelPart part, PoseStack pose, RenderType type, int light,
			int overlay, TextureAtlasSprite sprite, boolean sheeted, boolean hasFoil) {
		if (part instanceof SelfRenderer sr) {
			sr.submitSelf(new RenderCollector(pose, collector, type, light, overlay, -1, 0, sprite, null));
		} else
			collector.submitModelPart(part, pose, type, light, overlay, sprite, sheeted,
					hasFoil);
	}

	@Override
	public void submitModelPart(ModelPart part, PoseStack pose, RenderType type, int light,
			int overlay, TextureAtlasSprite sprite, boolean sheeted, boolean hasFoil, int tint,
			CrumblingOverlay crumblingOverlay, int outline) {
		if (part instanceof SelfRenderer sr) {
			sr.submitSelf(new RenderCollector(pose, collector, type, light, overlay, tint, outline, sprite, null));
		} else
			collector.submitModelPart(part, pose, type, light, overlay, sprite, sheeted,
					hasFoil, tint, crumblingOverlay, outline);
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

		public static void injectSNC(LocalRef<SubmitNodeCollector> snc) {
			var collector = snc.get();
			if (collector instanceof CPMSubmitNodeCollector)return;
			snc.set(new CPMSubmitNodeCollector(collector));
		}
	}

	@Override
	public void submitShadow(PoseStack poseStack, float radius, List<ShadowPiece> pieces) {
		collector.submitShadow(poseStack, radius, pieces);
	}

	@Override
	public void submitNameTag(PoseStack poseStack, @Nullable Vec3 nameTagAttachment, int offset, Component name,
			boolean seeThrough, int lightCoords, double distanceToCameraSq, CameraRenderState camera) {
		collector.submitNameTag(poseStack, nameTagAttachment, offset, name, seeThrough, lightCoords, distanceToCameraSq,
				camera);
	}

	@Override
	public void submitText(PoseStack poseStack, float x, float y, FormattedCharSequence string, boolean dropShadow,
			DisplayMode displayMode, int lightCoords, int color, int backgroundColor, int outlineColor) {
		collector.submitText(poseStack, x, y, string, dropShadow, displayMode, lightCoords, color, backgroundColor,
				outlineColor);
	}

	@Override
	public void submitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf rotation) {
		collector.submitFlame(poseStack, renderState, rotation);
	}

	@Override
	public void submitLeash(PoseStack poseStack, LeashState leashState) {
		collector.submitLeash(poseStack, leashState);
	}

	@Override
	public <S> void submitModel(Model<? super S> model, S state, PoseStack poseStack, Identifier texture,
			int lightCoords, int overlayCoords, int outlineColor, @Nullable CrumblingOverlay crumblingOverlay) {
		collector.submitModel(model, state, poseStack, texture, lightCoords, overlayCoords, outlineColor,
				crumblingOverlay);
	}

	@Override
	public <S> void submitModel(Model<S> model, S state, PoseStack poseStack, int lightCoords, int overlayCoords,
			int tintedColor, SpriteId sprite, SpriteGetter sprites, int outlineColor,
			@Nullable CrumblingOverlay crumblingOverlay) {
		collector.submitModel(model, state, poseStack, lightCoords, overlayCoords, tintedColor, sprite, sprites,
				outlineColor, crumblingOverlay);
	}

	@Override
	public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState) {
		collector.submitMovingBlock(poseStack, movingBlockRenderState);
	}

	@Override
	public void submitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> parts,
			int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
		collector.submitBlockModel(poseStack, renderType, parts, tintLayers, lightCoords, overlayCoords, outlineColor);
	}

	@Override
	public void submitBreakingBlockModel(PoseStack poseStack, BlockStateModel model, long seed, int progress) {
		collector.submitBreakingBlockModel(poseStack, model, seed, progress);
	}

	@Override
	public void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords,
			int outlineColor, int[] tintLayers, List<BakedQuad> quads, FoilType foilType) {
		collector.submitItem(poseStack, displayContext, lightCoords, overlayCoords, outlineColor, tintLayers, quads,
				foilType);
	}

	@Override
	public void submitCustomGeometry(PoseStack poseStack, RenderType renderType,
			CustomGeometryRenderer customGeometryRenderer) {
		collector.submitCustomGeometry(poseStack, renderType, customGeometryRenderer);
	}

	@Override
	public void submitParticleGroup(ParticleGroupRenderer particleGroupRenderer) {
		collector.submitParticleGroup(particleGroupRenderer);
	}
}
