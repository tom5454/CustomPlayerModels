package com.tom.cpm.shared.animation;

import java.util.Locale;

import com.tom.cpl.function.ToFloatFunction;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.text.I18n;

public enum VanillaPose implements IPose {
	CUSTOM,
	STANDING,
	WALKING,
	RUNNING,
	SNEAKING,
	SWIMMING,
	FALLING,
	SLEEPING,
	RIDING,
	FLYING,
	DYING,
	SKULL_RENDER,
	GLOBAL,
	CREATIVE_FLYING,
	EATING_LEFT,
	EATING_RIGHT,
	RETRO_SWIMMING,
	JUMPING,
	SNEAK_WALK,
	PUNCH_LEFT(s -> s.attackTime),
	PUNCH_RIGHT(s -> s.attackTime),
	ARMOR_HEAD,
	ARMOR_BODY,
	ARMOR_LEGS,
	ARMOR_BOOTS,
	WEARING_ELYTRA,
	BOW_LEFT(s -> s.bowPullback),
	BOW_RIGHT(s -> s.bowPullback),
	CROSSBOW_LEFT,
	CROSSBOW_RIGHT,
	CROSSBOW_CH_LEFT(s -> s.crossbowPullback),
	CROSSBOW_CH_RIGHT(s -> s.crossbowPullback),
	TRIDENT_LEFT,
	TRIDENT_RIGHT,
	TRIDENT_SPIN,
	SPYGLASS_LEFT,
	SPYGLASS_RIGHT,
	HOLDING_LEFT,
	HOLDING_RIGHT,
	WEARING_SKULL,
	BLOCKING_LEFT,
	BLOCKING_RIGHT,
	PARROT_LEFT,
	PARROT_RIGHT,
	HURT,
	ON_FIRE,
	FREEZING,
	ON_LADDER,
	CLIMBING_ON_LADDER,
	SPEAKING(s -> s.speakLevel),
	TOOT_HORN_LEFT,
	TOOT_HORN_RIGHT,
	IN_GUI,
	FIRST_PERSON_MOD,
	VOICE_MUTED,
	VR_FIRST_PERSON,
	VR_THIRD_PERSON_SITTING,
	VR_THIRD_PERSON_STANDING,
	FIRST_PERSON_HAND,
	HEALTH(syncedState(s -> s.health)),
	HUNGER(syncedState(s -> s.hunger)),
	AIR(syncedState(s -> s.air)),
	IN_MENU,
	INVISIBLE,
	LIGHT(s -> Math.max(s.skyLight, s.blockLight) / 15f),
	HEAD_ROTATION_YAW(VanillaPose::calcHeadYaw),
	HEAD_ROTATION_PITCH(s -> MathHelper.clamp((s.pitch + 90) / 180f, 0, 1)),
	BRUSH_LEFT,
	BRUSH_RIGHT,
	CRAWLING,
	;
	private final String i18nKey;
	private ToFloatFunction<AnimationState> stateGetter;
	public static final VanillaPose[] VALUES = values();
	public static final int DYNAMIC_DURATION_MUL = 1000;
	public static final int DYNAMIC_DURATION_DIV = 1001;

	private VanillaPose() {
		i18nKey = "label.cpm.pose." + name().toLowerCase(Locale.ROOT);
	}

	private static ToFloatFunction<AnimationState> syncedState(ToFloatFunction<ServerAnimationState> state) {
		return s -> {
			if(s.serverState.updated)return state.apply(s.serverState);
			else return state.apply(s.localState);
		};
	}

	private static float calcHeadYaw(AnimationState s) {
		float yaw = s.yaw % 360;
		if (yaw < 0)yaw += 360;
		float bodyYaw = s.bodyYaw % 360;
		if (bodyYaw < 0)bodyYaw += 360;
		float d = Math.abs(yaw - bodyYaw);
		if (d > 180.0f) {
			d = 360.0f - d;
		}
		double cd = (bodyYaw - yaw + 360.0) % 360.0;
		double ccd = (yaw - bodyYaw + 360.0) % 360.0;
		if(cd < ccd)d = -d;
		return MathHelper.clamp((d + 90) / 180f, 0, 1);
	}

	private VanillaPose(ToFloatFunction<AnimationState> stateGetter) {
		this();
		this.stateGetter = stateGetter;
	}

	@Override
	public String getName(I18n gui, String display) {
		if(display == null)return gui.i18nFormat(i18nKey);
		return gui.i18nFormat("label.cpm.vanilla_anim", gui.i18nFormat(i18nKey), display);
	}

	@Override
	public long getTime(AnimationState state, long time) {
		if(stateGetter != null && state != null) {
			return (long) (stateGetter.apply(state) * DYNAMIC_DURATION_MUL);
		}
		return time;
	}

	public boolean hasStateGetter() {
		return stateGetter != null;
	}
}
