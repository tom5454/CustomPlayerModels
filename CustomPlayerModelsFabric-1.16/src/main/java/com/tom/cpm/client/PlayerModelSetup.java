package com.tom.cpm.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

public class PlayerModelSetup {
	public static void setAngles(PlayerEntityModel<AbstractClientPlayerEntity> player, float f, float g, float i, float j, Arm arm, boolean swim) {
		boolean bl = false;
		boolean bl2 = swim;

		player.head.yaw = i * 0.017453292F;
		if (bl) {
			player.head.pitch = -0.7853982F;
		} else if (player.leaningPitch > 0.0F) {
			if (bl2) {
				player.head.pitch = lerpAngle(player.leaningPitch, player.head.pitch, -0.7853982F);
			} else {
				player.head.pitch = lerpAngle(player.leaningPitch, player.head.pitch, j * 0.017453292F);
			}
		} else {
			player.head.pitch = j * 0.017453292F;
		}

		player.torso.yaw = 0.0F;
		player.rightArm.pivotZ = 0.0F;
		player.rightArm.pivotX = -5.0F;
		player.leftArm.pivotZ = 0.0F;
		player.leftArm.pivotX = 5.0F;

		float k = 1.0F;
		if (bl) {
			k = 0;
			k /= 0.2F;
			k *= k * k;
		}
		if (k < 1.0F) {
			k = 1.0F;
		}

		player.rightArm.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 2.0F * g * 0.5F / k;
		player.leftArm.pitch = MathHelper.cos(f * 0.6662F) * 2.0F * g * 0.5F / k;

		player.rightArm.roll = 0.0F;
		player.leftArm.roll = 0.0F;

		player.rightLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g / k;
		player.leftLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g / k;
		player.rightLeg.yaw = 0.0F;
		player.leftLeg.yaw = 0.0F;
		player.rightLeg.roll = 0.0F;
		player.leftLeg.roll = 0.0F;

		if (player.riding) {
			player.rightArm.pitch += -0.62831855F;
			player.leftArm.pitch += -0.62831855F;

			player.rightLeg.pitch = -1.4137167F;
			player.rightLeg.yaw = 0.31415927F;
			player.rightLeg.roll = 0.07853982F;

			player.leftLeg.pitch = -1.4137167F;
			player.leftLeg.yaw = -0.31415927F;
			player.leftLeg.roll = -0.07853982F;
		}

		player.rightArm.yaw = 0.0F;
		player.leftArm.yaw = 0.0F;


		boolean bl3 = (arm == Arm.RIGHT);
		boolean bl4 = bl3 ? player.leftArmPose.method_30156() : player.rightArmPose.method_30156();
		if (bl3 != bl4) {
			method_30155(player);
			method_30154(player);
		} else {
			method_30154(player);
			method_30155(player);
		}

		method_29353(player, arm);

		if (player.sneaking) {
			player.torso.pitch = 0.5F;
			player.rightArm.pitch += 0.4F;
			player.leftArm.pitch += 0.4F;
			player.rightLeg.pivotZ = 4.0F;
			player.leftLeg.pivotZ = 4.0F;
			player.rightLeg.pivotY = 12.2F;
			player.leftLeg.pivotY = 12.2F;
			player.head.pivotY = 4.2F;
			player.torso.pivotY = 3.2F;
			player.leftArm.pivotY = 5.2F;
			player.rightArm.pivotY = 5.2F;
		} else {
			player.torso.pitch = 0.0F;
			player.rightLeg.pivotZ = 0.1F;
			player.leftLeg.pivotZ = 0.1F;
			player.rightLeg.pivotY = 12.0F;
			player.leftLeg.pivotY = 12.0F;
			player.head.pivotY = 0.0F;
			player.torso.pivotY = 0.0F;
			player.leftArm.pivotY = 2.0F;
			player.rightArm.pivotY = 2.0F;
		}

		CrossbowPosing.method_29350(player.rightArm, player.leftArm, 0);

		if (player.leaningPitch > 0.0F) {
			float l = f % 26.0F;


			float m = (arm == Arm.RIGHT && player.handSwingProgress > 0.0F) ? 0.0F : player.leaningPitch;
			float n = (arm == Arm.LEFT && player.handSwingProgress > 0.0F) ? 0.0F : player.leaningPitch;

			if (l < 14.0F) {
				player.leftArm.pitch = lerpAngle(n, player.leftArm.pitch, 0.0F);
				player.rightArm.pitch = MathHelper.lerp(m, player.rightArm.pitch, 0.0F);

				player.leftArm.yaw = lerpAngle(n, player.leftArm.yaw, 3.1415927F);
				player.rightArm.yaw = MathHelper.lerp(m, player.rightArm.yaw, 3.1415927F);

				player.leftArm.roll = lerpAngle(n, player.leftArm.roll, 3.1415927F + 1.8707964F * method_2807(l) / method_2807(14.0F));
				player.rightArm.roll = MathHelper.lerp(m, player.rightArm.roll, 3.1415927F - 1.8707964F * method_2807(l) / method_2807(14.0F));
			} else if (l >= 14.0F && l < 22.0F) {
				float o = (l - 14.0F) / 8.0F;

				player.leftArm.pitch = lerpAngle(n, player.leftArm.pitch, 1.5707964F * o);
				player.rightArm.pitch = MathHelper.lerp(m, player.rightArm.pitch, 1.5707964F * o);

				player.leftArm.yaw = lerpAngle(n, player.leftArm.yaw, 3.1415927F);
				player.rightArm.yaw = MathHelper.lerp(m, player.rightArm.yaw, 3.1415927F);

				player.leftArm.roll = lerpAngle(n, player.leftArm.roll, 5.012389F - 1.8707964F * o);
				player.rightArm.roll = MathHelper.lerp(m, player.rightArm.roll, 1.2707963F + 1.8707964F * o);
			} else if (l >= 22.0F && l < 26.0F) {
				float p = (l - 22.0F) / 4.0F;

				player.leftArm.pitch = lerpAngle(n, player.leftArm.pitch, 1.5707964F - 1.5707964F * p);
				player.rightArm.pitch = MathHelper.lerp(m, player.rightArm.pitch, 1.5707964F - 1.5707964F * p);

				player.leftArm.yaw = lerpAngle(n, player.leftArm.yaw, 3.1415927F);
				player.rightArm.yaw = MathHelper.lerp(m, player.rightArm.yaw, 3.1415927F);

				player.leftArm.roll = lerpAngle(n, player.leftArm.roll, 3.1415927F);
				player.rightArm.roll = MathHelper.lerp(m, player.rightArm.roll, 3.1415927F);
			}

			player.leftLeg.pitch = MathHelper.lerp(player.leaningPitch, player.leftLeg.pitch, 0.3F * MathHelper.cos(f * 0.33333334F + 3.1415927F));
			player.rightLeg.pitch = MathHelper.lerp(player.leaningPitch, player.rightLeg.pitch, 0.3F * MathHelper.cos(f * 0.33333334F));
		}

		player.helmet.copyPositionAndRotation(player.head);
	}

