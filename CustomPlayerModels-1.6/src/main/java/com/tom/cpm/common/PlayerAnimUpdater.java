package com.tom.cpm.common;

import java.util.function.BiConsumer;

import net.minecraft.entity.player.EntityPlayer;

import com.tom.cpm.shared.animation.ServerAnimationState;

public class PlayerAnimUpdater implements BiConsumer<EntityPlayer, ServerAnimationState> {

	@Override
	public void accept(EntityPlayer t, ServerAnimationState u) {
		u.updated = true;
		u.creativeFlying = t.capabilities.isFlying;
		u.falling = t.fallDistance;
		u.health = t.getHealth() / t.getMaxHealth();
		u.air = Math.max(t.getAir() / 300f, 0);
		u.hunger = t.getFoodStats().getFoodLevel() / 20f;
		u.inMenu = t.openContainer != t.inventoryContainer;
	}

}
