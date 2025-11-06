package com.tom.cpm.client;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public class CapeTransformUtil {

	public static void applyTransform(HumanoidModel<AvatarRenderState> model) {
		if (model instanceof PlayerCapeModel m) {
			m.cape.loadPose(CapeTransformUtil.combine(model.body.storePose(), m.cape.storePose()));
		}
	}

	public static PartPose combine(PartPose first, PartPose second) {
		// Create the first transformation matrix
		Matrix4f transformA = new Matrix4f()
				.translate(first.x(), first.y(), first.z())
				.rotateZYX(first.zRot(), first.yRot(), first.xRot())
				.scale(first.xScale(), first.yScale(), first.zScale());

		// Create the second transformation matrix
		Matrix4f transformB = new Matrix4f()
				.translate(second.x(), second.y(), second.z())
				.rotateZYX(second.zRot(), second.yRot(), second.xRot())
				.scale(second.xScale(), second.yScale(), second.zScale());

		// Multiply them to get the final transformation
		transformA.mul(transformB);

		// Extract final translation
		Vector3f translation = transformA.getTranslation(new Vector3f());

		// Extract final scale
		Vector3f scale = transformA.getScale(new Vector3f());

		// Extract final rotation
		Quaternionf rotation = transformA.getUnnormalizedRotation(new Quaternionf());
		Vector3f eulerAngles = rotation.getEulerAnglesZYX(new Vector3f());

		// Return the combined PartPose
		return new PartPose(
				translation.x, translation.y, translation.z,
				eulerAngles.x, eulerAngles.y, eulerAngles.z,
				scale.x, scale.y, scale.z
				);
	}
}
