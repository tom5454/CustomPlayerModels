package com.tom.cpm.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class PlayerModelSetup {
	public static final float scale = 0.0625F;

	@SuppressWarnings("incomplete-switch")
	public static void setRotationAngles(ModelPlayer player, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, EnumHandSide enumhandside)
	{
		boolean flag = false;
		player.bipedHead.rotateAngleY = netHeadYaw * 0.017453292F;

		if (flag)
		{
			player.bipedHead.rotateAngleX = -((float)Math.PI / 4F);
		}
		else
		{
			player.bipedHead.rotateAngleX = headPitch * 0.017453292F;
		}

		player.bipedBody.rotateAngleY = 0.0F;
		player.bipedRightArm.rotationPointZ = 0.0F;
		player.bipedRightArm.rotationPointX = -5.0F;
		player.bipedLeftArm.rotationPointZ = 0.0F;
		player.bipedLeftArm.rotationPointX = 5.0F;
		float f = 1.0F;

		if (flag)
		{
			f = 0;
			f = f / 0.2F;
			f = f * f * f;
		}

		if (f < 1.0F)
		{
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

		if (player.isRiding)
		{
			player.bipedRightArm.rotateAngleX += -((float)Math.PI / 5F);
			player.bipedLeftArm.rotateAngleX += -((float)Math.PI / 5F);
			player.bipedRightLeg.rotateAngleX = -1.4137167F;
			player.bipedRightLeg.rotateAngleY = ((float)Math.PI / 10F);
			player.bipedRightLeg.rotateAngleZ = 0.07853982F;
			player.bipedLeftLeg.rotateAngleX = -1.4137167F;
			player.bipedLeftLeg.rotateAngleY = -((float)Math.PI / 10F);
			player.bipedLeftLeg.rotateAngleZ = -0.07853982F;
		}

		player.bipedRightArm.rotateAngleY = 0.0F;
		player.bipedRightArm.rotateAngleZ = 0.0F;

		switch (player.leftArmPose)
		{
		case EMPTY:
			player.bipedLeftArm.rotateAngleY = 0.0F;
			break;
		case BLOCK:
			player.bipedLeftArm.rotateAngleX = player.bipedLeftArm.rotateAngleX * 0.5F - 0.9424779F;
			player.bipedLeftArm.rotateAngleY = 0.5235988F;
			break;
		case ITEM:
			player.bipedLeftArm.rotateAngleX = player.bipedLeftArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F);
			player.bipedLeftArm.rotateAngleY = 0.0F;
		}

		switch (player.rightArmPose)
		{
		case EMPTY:
			player.bipedRightArm.rotateAngleY = 0.0F;
			break;
		case BLOCK:
			player.bipedRightArm.rotateAngleX = player.bipedRightArm.rotateAngleX * 0.5F - 0.9424779F;
			player.bipedRightArm.rotateAngleY = -0.5235988F;
			break;
		case ITEM:
			player.bipedRightArm.rotateAngleX = player.bipedRightArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F);
			player.bipedRightArm.rotateAngleY = 0.0F;
		}

		if (player.swingProgress > 0.0F)
		{
			ModelRenderer modelrenderer = getArmForSide(player, enumhandside);
			float f1 = player.swingProgress;
			player.bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt(f1) * ((float)Math.PI * 2F)) * 0.2F;

			if (enumhandside == EnumHandSide.LEFT)
			{
				player.bipedBody.rotateAngleY *= -1.0F;
			}

			player.bipedRightArm.rotationPointZ = MathHelper.sin(player.bipedBody.rotateAngleY) * 5.0F;
			player.bipedRightArm.rotationPointX = -MathHelper.cos(player.bipedBody.rotateAngleY) * 5.0F;
			player.bipedLeftArm.rotationPointZ = -MathHelper.sin(player.bipedBody.rotateAngleY) * 5.0F;
			player.bipedLeftArm.rotationPointX = MathHelper.cos(player.bipedBody.rotateAngleY) * 5.0F;
			player.bipedRightArm.rotateAngleY += player.bipedBody.rotateAngleY;
			player.bipedLeftArm.rotateAngleY += player.bipedBody.rotateAngleY;
			player.bipedLeftArm.rotateAngleX += player.bipedBody.rotateAngleY;
			f1 = 1.0F - player.swingProgress;
			f1 = f1 * f1;
			f1 = f1 * f1;
			f1 = 1.0F - f1;
			float f2 = MathHelper.sin(f1 * (float)Math.PI);
			float f3 = MathHelper.sin(player.swingProgress * (float)Math.PI) * -(player.bipedHead.rotateAngleX - 0.7F) * 0.75F;
			modelrenderer.rotateAngleX = (float)(modelrenderer.rotateAngleX - (f2 * 1.2D + f3));
			modelrenderer.rotateAngleY += player.bipedBody.rotateAngleY * 2.0F;
			modelrenderer.rotateAngleZ += MathHelper.sin(player.swingProgress * (float)Math.PI) * -0.4F;
		}

		if (player.isSneak)
		{
			player.bipedBody.rotateAngleX = 0.5F;
			player.bipedRightArm.rotateAngleX += 0.4F;
			player.bipedLeftArm.rotateAngleX += 0.4F;
			player.bipedRightLeg.rotationPointZ = 4.0F;
			player.bipedLeftLeg.rotationPointZ = 4.0F;
			player.bipedRightLeg.rotationPointY = 9.0F;
			player.bipedLeftLeg.rotationPointY = 9.0F;
			player.bipedHead.rotationPointY = 1.0F;
		}
		else
		{
			player.bipedBody.rotateAngleX = 0.0F;
			player.bipedRightLeg.rotationPointZ = 0.1F;
			player.bipedLeftLeg.rotationPointZ = 0.1F;
			player.bipedRightLeg.rotationPointY = 12.0F;
			player.bipedLeftLeg.rotationPointY = 12.0F;
			player.bipedHead.rotationPointY = 0.0F;
		}

		player.bipedRightArm.rotateAngleZ += MathHelper.cos(0 * 0.09F) * 0.05F + 0.05F;
		player.bipedLeftArm.rotateAngleZ -= MathHelper.cos(0 * 0.09F) * 0.05F + 0.05F;
		player.bipedRightArm.rotateAngleX += MathHelper.sin(0 * 0.067F) * 0.05F;
		player.bipedLeftArm.rotateAngleX -= MathHelper.sin(0 * 0.067F) * 0.05F;

		if (player.rightArmPose == ModelBiped.ArmPose.BOW_AND_ARROW)
		{
			player.bipedRightArm.rotateAngleY = -0.1F + player.bipedHead.rotateAngleY;
			player.bipedLeftArm.rotateAngleY = 0.1F + player.bipedHead.rotateAngleY + 0.4F;
			player.bipedRightArm.rotateAngleX = -((float)Math.PI / 2F) + player.bipedHead.rotateAngleX;
			player.bipedLeftArm.rotateAngleX = -((float)Math.PI / 2F) + player.bipedHead.rotateAngleX;
		}
		else if (player.leftArmPose == ModelBiped.ArmPose.BOW_AND_ARROW)
		{
			player.bipedRightArm.rotateAngleY = -0.1F + player.bipedHead.rotateAngleY - 0.4F;
			player.bipedLeftArm.rotateAngleY = 0.1F + player.bipedHead.rotateAngleY;
			player.bipedRightArm.rotateAngleX = -((float)Math.PI / 2F) + player.bipedHead.rotateAngleX;
			player.bipedLeftArm.rotateAngleX = -((float)Math.PI / 2F) + player.bipedHead.rotateAngleX;
		}

		ModelBiped.copyModelAngles(player.bipedHead, player.bipedHeadwear);
	}

	protected static ModelRenderer getArmForSide(ModelPlayer player, EnumHandSide side)
	{
		return side == EnumHandSide.LEFT ? player.bipedLeftArm : player.bipedRightArm;
	}

	public static void render(ModelPlayer player)
	{
		GlStateManager.pushMatrix();

		if (player.isSneak)
		{
			GlStateManager.translate(0.0F, 0.2F, 0.0F);
		}

		player.bipedHead.render(scale);
		player.bipedBody.render(scale);
		player.bipedRightArm.render(scale);
		player.bipedLeftArm.render(scale);
		player.bipedRightLeg.render(scale);
		player.bipedLeftLeg.render(scale);
		player.bipedHeadwear.render(scale);

		GlStateManager.popMatrix();
	}
}
