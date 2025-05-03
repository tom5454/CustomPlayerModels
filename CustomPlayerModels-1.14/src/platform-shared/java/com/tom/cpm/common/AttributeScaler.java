package com.tom.cpm.common;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;

import com.tom.cpm.shared.network.NetHandler.ScalerInterface;
import com.tom.cpm.shared.util.ScalingOptions;

public class AttributeScaler implements ScalerInterface<ServerPlayerEntity, List<IAttribute>> {
	private static final UUID CPM_ATTR_UUID = UUID.fromString("24bba381-9615-4530-8fcf-4fc42393a4b5");

	@Override
	public void setScale(List<IAttribute> key, ServerPlayerEntity player, float value) {
		key.forEach(a -> {
			IAttributeInstance ai = player.getAttributes().getInstance(a);
			if (ai != null) {
				ai.removeModifier(CPM_ATTR_UUID);
				if (Math.abs(value - 1) > 0.01f)
					ai.addModifier(new AttributeModifier(CPM_ATTR_UUID, "cpm", value - 1, Operation.MULTIPLY_BASE).setSerialize(false));
			}
		});
	}

	@Override
	public List<IAttribute> toKey(ScalingOptions opt) {
		switch (opt) {
		case HEALTH: return Collections.singletonList(SharedMonsterAttributes.MAX_HEALTH);
		case ATTACK_DMG: return Collections.singletonList(SharedMonsterAttributes.ATTACK_DAMAGE);
		case ATTACK_KNOCKBACK: return Collections.singletonList(SharedMonsterAttributes.ATTACK_KNOCKBACK);
		case ATTACK_SPEED: return Collections.singletonList(SharedMonsterAttributes.ATTACK_SPEED);
		case DEFENSE: return Collections.singletonList(SharedMonsterAttributes.ARMOR);
		case MOB_VISIBILITY: return Collections.singletonList(SharedMonsterAttributes.FOLLOW_RANGE);
		case MOTION: return Collections.singletonList(SharedMonsterAttributes.MOVEMENT_SPEED);
		case KNOCKBACK_RESIST: return Collections.singletonList(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
		case REACH: return PlatformCommon.getReachAttr();
		default: return null;
		}
	}

	@Override
	public String getMethodName() {
		return ATTRIBUTE;
	}
}
