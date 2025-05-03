package com.tom.cpm.common;

import java.util.Locale;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.tom.cpm.shared.network.NetHandler.ScalerInterface;
import com.tom.cpm.shared.util.ScalingOptions;

import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.api.ScaleTypes;
import virtuoel.pehkui.api.TypedScaleModifier;

public class PehkuiInterface implements ScalerInterface<ServerPlayer, ScaleType> {

	@Override
	public void setScale(ScaleType key, ServerPlayer player, float newScale) {
		ScaleData scaleData = key.getScaleData(player);
		scaleData.setTargetScale(newScale);
		scaleData.setScaleTickDelay(1);
		scaleData.setPersistence(false);
	}

	@Override
	public ScaleType toKey(ScalingOptions opt) {
		String name = opt.name().toLowerCase(Locale.ROOT);
		ScaleType[] type = new ScaleType[1];
		ScaleModifier modifier = registerScaleModifier(name, () -> new TypedScaleModifier(() -> type[0]));
		type[0] = registerScaleType(name, builder -> builder.affectsDimensions().addDependentModifier(modifier));
		getBaseType(opt).getDefaultBaseValueModifiers().add(modifier);
		return type[0];
	}

	private static ScaleModifier registerScaleModifier(String name, Supplier<ScaleModifier> factory) {
		return ScaleRegistries.register(ScaleRegistries.SCALE_MODIFIERS, new ResourceLocation("cpm", name), factory.get());
	}

	private static ScaleType registerScaleType(String name, UnaryOperator<ScaleType.Builder> builder) {
		return ScaleRegistries.register(ScaleRegistries.SCALE_TYPES, new ResourceLocation("cpm", name), builder.apply(ScaleType.Builder.create()).build());
	}

	private ScaleType getBaseType(ScalingOptions opt) {
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
		case JUMP_HEIGHT:
			return ScaleTypes.JUMP_HEIGHT;
		case PROJECTILE_DMG:
			return ScaleTypes.PROJECTILES;
		case EXPLOSION_DMG:
			return ScaleTypes.EXPLOSIONS;
		default:
			return null;
		}
	}

	@Override
	public String getMethodName() {
		return PEHKUI;
	}
}
