package com.tom.cpm.common;

import net.minecraft.server.network.ServerPlayerEntity;

import com.tom.cpm.shared.network.NetHandler.ScalerInterface;
import com.tom.cpm.shared.util.ScalingOptions;

import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.api.ScaleTypes;

public class PehkuiInterface implements ScalerInterface<ServerPlayerEntity, ScaleType> {

	@Override
	public void setScale(ScaleType key, ServerPlayerEntity player, float newScale) {
		ScaleData scaleData = key.getScaleData(player);
		scaleData.setTargetScale(newScale);
		scaleData.setScale(newScale);
		scaleData.setScale(newScale);
		scaleData.tick();
		scaleData.onUpdate();
	}

	@Override
	public ScaleType toKey(ScalingOptions opt) {
		switch (opt) {
		case ENTITY:
			return ScaleTypes.BASE;
		case EYE_HEIGHT:
			return ScaleTypes.EYE_HEIGHT;
		case HITBOX_HEIGHT:
			return ScaleTypes.HITBOX_HEIGHT;
		case HITBOX_WIDTH:
			return ScaleTypes.HITBOX_WIDTH;
		case ATTACK_DMG:
			return ScaleTypes.ATTACK;
		case ATTACK_KNOCKBACK:
			return ScaleTypes.KNOCKBACK;
		case ATTACK_SPEED:
			return ScaleTypes.ATTACK_SPEED;
		case DEFENSE:
			return ScaleTypes.DEFENSE;
		case FALL_DAMAGE:
			return ScaleTypes.FALLING;
		case FLIGHT_SPEED:
			return ScaleTypes.FLIGHT;
		case HEALTH:
			return ScaleTypes.HEALTH;
		case MINING_SPEED:
			return ScaleTypes.MINING_SPEED;
		case MOB_VISIBILITY:
			return ScaleTypes.VISIBILITY;
		case MOTION:
			return ScaleTypes.MOTION;
		case REACH:
			return ScaleTypes.REACH;
		case STEP_HEIGHT:
			return ScaleTypes.STEP_HEIGHT;
		case THIRD_PERSON:
			return ScaleTypes.THIRD_PERSON;
		case VIEW_BOBBING:
			return ScaleTypes.VIEW_BOBBING;
		case WIDTH:
			return ScaleTypes.WIDTH;
		case HEIGHT:
			return ScaleTypes.HEIGHT;
		default:
			return null;
		}
	}
}
