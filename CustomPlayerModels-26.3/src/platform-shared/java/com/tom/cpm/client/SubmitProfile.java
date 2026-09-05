package com.tom.cpm.client;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.Submit;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpl.render.RecordBuffer;
import com.tom.cpm.client.SelfRenderer.RenderCollector;

public interface SubmitProfile {
	void submit(RenderCollector collector, RecordBuffer buffer);
	RenderType getRenderType();

	public static class ModelSubmitProfile implements SubmitProfile {
		private RenderType rt;

		public ModelSubmitProfile(RenderType rt) {
			this.rt = rt;
		}

		@Override
		public void submit(RenderCollector collector, RecordBuffer buffer) {
			submit(rt, collector, buffer);
		}

		public static void submit(RenderType rt, RenderCollector collector, RecordBuffer buffer) {
			collector.collector().submitModel(new CustomModelLayer(buffer, collector.lw()), Unit.INSTANCE, collector.pose(), rt, collector.light(), collector.overlay(), collector.tint(), collector.sprite(), collector.outline());
		}

		public static Function<Identifier, SubmitProfile> of(Function<Identifier, RenderType> rtFact) {
			return rl -> new ModelSubmitProfile(rtFact.apply(rl));
		}

		public static Supplier<SubmitProfile> of(Supplier<RenderType> rtFact) {
			return () -> new ModelSubmitProfile(rtFact.get());
		}

		@Override
		public RenderType getRenderType() {
			return rt;
		}
	}

	public static class GizmoSubmitProfile implements SubmitProfile {

		@Override
		public void submit(RenderCollector collector, RecordBuffer buffer) {
			OrderedSubmitNodeCollector coll = collector.collector();
			if (collector.collector() instanceof SubmitNodeCollector st) {
				coll = st.order(1);
			}
			if (coll instanceof SubmitNodeCollection st) {
				PoseStack.Pose pose = collector.pose().last().copy();
				Submit<Unit> submit = new Submit<>(CustomRenderTypes.linesNoDepth(), pose, new CustomModelLayer(buffer, collector.lw()), Unit.INSTANCE, collector.light(), collector.overlay(), collector.tint(), collector.sprite(), null);
				Platform.submitAlwaysOnTop(st, submit);
			}
		}

		public static Supplier<SubmitProfile> of() {
			GizmoSubmitProfile profile = new GizmoSubmitProfile();
			return () -> profile;
		}

		@Override
		public RenderType getRenderType() {
			return CustomRenderTypes.linesNoDepth();
		}
	}
}
