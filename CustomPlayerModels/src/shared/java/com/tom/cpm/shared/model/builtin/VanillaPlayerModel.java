package com.tom.cpm.shared.model.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.render.PlayerModelSetup.ArmPose;
import com.tom.cpm.shared.util.PlayerModelLayer;

public class VanillaPlayerModel {
	public List<VanillaPartRenderer> parts = new ArrayList<>();
	public VanillaPartRenderer head;
	public VanillaPartRenderer body;
	public VanillaPartRenderer rightArm;
	public VanillaPartRenderer leftArm;
	public VanillaPartRenderer rightLeg;
	public VanillaPartRenderer leftLeg;

	public VanillaPartRenderer cape;

	public VanillaPartRenderer elytraLeft;
	public VanillaPartRenderer elytraRight;

	public VanillaPartRenderer armorHelmet;
	public VanillaPartRenderer armorBody;
	public VanillaPartRenderer armorLeftArm;
	public VanillaPartRenderer armorRightArm;
	public VanillaPartRenderer armorLeggingsBody;
	public VanillaPartRenderer armorLeftLeg;
	public VanillaPartRenderer armorRightLeg;
	public VanillaPartRenderer armorLeftFoot;
	public VanillaPartRenderer armorRightFoot;

	public boolean crouching;
	public boolean riding;
	public float attackTime;
	public float swimAmount;
	public float useAmount;
	public ArmPose leftArmPose = ArmPose.EMPTY;
	public ArmPose rightArmPose = ArmPose.EMPTY;

	public VanillaPlayerModel(SkinType skin) {
		head = new VanillaPartRenderer(this, PlayerModelParts.HEAD, skin, 64, 64);
		body = new VanillaPartRenderer(this, PlayerModelParts.BODY, skin, 64, 64);
		leftArm = new VanillaPartRenderer(this, PlayerModelParts.LEFT_ARM, skin, 64, 64);
		rightArm = new VanillaPartRenderer(this, PlayerModelParts.RIGHT_ARM, skin, 64, 64);
		leftLeg = new VanillaPartRenderer(this, PlayerModelParts.LEFT_LEG, skin, 64, 64);
		rightLeg = new VanillaPartRenderer(this, PlayerModelParts.RIGHT_LEG, skin, 64, 64);

		cape = new VanillaPartRenderer(this, RootModelType.CAPE, skin, 64, 32);

		elytraLeft = new VanillaPartRenderer(this, RootModelType.ELYTRA_LEFT, skin, 64, 32);
		elytraRight = new VanillaPartRenderer(this, RootModelType.ELYTRA_RIGHT, skin, 64, 32);

		armorHelmet = new VanillaPartRenderer(this, RootModelType.ARMOR_HELMET, skin, 64, 32);
		armorBody = new VanillaPartRenderer(this, RootModelType.ARMOR_BODY, skin, 64, 32);
		armorLeftArm = new VanillaPartRenderer(this, RootModelType.ARMOR_LEFT_ARM, skin, 64, 32);
		armorRightArm = new VanillaPartRenderer(this, RootModelType.ARMOR_RIGHT_ARM, skin, 64, 32);
		armorLeggingsBody = new VanillaPartRenderer(this, RootModelType.ARMOR_LEGGINGS_BODY, skin, 64, 32);
		armorLeftLeg = new VanillaPartRenderer(this, RootModelType.ARMOR_LEFT_LEG, skin, 64, 32);
		armorRightLeg = new VanillaPartRenderer(this, RootModelType.ARMOR_RIGHT_LEG, skin, 64, 32);
		armorLeftFoot = new VanillaPartRenderer(this, RootModelType.ARMOR_LEFT_FOOT, skin, 64, 32);
		armorRightFoot = new VanillaPartRenderer(this, RootModelType.ARMOR_RIGHT_FOOT, skin, 64, 32);
	}

