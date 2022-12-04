package com.tom.cpm.shared.animation;

import java.util.Locale;

import com.tom.cpl.function.ToFloatFunction;
import com.tom.cpl.gui.IGui;

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
	;
	private final String i18nKey;
	private ToFloatFunction<AnimationState> stateGetter;
	public static final VanillaPose[] VALUES = values();
	public static final int DYNAMIC_DURATION_MUL = 1000;
	public static final int DYNAMIC_DURATION_DIV = 1001;

	private VanillaPose() {
		i18nKey = "label.cpm.pose." + name().toLowerCase(Locale.ROOT);
	}

	private VanillaPose(ToFloatFunction<AnimationState> stateGetter) {
		this();
		this.stateGetter = stateGetter;
	}

	@Override
	public String getName(IGui gui, String display) {
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
