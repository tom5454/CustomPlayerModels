package com.tom.cpm.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.texture.PlayerSkinProvider.SkinTextureAvailableCallback;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

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

	public PlayerProfile(GameProfile profile) {
		this.profile = profile;
		if(profile.getId() != null)
			this.skinType = DefaultSkinHelper.getModel(profile.getId());
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
	public PlayerEntityModel<AbstractClientPlayerEntity> getModel() {
		return MinecraftClient.getInstance().getEntityRenderDispatcher().modelRenderers.get(skinType == null ? "default" : skinType).getModel();
	}

	@Override
	public CompletableFuture<Void> loadSkin0() {
		Map<Type, MinecraftProfileTexture> map = MinecraftClient.getInstance().getSkinProvider().getTextures(profile);
		if (map.containsKey(Type.SKIN)) {
			MinecraftProfileTexture tex = map.get(Type.SKIN);
			url = tex.getUrl();
			skinType = tex.getMetadata("model");

			if (skinType == null) {
				skinType = "default";
			}
			return CompletableFuture.completedFuture(null);
		}
		CompletableFuture<Void> cf = new CompletableFuture<>();
		MinecraftClient.getInstance().getSkinProvider().loadSkin(profile, new SkinTextureAvailableCallback() {

			@Override
			public void onSkinTextureAvailable(Type typeIn, Identifier identifier, MinecraftProfileTexture profileTexture) {
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
		EntityPose p = player.getPose();
		if(p == EntityPose.SLEEPING)pose = VanillaPose.SLEEPING;
		else if(!player.isAlive())pose = VanillaPose.DYING;
		else if(p == EntityPose.FALL_FLYING)pose = VanillaPose.FLYING;
		else if(player.fallDistance > 4)pose = VanillaPose.FALLING;
		else if(player.hasVehicle())pose = VanillaPose.RIDING;
		else if(p == EntityPose.SWIMMING)pose = VanillaPose.SWIMMING;
		else if(player.isSprinting())pose = VanillaPose.RUNNING;
		else if(p == EntityPose.CROUCHING)pose = VanillaPose.SNEAKING;
		else if(player.horizontalSpeed - player.prevHorizontalSpeed > 0)pose = VanillaPose.WALKING;
		else pose = VanillaPose.STANDING;

		encodedGesture = 0;
		if(player.isPartVisible(PlayerModelPart.HAT))encodedGesture |= 1;
		if(player.isPartVisible(PlayerModelPart.JACKET))encodedGesture |= 2;
		if(player.isPartVisible(PlayerModelPart.LEFT_PANTS_LEG))encodedGesture |= 4;
		if(player.isPartVisible(PlayerModelPart.RIGHT_PANTS_LEG))encodedGesture |= 8;
		if(player.isPartVisible(PlayerModelPart.LEFT_SLEEVE))encodedGesture |= 16;
		if(player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE))encodedGesture |= 32;

		ItemStack is = player.getEquippedStack(EquipmentSlot.HEAD);
		hasPlayerHead = is.getItem() instanceof BlockItem && ((BlockItem)is.getItem()).getBlock() instanceof AbstractSkullBlock;
	}

	@Override
	public int getEncodedGestureId() {
		return encodedGesture;
	}

	public void setRenderPose(VanillaPose pose) {
		this.pose = pose;
		this.encodedGesture = 0;
	}
}
