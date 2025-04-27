package com.tom.cpm.common;

import java.util.function.BiConsumer;

import net.minecraft.world.entity.player.Player;

import com.tom.cpm.shared.animation.ServerAnimationState;

public class PlayerAnimUpdater implements BiConsumer<Player, ServerAnimationState> {

	@Override
	public void accept(Player t, ServerAnimationState u) {
		u.updated = true;
		u.creativeFlying = t.getAbilities().flying;
		u.falling = (float) t.fallDistance;
		u.health = t.getHealth() / t.getMaxHealth();
		u.air = Math.max(t.getAirSupply() / (float) t.getMaxAirSupply(), 0);
		u.hunger = t.getFoodData().getFoodLevel() / 20f;
		u.inMenu = t.containerMenu != t.inventoryMenu;
	}

}
