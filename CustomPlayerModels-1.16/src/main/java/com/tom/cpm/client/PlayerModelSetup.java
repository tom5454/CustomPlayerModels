package com.tom.cpm.client;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelHelper;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

public class PlayerModelSetup {

	public static void setRotationAngles(PlayerModel<AbstractClientPlayerEntity> player, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, HandSide handside, boolean swim) {
		boolean flag = false;
		boolean flag1 = swim;
		player.swimAmount = swim ? 1 : 0;
		player.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		if (flag) {
			player.head.xRot = (-(float)Math.PI / 4F);
		} else if (player.swimAmount > 0.0F) {
			if (flag1) {
				player.head.xRot = rotLerpRad(player.swimAmount, player.head.xRot, (-(float)Math.PI / 4F));
			} else {
				player.head.xRot = rotLerpRad(player.swimAmount, player.head.xRot, headPitch * ((float)Math.PI / 180F));
			}
		} else {
			player.head.xRot = headPitch * ((float)Math.PI / 180F);
		}

		player.body.yRot = 0.0F;
		player.rightArm.z = 0.0F;
		player.rightArm.x = -5.0F;
		player.leftArm.z = 0.0F;
		player.leftArm.x = 5.0F;
		float f = 1.0F;
		if (flag) {
			f = 0;
			f = f / 0.2F;
			f = f * f * f;
		}

		if (f < 1.0F) {
			f = 1.0F;
		}

		player.rightArm.xRot = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
		player.leftArm.xRot = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
		player.rightArm.zRot = 0.0F;
		player.leftArm.zRot = 0.0F;
		player.rightLeg.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
		player.leftLeg.xRot = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount / f;
		player.rightLeg.yRot = 0.0F;
		player.leftLeg.yRot = 0.0F;
		player.rightLeg.zRot = 0.0F;
		player.leftLeg.zRot = 0.0F;
		if (player.riding) {
			player.rightArm.xRot += (-(float)Math.PI / 5F);
			player.leftArm.xRot += (-(float)Math.PI / 5F);
			player.rightLeg.xRot = -1.4137167F;
			player.rightLeg.yRot = ((float)Math.PI / 10F);
			player.rightLeg.zRot = 0.07853982F;
			player.leftLeg.xRot = -1.4137167F;
			player.leftLeg.yRot = (-(float)Math.PI / 10F);
			player.leftLeg.zRot = -0.07853982F;
		}

		player.rightArm.yRot = 0.0F;
		player.leftArm.yRot = 0.0F;
		boolean flag2 = handside == HandSide.RIGHT;
		boolean flag3 = flag2 ? player.leftArmPose.isTwoHanded() : player.rightArmPose.isTwoHanded();
		if (flag2 != flag3) {
			poseLeftArm(player);
			poseRightArm(player);
		} else {
			poseRightArm(player);
			poseLeftArm(player);
		}

		setupAttackAnimation(player, handside);
		if (player.crouching) {
			player.body.xRot = 0.5F;
			player.rightArm.xRot += 0.4F;
			player.leftArm.xRot += 0.4F;
			player.rightLeg.z = 4.0F;
			player.leftLeg.z = 4.0F;
			player.rightLeg.y = 12.2F;
			player.leftLeg.y = 12.2F;
			player.head.y = 4.2F;
			player.body.y = 3.2F;
			player.leftArm.y = 5.2F;
			player.rightArm.y = 5.2F;
		} else {
			player.body.xRot = 0.0F;
			player.rightLeg.z = 0.1F;
			player.leftLeg.z = 0.1F;
			player.rightLeg.y = 12.0F;
			player.leftLeg.y = 12.0F;
			player.head.y = 0.0F;
			player.body.y = 0.0F;
			player.leftArm.y = 2.0F;
			player.rightArm.y = 2.0F;
		}

		ModelHelper.bobArms(player.rightArm, player.leftArm, 0);
		if (player.swimAmount > 0.0F) {
			float f1 = limbSwing % 26.0F;
			float f2 = handside == HandSide.RIGHT && player.attackTime > 0.0F ? 0.0F : player.swimAmount;
			float f3 = handside == HandSide.LEFT && player.attackTime > 0.0F ? 0.0F : player.swimAmount;
			if (f1 < 14.0F) {
				player.leftArm.xRot = rotLerpRad(f3, player.leftArm.xRot, 0.0F);
				player.rightArm.xRot = MathHelper.lerp(f2, player.rightArm.xRot, 0.0F);
				player.leftArm.yRot = rotLerpRad(f3, player.leftArm.yRot, (float)Math.PI);
				player.rightArm.yRot = MathHelper.lerp(f2, player.rightArm.yRot, (float)Math.PI);
				player.leftArm.zRot = rotLerpRad(f3, player.leftArm.zRot, (float)Math.PI + 1.8707964F * getArmAngleSq(f1) / getArmAngleSq(14.0F));
				player.rightArm.zRot = MathHelper.lerp(f2, player.rightArm.zRot, (float)Math.PI - 1.8707964F * getArmAngleSq(f1) / getArmAngleSq(14.0F));
			} else if (f1 >= 14.0F && f1 < 22.0F) {
				float f6 = (f1 - 14.0F) / 8.0F;
				player.leftArm.xRot = rotLerpRad(f3, player.leftArm.xRot, ((float)Math.PI / 2F) * f6);
				player.rightArm.xRot = MathHelper.lerp(f2, player.rightArm.xRot, ((float)Math.PI / 2F) * f6);
				player.leftArm.yRot = rotLerpRad(f3, player.leftArm.yRot, (float)Math.PI);
				player.rightArm.yRot = MathHelper.lerp(f2, player.rightArm.yRot, (float)Math.PI);
				player.leftArm.zRot = rotLerpRad(f3, player.leftArm.zRot, 5.012389F - 1.8707964F * f6);
				player.rightArm.zRot = MathHelper.lerp(f2, player.rightArm.zRot, 1.2707963F + 1.8707964F * f6);
			} else if (f1 >= 22.0F && f1 < 26.0F) {
				float f4 = (f1 - 22.0F) / 4.0F;
				player.leftArm.xRot = rotLerpRad(f3, player.leftArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f4);
				player.rightArm.xRot = MathHelper.lerp(f2, player.rightArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f4);
				player.leftArm.yRot = rotLerpRad(f3, player.leftArm.yRot, (float)Math.PI);
				player.rightArm.yRot = MathHelper.lerp(f2, player.rightArm.yRot, (float)Math.PI);
				player.leftArm.zRot = rotLerpRad(f3, player.leftArm.zRot, (float)Math.PI);
				player.rightArm.zRot = MathHelper.lerp(f2, player.rightArm.zRot, (float)Math.PI);
			}

			player.leftLeg.xRot = MathHelper.lerp(player.swimAmount, player.leftLeg.xRot, 0.3F * MathHelper.cos(limbSwing * 0.33333334F + (float)Math.PI));
			player.rightLeg.xRot = MathHelper.lerp(player.swimAmount, player.rightLeg.xRot, 0.3F * MathHelper.cos(limbSwing * 0.33333334F));
		}

		player.hat.copyFrom(player.head);
	}