	private static void method_30154(PlayerEntityModel<AbstractClientPlayerEntity> player) {
		switch (player.rightArmPose) {
		case EMPTY:
			player.rightArm.yaw = 0.0F;
			break;
		case BLOCK:
			player.rightArm.pitch = player.rightArm.pitch * 0.5F - 0.9424779F;
			player.rightArm.yaw = -0.5235988F;
			break;
		case ITEM:
			player.rightArm.pitch = player.rightArm.pitch * 0.5F - 0.31415927F;
			player.rightArm.yaw = 0.0F;
			break;
		case THROW_SPEAR:
			player.rightArm.pitch = player.rightArm.pitch * 0.5F - 3.1415927F;
			player.rightArm.yaw = 0.0F;
			break;
		case BOW_AND_ARROW:
			player.rightArm.yaw = -0.1F + player.head.yaw;
			player.leftArm.yaw = 0.1F + player.head.yaw + 0.4F;
			player.rightArm.pitch = -1.5707964F + player.head.pitch;
			player.leftArm.pitch = -1.5707964F + player.head.pitch;
			break;
		default:
			break;
		}
	}

	private static void method_30155(PlayerEntityModel<AbstractClientPlayerEntity> player) {
		switch (player.leftArmPose) {
		case EMPTY:
			player.leftArm.yaw = 0.0F;
			break;
		case BLOCK:
			player.leftArm.pitch = player.leftArm.pitch * 0.5F - 0.9424779F;
			player.leftArm.yaw = 0.5235988F;
			break;
		case ITEM:
			player.leftArm.pitch = player.leftArm.pitch * 0.5F - 0.31415927F;
			player.leftArm.yaw = 0.0F;
			break;
		case THROW_SPEAR:
			player.leftArm.pitch = player.leftArm.pitch * 0.5F - 3.1415927F;
			player.leftArm.yaw = 0.0F;
			break;
		case BOW_AND_ARROW:
			player.rightArm.yaw = -0.1F + player.head.yaw - 0.4F;
			player.leftArm.yaw = 0.1F + player.head.yaw;
			player.rightArm.pitch = -1.5707964F + player.head.pitch;
			player.leftArm.pitch = -1.5707964F + player.head.pitch;
			break;
		default:
			break;
		}
	}

