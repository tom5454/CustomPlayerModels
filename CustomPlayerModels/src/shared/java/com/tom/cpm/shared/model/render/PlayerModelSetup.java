package com.tom.cpm.shared.model.render;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Hand;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.builtin.VanillaPartRenderer;
import com.tom.cpm.shared.model.builtin.VanillaPlayerModel;

public class PlayerModelSetup {

	public static void setRotationAngles(VanillaPlayerModel player, float limbSwing, float limbSwingAmount, Hand handside, boolean swim) {
		boolean flag1 = swim;
		player.swimAmount = swim ? 1 : 0;
		player.head.yRot = 0;
		if (player.swimAmount > 0.0F) {
			if (flag1) {
				player.head.xRot = rotLerpRad(player.swimAmount, player.head.xRot, (-(float)Math.PI / 4F));
			} else {
				player.head.xRot = rotLerpRad(player.swimAmount, player.head.xRot, 0);
			}
		} else {
			player.head.xRot = 0;
		}

		player.body.yRot = 0.0F;
		player.rightArm.z = 0.0F;
		player.rightArm.x = -5.0F;
		player.leftArm.z = 0.0F;
		player.leftArm.x = 5.0F;
		float f = 1.0F;
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
		boolean flag2 = handside == Hand.RIGHT;
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

		bobArms(player.rightArm, player.leftArm, 0);
		if (player.swimAmount > 0.0F) {
			float f1 = limbSwing % 26.0F;
			float f2 = handside == Hand.RIGHT && player.attackTime > 0.0F ? 0.0F : player.swimAmount;
			float f3 = handside == Hand.LEFT && player.attackTime > 0.0F ? 0.0F : player.swimAmount;
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
	}

	private static float rotLerpRad(float angleIn, float maxAngleIn, float mulIn) {
		float f = (mulIn - maxAngleIn) % ((float)Math.PI * 2F);
		if (f < -(float)Math.PI) {
			f += ((float)Math.PI * 2F);
		}

		if (f >= (float)Math.PI) {
			f -= ((float)Math.PI * 2F);
		}

		return maxAngleIn + angleIn * f;
	}

	private static void bobArms(VanillaPartRenderer p_239101_0_, VanillaPartRenderer p_239101_1_, float p_239101_2_) {
		p_239101_0_.zRot += MathHelper.cos(p_239101_2_ * 0.09F) * 0.05F + 0.05F;
		p_239101_1_.zRot -= MathHelper.cos(p_239101_2_ * 0.09F) * 0.05F + 0.05F;
		p_239101_0_.xRot += MathHelper.sin(p_239101_2_ * 0.067F) * 0.05F;
		p_239101_1_.xRot -= MathHelper.sin(p_239101_2_ * 0.067F) * 0.05F;
	}

	private static void poseLeftArm(VanillaPlayerModel player) {
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
			animateCrossbowCharge(player.rightArm, player.leftArm, player.useAmount, false);
			break;
		case CROSSBOW_HOLD:
			animateCrossbowHold(player.rightArm, player.leftArm, player.head, false);
			break;
		case SPYGLASS:
			player.leftArm.xRot = MathHelper.clamp(player.head.xRot - 1.9198622F - (player.crouching ? 0.2617994F : 0.0F), -2.4F, 3.3F);
			player.leftArm.yRot = player.head.yRot + 0.2617994F;
			break;
		case TOOT_HORN:
			player.leftArm.xRot = MathHelper.clamp(player.head.xRot, -1.2F, 1.2F) - 1.4835298F;
			player.leftArm.yRot = player.head.yRot + ((float)Math.PI / 6F);
			break;
		}

	}