	private static void poseLeftArm(PlayerModel<AbstractClientPlayerEntity> player) {
		switch(player.leftArmPose) {
		case EMPTY:
			player.leftArm.yRot = 0.0F;
			break;
		case BLOCK:
			player.leftArm.xRot = player.leftArm.xRot * 0.5F - 0.9424779F;
			player.leftArm.yRot = ((float)Math.PI / 6F);
			break;
		case ITEM:
			player.leftArm.xRot = player.leftArm.xRot * 0.5F - ((float)Math.PI / 10F);
			player.leftArm.yRot = 0.0F;
			break;
		case THROW_SPEAR:
			player.leftArm.xRot = player.leftArm.xRot * 0.5F - (float)Math.PI;
			player.leftArm.yRot = 0.0F;
			break;
		case BOW_AND_ARROW:
			player.rightArm.yRot = -0.1F + player.head.yRot - 0.4F;
			player.leftArm.yRot = 0.1F + player.head.yRot;
			player.rightArm.xRot = (-(float)Math.PI / 2F) + player.head.xRot;
			player.leftArm.xRot = (-(float)Math.PI / 2F) + player.head.xRot;
			break;
		case CROSSBOW_CHARGE:
			break;
		case CROSSBOW_HOLD:
			ModelHelper.animateCrossbowHold(player.rightArm, player.leftArm, player.head, false);
		}

	}

