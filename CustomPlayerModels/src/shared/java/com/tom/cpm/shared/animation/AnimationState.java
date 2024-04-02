package com.tom.cpm.shared.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.tom.cpl.block.World;
import com.tom.cpl.block.entity.ActiveEffect;
import com.tom.cpl.block.entity.EntityType;
import com.tom.cpl.item.Inventory;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.HandAnimation;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.model.render.PlayerModelSetup.ArmPose;
import com.tom.cpm.shared.network.ModelEventType;
import com.tom.cpm.shared.network.NetworkUtil;

public class AnimationState {
	public final ServerAnimationState serverState = new ServerAnimationState();
	public final ServerAnimationState localState = new ServerAnimationState();
	public Inventory playerInventory;
	public World world;
	public EntityType vehicle;
	public int encodedState;
	public long jumping;
	public boolean hasSkullOnHead;
	public boolean wearingHelm, wearingBody, wearingLegs, wearingBoots, wearingElytra;
	public boolean sleeping, dying, riding, elytraFlying, swimming, retroSwimming, sprinting, sneaking, takingDmg, tridentSpin, crawling;
	public float moveAmountX, moveAmountY, moveAmountZ, attackTime, swimAmount, bowPullback, crossbowPullback, yaw, bodyYaw, pitch, speakLevel;
	public int hurtTime, skyLight, blockLight;
	public Hand mainHand = Hand.RIGHT, activeHand = Hand.RIGHT, swingingHand = Hand.RIGHT;
	public ArmPose leftArm = ArmPose.EMPTY, rightArm = ArmPose.EMPTY;
	public HandAnimation usingAnimation = HandAnimation.NONE;
	public boolean parrotLeft, parrotRight, isFreezing, isBurning, isOnLadder, isClimbing, inGui, firstPersonMod, voiceMuted, invisible;
	public byte[] gestureData;
	public byte[] prevGestureData;
	public long lastGestureReceiveTime;
	public VRState vrState;
	public AnimationMode animationMode;
	public long dayTime;
	public List<ActiveEffect> allEffects = new ArrayList<>();

	public void resetPlayer() {
		sleeping = false;
		dying = false;
		riding = false;
		elytraFlying = false;
		swimming = false;
		retroSwimming = false;
		sprinting = false;
		sneaking = false;
		crawling = false;
		tridentSpin = false;
		usingAnimation = HandAnimation.NONE;
		encodedState = 0;
		moveAmountX = 0;
		moveAmountY = 0;
		moveAmountZ = 0;
		bowPullback = 0;
		crossbowPullback = 0;
		yaw = 0;
		bodyYaw = 0;
		pitch = 0;
		hurtTime = 0;
		speakLevel = 0;
		voiceMuted = false;
		invisible = false;
		vehicle = null;
		allEffects.clear();
		if(playerInventory != null)playerInventory.reset();
	}

	public void resetModel() {
		attackTime = 0;
		swimAmount = 0;
		rightArm = ArmPose.EMPTY;
		leftArm = ArmPose.EMPTY;
		vrState = null;
	}

