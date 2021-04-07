package com.tom.cpm.client;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.model.SkinType;

public class PlayerProfile extends Player<EntityPlayer, ModelBase> {
	private final GameProfile profile;
	private VanillaPose pose;
	public boolean hasPlayerHead;
	public int encGesture;

	public static PlayerProfile create(Object object) {
		return new PlayerProfile((GameProfile) object);
	}

	private PlayerProfile(GameProfile profile) {
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
	public void loadSkin(Runnable onLoaded) {
		Minecraft.getMinecraft().func_152342_ad().func_152790_a(profile, new SkinCB(onLoaded), true);
		if(MinecraftObjectHolder.DEBUGGING && onLoaded != null)onLoaded.run();
	}

	public class SkinCB implements SkinManager.SkinAvailableCallback {
		private final Runnable onLoaded;

		public SkinCB(Runnable onLoaded) {
			this.onLoaded = onLoaded;
		}

		@Override
		public void func_152121_a(Type p_152121_1_, ResourceLocation p_152121_2_) {}

		//Called from CPMASMClientHooks.loadSkinHook 1.8+ implementation
		public void skinAvailable(Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
			switch (typeIn) {
			case SKIN:
				url = profileTexture.getUrl();
				if(onLoaded != null)onLoaded.run();

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
	public VanillaPose getPose() {
		return pose;
	}

	@Override
	public void updateFromPlayer(EntityPlayer player) {
		if(player.isPlayerSleeping())pose = VanillaPose.SLEEPING;
		else if(player.isDead)pose = VanillaPose.DYING;
		else if(player.fallDistance > 4 && !player.capabilities.isFlying)pose = VanillaPose.FALLING;
		else if(player.isRiding())pose = VanillaPose.RIDING;
		else if(player.isSprinting())pose = VanillaPose.RUNNING;
		else if(player.isSneaking())pose = VanillaPose.SNEAKING;
		else if(player.distanceWalkedModified - player.prevDistanceWalkedModified > 0)pose = VanillaPose.WALKING;
		else pose = VanillaPose.STANDING;

		ItemStack is = player.getEquipmentInSlot(4);
		hasPlayerHead = is != null && is.getItem() instanceof ItemSkull;
	}

	@Override
	public int getEncodedGestureId() {
		return encGesture;
	}

	public void setRenderPose(VanillaPose pose) {
		this.pose = pose;
	}
}
