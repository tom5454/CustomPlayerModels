package com.tom.cpm.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.texture.PlayerSkinProvider.SkinTextureAvailableCallback;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.HandAnimation;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.render.PlayerModelSetup.ArmPose;
import com.tom.cpm.shared.skin.PlayerTextureLoader;

public class PlayerProfile extends Player<PlayerEntity> {
	private final GameProfile profile;
	private String skinType;

	public PlayerProfile(GameProfile profile) {
		this.profile = new GameProfile(profile.getId(), profile.getName());
		cloneProperties(profile.getProperties(), this.profile.getProperties());

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
	protected PlayerTextureLoader initTextures() {
		return new PlayerTextureLoader() {

			@Override
			protected CompletableFuture<Void> load0() {
				Map<Type, MinecraftProfileTexture> map = MinecraftClient.getInstance().getSkinProvider().getTextures(profile);
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
				MinecraftClient.getInstance().getSkinProvider().loadSkin(profile, new SkinTextureAvailableCallback() {

					@Override
					public void onSkinTextureAvailable(Type typeIn, Identifier identifier, MinecraftProfileTexture profileTexture) {
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
	public UUID getUUID() {
		return profile.getId();
	}

	@Override
	public void updateFromPlayer(PlayerEntity player) {
		EntityPose p = player.getPose();
		animState.resetPlayer();
		switch (p) {
		case CROUCHING:
			animState.sneaking = true;
			break;
		case DYING:
			break;
		case FALL_FLYING:
			animState.elytraFlying = true;
			break;
		case SLEEPING:
			animState.sleeping = true;
			break;
		case SPIN_ATTACK:
			animState.tridentSpin = true;
			break;
		case STANDING:
			break;
		case SWIMMING:
			animState.swimming = true;
			break;
		default:
			break;
		}
		if(!player.isAlive())animState.dying = true;
		if(player.hasVehicle())animState.riding = true;
		if(player.getAbilities().flying)animState.creativeFlying = true;
		if(player.isSprinting())animState.sprinting = true;
		if(player.isUsingItem()) {
			animState.usingAnimation = HandAnimation.of(player.getActiveItem().getUseAction());
		}
		animState.fallDistance = player.fallDistance;
		animState.moveAmountX = (float) (player.getX() - player.prevX);
		animState.moveAmountY = (float) (player.getY() - player.prevY);
		animState.moveAmountZ = (float) (player.getZ() - player.prevZ);
		animState.yaw = player.getYaw();
		animState.pitch = player.getPitch();

		if(player.isPartVisible(PlayerModelPart.HAT))animState.encodedState |= 1;
		if(player.isPartVisible(PlayerModelPart.JACKET))animState.encodedState |= 2;
		if(player.isPartVisible(PlayerModelPart.LEFT_PANTS_LEG))animState.encodedState |= 4;
		if(player.isPartVisible(PlayerModelPart.RIGHT_PANTS_LEG))animState.encodedState |= 8;
		if(player.isPartVisible(PlayerModelPart.LEFT_SLEEVE))animState.encodedState |= 16;
		if(player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE))animState.encodedState |= 32;

		ItemStack is = player.getEquippedStack(EquipmentSlot.HEAD);
		animState.hasSkullOnHead = is.getItem() instanceof BlockItem && ((BlockItem)is.getItem()).getBlock() instanceof AbstractSkullBlock;
		animState.wearingHelm = !is.isEmpty();
		is = player.getEquippedStack(EquipmentSlot.CHEST);
		animState.wearingElytra = is.getItem() instanceof ElytraItem;
		animState.wearingBody = !is.isEmpty();
		animState.wearingLegs = !player.getEquippedStack(EquipmentSlot.LEGS).isEmpty();
		animState.wearingBoots = !player.getEquippedStack(EquipmentSlot.FEET).isEmpty();
		animState.mainHand = Hand.of(player.getMainArm());
		animState.activeHand = Hand.of(animState.mainHand, player.getActiveHand());
		animState.swingingHand = Hand.of(animState.mainHand, player.preferredHand);
		animState.hurtTime = player.hurtTime;
		animState.isOnLadder = player.isClimbing();
		animState.isBurning = player.isOnFire();
		animState.isFreezing = player.getFrozenTicks() > 0;

		if(player.getActiveItem().getItem() instanceof CrossbowItem) {
			float f = CrossbowItem.getPullTime(player.getActiveItem());
			float f1 = MathHelper.clamp(player.getItemUseTime(), 0.0F, f);
			animState.crossbowPullback = f1 / f;
		}

		if(player.getActiveItem().getItem() instanceof BowItem) {
			float f = 20F;
			float f1 = MathHelper.clamp(player.getItemUseTime(), 0.0F, f);
			animState.bowPullback = f1 / f;
		}

		animState.parrotLeft = !player.getShoulderEntityLeft().getString("id").isEmpty();
		animState.parrotRight = !player.getShoulderEntityRight().getString("id").isEmpty();
	}

	@Override
	public void updateFromModel(Object model) {
		if(model instanceof PlayerEntityModel) {
			PlayerEntityModel m = (PlayerEntityModel) model;
			animState.resetModel();
			animState.attackTime = m.handSwingProgress;
			animState.swimAmount = m.leaningPitch;
			animState.leftArm = ArmPose.of(m.leftArmPose);
			animState.rightArm = ArmPose.of(m.rightArmPose);
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
