package com.tom.cpm.client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager.ISkinAvailableCallback;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.model.SkinType;

public class PlayerProfile extends Player<PlayerEntity, Model> {
	private final GameProfile profile;
	private String skinType;
	private VanillaPose pose;
	private int encodedGesture;
	public boolean hasPlayerHead;

	public static PlayerProfile create(Object object) {
		return new PlayerProfile((GameProfile) object);
	}

	private PlayerProfile(GameProfile profile) {
		this.profile = profile;
		if(profile.getId() != null)
			this.skinType = DefaultPlayerSkin.getSkinType(profile.getId());
	}

	@Override
	public SkinType getSkinType() {
		return SkinType.get(skinType);
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
	public PlayerModel<AbstractClientPlayerEntity> getModel() {
		return Minecraft.getInstance().getRenderManager().getSkinMap().get(skinType == null ? "default" : skinType).getEntityModel();
	}

	@Override
	public CompletableFuture<Void> loadSkin0() {
		CompletableFuture<Void> cf = new CompletableFuture<>();
		Minecraft.getInstance().getSkinManager().loadProfileTextures(profile, new ISkinAvailableCallback() {

			@Override
			public void onSkinTextureAvailable(Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
				switch (typeIn) {
				case SKIN:
					skinType = profileTexture.getMetadata("model");

					if (skinType == null) {
						skinType = "default";
					}
					url = profileTexture.getUrl();
					cf.complete(null);

					break;
				default:
					break;
				}
			}
		}, true);
		return cf;
	}

	@Override
	public UUID getUUID() {
		return profile.getId();
	}

	@Override
	public VanillaPose getPose() {
		return pose;
	}

	@Override
	public void updateFromPlayer(PlayerEntity player) {
		Pose p = player.getPose();
		if(p == Pose.SLEEPING)pose = VanillaPose.SLEEPING;
		else if(!player.isAlive())pose = VanillaPose.DYING;
		else if(p == Pose.FALL_FLYING)pose = VanillaPose.FLYING;
		else if(player.fallDistance > 4)pose = VanillaPose.FALLING;
		else if(player.isPassenger() && (player.getRidingEntity() != null && player.getRidingEntity().shouldRiderSit()))pose = VanillaPose.RIDING;
		else if(p == Pose.SWIMMING)pose = VanillaPose.SWIMMING;
		else if(player.isSprinting())pose = VanillaPose.RUNNING;
		else if(p == Pose.CROUCHING)pose = VanillaPose.SNEAKING;
		else if(player.distanceWalkedModified - player.prevDistanceWalkedModified > 0)pose = VanillaPose.WALKING;
		else pose = VanillaPose.STANDING;

		encodedGesture = 0;
		if(player.isWearing(PlayerModelPart.HAT))encodedGesture |= 1;
		if(player.isWearing(PlayerModelPart.JACKET))encodedGesture |= 2;
		if(player.isWearing(PlayerModelPart.LEFT_PANTS_LEG))encodedGesture |= 4;
		if(player.isWearing(PlayerModelPart.RIGHT_PANTS_LEG))encodedGesture |= 8;
		if(player.isWearing(PlayerModelPart.LEFT_SLEEVE))encodedGesture |= 16;
		if(player.isWearing(PlayerModelPart.RIGHT_SLEEVE))encodedGesture |= 32;

		ItemStack is = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
		hasPlayerHead = is.getItem() instanceof BlockItem && ((BlockItem)is.getItem()).getBlock() instanceof AbstractSkullBlock;
	}

	public void setRenderPose(VanillaPose pose) {
		this.pose = pose;
		this.encodedGesture = 0;
	}

	@Override
	public int getEncodedGestureId() {
		return encodedGesture;
	}
}
