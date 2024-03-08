package com.tom.cpm.common;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;

import com.tom.cpm.shared.network.NetHandler.ScalerInterface;
import com.tom.cpm.shared.util.ScalingOptions;

public class AttributeScaler implements ScalerInterface<ServerPlayerEntity, List<Attribute>> {
	private static final UUID CPM_ATTR_UUID = UUID.fromString("24bba381-9615-4530-8fcf-4fc42393a4b5");

	@Override
	public void setScale(List<Attribute> key, ServerPlayerEntity player, float value) {
		key.forEach(a -> {
			ModifiableAttributeInstance ai = player.getAttributes().getInstance(a);
			if (ai != null) {
				ai.removeModifier(CPM_ATTR_UUID);
				if (Math.abs(value - 1) > 0.01f)
					ai.addTransientModifier(new AttributeModifier(CPM_ATTR_UUID, "cpm", value - 1, Operation.MULTIPLY_BASE));
			}
		});
	}

	@Override
	public List<Attribute> toKey(ScalingOptions opt) {
		switch (opt) {
		case HEALTH: return Collections.singletonList(Attributes.MAX_HEALTH);
		case ATTACK_DMG: return Collections.singletonList(Attributes.ATTACK_DAMAGE);
		case ATTACK_KNOCKBACK: return Collections.singletonList(Attributes.ATTACK_KNOCKBACK);
		case ATTACK_SPEED: return Collections.singletonList(Attributes.ATTACK_SPEED);
		case DEFENSE: return Collections.singletonList(Attributes.ARMOR);
		case FLIGHT_SPEED: return Collections.singletonList(Attributes.FLYING_SPEED);
		case MOB_VISIBILITY: return Collections.singletonList(Attributes.FOLLOW_RANGE);
		case MOTION: return Collections.singletonList(Attributes.MOVEMENT_SPEED);
		case KNOCKBACK_RESIST: return Collections.singletonList(Attributes.KNOCKBACK_RESISTANCE);
		case REACH: return PlatformCommon.getReachAttr();
		default: return null;
		}
	}

	@Override
	public String getMethodName() {
		return ATTRIBUTE;
	}
}
