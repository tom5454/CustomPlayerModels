package com.tom.cpm.client;

import java.util.function.BiConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpl.render.RecordBuffer;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpm.client.SubmitProfile.ModelSubmitProfile;

public interface SelfRenderer {
	void submitSelf(RenderCollector collector);

	public static record RenderCollector(PoseStack pose, OrderedSubmitNodeCollector collector, RenderType defaultRt, int light, int overlay, int tint, int outline, UvMapping sprite, float lw, Object state) {

		public void submitVanilla(ModelPart part) {
			collector.submitModel(new VanillaPartLayer(part, PlayerRenderManager.entityRt), part.storePose(), pose, defaultRt, light, overlay, tint, sprite, outline);
		}

		public BiConsumer<NativeRenderType, RecordBuffer> recordBuffer() {
			return (rt, rb) -> {
				SubmitProfile profile = rt.getNativeType();
				if (profile != null) {
					profile.submit(this, rb);
				} else {
					ModelSubmitProfile.submit(defaultRt, this, rb);
				}
			};
		}

		public void storeState(PlayerModel pm) {
			if (state instanceof PlayerRenderStateAccess prs)
				prs.cpm$storeState(pm);
		}
	}
}
