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
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;

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

public class PlayerProfile extends Player<net.minecraft.world.entity.player.Player, Model> {
	private final GameProfile profile;
	private String skinType;

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
	public void updateFromPlayer(net.minecraft.world.entity.player.Player player) {
		Pose p = player.getPose();
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
		if(player.isPassenger() && (player.getVehicle() != null && player.getVehicle().shouldRiderSit()))animState.riding = true;
		if(player.getAbilities().flying)animState.creativeFlying = true;
		if(player.isSprinting())animState.sprinting = true;
		if(player.isUsingItem()) {
			animState.usingAnimation = HandAnimation.of(player.getUseItem().getUseAnimation());
		}
		animState.fallDistance = player.fallDistance;
		animState.moveAmountX = (float) (player.getX() - player.xo);
		animState.moveAmountY = (float) (player.getY() - player.yo);
		animState.moveAmountZ = (float) (player.getZ() - player.zo);
		animState.yaw = player.getYRot();
		animState.pitch = player.getXRot();

		if(player.isModelPartShown(PlayerModelPart.HAT))animState.encodedState |= 1;
		if(player.isModelPartShown(PlayerModelPart.JACKET))animState.encodedState |= 2;
		if(player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG))animState.encodedState |= 4;
		if(player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG))animState.encodedState |= 8;
		if(player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE))animState.encodedState |= 16;
		if(player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE))animState.encodedState |= 32;

		ItemStack is = player.getItemBySlot(EquipmentSlot.HEAD);
		animState.hasSkullOnHead = is.getItem() instanceof BlockItem && ((BlockItem)is.getItem()).getBlock() instanceof AbstractSkullBlock;
		animState.wearingHelm = !is.isEmpty();
		is = player.getItemBySlot(EquipmentSlot.CHEST);
		animState.wearingElytra = is.getItem() instanceof ElytraItem;
		animState.wearingBody = !is.isEmpty();
		animState.wearingLegs = !player.getItemBySlot(EquipmentSlot.LEGS).isEmpty();
		animState.wearingBoots = !player.getItemBySlot(EquipmentSlot.FEET).isEmpty();
		animState.mainHand = Hand.of(player.getMainArm());
		animState.activeHand = Hand.of(animState.mainHand, player.getUsedItemHand());
		animState.swingingHand = Hand.of(animState.mainHand, player.swingingArm);

		if(player.getUseItem().getItem() instanceof CrossbowItem) {
			float f = CrossbowItem.getChargeDuration(player.getUseItem());
			float f1 = MathHelper.clamp(player.getTicksUsingItem(), 0.0F, f);
			animState.crossbowPullback = f1 / f;
		}

		if(player.getUseItem().getItem() instanceof BowItem) {
			float f = 20;
			float f1 = MathHelper.clamp(player.getTicksUsingItem(), 0.0F, f);
			animState.bowPullback = f1 / f;
		}

		animState.parrotLeft = !player.getShoulderEntityLeft().getString("id").isEmpty();
		animState.parrotRight = !player.getShoulderEntityRight().getString("id").isEmpty();
	}

	@Override
	public void updateFromModel(Object model) {
		if(model instanceof PlayerModel) {
			PlayerModel m = (PlayerModel) model;
			animState.resetModel();
			animState.attackTime = m.attackTime;
			animState.swimAmount = m.swimAmount;
			animState.leftArm = ArmPose.of(m.leftArmPose);
			animState.rightArm = ArmPose.of(m.rightArmPose);
		}
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
