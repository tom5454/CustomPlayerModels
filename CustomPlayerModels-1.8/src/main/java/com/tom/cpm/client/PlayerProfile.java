package com.tom.cpm.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.HandAnimation;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.render.PlayerModelSetup.ArmPose;
import com.tom.cpm.shared.skin.PlayerTextureLoader;

public class PlayerProfile extends Player<EntityPlayer> {
	private final GameProfile profile;
	private String skinType;

	public PlayerProfile(GameProfile profile) {
		this.profile = new GameProfile(profile.getId(), profile.getName());
		cloneProperties(profile.getProperties(), this.profile.getProperties());

		if(profile.getId() != null)
			this.skinType = DefaultPlayerSkin.getSkinType(profile.getId());
	}

	public PlayerProfile() {
		this(Minecraft.getMinecraft().getSession().getProfile());
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
	protected PlayerTextureLoader initTextures() {
		return new PlayerTextureLoader(Minecraft.getMinecraft().getSkinManager().skinCacheDir) {

			@Override
			protected CompletableFuture<Void> load0() {
				Map<Type, MinecraftProfileTexture> map = Minecraft.getMinecraft().getSkinManager().loadSkinFromCache(profile);
				defineAll(map, MinecraftProfileTexture::getUrl, MinecraftProfileTexture::getHash);
				if (map.containsKey(Type.SKIN)) {
					MinecraftProfileTexture tex = map.get(Type.SKIN);
					skinType = tex.getMetadata("model");

					if (skinType == null) {
						skinType = "default";
					}
					return CompletableFuture.completedFuture(null);
				}
				CompletableFuture<Void> cf = new CompletableFuture<>();
				Minecraft.getMinecraft().getSkinManager().loadProfileTextures(profile, new SkinManager.SkinAvailableCallback() {

					@Override
					public void skinAvailable(Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
						defineTexture(typeIn, profileTexture.getUrl(), profileTexture.getHash());
						switch (typeIn) {
						case SKIN:
							skinType = profileTexture.getMetadata("model");

							if (skinType == null) {
								skinType = "default";
							}
							ClientProxy.mc.getDefinitionLoader().execute(() -> Minecraft.getMinecraft().addScheduledTask(() -> cf.complete(null)));
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
	public UUID getUUID() {
		return profile.getId();
	}

	@Override
	public void updateFromPlayer(EntityPlayer player) {
		animState.resetPlayer();
		if(player.isPlayerSleeping())animState.sleeping = true;
		if(player.isDead)animState.dying = true;
		if(player.isRiding() && (player.ridingEntity != null && player.ridingEntity.shouldRiderSit()))animState.riding = true;
		if(player.isSneaking())animState.sneaking = true;
		if(player.capabilities.isFlying)animState.creativeFlying = true;
		if(player.isSprinting())animState.sprinting = true;
		if(player.isUsingItem() && player.getItemInUse() != null) {
			animState.usingAnimation = HandAnimation.of(player.getItemInUse().getItemUseAction());
		}
		if(player.isInWater())animState.retroSwimming = true;
		if(!player.capabilities.isFlying)animState.fallDistance = player.fallDistance;
		animState.moveAmountX = (float) (player.posX - player.prevPosX);
		animState.moveAmountY = (float) (player.posY - player.prevPosY);
		animState.moveAmountZ = (float) (player.posZ - player.prevPosZ);
		animState.yaw = player.rotationYaw;
		animState.pitch = player.rotationPitch;

		if(player.isWearing(EnumPlayerModelParts.HAT))animState.encodedState |= 1;
		if(player.isWearing(EnumPlayerModelParts.JACKET))animState.encodedState |= 2;
		if(player.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG))animState.encodedState |= 4;
		if(player.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG))animState.encodedState |= 8;
		if(player.isWearing(EnumPlayerModelParts.LEFT_SLEEVE))animState.encodedState |= 16;
		if(player.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE))animState.encodedState |= 32;

		ItemStack is = player.getEquipmentInSlot(4);
		animState.hasSkullOnHead = is != null && is.getItem() instanceof ItemSkull;
		animState.wearingHelm = is != null;
		animState.wearingBody = player.getEquipmentInSlot(3) != null;
		animState.wearingLegs = player.getEquipmentInSlot(2) != null;
		animState.wearingBoots = player.getEquipmentInSlot(1) != null;
		animState.mainHand = Hand.RIGHT;
		animState.activeHand = animState.mainHand;
		animState.hurtTime = player.hurtTime;
		animState.isOnLadder = player.isOnLadder();
		animState.isBurning = player.canRenderOnFire();

		if(player.getItemInUse() != null && player.getItemInUse().getItem() instanceof ItemBow) {
			float f = 20F;
			float f1 = MathHelper.clamp(player.getItemInUseDuration(), 0.0F, f);
			animState.bowPullback = f1 / f;
		}
	}

	@Override
	public void updateFromModel(Object model) {
		if(model instanceof ModelPlayer) {
			ModelPlayer m = (ModelPlayer) model;
			animState.resetModel();
			animState.attackTime = m.swingProgress;
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
}