	protected static void method_29353(PlayerEntityModel<AbstractClientPlayerEntity> player, Arm arm) {
		if (player.handSwingProgress <= 0.0F) {
			return;
		}

		ModelPart modelPart = getArm(player, arm);

		float g = player.handSwingProgress;
		player.torso.yaw = MathHelper.sin(MathHelper.sqrt(g) * 6.2831855F) * 0.2F;
		if (arm == Arm.LEFT) {
			player.torso.yaw *= -1.0F;
		}
		player.rightArm.pivotZ = MathHelper.sin(player.torso.yaw) * 5.0F;
		player.rightArm.pivotX = -MathHelper.cos(player.torso.yaw) * 5.0F;
		player.leftArm.pivotZ = -MathHelper.sin(player.torso.yaw) * 5.0F;
		player.leftArm.pivotX = MathHelper.cos(player.torso.yaw) * 5.0F;
		player.rightArm.yaw += player.torso.yaw;
		player.leftArm.yaw += player.torso.yaw;
		player.leftArm.pitch += player.torso.yaw;

		g = 1.0F - player.handSwingProgress;
		g *= g;
		g *= g;
		g = 1.0F - g;
		float h = MathHelper.sin(g * 3.1415927F);
		float i = MathHelper.sin(player.handSwingProgress * 3.1415927F) * -(player.head.pitch - 0.7F) * 0.75F;
		modelPart.pitch = (float)(modelPart.pitch - h * 1.2D + i);
		modelPart.yaw += player.torso.yaw * 2.0F;
		modelPart.roll += MathHelper.sin(player.handSwingProgress * 3.1415927F) * -0.4F;
	}

	protected static float lerpAngle(float f, float g, float h) {
		float i = (h - g) % 6.2831855F;
		if (i < -3.1415927F) {
			i += 6.2831855F;
		}
		if (i >= 3.1415927F) {
			i -= 6.2831855F;
		}
		return g + f * i;
	}


	private static float method_2807(float f) { return -65.0F * f + f * f; }

	protected static ModelPart getArm(PlayerEntityModel<AbstractClientPlayerEntity> player, Arm arm) {
		if (arm == Arm.LEFT) {
			return player.leftArm;
		}
		return player.rightArm;
	}
}
