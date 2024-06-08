package com.tom.cpm.shared.util;

import java.util.Locale;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.shared.network.ServerCaps;

public enum ScalingOptions {
	ENTITY("scale", ServerCaps.SCALING, false),
	EYE_HEIGHT("eyeHeight", ServerCaps.EYE_HEIGHT, false),
	HITBOX_WIDTH("hitboxW", ServerCaps.HITBOX_SCALING, false),
	HITBOX_HEIGHT("hitboxH", ServerCaps.HITBOX_SCALING, false),
	THIRD_PERSON("thirdPerson", ServerCaps.THIRD_PERSON),
	VIEW_BOBBING("viewBonning", ServerCaps.THIRD_PERSON),
	MOTION("motion", ServerCaps.ATTRIBUTE_SCALE, false),
	STEP_HEIGHT("stepHeight", ServerCaps.ATTRIBUTE_SCALE, false),
	FLIGHT_SPEED("flight", ServerCaps.ATTRIBUTE_SCALE, false),
	FALL_DAMAGE("fdmg", ServerCaps.ATTRIBUTE_SCALE, false),
	REACH("reach", ServerCaps.ATTRIBUTE_SCALE, false),
	MINING_SPEED("msp", ServerCaps.ATTRIBUTE_SCALE, false),
	ATTACK_SPEED("asp", ServerCaps.ATTRIBUTE_SCALE, false),
	ATTACK_KNOCKBACK("akb", ServerCaps.ATTRIBUTE_SCALE, false),
	ATTACK_DMG("attack", ServerCaps.ATTRIBUTE_SCALE, false),
	DEFENSE("defense", ServerCaps.ATTRIBUTE_SCALE, false),
	HEALTH("health", ServerCaps.ATTRIBUTE_SCALE, false, 0.1F, 4F),
	MOB_VISIBILITY("mob_vis", ServerCaps.ATTRIBUTE_SCALE, false),
	WIDTH("width", ServerCaps.ATTRIBUTE_SCALE, false),
	HEIGHT("height", ServerCaps.ATTRIBUTE_SCALE, false),
	JUMP_HEIGHT("jump_height", ServerCaps.ATTRIBUTE_SCALE, false),
	PROJECTILE_DMG("proj_dmg", ServerCaps.ATTRIBUTE_SCALE, false),
	EXPLOSION_DMG("exp_dmg", ServerCaps.ATTRIBUTE_SCALE, false),
	KNOCKBACK_RESIST("kbr", ServerCaps.ATTRIBUTE_SCALE, false),
	SAFE_FALL_DISTANCE("safe_fall", ServerCaps.ATTRIBUTE_SCALE, false),
	;
	public static final ScalingOptions[] VALUES = values();

	private final String netKey;
	private final ServerCaps caps;
	private final boolean en;
	private final float min, max;

	private ScalingOptions(String netKey, ServerCaps caps) {
		this(netKey, caps, true);
	}

	private ScalingOptions(String netKey, ServerCaps caps, boolean en) {
		this(netKey, caps, en, 0.01F, 10F);
	}

	private ScalingOptions(String netKey, ServerCaps caps, boolean en, float min, float max) {
		this.netKey = netKey;
		this.caps = caps;
		this.en = en;
		this.min = min;
		this.max = max;
	}

	public String getNetKey() {
		return netKey;
	}

	public ServerCaps getCaps() {
		return caps;
	}

	public boolean getDefualtEnabled() {
		return en;
	}

	public float getMin() {
		return min;
	}

	public float getMax() {
		return max;
	}

	public FormatText asText() {
		return new FormatText("label.cpm.tree.scaling." + name().toLowerCase(Locale.ROOT));
	}
}
