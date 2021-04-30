package com.tom.cpm.client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.model.SkinType;

public class PlayerProfile extends Player<EntityPlayer, ModelPlayer> {
	private final GameProfile profile;
	private String skinType;
	private VanillaPose pose = VanillaPose.STANDING;
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
	public ModelPlayer getModel() {
		return Minecraft.getMinecraft().getRenderManager().getSkinMap().get(skinType == null ? "default" : skinType).getMainModel();
	}

	@Override
	public CompletableFuture<Void> loadSkin0() {
		CompletableFuture<Void> cf = new CompletableFuture<>();
		Minecraft.getMinecraft().getSkinManager().loadProfileTextures(profile, new SkinManager.SkinAvailableCallback() {

			@Override
			public void skinAvailable(Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
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
	public void updateFromPlayer(EntityPlayer player) {
		if(player.isPlayerSleeping())pose = VanillaPose.SLEEPING;
		else if(player.isDead)pose = VanillaPose.DYING;
		else if(player.isElytraFlying())pose = VanillaPose.FLYING;
		else if(player.fallDistance > 4)pose = VanillaPose.FALLING;
		else if(player.isRiding() && (player.getRidingEntity() != null && player.getRidingEntity().shouldRiderSit()))pose = VanillaPose.RIDING;
		else if(player.isSprinting())pose = VanillaPose.RUNNING;
		else if(player.isSneaking())pose = VanillaPose.SNEAKING;
		else if(player.distanceWalkedModified - player.prevDistanceWalkedModified > 0)pose = VanillaPose.WALKING;
		else pose = VanillaPose.STANDING;

		encodedGesture = 0;
		if(player.isWearing(EnumPlayerModelParts.HAT))encodedGesture |= 1;
		if(player.isWearing(EnumPlayerModelParts.JACKET))encodedGesture |= 2;
		if(player.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG))encodedGesture |= 4;
		if(player.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG))encodedGesture |= 8;
		if(player.isWearing(EnumPlayerModelParts.LEFT_SLEEVE))encodedGesture |= 16;
		if(player.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE))encodedGesture |= 32;

		ItemStack is = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		hasPlayerHead = is.getItem() instanceof ItemSkull;
	}

	@Override
	public int getEncodedGestureId() {
		return encodedGesture;
	}

	public void setRenderPose(VanillaPose pose) {
		this.pose = pose;
		encodedGesture = 0;
	}
}
