package com.tom.cpm.client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.player.PlayerEntity;

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

public class PlayerProfile extends Player<PlayerEntity> implements IPlayerProfile {
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
	public void updateFromPlayer(PlayerEntity player) {
		animState.resetPlayer();
		if(player.method_943())animState.sleeping = true;
		if(player.dead)animState.dying = true;
		if(player.method_1360())animState.riding = true;
		if(player.method_1373())animState.sneaking = true;
		if(player.method_1334())animState.retroSwimming = true;
		animState.moveAmountX = round(player.x - player.prevX);
		animState.moveAmountY = round(player.y - player.prevY);
		animState.moveAmountZ = round(player.z - player.prevZ);
		animState.yaw = player.yaw * 2 - player.field_1012;
		animState.pitch = player.pitch;
		animState.bodyYaw = player.field_1012;

		animState.encodedState = encGesture;

		animState.wearingHelm =  player.inventory.armor[3] != null;
		animState.wearingBody =  player.inventory.armor[2] != null;
		animState.wearingLegs =  player.inventory.armor[1] != null;
		animState.wearingBoots = player.inventory.armor[0] != null;
		animState.mainHand = Hand.RIGHT;
		animState.activeHand = animState.mainHand;
		animState.hurtTime = player.hurtTime;
		animState.isOnLadder = player.method_932();
		animState.isBurning = player.method_1359();
		animState.inGui = inGui;
		PlayerInventory.setInv(animState, player.inventory);
		WorldImpl.setWorld(animState, player);
		if (player.field_1595 != null)animState.vehicle = EntityTypeHandlerImpl.impl.wrap(player.field_1595.getClass());
	}

	private float round(double d) {
		return Math.abs(d) < 0.01 ? 0 : (float) d;
	}

	@Override
	public void updateFromModel(Object model) {
		if(model instanceof BipedEntityModel) {
			BipedEntityModel m = (BipedEntityModel) model;
			animState.resetModel();
			animState.attackTime = m.handWingProgress;
			animState.leftArm = ArmPose.EMPTY;
			if(m.rightArmPose)animState.rightArm = ArmPose.ITEM;
			if(m.leftArmPose)animState.leftArm = ArmPose.ITEM;
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