	private static void poseRightArm(PlayerModel<AbstractClientPlayerEntity> player) {
		switch(player.rightArmPose) {
		case EMPTY:
			player.rightArm.yRot = 0.0F;
			break;
		case BLOCK:
			player.rightArm.xRot = player.rightArm.xRot * 0.5F - 0.9424779F;
			player.rightArm.yRot = (-(float)Math.PI / 6F);
			break;
		case ITEM:
			player.rightArm.xRot = player.rightArm.xRot * 0.5F - ((float)Math.PI / 10F);
			player.rightArm.yRot = 0.0F;
			break;
		case THROW_SPEAR:
			player.rightArm.xRot = player.rightArm.xRot * 0.5F - (float)Math.PI;
			player.rightArm.yRot = 0.0F;
			break;
		case BOW_AND_ARROW:
			player.rightArm.yRot = -0.1F + player.head.yRot;
			player.leftArm.yRot = 0.1F + player.head.yRot + 0.4F;
			player.rightArm.xRot = (-(float)Math.PI / 2F) + player.head.xRot;
			player.leftArm.xRot = (-(float)Math.PI / 2F) + player.head.xRot;
			break;
		case CROSSBOW_CHARGE:
			break;
		case CROSSBOW_HOLD:
			ModelHelper.animateCrossbowHold(player.rightArm, player.leftArm, player.head, true);
		}

	}

	public static float rotLerpRad(float angleIn, float maxAngleIn, float mulIn) {
		float f = (mulIn - maxAngleIn) % ((float)Math.PI * 2F);
		if (f < -(float)Math.PI) {
			f += ((float)Math.PI * 2F);
		}

		if (f >= (float)Math.PI) {
			f -= ((float)Math.PI * 2F);
		}

		return maxAngleIn + angleIn * f;
	}

	private static void setupAttackAnimation(PlayerModel<AbstractClientPlayerEntity> player, HandSide handside) {
		if (!(player.attackTime <= 0.0F)) {
			ModelRenderer modelrenderer = getArmForSide(player, handside);
			float f = player.attackTime;
			player.body.yRot = MathHelper.sin(MathHelper.sqrt(f) * ((float)Math.PI * 2F)) * 0.2F;
			if (handside == HandSide.LEFT) {
				player.body.yRot *= -1.0F;
			}

			player.rightArm.z = MathHelper.sin(player.body.yRot) * 5.0F;
			player.rightArm.x = -MathHelper.cos(player.body.yRot) * 5.0F;
			player.leftArm.z = -MathHelper.sin(player.body.yRot) * 5.0F;
			player.leftArm.x = MathHelper.cos(player.body.yRot) * 5.0F;
			player.rightArm.yRot += player.body.yRot;
			player.leftArm.yRot += player.body.yRot;
			player.leftArm.xRot += player.body.yRot;
			f = 1.0F - player.attackTime;
			f = f * f;
			f = f * f;
			f = 1.0F - f;
			float f1 = MathHelper.sin(f * (float)Math.PI);
			float f2 = MathHelper.sin(player.attackTime * (float)Math.PI) * -(player.head.xRot - 0.7F) * 0.75F;
			modelrenderer.xRot = (float)(modelrenderer.xRot - (f1 * 1.2D + f2));
			modelrenderer.yRot += player.body.yRot * 2.0F;
			modelrenderer.zRot += MathHelper.sin(player.attackTime * (float)Math.PI) * -0.4F;
		}
	}

	private static ModelRenderer getArmForSide(PlayerModel<AbstractClientPlayerEntity> player, HandSide side) {
		return side == HandSide.LEFT ? player.leftArm : player.rightArm;
	}

	private static float getArmAngleSq(float limbSwing) {
		return -65.0F * limbSwing + limbSwing * limbSwing;
	}
}
