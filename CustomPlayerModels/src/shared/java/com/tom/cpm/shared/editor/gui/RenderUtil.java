package com.tom.cpm.shared.editor.gui;

import com.tom.cpl.math.BoundingBox;
import com.tom.cpl.math.Mat3f;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.tree.ScalingElement;
import com.tom.cpm.shared.model.PlayerPartValues;
import com.tom.cpm.shared.model.render.BoxRender;
import com.tom.cpm.shared.util.ScalingOptions;

public class RenderUtil {

	public static void renderBounds(MatrixStack stack, VertexBuffer b, VanillaPose poseToApply, boolean applySc, ScalingElement sce) {
		stack.push();
		stack.translate(0.5f, 0, 0.5f);
		stack.scale(1.1f, 1.1f, 1.1f);
		float scaling = applySc ? sce.getScale() : 0;
		if(scaling > 0 && poseToApply != VanillaPose.SKULL_RENDER)stack.scale(scaling, scaling, scaling);
		BoundingBox bb = PlayerPartValues.getBounds(poseToApply);
		float eyeh = PlayerPartValues.getEyeHeight(poseToApply);
		eyeh *= sce.getScale(ScalingOptions.EYE_HEIGHT);
		if(poseToApply == VanillaPose.SLEEPING) {
			stack.translate(0, 0, 1.36f);
			stack.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90));
			stack.rotate(Vec3f.POSITIVE_Z.getDegreesQuaternion(90));
			stack.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
		}
		float w = sce.getScale(ScalingOptions.HITBOX_WIDTH);
		float h = sce.getScale(ScalingOptions.HITBOX_HEIGHT);
		bb = new BoundingBox(
				bb.minX * w,
				bb.minY,
				bb.minZ * w,
				bb.maxX * w,
				bb.maxY * h,
				bb.maxZ * w);
		BoxRender.drawBoundingBox(stack, b, bb, 1, 1, 1, 1);
		if(eyeh > 0) {
			bb = new BoundingBox(
					bb.minX,
					(eyeh - 0.01F),
					bb.minZ,
					bb.maxX,
					(eyeh + 0.01F),
					bb.maxZ);
			BoxRender.drawBoundingBox(stack, b, bb, 1, 0, 0, 1);
			Mat4f matrix4f = stack.getLast().getMatrix();
			Mat3f matrix3f = stack.getLast().getNormal();
			b.pos(matrix4f, 0.0F, eyeh, 0.0F).color(0, 0, 1, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
			b.pos(matrix4f, -2.0F, eyeh, 0.0F).color(0, 0, 1, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		}
		stack.pop();
	}
}
