package com.tom.cpm.client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
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
import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.cpl.block.entity.ActiveEffect;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.HandAnimation;
import com.tom.cpm.common.EntityTypeHandlerImpl;
import com.tom.cpm.common.PlayerInventory;
import com.tom.cpm.common.WorldImpl;
import com.tom.cpm.mixinplugin.FPMDetector;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.render.PlayerModelSetup.ArmPose;
import com.tom.cpm.shared.skin.PlayerTextureLoader;
import com.tom.cpm.shared.skin.TextureType;

public class PlayerProfile extends Player<net.minecraft.world.entity.player.Player> {
	public static boolean inGui;
	public static BooleanSupplier inFirstPerson;
	static {
		inFirstPerson = () -> false;
		if (FPMDetector.doApply()) {
			FirstPersonDetector.init();
		}
	}

	private final GameProfile profile;
	private String skinType;

	public PlayerProfile(GameProfile profile) {
		this.profile = new GameProfile(profile.getId(), profile.getName());
		cloneProperties(profile.getProperties(), this.profile.getProperties());

		if(profile.getId() != null)
			this.skinType = DefaultPlayerSkin.get(profile.getId()).model().id();
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
		if(Platform.isSitting(player))animState.riding = true;
		if(player.isSprinting())animState.sprinting = true;
		if(player.isUsingItem()) {
			animState.usingAnimation = HandAnimation.of(player.getUseItem().getUseAnimation());
		}
		if(player.isInWater())animState.retroSwimming = true;
		animState.moveAmountX = (float) (player.getX() - player.xo);
		animState.moveAmountY = (float) (player.getY() - player.yo);
		animState.moveAmountZ = (float) (player.getZ() - player.zo);
		animState.yaw = player.getYRot();
		animState.pitch = player.getXRot();
		animState.bodyYaw = player.yBodyRot;

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
		animState.hurtTime = player.hurtTime;
		animState.isOnLadder = player.onClimbable();
		animState.isBurning = player.displayFireAnimation();
		animState.isFreezing = player.getTicksFrozen() > 0;
		animState.inGui = inGui;
		animState.firstPersonMod = inFirstPerson.getAsBoolean();
		PlayerInventory.setInv(animState, player.getInventory());
		WorldImpl.setWorld(animState, player);
		if (player.getVehicle() != null)animState.vehicle = EntityTypeHandlerImpl.impl.wrap(player.getVehicle().getType());
		player.getActiveEffects().forEach(e -> animState.allEffects.add(new ActiveEffect(BuiltInRegistries.MOB_EFFECT.getKey(e.getEffect()).toString(), e.getAmplifier(), e.getDuration(), !e.isVisible())));

		if(player.getUseItem().getItem() instanceof CrossbowItem) {
			float f = CrossbowItem.getChargeDuration(player.getUseItem());
			float f1 = MathHelper.clamp(player.getTicksUsingItem(), 0.0F, f);
			animState.crossbowPullback = f1 / f;
		}

		if(player.getUseItem().getItem() instanceof BowItem) {
			float f = 20F;
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
			/*if(CustomPlayerModelsClient.vrLoaded)
				animState.vrState = VRPlayerRenderer.getVRState(animState.animationMode, model);*/
		}
	}

	@Override
	protected PlayerTextureLoader initTextures() {
		return new PlayerTextureLoader() {

			@Override
			protected CompletableFuture<Void> load0() {
				Minecraft mc = Minecraft.getInstance();
				return mc.getSkinManager().getOrLoad(profile).thenAcceptAsync(skin -> {
					AbstractTexture s = mc.getTextureManager().getTexture(skin.texture(), null);
					AbstractTexture c = mc.getTextureManager().getTexture(skin.capeTexture(), null);
					AbstractTexture e = mc.getTextureManager().getTexture(skin.elytraTexture(), null);
					if (s instanceof HttpTexture h)defineTexture(TextureType.SKIN, h.urlString, h.file);
					if (c instanceof HttpTexture h)defineTexture(TextureType.CAPE, h.urlString, h.file);
					if (e instanceof HttpTexture h)defineTexture(TextureType.ELYTRA, h.urlString, h.file);
					skinType = skin.model().id();
				}, t -> RenderSystem.recordRenderCall(t::run));
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
