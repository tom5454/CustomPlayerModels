package com.tom.cpm.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import com.tom.cpl.block.entity.ActiveEffect;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.HandAnimation;
import com.tom.cpm.common.EntityTypeHandlerImpl;
import com.tom.cpm.common.PlayerInventory;
import com.tom.cpm.common.WorldImpl;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.render.PlayerModelSetup.ArmPose;
import com.tom.cpm.shared.skin.PlayerTextureLoader;

public class PlayerProfile extends Player<EntityPlayer> {
	public static boolean inGui;
	private final GameProfile profile;
	private String skinType;

	public static GameProfile getPlayerProfile(EntityPlayer player) {
		if (player == null)return null;
		GameProfile profile = player.getGameProfile();
		if (profile.getProperties().isEmpty()) {
			NetHandlerPlayClient conn = Minecraft.getMinecraft().getConnection();
			if (conn != null) {
				NetworkPlayerInfo info = conn.getPlayerInfo(profile.getId());
				if(info != null)profile = info.getGameProfile();
			}
		}
		return profile;
	}

	public PlayerProfile(GameProfile profile) {
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
	public UUID getUUID() {
		return profile.getId();
	}

	@Override
	public void updateFromPlayer(EntityPlayer player) {
		animState.resetPlayer();
		if(player.isPlayerSleeping())animState.sleeping = true;
		if(player.isDead)animState.dying = true;
		if(player.isElytraFlying())animState.elytraFlying = true;
		if(player.isRiding() && (player.getRidingEntity() != null && player.getRidingEntity().shouldRiderSit()))animState.riding = true;
		if(player.isSneaking())animState.sneaking = true;
		if(player.isSprinting())animState.sprinting = true;
		if(player.isHandActive()) {
			animState.usingAnimation = HandAnimation.of(player.getActiveItemStack().getItemUseAction());
		}
		if(player.isInWater())animState.retroSwimming = true;
		animState.moveAmountX = (float) (player.posX - player.prevPosX);
		animState.moveAmountY = (float) (player.posY - player.prevPosY);
		animState.moveAmountZ = (float) (player.posZ - player.prevPosZ);
		animState.yaw = player.rotationYawHead * 2 - player.renderYawOffset;
		animState.pitch = player.rotationPitch;
		animState.bodyYaw = player.rotationYawHead;

		if(player.isWearing(EnumPlayerModelParts.HAT))animState.encodedState |= 1;
		if(player.isWearing(EnumPlayerModelParts.JACKET))animState.encodedState |= 2;
		if(player.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG))animState.encodedState |= 4;
		if(player.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG))animState.encodedState |= 8;
		if(player.isWearing(EnumPlayerModelParts.LEFT_SLEEVE))animState.encodedState |= 16;
		if(player.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE))animState.encodedState |= 32;

		ItemStack is = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		animState.hasSkullOnHead = is != null && is.getItem() instanceof ItemSkull;
		animState.wearingHelm = is != null;
		is = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		animState.wearingElytra = is != null && is.getItem() instanceof ItemElytra;
		animState.wearingBody = is != null;
		animState.wearingLegs = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS) != null;
		animState.wearingBoots = player.getItemStackFromSlot(EntityEquipmentSlot.FEET) != null;
		animState.mainHand = Hand.of(player.getPrimaryHand());
		animState.activeHand = Hand.of(animState.mainHand, player.getActiveHand());
		animState.swingingHand = Hand.of(animState.mainHand, player.swingingHand);
		animState.hurtTime = player.hurtTime;
		animState.isOnLadder = player.isOnLadder();
		animState.isBurning = player.canRenderOnFire();
		animState.inGui = inGui;
		PlayerInventory.setInv(animState, player.inventory);
		WorldImpl.setWorld(animState, player);
		if (player.getRidingEntity() != null)animState.vehicle = EntityTypeHandlerImpl.impl.wrap(player.getRidingEntity().getClass());
		player.getActivePotionEffects().forEach(e -> animState.allEffects.add(new ActiveEffect(ForgeRegistries.POTIONS.getKey(e.getPotion()).toString(), e.getAmplifier(), e.getDuration(), !e.doesShowParticles())));

		if(player.getActiveItemStack() != null && player.getActiveItemStack().getItem() instanceof ItemBow) {
			float f = 20F;
			float f1 = MathHelper.clamp(player.getItemInUseMaxCount(), 0.0F, f);
			animState.bowPullback = f1 / f;
		}
	}

	@Override
	public void updateFromModel(Object model) {
		if(model instanceof ModelPlayer) {
			ModelPlayer m = (ModelPlayer) model;
			animState.resetModel();
			animState.attackTime = m.swingProgress;
			animState.leftArm = ArmPose.of(m.leftArmPose);
			animState.rightArm = ArmPose.of(m.rightArmPose);
		}
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
	public String getName() {
		return profile.getName();
	}

	@Override
	public Object getGameProfile() {
		return profile;
	}
}
