package com.tom.cpm.client;

import java.util.Collections;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Unit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.cpl.render.RecordBuffer;

public class CustomModelLayer extends Model<Unit> {

	public CustomModelLayer(RecordBuffer buffer, float lw) {
		super(new ModelLayerPart(buffer, lw), null);
	}

	private static class ModelLayerPart extends ModelPart {
		private final RecordBuffer buffer;
		private final float lw;

		public ModelLayerPart(RecordBuffer buffer, float lw) {
			super(Collections.emptyList(), Collections.emptyMap());
			this.buffer = buffer;
			this.lw = lw;
		}

		@Override
		public void render(PoseStack p, VertexConsumer c, int light, int overlay, int tint) {
			buffer.replay(new VBuffer(c, light, overlay, lw, p));
		}
	}
}
