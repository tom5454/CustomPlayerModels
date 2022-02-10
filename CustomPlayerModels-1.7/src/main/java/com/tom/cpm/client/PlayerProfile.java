package com.tom.cpm.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
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

public class PlayerProfile extends Player<EntityPlayer, ModelBase> {
	private final GameProfile profile;
	public int encGesture;

	public PlayerProfile(GameProfile profile) {
		this.profile = profile;
	}

	@Override
	public SkinType getSkinType() {
		return SkinType.DEFAULT;
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
	public ModelBiped getModel() {
		return ((RenderPlayer) RenderManager.instance.entityRenderMap.get(EntityPlayer.class)).modelBipedMain;
	}

	@Override
	protected PlayerTextureLoader initTextures() {
		return new PlayerTextureLoader() {

			@Override
			protected CompletableFuture<Void> load0() {
				Map<Type, MinecraftProfileTexture> map = Minecraft.getMinecraft().func_152342_ad().func_152788_a(profile);
				defineAll(map, MinecraftProfileTexture::getUrl);
				if (map.containsKey(Type.SKIN)) {
					return CompletableFuture.completedFuture(null);
				}
				CompletableFuture<Void> cf = new CompletableFuture<>();
				Minecraft.getMinecraft().func_152342_ad().func_152790_a(profile, new SkinCB(cf, this::defineTexture), true);
				return cf;
			}
		};
	}

	public class SkinCB implements SkinManager.SkinAvailableCallback {
		private final CompletableFuture<Void> cf;
		private final BiConsumer<Type, String> define;

		public SkinCB(CompletableFuture<Void> cf, BiConsumer<Type, String> define) {
			this.cf = cf;
			this.define = define;
		}

		@Override
		public void func_152121_a(Type p_152121_1_, ResourceLocation p_152121_2_) {}

		//Called from CPMASMClientHooks.loadSkinHook 1.8+ implementation
		public void skinAvailable(Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
			define.accept(typeIn, profileTexture.getUrl());
			switch (typeIn) {
			case SKIN:
				ClientProxy.mc.getDefinitionLoader().execute(() -> Minecraft.getMinecraft().func_152344_a(() -> cf.complete(null)));

				break;
			default:
				break;
			}
		}
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

		animState.encodedState = encGesture;

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
		if(model instanceof ModelBiped) {
			ModelBiped m = (ModelBiped) model;
			animState.resetModel();
			animState.attackTime = m.onGround;
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