	private static void poseRightArm(VanillaPlayerModel player) {
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
			animateCrossbowCharge(player.rightArm, player.leftArm, player.useAmount, true);
			break;
		case CROSSBOW_HOLD:
			animateCrossbowHold(player.rightArm, player.leftArm, player.head, true);
			break;
		case SPYGLASS:
			player.rightArm.xRot = MathHelper.clamp(player.head.xRot - 1.9198622F - (player.crouching ? 0.2617994F : 0.0F), -2.4F, 3.3F);
			player.rightArm.yRot = player.head.yRot - 0.2617994F;
			break;
		case TOOT_HORN:
			player.rightArm.xRot = MathHelper.clamp(player.head.xRot, -1.2F, 1.2F) - 1.4835298F;
			player.rightArm.yRot = player.head.yRot - ((float)Math.PI / 6F);
			break;
		}

	}

	private static void setupAttackAnimation(VanillaPlayerModel player, Hand handside) {
		if (!(player.attackTime <= 0.0F)) {
			VanillaPartRenderer modelrenderer = getArmForSide(player, handside);
			float f = player.attackTime;
			player.body.yRot = MathHelper.sin(MathHelper.sqrt(f) * ((float)Math.PI * 2F)) * 0.2F;
			if (handside == Hand.LEFT) {
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

	private static void animateCrossbowHold(VanillaPartRenderer p_239104_0_, VanillaPartRenderer p_239104_1_, VanillaPartRenderer p_239104_2_, boolean p_239104_3_) {
		VanillaPartRenderer modelrenderer = p_239104_3_ ? p_239104_0_ : p_239104_1_;
		VanillaPartRenderer modelrenderer1 = p_239104_3_ ? p_239104_1_ : p_239104_0_;
		modelrenderer.yRot = (p_239104_3_ ? -0.3F : 0.3F) + p_239104_2_.yRot;
		modelrenderer1.yRot = (p_239104_3_ ? 0.6F : -0.6F) + p_239104_2_.yRot;
		modelrenderer.xRot = (-(float)Math.PI / 2F) + p_239104_2_.xRot + 0.1F;
		modelrenderer1.xRot = -1.5F + p_239104_2_.xRot;
	}

	public static void animateCrossbowCharge(VanillaPartRenderer p_102087_, VanillaPartRenderer p_102088_, float chargeValue, boolean p_102090_) {
		VanillaPartRenderer modelpart = p_102090_ ? p_102087_ : p_102088_;
		VanillaPartRenderer modelpart1 = p_102090_ ? p_102088_ : p_102087_;
		modelpart.yRot = p_102090_ ? -0.8F : 0.8F;
		modelpart.xRot = -0.97079635F;
		modelpart1.xRot = modelpart.xRot;
		modelpart1.yRot = MathHelper.lerp(chargeValue, 0.4F, 0.85F) * (p_102090_ ? 1 : -1);
		modelpart1.xRot = MathHelper.lerp(chargeValue, modelpart1.xRot, (-(float)Math.PI / 2F));
	}

	private static VanillaPartRenderer getArmForSide(VanillaPlayerModel player, Hand side) {
		return side == Hand.LEFT ? player.leftArm : player.rightArm;
	}

	private static float getArmAngleSq(float limbSwing) {
		return -65.0F * limbSwing + limbSwing * limbSwing;
	}

	public static enum ArmPose {
		EMPTY(false),
		ITEM(false),
		BLOCK(false),
		BOW_AND_ARROW(true),
		THROW_SPEAR(false),
		CROSSBOW_CHARGE(true),
		CROSSBOW_HOLD(true),
		SPYGLASS(false),
		TOOT_HORN(false),
		;

		public static final ArmPose[] VALUES = values();

		private final boolean twoHanded;

		private ArmPose(boolean p_i241257_3_) {
			this.twoHanded = p_i241257_3_;
		}

		public boolean isTwoHanded() {
			return this.twoHanded;
		}

		public static <T extends Enum<T>> ArmPose of(T value) {
			for (int i = 0; i < VALUES.length; i++) {
				ArmPose armPose = VALUES[i];
				if(armPose.name().equals(value.name()))
					return armPose;
			}
			return EMPTY;
		}
	}

	public static void initDefaultPose(RootModelElement rme, VanillaModelPart part) {
		if(part == RootModelType.CAPE) {
			rme.defPos = new Vec3f(0, -0.85F + 1, -1.1F + 0.125F * 32f);
			rme.defRot = new Vec3f(0, (float) Math.toRadians(180.0F), (float) -Math.toRadians(6.0F));
		} else if(part == RootModelType.ELYTRA_LEFT) {
			rme.defPos = new Vec3f(5, 0, 0);
			rme.defRot = new Vec3f(0.2617994F, 0, -0.2617994F);
		} else if(part == RootModelType.ELYTRA_RIGHT) {
			rme.defPos = new Vec3f(-5, 0, 0);
			rme.defRot = new Vec3f(0.2617994F, 0, 0.2617994F);
		}
	}
}
