package com.tom.cpm.bukkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;

import com.tom.cpm.shared.network.NetHandler.ScalerInterface;
import com.tom.cpm.shared.util.ScalingOptions;

public class AttributeScaler implements ScalerInterface<Player, List<Attribute>> {
	private static final UUID CPM_ATTR_UUID = UUID.fromString("24bba381-9615-4530-8fcf-4fc42393a4b5");

	@Override
	public void setScale(List<Attribute> key, Player player, float value) {
		key.forEach(a -> {
			AttributeInstance ai = player.getAttribute(a);
			if (ai != null) {
				ai.getModifiers().stream().filter(i -> i.getUniqueId().equals(CPM_ATTR_UUID)).collect(Collectors.toList()).forEach(ai::removeModifier);
				if (Math.abs(value - 1) > 0.01f)
					ai.addModifier(new AttributeModifier(CPM_ATTR_UUID, "cpm", value - 1, Operation.ADD_SCALAR));
			}
		});
	}

	@Override
	public List<Attribute> toKey(ScalingOptions opt) {
		try {
			switch (opt) {
			case HEALTH: return Collections.singletonList(Attribute.GENERIC_MAX_HEALTH);
			case ATTACK_DMG: return Collections.singletonList(Attribute.GENERIC_ATTACK_DAMAGE);
			case ATTACK_KNOCKBACK: return Collections.singletonList(Attribute.GENERIC_ATTACK_KNOCKBACK);
			case ATTACK_SPEED: return Collections.singletonList(Attribute.GENERIC_ATTACK_SPEED);
			case DEFENSE: return Collections.singletonList(Attribute.GENERIC_ARMOR);
			case FLIGHT_SPEED: return Collections.singletonList(Attribute.GENERIC_FLYING_SPEED);
			case MOB_VISIBILITY: return Collections.singletonList(Attribute.GENERIC_FOLLOW_RANGE);
			case MOTION: return Collections.singletonList(Attribute.GENERIC_MOVEMENT_SPEED);
			case ENTITY: return Collections.singletonList(Attribute.GENERIC_SCALE);
			case REACH: return Arrays.asList(Attribute.PLAYER_BLOCK_INTERACTION_RANGE, Attribute.PLAYER_ENTITY_INTERACTION_RANGE);
			case MINING_SPEED: return Collections.singletonList(Attribute.PLAYER_BLOCK_BREAK_SPEED);
			case SAFE_FALL_DISTANCE: return Collections.singletonList(Attribute.GENERIC_SAFE_FALL_DISTANCE);
			case JUMP_HEIGHT: return Collections.singletonList(Attribute.GENERIC_JUMP_STRENGTH);
			case KNOCKBACK_RESIST: return Collections.singletonList(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
			case STEP_HEIGHT: return Collections.singletonList(Attribute.GENERIC_STEP_HEIGHT);
			default: return null;
			}
		} catch (Throwable e) {
			return null;
		}
	}

	@Override
	public String getMethodName() {
		return ATTRIBUTE;
	}
}
