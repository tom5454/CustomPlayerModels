package com.tom.cpm.client;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemBow;
import net.minecraft.src.ModelBiped;
import net.minecraft.src.PotionEffect;

import com.tom.cpl.block.entity.ActiveEffect;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.HandAnimation;
import com.tom.cpm.common.EntityTypeHandlerImpl;
import com.tom.cpm.common.PlayerInventory;
import com.tom.cpm.common.WorldImpl;
import com.tom.cpm.retro.GameProfile;
import com.tom.cpm.retro.GameProfileManager;
import com.tom.cpm.retro.NetHandlerExt.IPlayerProfile;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.render.PlayerModelSetup.ArmPose;
import com.tom.cpm.shared.skin.PlayerTextureLoader;

public class PlayerProfile extends Player<EntityPlayer> implements IPlayerProfile {
	public static boolean inGui;
	private final GameProfile profile;
	public int encGesture;

	public PlayerProfile(GameProfile profile) {
		this.profile = new GameProfile(profile.getId(), profile.getName());
	}

	@Override
	public SkinType getSkinType() {
		return SkinType.DEFAULT;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PlayerProfile other = (PlayerProfile) obj;
		if (profile == null) {
			if (other.profile != null) return false;
		} else if (!profile.equals(other.profile)) return false;
		return true;
	}

	@Override
	protected PlayerTextureLoader initTextures() {
		return new PlayerTextureLoader() {

			@Override
			protected CompletableFuture<Void> load0() {
				return GameProfileManager.getProfileFuture(profile.getName()).thenAccept(gp -> {
					defineAll(gp.getTextureURLMap(), f -> f);
				});
			}
		};
	}

	@Override
	public UUID getUUID() {
		return profile.getId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateFromPlayer(EntityPlayer player) {
		animState.resetPlayer();
		if(player.isPlayerSleeping())animState.sleeping = true;
		if(player.isDead)animState.dying = true;
		if(player.isRiding() && (player.ridingEntity != null && player.ridingEntity.shouldRiderSit()))animState.riding = true;
		if(player.isSneaking())animState.sneaking = true;
		if(player.isSprinting())animState.sprinting = true;
		if(player.isUsingItem() && player.getItemInUse() != null) {
			animState.usingAnimation = HandAnimation.of(player.getItemInUse().getItemUseAction());
		}
		if(player.isInWater())animState.retroSwimming = true;
		animState.moveAmountX = (float) (player.posX - player.prevPosX);
		animState.moveAmountY = (float) (player.posY - player.prevPosY);
		animState.moveAmountZ = (float) (player.posZ - player.prevPosZ);
		animState.yaw = player.rotationYawHead * 2 - player.renderYawOffset;
		animState.pitch = player.rotationPitch;
		animState.bodyYaw = player.rotationYawHead;

		animState.encodedState = encGesture;

		animState.wearingHelm =  player.inventory.armorInventory[3] != null;
		animState.wearingBody =  player.inventory.armorInventory[2] != null;
		animState.wearingLegs =  player.inventory.armorInventory[1] != null;
		animState.wearingBoots = player.inventory.armorInventory[0] != null;
		animState.mainHand = Hand.RIGHT;
		animState.activeHand = animState.mainHand;
		animState.hurtTime = player.hurtTime;
		animState.isOnLadder = player.isOnLadder();
		animState.isBurning = player.isBurning();
		animState.inGui = inGui;
		PlayerInventory.setInv(animState, player.inventory);
		WorldImpl.setWorld(animState, player);
		if (player.ridingEntity != null)animState.vehicle = EntityTypeHandlerImpl.impl.wrap(player.ridingEntity.getClass());
		((Collection<PotionEffect>) player.getActivePotionEffects()).forEach(e -> {
			animState.allEffects.add(new ActiveEffect(e.getEffectName(), e.getAmplifier(), e.getDuration(), false));
		});

		if(player.getItemInUse() != null && player.getItemInUse().getItem() instanceof ItemBow) {
			float f = 20F;
			float f1 = MathHelper.clamp(player.getItemInUseDuration(), 0.0F, f);
			animState.bowPullback = f1 / f;
		}
	}

	@Override
	public void updateFromModel(Object model) {
		if(model instanceof ModelBiped) {
			ModelBiped m = (ModelBiped) model;
			animState.resetModel();
			animState.attackTime = m.onGround;
			animState.leftArm = ArmPose.EMPTY;
			if(m.heldItemRight == 1)animState.rightArm = ArmPose.ITEM;
			else if(m.heldItemRight == 3)animState.rightArm = ArmPose.BLOCK;
			if(m.heldItemLeft == 1)animState.leftArm = ArmPose.ITEM;
			else if(m.heldItemLeft == 3)animState.leftArm = ArmPose.BLOCK;
			if(m.aimedBow)animState.rightArm = ArmPose.BOW_AND_ARROW;
		}
	}

	@Override
	public String getName() {
		return profile.getName();
	}

	@Override
	public Object getGameProfile() {
		return profile;
	}

	@Override
	public void setEncGesture(int g) {
		encGesture = g;
	}
}
