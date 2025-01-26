package com.tom.cpm.client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.render.model.ModelBiped;

import com.tom.cpl.util.Hand;
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

public class PlayerProfile extends Player<net.minecraft.core.entity.player.Player> implements IPlayerProfile {
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
	public void updateFromPlayer(net.minecraft.core.entity.player.Player player) {
		animState.resetPlayer();
		if(player.isPlayerSleeping())animState.sleeping = true;
		if(!player.isAlive())animState.dying = true;
		if(player.vehicle != null)animState.riding = true;
		if(player.isSneaking())animState.sneaking = true;
		if(player.isInWater())animState.retroSwimming = true;
		animState.moveAmountX = round(player.x - player.xo);
		animState.moveAmountY = round(player.y - player.yo);
		animState.moveAmountZ = round(player.z - player.zo);
		animState.yaw = player.yRot * 2 - player.yBodyRot;
		animState.pitch = player.xRot;
		animState.bodyYaw = player.yBodyRot;

		animState.encodedState = encGesture;

		animState.wearingHelm =  player.inventory.armorInventory[3] != null;
		animState.wearingBody =  player.inventory.armorInventory[2] != null;
		animState.wearingLegs =  player.inventory.armorInventory[1] != null;
		animState.wearingBoots = player.inventory.armorInventory[0] != null;
		animState.mainHand = Hand.RIGHT;
		animState.activeHand = animState.mainHand;
		animState.hurtTime = player.hurtTime;
		animState.isOnLadder = player.canClimb();
		animState.isBurning = player.isOnFire();
		animState.inGui = inGui;
		PlayerInventory.setInv(animState, player.inventory);
		WorldImpl.setWorld(animState, player);
		if (player.vehicle != null)animState.vehicle = EntityTypeHandlerImpl.impl.wrap(player.vehicle.getClass());
	}

	private float round(double d) {
		return Math.abs(d) < 0.01 ? 0 : (float) d;
	}

	@Override
	public void updateFromModel(Object model) {
		if(model instanceof ModelBiped) {
			ModelBiped m = (ModelBiped) model;
			animState.resetModel();
			animState.attackTime = m.onGround;
			animState.leftArm = ArmPose.EMPTY;
			if(m.holdingRightHand)animState.rightArm = ArmPose.ITEM;
			if(m.holdingLeftHand)animState.leftArm = ArmPose.ITEM;
			//holdingLarge
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