	public VanillaPose getMainPose(long time, AnimationRegistry registry) {
		if(sleeping)return VanillaPose.SLEEPING;
		else if(dying)return VanillaPose.DYING;
		else if(elytraFlying)return VanillaPose.FLYING;
		else if(tridentSpin)return VanillaPose.TRIDENT_SPIN;
		else if(localState.falling > 4 || serverState.falling > 4)return VanillaPose.FALLING;
		else if(riding)return VanillaPose.RIDING;
		else if(localState.creativeFlying || serverState.creativeFlying)return VanillaPose.CREATIVE_FLYING;
		else if(crawling && registry.hasPoseAnimations(VanillaPose.CRAWLING))return VanillaPose.CRAWLING;
		else if(swimming)return VanillaPose.SWIMMING;
		else if(retroSwimming && registry.hasPoseAnimations(VanillaPose.RETRO_SWIMMING))return VanillaPose.RETRO_SWIMMING;
		else if(isClimbing && Math.abs(moveAmountY) > 0.05F && registry.hasPoseAnimations(VanillaPose.CLIMBING_ON_LADDER))return VanillaPose.CLIMBING_ON_LADDER;
		else if(isClimbing && registry.hasPoseAnimations(VanillaPose.ON_LADDER))return VanillaPose.ON_LADDER;
		else if(jumping + 500 > time && registry.hasPoseAnimations(VanillaPose.JUMPING))return VanillaPose.JUMPING;
		else if(sneaking)return (Math.abs(moveAmountX) > 0 || Math.abs(moveAmountZ) > 0) && registry.hasPoseAnimations(VanillaPose.SNEAK_WALK) ? VanillaPose.SNEAK_WALK : VanillaPose.SNEAKING;
		else if(sprinting)return VanillaPose.RUNNING;
		else if(Math.abs(moveAmountX) > 0 || Math.abs(moveAmountZ) > 0)return VanillaPose.WALKING;
		else return VanillaPose.STANDING;
	}

	public void collectAnimations(Consumer<VanillaPose> h, AnimationRegistry registry) {
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
		if(hurtTime > 0)h.accept(VanillaPose.HURT);
		if(isBurning)h.accept(VanillaPose.ON_FIRE);
		if(isFreezing)h.accept(VanillaPose.FREEZING);
		if(speakLevel > 0.1F)h.accept(VanillaPose.SPEAKING);
		if(inGui)h.accept(VanillaPose.IN_GUI);
		if(firstPersonMod)h.accept(VanillaPose.FIRST_PERSON_MOD);
		if(voiceMuted)h.accept(VanillaPose.VOICE_MUTED);
		if(vrState != null) {
			switch (vrState) {
			case FIRST_PERSON: h.accept(VanillaPose.VR_FIRST_PERSON); break;
			case THIRD_PERSON_SITTING: h.accept(VanillaPose.VR_THIRD_PERSON_SITTING); break;
			case THIRD_PERSON_STANDING: h.accept(VanillaPose.VR_THIRD_PERSON_STANDING); break;
			default: break;
			}
		}
		if(localState.inMenu || serverState.inMenu)h.accept(VanillaPose.IN_MENU);
		h.accept(VanillaPose.HEALTH);
		h.accept(VanillaPose.HUNGER);
		h.accept(VanillaPose.AIR);
		if(invisible)h.accept(VanillaPose.INVISIBLE);
		h.accept(VanillaPose.LIGHT);
		h.accept(VanillaPose.HEAD_ROTATION_YAW);
		h.accept(VanillaPose.HEAD_ROTATION_PITCH);
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
		case TOOT_HORN:
			return left ? VanillaPose.TOOT_HORN_LEFT : VanillaPose.TOOT_HORN_RIGHT;
		case BRUSH:
			return left ? VanillaPose.BRUSH_LEFT : VanillaPose.BRUSH_RIGHT;
		default:
			break;
		}
		return null;
	}

	public void receiveEvent(NBTTagCompound tag, boolean isClient) {
		if(!isClient) {
			for(ModelEventType t : ModelEventType.VALUES) {
				if(tag.hasKey(t.getName())) {
					t.read(this, tag);
				}
			}
		}
		if(tag.hasKey(NetworkUtil.GESTURE)) {
			prevGestureData = gestureData;
			gestureData = tag.getByteArray(NetworkUtil.GESTURE);
			lastGestureReceiveTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
		}
	}

	public void jump() {
		ModelEventType.JUMPING.trigger(this);
	}

	public void preAnimate() {
		if(isOnLadder && moveAmountY > 0)isClimbing = true;
		else if(!isOnLadder)isClimbing = false;
	}

	public static enum VRState {
		FIRST_PERSON,
		THIRD_PERSON_SITTING,
		THIRD_PERSON_STANDING
	}
}
