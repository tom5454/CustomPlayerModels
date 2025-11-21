package com.tom.cpm.client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.PropertyMap;

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

public class PlayerProfile extends Player<Avatar> {
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

	public static GameProfile getPlayerProfile(Avatar avatar) {
		if (avatar == null)return null;
		if (avatar instanceof AbstractClientPlayer player) {
			var profile = player.getGameProfile();
			if (profile.properties().isEmpty()) {
				var conn = Minecraft.getInstance().getConnection();
				if (conn != null) {
					var info = conn.getPlayerInfo(profile.id());
					if(info != null)profile = info.getProfile();
				}
			}
			return profile;
		} else if (avatar instanceof MannequinAccess man) {
			var info = man.cpm$getInfo();
			if (info != null)
				return info.gameProfile();
		}

		return new GameProfile(avatar.getUUID(), avatar.getScoreboardName());
	}

	public PlayerProfile(GameProfile profile) {
		this.profile = new GameProfile(profile.id(), profile.name(), new PropertyMap(cloneProperties(profile.properties())));

		if(profile.id() != null)
			this.skinType = DefaultPlayerSkin.get(profile.id()).model().getSerializedName();
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
		return profile.id();
	}

	@Override
	public void updateFromPlayer(Avatar player) {
		Pose p = player.getPose();
		animState.resetPlayer();
		switch (p) {
		case FALL_FLYING:
			animState.elytraFlying = true;
			break;
		case SLEEPING:
			animState.sleeping = true;
			break;
		case SPIN_ATTACK:
			animState.tridentSpin = true;
			break;
		default:
			break;
		}
		animState.sneaking = player.isCrouching();
		animState.crawling = player.isVisuallyCrawling();
		animState.swimming = player.isVisuallySwimming();
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
		animState.wearingElytra = is.get(DataComponents.GLIDER) != null;
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
		if (player instanceof AbstractClientPlayer pl) {
			PlayerInventory.setInv(animState, pl.getInventory());
		} else {
			// Emulate equipment
		}
		WorldImpl.setWorld(animState, player);
		if (player.getVehicle() != null)animState.vehicle = EntityTypeHandlerImpl.impl.wrap(player.getVehicle().getType());
		player.getActiveEffects().forEach(e -> animState.allEffects.add(new ActiveEffect(BuiltInRegistries.MOB_EFFECT.getKey(e.getEffect().value()).toString(), e.getAmplifier(), e.getDuration(), !e.isVisible())));

		if(player.getUseItem().getItem() instanceof CrossbowItem) {
			float f = CrossbowItem.getChargeDuration(player.getUseItem(), player);
			float f1 = MathHelper.clamp(player.getTicksUsingItem(), 0.0F, f);
			animState.crossbowPullback = f1 / f;
		}

		if(player.getUseItem().getItem() instanceof BowItem) {
			float f = 20F;
			float f1 = MathHelper.clamp(player.getTicksUsingItem(), 0.0F, f);
			animState.bowPullback = f1 / f;
		}
	}

	@Override
	public void updateFromModel(Object model) {
		if(model instanceof PlayerModel) {
			/*if(CustomPlayerModelsClient.vrLoaded)
				animState.vrState = VRPlayerRenderer.getVRState(animState.animationMode, model);
			else*/
			animState.vrState = null;
		}
	}

	public void updateFromState(AvatarRenderState state) {
		animState.resetModel();
		animState.attackTime = state.attackTime;
		animState.swimAmount = state.swimAmount;
		animState.leftArm = ArmPose.of(state.leftArmPose);
		animState.rightArm = ArmPose.of(state.rightArmPose);
		animState.parrotLeft = state.parrotOnLeftShoulder != null;
		animState.parrotRight = state.parrotOnRightShoulder != null;
	}

	@Override
	protected PlayerTextureLoader initTextures() {
		var cache = Minecraft.getInstance().getSkinManager().skinTextures.root.toFile();
		return new PlayerTextureLoader(cache) {

			@Override
			protected CompletableFuture<Void> load0() {
				return CompletableFuture.supplyAsync(() -> {
					Minecraft mc = Minecraft.getInstance();
					var mss = mc.services().sessionService();
					var pt = mss.getPackedTextures(profile);
					if (pt == null)return MinecraftProfileTextures.EMPTY;
					var mpts = mss.unpackTextures(pt);
					return mpts;
				}, Util.backgroundExecutor().forName("CPM:unpackSkinTextures")).thenAcceptAsync(mpts -> {
					var skin = mpts.skin();
					var cape = mpts.cape();
					var elytra = mpts.elytra();
					if (skin != null) {
						skinType = skin.getMetadata("model");
						defineTexture(TextureType.SKIN, skin.getUrl(), skin.getHash());
					}
					if (cape != null) {
						defineTexture(TextureType.CAPE, cape.getUrl(), cape.getHash());
					}
					if (elytra != null) {
						defineTexture(TextureType.ELYTRA, elytra.getUrl(), elytra.getHash());
					}
				}, t -> Minecraft.getInstance().schedule(t::run));
			}
		};
	}

	@Override
	public String getName() {
		return profile.name();
	}

	@Override
	public Object getGameProfile() {
		return profile;
	}
}