	public void setAllVisible(boolean b) {
		this.head.visible = b;
		this.body.visible = b;
		this.rightArm.visible = b;
		this.leftArm.visible = b;
		this.rightLeg.visible = b;
		this.leftLeg.visible = b;
	}

	public void render(MatrixStack stack, VertexBuffer buffer) {
		head.render(stack, buffer);
		body.render(stack, buffer);
		leftArm.render(stack, buffer);
		rightArm.render(stack, buffer);
		leftLeg.render(stack, buffer);
		rightLeg.render(stack, buffer);
	}

	public void poseLayer(PlayerModelLayer layer, Set<PlayerModelLayer> layers) {
		switch (layer) {
		case BODY:
			armorBody.copyFrom(body);
			armorLeftArm.copyFrom(leftArm);
			armorRightArm.copyFrom(rightArm);
			break;

		case BOOTS:
			armorLeftFoot.copyFrom(leftLeg);
			armorRightFoot.copyFrom(rightLeg);
			break;

		case CAPE:
		{
			float f1 = 0;
			if (crouching) {
				f1 += 25.0F;
			}
			if (!layers.contains(PlayerModelLayer.BODY)) {
				if (crouching) {
					cape.z = 1.4F + 0.125F * 3;
					cape.y = 1.85F + 1 - 0.125F * 4;
				} else {
					cape.z = 0.0F + 0.125F * 16f;
					cape.y = 0.0F;
				}
			} else if (crouching) {
				cape.z = 0.3F + 0.125F * 16f;
				cape.y = 0.8F + 0.3f;
			} else {
				cape.z = -1.1F + 0.125F * 32f;
				cape.y = -0.85F + 1;
			}
			cape.xRot = (float) -Math.toRadians(6.0F + f1);
			cape.yRot = (float) Math.toRadians(180.0F);
		}
		break;

		case ELYTRA:
			elytraLeft.x = 5.0F;
			elytraLeft.y = 0;
			elytraLeft.xRot = 0.2617994F;
			elytraLeft.zRot = -0.2617994F;
			elytraLeft.yRot = 0;
			elytraRight.x = -elytraLeft.x;
			elytraRight.yRot = -elytraLeft.yRot;
			elytraRight.y = elytraLeft.y;
			elytraRight.xRot = elytraLeft.xRot;
			elytraRight.zRot = -elytraLeft.zRot;
			break;

		case HELMET:
			armorHelmet.copyFrom(head);
			break;

		case LEGS:
			armorLeggingsBody.copyFrom(body);
			armorLeftLeg.copyFrom(leftLeg);
			armorRightLeg.copyFrom(rightLeg);
			break;

		default:
			break;
		}
	}

	public void renderLayer(MatrixStack stack, VertexBuffer buffer, PlayerModelLayer layer) {
		switch (layer) {
		case BODY:
			armorBody.render(stack, buffer);
			armorLeftArm.render(stack, buffer);
			armorRightArm.render(stack, buffer);
			break;

		case BOOTS:
			armorLeftFoot.render(stack, buffer);
			armorRightFoot.render(stack, buffer);
			break;

		case CAPE:
			cape.render(stack, buffer);
			break;

		case ELYTRA:
			stack.push();
			stack.translate(0.0D, 0.0D, 0.125D);
			elytraLeft.render(stack, buffer);
			elytraRight.render(stack, buffer);
			stack.pop();
			break;

		case HELMET:
			armorHelmet.render(stack, buffer);
			break;

		case LEGS:
			armorLeggingsBody.render(stack, buffer);
			armorLeftLeg.render(stack, buffer);
			armorRightLeg.render(stack, buffer);
			break;

		default:
			break;
		}
	}

	public void reset() {
		crouching = false;
		riding = false;
		attackTime = 0;
		leftArmPose = ArmPose.EMPTY;
		rightArmPose = ArmPose.EMPTY;
		useAmount = 0;
		parts.forEach(VanillaPartRenderer::reset);
	}
}
