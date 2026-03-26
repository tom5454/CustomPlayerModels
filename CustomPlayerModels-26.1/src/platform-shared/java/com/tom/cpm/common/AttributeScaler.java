package com.tom.cpm.common;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.tom.cpm.shared.network.NetHandler.ScalerInterface;
import com.tom.cpm.shared.util.ScalingOptions;

public class AttributeScaler implements ScalerInterface<ServerPlayer, List<Holder<Attribute>>> {
	private static final Identifier CPM_ATTR_ID = Identifier.tryBuild("cpm", "24bba381-9615-4530-8fcf-4fc42393a4b5");

	@Override
	public void setScale(List<Holder<Attribute>> key, ServerPlayer player, float value) {
		key.forEach(a -> {
			AttributeInstance ai = player.getAttributes().getInstance(a);
			if (ai != null) {
				ai.removeModifier(CPM_ATTR_ID);
				if (Math.abs(value - 1) > 0.01f)
					ai.addTransientModifier(new AttributeModifier(CPM_ATTR_ID, value - 1, Operation.ADD_MULTIPLIED_BASE));
			}
		});
	}

	@Override
	public List<Holder<Attribute>> toKey(ScalingOptions opt) {
		switch (opt) {
		case ENTITY: return Collections.singletonList(Attributes.SCALE);
		case HEALTH: return Collections.singletonList(Attributes.MAX_HEALTH);
		case STEP_HEIGHT: return Collections.singletonList(Attributes.STEP_HEIGHT);
		case ATTACK_DMG: return Collections.singletonList(Attributes.ATTACK_DAMAGE);
		case ATTACK_KNOCKBACK: return Collections.singletonList(Attributes.ATTACK_KNOCKBACK);
		case ATTACK_SPEED: return Collections.singletonList(Attributes.ATTACK_SPEED);
		case DEFENSE: return Collections.singletonList(Attributes.ARMOR);
		case REACH: return List.of(Attributes.BLOCK_INTERACTION_RANGE, Attributes.ENTITY_INTERACTION_RANGE);
		case MOB_VISIBILITY: return Collections.singletonList(Attributes.FOLLOW_RANGE);
		case MOTION: return Collections.singletonList(Attributes.MOVEMENT_SPEED);
		case KNOCKBACK_RESIST: return Collections.singletonList(Attributes.KNOCKBACK_RESISTANCE);
		case JUMP_HEIGHT: return Collections.singletonList(Attributes.JUMP_STRENGTH);
		case FALL_DAMAGE: return Collections.singletonList(Attributes.FALL_DAMAGE_MULTIPLIER);
		case MINING_SPEED: return Collections.singletonList(Attributes.BLOCK_BREAK_SPEED);
		case SAFE_FALL_DISTANCE: return Collections.singletonList(Attributes.SAFE_FALL_DISTANCE);
		case THIRD_PERSON: return Collections.singletonList(Attributes.CAMERA_DISTANCE);
		default: return null;
		}
	}

	@Override
	public String getMethodName() {
		return ATTRIBUTE;
	}
}
