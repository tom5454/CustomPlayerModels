package com.tom.cpm.client;

import java.util.function.BiConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Unit;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpl.render.RecordBuffer;
import com.tom.cpl.render.VBuffers.NativeRenderType;

public interface SelfRenderer {
	void submitSelf(RenderCollector collector);

	public static record RenderCollector(PoseStack pose, OrderedSubmitNodeCollector collector, RenderType defaultRt, int light, int overlay, int tint, int outline, TextureAtlasSprite sprite, Object state) {

		public void submitVanilla(ModelPart part) {
			collector.submitModel(new VanillaPartLayer(part, PlayerRenderManager.entity), part.storePose(), pose, defaultRt, light, overlay, tint, sprite, outline, null);
		}

		public BiConsumer<NativeRenderType, RecordBuffer> recordBuffer() {
			return (rt, rb) -> {
				RenderType nrt = rt.getNativeType();
				if (nrt == null)nrt = defaultRt;
				collector.submitModel(new CustomModelLayer(rb), Unit.INSTANCE, pose, nrt, light, overlay, tint, sprite, outline, null);
			};
		}

		public void storeState(PlayerModel pm) {
			if (state instanceof PlayerRenderStateAccess prs)
				prs.cpm$storeState(pm);
		}
	}
}
