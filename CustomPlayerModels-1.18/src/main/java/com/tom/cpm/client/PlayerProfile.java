package com.tom.cpm.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager.SkinTextureCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.skin.PlayerTextureLoader;

public class PlayerProfile extends Player<net.minecraft.world.entity.player.Player, Model> {
	private final GameProfile profile;
	private String skinType;
	private VanillaPose pose;
	private int encodedGesture;
	public boolean hasPlayerHead;

	public PlayerProfile(GameProfile profile) {
		this.profile = profile;
		if(profile.getId() != null)
			this.skinType = DefaultPlayerSkin.getSkinModelName(profile.getId());
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
	public PlayerModel<AbstractClientPlayer> getModel() {
		return ((PlayerRenderer)Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().get(skinType == null ? "default" : skinType)).getModel();
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
	public void updateFromPlayer(net.minecraft.world.entity.player.Player player) {
		Pose p = player.getPose();
		if(p == Pose.SLEEPING)pose = VanillaPose.SLEEPING;
		else if(!player.isAlive())pose = VanillaPose.DYING;
		else if(p == Pose.FALL_FLYING)pose = VanillaPose.FLYING;
		else if(player.fallDistance > 4)pose = VanillaPose.FALLING;
		else if(player.isPassenger() && (player.getVehicle() != null && player.getVehicle().shouldRiderSit()))pose = VanillaPose.RIDING;
		else if(p == Pose.SWIMMING)pose = VanillaPose.SWIMMING;
		else if(player.isSprinting() && p == Pose.CROUCHING)pose = VanillaPose.SNEAKING;
		else if(player.isSprinting())pose = VanillaPose.RUNNING;
		else if(p == Pose.CROUCHING)pose = VanillaPose.SNEAKING;
		else if(Math.abs(player.getX() - player.xo) > 0 || Math.abs(player.getZ() - player.zo) > 0)pose = VanillaPose.WALKING;
		else pose = VanillaPose.STANDING;

		encodedGesture = 0;
		if(player.isModelPartShown(PlayerModelPart.HAT))encodedGesture |= 1;
		if(player.isModelPartShown(PlayerModelPart.JACKET))encodedGesture |= 2;
		if(player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG))encodedGesture |= 4;
		if(player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG))encodedGesture |= 8;
		if(player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE))encodedGesture |= 16;
		if(player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE))encodedGesture |= 32;

		ItemStack is = player.getItemBySlot(EquipmentSlot.HEAD);
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

	@Override
	protected PlayerTextureLoader initTextures() {
		return new PlayerTextureLoader() {

			@Override
			protected CompletableFuture<Void> load0() {
				Map<Type, MinecraftProfileTexture> map = Minecraft.getInstance().getSkinManager().getInsecureSkinInformation(profile);
				defineAll(map, MinecraftProfileTexture::getUrl);
				if (map.containsKey(Type.SKIN)) {
					MinecraftProfileTexture tex = map.get(Type.SKIN);
					skinType = tex.getMetadata("model");

					if (skinType == null) {
						skinType = "default";
					}
					return CompletableFuture.completedFuture(null);
				}
				CompletableFuture<Void> cf = new CompletableFuture<>();
				Minecraft.getInstance().getSkinManager().registerSkins(profile, new SkinTextureCallback() {

					@Override
					public void onSkinTextureAvailable(Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
						defineTexture(typeIn, profileTexture.getUrl());
						switch (typeIn) {
						case SKIN:
							skinType = profileTexture.getMetadata("model");

							if (skinType == null) {
								skinType = "default";
							}
							RenderSystem.recordRenderCall(() -> cf.complete(null));
							break;

						default:
							break;
						}
					}
				}, true);
				return cf;
			}
		};
	}

	@Override
	public String getName() {
		return profile.getName();
	}

	@Override
	public Object getGameProfile() {
		return profile;
	}
}
