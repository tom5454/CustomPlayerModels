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
		player.swimAnimation = swim ? 1 : 0;
		player.bipedHead.rotateAngleY = netHeadYaw * ((float)Math.PI / 180F);
		if (flag) {
			player.bipedHead.rotateAngleX = (-(float)Math.PI / 4F);
		} else if (player.swimAnimation > 0.0F) {
			if (flag1) {
				player.bipedHead.rotateAngleX = rotLerpRad(player.swimAnimation, player.bipedHead.rotateAngleX, (-(float)Math.PI / 4F));
			} else {
				player.bipedHead.rotateAngleX = rotLerpRad(player.swimAnimation, player.bipedHead.rotateAngleX, headPitch * ((float)Math.PI / 180F));
			}
		} else {
			player.bipedHead.rotateAngleX = headPitch * ((float)Math.PI / 180F);
		}

		player.bipedBody.rotateAngleY = 0.0F;
		player.bipedRightArm.rotationPointZ = 0.0F;
		player.bipedRightArm.rotationPointX = -5.0F;
		player.bipedLeftArm.rotationPointZ = 0.0F;
		player.bipedLeftArm.rotationPointX = 5.0F;
		float f = 1.0F;
		if (flag) {
			f = 0;
			f = f / 0.2F;
			f = f * f * f;
		}

		if (f < 1.0F) {
			f = 1.0F;
		}

		player.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
		player.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
		player.bipedRightArm.rotateAngleZ = 0.0F;
		player.bipedLeftArm.rotateAngleZ = 0.0F;
		player.bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
		player.bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount / f;
		player.bipedRightLeg.rotateAngleY = 0.0F;
		player.bipedLeftLeg.rotateAngleY = 0.0F;
		player.bipedRightLeg.rotateAngleZ = 0.0F;
		player.bipedLeftLeg.rotateAngleZ = 0.0F;
		if (player.isSitting) {
			player.bipedRightArm.rotateAngleX += (-(float)Math.PI / 5F);
			player.bipedLeftArm.rotateAngleX += (-(float)Math.PI / 5F);
			player.bipedRightLeg.rotateAngleX = -1.4137167F;
			player.bipedRightLeg.rotateAngleY = ((float)Math.PI / 10F);
			player.bipedRightLeg.rotateAngleZ = 0.07853982F;
			player.bipedLeftLeg.rotateAngleX = -1.4137167F;
			player.bipedLeftLeg.rotateAngleY = (-(float)Math.PI / 10F);
			player.bipedLeftLeg.rotateAngleZ = -0.07853982F;
		}

		player.bipedRightArm.rotateAngleY = 0.0F;
		player.bipedLeftArm.rotateAngleY = 0.0F;
		boolean flag2 = handside == HandSide.RIGHT;
		boolean flag3 = flag2 ? player.leftArmPose.func_241657_a_() : player.rightArmPose.func_241657_a_();
		if (flag2 != flag3) {
			func_241655_c_(player);
			func_241654_b_(player);
		} else {
			func_241654_b_(player);
			func_241655_c_(player);
		}

		func_230486_a_(player, handside);
		if (player.isSneak) {
			player.bipedBody.rotateAngleX = 0.5F;
			player.bipedRightArm.rotateAngleX += 0.4F;
			player.bipedLeftArm.rotateAngleX += 0.4F;
			player.bipedRightLeg.rotationPointZ = 4.0F;
			player.bipedLeftLeg.rotationPointZ = 4.0F;
			player.bipedRightLeg.rotationPointY = 12.2F;
			player.bipedLeftLeg.rotationPointY = 12.2F;
			player.bipedHead.rotationPointY = 4.2F;
			player.bipedBody.rotationPointY = 3.2F;
			player.bipedLeftArm.rotationPointY = 5.2F;
			player.bipedRightArm.rotationPointY = 5.2F;
		} else {
			player.bipedBody.rotateAngleX = 0.0F;
			player.bipedRightLeg.rotationPointZ = 0.1F;
			player.bipedLeftLeg.rotationPointZ = 0.1F;
			player.bipedRightLeg.rotationPointY = 12.0F;
			player.bipedLeftLeg.rotationPointY = 12.0F;
			player.bipedHead.rotationPointY = 0.0F;
			player.bipedBody.rotationPointY = 0.0F;
			player.bipedLeftArm.rotationPointY = 2.0F;
			player.bipedRightArm.rotationPointY = 2.0F;
		}

		ModelHelper.func_239101_a_(player.bipedRightArm, player.bipedLeftArm, 0);
		if (player.swimAnimation > 0.0F) {
			float f1 = limbSwing % 26.0F;
			float f2 = handside == HandSide.RIGHT && player.swingProgress > 0.0F ? 0.0F : player.swimAnimation;
			float f3 = handside == HandSide.LEFT && player.swingProgress > 0.0F ? 0.0F : player.swimAnimation;
			if (f1 < 14.0F) {
				player.bipedLeftArm.rotateAngleX = rotLerpRad(f3, player.bipedLeftArm.rotateAngleX, 0.0F);
				player.bipedRightArm.rotateAngleX = MathHelper.lerp(f2, player.bipedRightArm.rotateAngleX, 0.0F);
				player.bipedLeftArm.rotateAngleY = rotLerpRad(f3, player.bipedLeftArm.rotateAngleY, (float)Math.PI);
				player.bipedRightArm.rotateAngleY = MathHelper.lerp(f2, player.bipedRightArm.rotateAngleY, (float)Math.PI);
				player.bipedLeftArm.rotateAngleZ = rotLerpRad(f3, player.bipedLeftArm.rotateAngleZ, (float)Math.PI + 1.8707964F * getArmAngleSq(f1) / getArmAngleSq(14.0F));
				player.bipedRightArm.rotateAngleZ = MathHelper.lerp(f2, player.bipedRightArm.rotateAngleZ, (float)Math.PI - 1.8707964F * getArmAngleSq(f1) / getArmAngleSq(14.0F));
			} else if (f1 >= 14.0F && f1 < 22.0F) {
				float f6 = (f1 - 14.0F) / 8.0F;
				player.bipedLeftArm.rotateAngleX = rotLerpRad(f3, player.bipedLeftArm.rotateAngleX, ((float)Math.PI / 2F) * f6);
				player.bipedRightArm.rotateAngleX = MathHelper.lerp(f2, player.bipedRightArm.rotateAngleX, ((float)Math.PI / 2F) * f6);
				player.bipedLeftArm.rotateAngleY = rotLerpRad(f3, player.bipedLeftArm.rotateAngleY, (float)Math.PI);
				player.bipedRightArm.rotateAngleY = MathHelper.lerp(f2, player.bipedRightArm.rotateAngleY, (float)Math.PI);
				player.bipedLeftArm.rotateAngleZ = rotLerpRad(f3, player.bipedLeftArm.rotateAngleZ, 5.012389F - 1.8707964F * f6);
				player.bipedRightArm.rotateAngleZ = MathHelper.lerp(f2, player.bipedRightArm.rotateAngleZ, 1.2707963F + 1.8707964F * f6);
			} else if (f1 >= 22.0F && f1 < 26.0F) {
				float f4 = (f1 - 22.0F) / 4.0F;
				player.bipedLeftArm.rotateAngleX = rotLerpRad(f3, player.bipedLeftArm.rotateAngleX, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f4);
				player.bipedRightArm.rotateAngleX = MathHelper.lerp(f2, player.bipedRightArm.rotateAngleX, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f4);
				player.bipedLeftArm.rotateAngleY = rotLerpRad(f3, player.bipedLeftArm.rotateAngleY, (float)Math.PI);
				player.bipedRightArm.rotateAngleY = MathHelper.lerp(f2, player.bipedRightArm.rotateAngleY, (float)Math.PI);
				player.bipedLeftArm.rotateAngleZ = rotLerpRad(f3, player.bipedLeftArm.rotateAngleZ, (float)Math.PI);
				player.bipedRightArm.rotateAngleZ = MathHelper.lerp(f2, player.bipedRightArm.rotateAngleZ, (float)Math.PI);
			}

			player.bipedLeftLeg.rotateAngleX = MathHelper.lerp(player.swimAnimation, player.bipedLeftLeg.rotateAngleX, 0.3F * MathHelper.cos(limbSwing * 0.33333334F + (float)Math.PI));
			player.bipedRightLeg.rotateAngleX = MathHelper.lerp(player.swimAnimation, player.bipedRightLeg.rotateAngleX, 0.3F * MathHelper.cos(limbSwing * 0.33333334F));
		}

		player.bipedHeadwear.copyModelAngles(player.bipedHead);
	}

	private static void func_241655_c_(PlayerModel<AbstractClientPlayerEntity> player) {
		switch(player.leftArmPose) {
		case EMPTY:
			player.bipedLeftArm.rotateAngleY = 0.0F;
			break;
		case BLOCK:
			player.bipedLeftArm.rotateAngleX = player.bipedLeftArm.rotateAngleX * 0.5F - 0.9424779F;
			player.bipedLeftArm.rotateAngleY = ((float)Math.PI / 6F);
			break;
		case ITEM:
			player.bipedLeftArm.rotateAngleX = player.bipedLeftArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F);
			player.bipedLeftArm.rotateAngleY = 0.0F;
			break;
		case THROW_SPEAR:
			player.bipedLeftArm.rotateAngleX = player.bipedLeftArm.rotateAngleX * 0.5F - (float)Math.PI;
			player.bipedLeftArm.rotateAngleY = 0.0F;
			break;
		case BOW_AND_ARROW:
			player.bipedRightArm.rotateAngleY = -0.1F + player.bipedHead.rotateAngleY - 0.4F;
			player.bipedLeftArm.rotateAngleY = 0.1F + player.bipedHead.rotateAngleY;
			player.bipedRightArm.rotateAngleX = (-(float)Math.PI / 2F) + player.bipedHead.rotateAngleX;
			player.bipedLeftArm.rotateAngleX = (-(float)Math.PI / 2F) + player.bipedHead.rotateAngleX;
			break;
		case CROSSBOW_CHARGE:
			break;
		case CROSSBOW_HOLD:
			ModelHelper.func_239104_a_(player.bipedRightArm, player.bipedLeftArm, player.bipedHead, false);
		}

	}

	private static void func_241654_b_(PlayerModel<AbstractClientPlayerEntity> player) {
		switch(player.rightArmPose) {
		case EMPTY:
			player.bipedRightArm.rotateAngleY = 0.0F;
			break;
		case BLOCK:
			player.bipedRightArm.rotateAngleX = player.bipedRightArm.rotateAngleX * 0.5F - 0.9424779F;
			player.bipedRightArm.rotateAngleY = (-(float)Math.PI / 6F);
			break;
		case ITEM:
			player.bipedRightArm.rotateAngleX = player.bipedRightArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F);
			player.bipedRightArm.rotateAngleY = 0.0F;
			break;
		case THROW_SPEAR:
			player.bipedRightArm.rotateAngleX = player.bipedRightArm.rotateAngleX * 0.5F - (float)Math.PI;
			player.bipedRightArm.rotateAngleY = 0.0F;
			break;
		case BOW_AND_ARROW:
			player.bipedRightArm.rotateAngleY = -0.1F + player.bipedHead.rotateAngleY;
			player.bipedLeftArm.rotateAngleY = 0.1F + player.bipedHead.rotateAngleY + 0.4F;
			player.bipedRightArm.rotateAngleX = (-(float)Math.PI / 2F) + player.bipedHead.rotateAngleX;
			player.bipedLeftArm.rotateAngleX = (-(float)Math.PI / 2F) + player.bipedHead.rotateAngleX;
			break;
		case CROSSBOW_CHARGE:
			break;
		case CROSSBOW_HOLD:
			ModelHelper.func_239104_a_(player.bipedRightArm, player.bipedLeftArm, player.bipedHead, true);
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

	private static void func_230486_a_(PlayerModel<AbstractClientPlayerEntity> player, HandSide handside) {
		if (!(player.swingProgress <= 0.0F)) {
			ModelRenderer modelrenderer = getArmForSide(player, handside);
			float f = player.swingProgress;
			player.bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt(f) * ((float)Math.PI * 2F)) * 0.2F;
			if (handside == HandSide.LEFT) {
				player.bipedBody.rotateAngleY *= -1.0F;
			}

			player.bipedRightArm.rotationPointZ = MathHelper.sin(player.bipedBody.rotateAngleY) * 5.0F;
			player.bipedRightArm.rotationPointX = -MathHelper.cos(player.bipedBody.rotateAngleY) * 5.0F;
			player.bipedLeftArm.rotationPointZ = -MathHelper.sin(player.bipedBody.rotateAngleY) * 5.0F;
			player.bipedLeftArm.rotationPointX = MathHelper.cos(player.bipedBody.rotateAngleY) * 5.0F;
			player.bipedRightArm.rotateAngleY += player.bipedBody.rotateAngleY;
			player.bipedLeftArm.rotateAngleY += player.bipedBody.rotateAngleY;
			player.bipedLeftArm.rotateAngleX += player.bipedBody.rotateAngleY;
			f = 1.0F - player.swingProgress;
			f = f * f;
			f = f * f;
			f = 1.0F - f;
			float f1 = MathHelper.sin(f * (float)Math.PI);
			float f2 = MathHelper.sin(player.swingProgress * (float)Math.PI) * -(player.bipedHead.rotateAngleX - 0.7F) * 0.75F;
			modelrenderer.rotateAngleX = (float)(modelrenderer.rotateAngleX - (f1 * 1.2D + f2));
			modelrenderer.rotateAngleY += player.bipedBody.rotateAngleY * 2.0F;
			modelrenderer.rotateAngleZ += MathHelper.sin(player.swingProgress * (float)Math.PI) * -0.4F;
		}
	}

	private static ModelRenderer getArmForSide(PlayerModel<AbstractClientPlayerEntity> player, HandSide side) {
		return side == HandSide.LEFT ? player.bipedLeftArm : player.bipedRightArm;
	}

	private static float getArmAngleSq(float limbSwing) {
		return -65.0F * limbSwing + limbSwing * limbSwing;
	}
}
