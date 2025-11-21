package com.tom.cpm.common;

import java.util.function.BiConsumer;

import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;

import com.tom.cpm.shared.animation.ServerAnimationState;

public class PlayerAnimUpdater implements BiConsumer<Avatar, ServerAnimationState> {

	@Override
	public void accept(Avatar t, ServerAnimationState u) {
		u.updated = true;
		u.falling = (float) t.fallDistance;
		u.health = t.getHealth() / t.getMaxHealth();
		u.air = Math.max(t.getAirSupply() / (float) t.getMaxAirSupply(), 0);
		if (t instanceof Player p) {
			u.creativeFlying = p.getAbilities().flying;
			u.hunger = p.getFoodData().getFoodLevel() / 20f;
			u.inMenu = p.containerMenu != p.inventoryMenu;
		} else {
			u.creativeFlying = false;
			u.hunger = 1f;
			u.inMenu = false;
		}
	}

}
