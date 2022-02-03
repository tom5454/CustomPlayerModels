package com.tom.cpm.shared.animation;

import java.util.function.Consumer;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.HandAnimation;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.model.render.PlayerModelSetup.ArmPose;
import com.tom.cpm.shared.network.ModelEventType;

public class AnimationState {
	public IPose currentPose;
	public int encodedState;
	public long jumping;
	public boolean hasSkullOnHead;
	public boolean wearingHelm, wearingBody, wearingLegs, wearingBoots, wearingElytra;
	public boolean sleeping, dying, riding, elytraFlying, creativeFlying, creativeFlyingServer, swimming, retroSwimming, sprinting, sneaking, takingDmg, tridentSpin;
	public float fallDistance, fallDistanceServer, moveAmountX, moveAmountY, moveAmountZ, attackTime, swimAmount, bowPullback, crossbowPullback, yaw, pitch;
	public Hand mainHand = Hand.RIGHT, activeHand = Hand.RIGHT, swingingHand = Hand.RIGHT;
	public IPose selectedPose;
	public ArmPose leftArm = ArmPose.EMPTY, rightArm = ArmPose.EMPTY;
	public HandAnimation usingAnimation = HandAnimation.NONE;
	public boolean parrotLeft, parrotRight;

	public void resetPlayer() {
		sleeping = false;
		dying = false;
		riding = false;
		elytraFlying = false;
		creativeFlying = false;
		swimming = false;
		retroSwimming = false;
		sprinting = false;
		sneaking = false;
		tridentSpin = false;
		usingAnimation = HandAnimation.NONE;
		encodedState = 0;
		fallDistance = 0;
		moveAmountX = 0;
		moveAmountY = 0;
		moveAmountZ = 0;
		bowPullback = 0;
		crossbowPullback = 0;
		yaw = 0;
		pitch = 0;
	}

	public void resetModel() {
		attackTime = 0;
		swimAmount = 0;
		rightArm = ArmPose.EMPTY;
		leftArm = ArmPose.EMPTY;
	}

	public VanillaPose getMainPose(long time, AnimationRegistry registry) {
		if(sleeping)return VanillaPose.SLEEPING;
		else if(dying)return VanillaPose.DYING;
		else if(elytraFlying)return VanillaPose.FLYING;
		else if(tridentSpin)return VanillaPose.TRIDENT_SPIN;
		else if(fallDistance > 4 || fallDistanceServer > 4)return VanillaPose.FALLING;
		else if(creativeFlying || creativeFlyingServer)return VanillaPose.CREATIVE_FLYING;
		else if(riding)return VanillaPose.RIDING;
		else if(swimming)return VanillaPose.SWIMMING;
		else if(retroSwimming && registry.hasPoseAnimations(VanillaPose.RETRO_SWIMMING))return VanillaPose.RETRO_SWIMMING;
		else if(jumping + 500 > time && registry.hasPoseAnimations(VanillaPose.JUMPING))return VanillaPose.JUMPING;
		else if(sneaking)return (Math.abs(moveAmountX) > 0 || Math.abs(moveAmountZ) > 0) && registry.hasPoseAnimations(VanillaPose.SNEAK_WALK) ? VanillaPose.SNEAK_WALK : VanillaPose.SNEAKING;
		else if(sprinting)return VanillaPose.RUNNING;
		else if(Math.abs(moveAmountX) > 0 || Math.abs(moveAmountZ) > 0)return VanillaPose.WALKING;
		else return VanillaPose.STANDING;
	}

	public void collectAnimations(Consumer<VanillaPose> h) {
		h.accept(VanillaPose.GLOBAL);
		if(attackTime > 0F) {
			if(swingingHand == Hand.LEFT)h.accept(VanillaPose.PUNCH_LEFT);
			else if(swingingHand == Hand.RIGHT)h.accept(VanillaPose.PUNCH_RIGHT);
		}
		if(hasSkullOnHead)h.accept(VanillaPose.WEARING_SKULL);
		else if(wearingHelm)h.accept(VanillaPose.ARMOR_HEAD);
		if(wearingElytra)h.accept(VanillaPose.WEARING_ELYTRA);
		else if(wearingBody)h.accept(VanillaPose.ARMOR_BODY);
		if(wearingLegs)h.accept(VanillaPose.ARMOR_LEGS);
		if(wearingBoots)h.accept(VanillaPose.ARMOR_BOOTS);
		VanillaPose pose = getArmPose(leftArm, true);
		if(pose != null)h.accept(pose);
		pose = getArmPose(rightArm, false);
		if(pose != null)h.accept(pose);
		if(usingAnimation == HandAnimation.EAT || usingAnimation == HandAnimation.DRINK) {
			if(activeHand == Hand.LEFT)h.accept(VanillaPose.EATING_LEFT);
			else h.accept(VanillaPose.EATING_RIGHT);
		}
		if(parrotLeft)h.accept(VanillaPose.PARROT_LEFT);
		if(parrotRight)h.accept(VanillaPose.PARROT_RIGHT);
	}

	private static VanillaPose getArmPose(ArmPose pose, boolean left) {
		switch (pose) {
		case BLOCK:
			return left ? VanillaPose.BLOCKING_LEFT : VanillaPose.BLOCKING_RIGHT;
		case BOW_AND_ARROW:
			return left ? VanillaPose.BOW_LEFT : VanillaPose.BOW_RIGHT;
		case CROSSBOW_CHARGE:
			return left ? VanillaPose.CROSSBOW_CH_LEFT : VanillaPose.CROSSBOW_CH_RIGHT;
		case CROSSBOW_HOLD:
			return left ? VanillaPose.CROSSBOW_LEFT : VanillaPose.CROSSBOW_RIGHT;
		case EMPTY:
			break;
		case ITEM:
			return left ? VanillaPose.HOLDING_LEFT : VanillaPose.HOLDING_RIGHT;
		case SPYGLASS:
			return left ? VanillaPose.SPYGLASS_LEFT : VanillaPose.SPYGLASS_RIGHT;
		case THROW_SPEAR:
			return left ? VanillaPose.TRIDENT_LEFT : VanillaPose.TRIDENT_RIGHT;
		default:
			break;
		}
		return null;
	}

	public void receiveEvent(NBTTagCompound tag, boolean isClient) {
		if(!isClient) {
			if(tag.hasKey(ModelEventType.FALLING.getName()))fallDistanceServer = tag.getFloat(ModelEventType.FALLING.getName());
			if(tag.hasKey(ModelEventType.CREATIVE_FLYING.getName()))creativeFlyingServer = tag.getBoolean(ModelEventType.CREATIVE_FLYING.getName());
			if(tag.hasKey(ModelEventType.JUMPING.getName()))jumping = tag.getBoolean(ModelEventType.JUMPING.getName()) ? MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime() : 0;
		}
	}

	public void jump() {
		jumping = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
	}
}
