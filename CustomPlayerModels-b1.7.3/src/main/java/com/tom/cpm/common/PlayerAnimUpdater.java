package com.tom.cpm.common;

import java.util.function.BiConsumer;

import net.minecraft.entity.player.PlayerEntity;

import com.tom.cpm.shared.animation.ServerAnimationState;

public class PlayerAnimUpdater implements BiConsumer<PlayerEntity, ServerAnimationState> {

	@Override
	public void accept(PlayerEntity t, ServerAnimationState u) {
		u.updated = true;
		u.falling = t.field_1636;
		u.health = t.health / 20f;
		u.air = Math.max(t.air / 300f, 0);
		//u.hunger = t.getFoodStats().getFoodLevel() / 20f;
		u.inMenu = t.container != t.playerContainer;
	}

}
