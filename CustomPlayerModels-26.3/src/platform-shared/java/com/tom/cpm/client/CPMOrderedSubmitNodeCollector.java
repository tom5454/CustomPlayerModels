package com.tom.cpm.client;

import java.util.List;

import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhase;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState.LeashState;
import net.minecraft.client.renderer.entity.state.EntityRenderState.ShadowPiece;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives.Group;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.SelfRenderer.RenderCollector;

public class CPMOrderedSubmitNodeCollector implements OrderedSubmitNodeCollector {
	private final OrderedSubmitNodeCollector collector;

	public CPMOrderedSubmitNodeCollector(OrderedSubmitNodeCollector collector) {
		this.collector = collector;
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
	public void submitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> parts,
			int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
		collector.submitBlockModel(poseStack, renderType, parts, tintLayers, lightCoords, overlayCoords, outlineColor);
	}

	@Override
	public void submitCustomGeometry(PoseStack poseStack, RenderType renderType,
			CustomGeometryRenderer customGeometryRenderer) {
		collector.submitCustomGeometry(poseStack, renderType, customGeometryRenderer);
	}

	@Override
	public void submitShapeOutline(PoseStack poseStack, VoxelShape shape, RenderType renderType, int color, float width,
			boolean afterTerrain) {
		collector.submitShapeOutline(poseStack, shape, renderType, color, width, afterTerrain);
	}

	@Override
	public void submitNameTag(PoseStack poseStack, @Nullable Vec3 nameTagAttachment, int offset, Component name,
			boolean seeThrough, int lightCoords, CameraRenderState camera) {
		collector.submitNameTag(poseStack, nameTagAttachment, offset, name, seeThrough, lightCoords, camera);
	}

	@Override
	public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState,
			int outlineColor) {
		collector.submitMovingBlock(poseStack, movingBlockRenderState, outlineColor);
	}

	@Override
	public void submitQuadParticleGroup(QuadParticleRenderState particles) {
		collector.submitQuadParticleGroup(particles);
	}

	@Override
	public void submitGizmoPrimitives(Group group, CameraRenderState camera, boolean onTop) {
		collector.submitGizmoPrimitives(group, camera, onTop);
	}

	@Override
	public <T extends SubmitNode> void submitCustom(SubmitRenderPhase<T> phase, T node) {
		collector.submitCustom(phase, node);
	}

	@Override
	public <S> void submitModel(Model<? super S> model, S state, PoseStack poseStack, RenderType renderType,
			int lightCoords, int overlayCoords, int tintedColor, @Nullable UvMapping uvMapping, int outlineColor) {
		if (model.root() instanceof SelfRenderer sr) {
			model.setupAnim(state);
			sr.submitSelf(new RenderCollector(poseStack, collector, renderType, lightCoords, overlayCoords, tintedColor, outlineColor, uvMapping, 0f, state));
		} else
			collector.submitModel(model, state, poseStack, renderType, lightCoords, overlayCoords, tintedColor, uvMapping, outlineColor);
	}

	@Override
	public <S> void submitCrumblingOverlay(Model<? super S> model, S state, PoseStack poseStack, RenderType renderType,
			int lightCoords, int overlayCoords, int tintedColor, CrumblingOverlay crumblingOverlay) {
		collector.submitCrumblingOverlay(model, state, poseStack, renderType, lightCoords, overlayCoords, tintedColor, crumblingOverlay);
	}

	@Override
	public void submitBreakingBlockModel(PoseStack poseStack, List<BlockStateModelPart> parts, int progress,
			boolean isBlockTranslucent) {
		collector.submitBreakingBlockModel(poseStack, parts, progress, isBlockTranslucent);
	}

	@Override
	public void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords,
			int outlineColor, int[] tintLayers, ItemQuads quads, FoilType foilType) {
		collector.submitItem(poseStack, displayContext, lightCoords, overlayCoords, outlineColor, tintLayers, quads,
				foilType);
	}

	@Override
	public void submitTextBackground(PoseStack poseStack, float x0, float y0, float x1, float y1, int color,
			DisplayMode displayMode, int lightCoords) {
		collector.submitTextBackground(poseStack, x0, y0, x1, y1, color, displayMode, lightCoords);
	}
}
