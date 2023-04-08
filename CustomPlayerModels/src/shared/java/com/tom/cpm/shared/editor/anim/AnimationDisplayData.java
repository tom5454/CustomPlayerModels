package com.tom.cpm.shared.editor.anim;

import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.DisplayItem;
import com.tom.cpm.shared.util.PlayerModelLayer;

public enum AnimationDisplayData {
	STANDING(VanillaPose.STANDING, null, null, Type.POSE),
	WALKING(VanillaPose.WALKING, null, null, Type.POSE),
	RUNNING(VanillaPose.RUNNING, null, null, Type.POSE),
	SNEAKING(VanillaPose.SNEAKING, null, null, Type.POSE),
	SWIMMING(VanillaPose.SWIMMING, null, null, Type.POSE),
	FALLING(VanillaPose.FALLING, null, null, Type.POSE_SERVER),
	SLEEPING(VanillaPose.SLEEPING, null, null, Type.POSE),
	RIDING(VanillaPose.RIDING, null, null, Type.POSE),
	FLYING(VanillaPose.FLYING, null, null, Type.POSE),
	DYING(VanillaPose.DYING, null, null, Type.POSE),
	SKULL_RENDER(VanillaPose.SKULL_RENDER, null, null, Type.GLOBAL),
	GLOBAL(VanillaPose.GLOBAL, null, null, Type.GLOBAL),
	CREATIVE_FLYING(VanillaPose.CREATIVE_FLYING, null, null, Type.POSE_SERVER),
	EATING_LEFT(VanillaPose.EATING_LEFT, ItemSlot.LEFT_HAND, DisplayItem.FOOD, Type.HAND),
	EATING_RIGHT(VanillaPose.EATING_RIGHT, ItemSlot.RIGHT_HAND, DisplayItem.FOOD, Type.HAND),
	RETRO_SWIMMING(VanillaPose.RETRO_SWIMMING, null, null, Type.POSE),
	JUMPING(VanillaPose.JUMPING, null, null, Type.POSE_SERVER),
	SNEAK_WALK(VanillaPose.SNEAK_WALK, null, null, Type.POSE),
	PUNCH_LEFT(VanillaPose.PUNCH_LEFT, null, null, Type.HAND),
	PUNCH_RIGHT(VanillaPose.PUNCH_RIGHT, null, null, Type.HAND),
	ARMOR_HEAD(VanillaPose.ARMOR_HEAD, PlayerModelLayer.HELMET),
	ARMOR_BODY(VanillaPose.ARMOR_BODY, PlayerModelLayer.BODY),
	ARMOR_LEGS(VanillaPose.ARMOR_LEGS, PlayerModelLayer.LEGS),
	ARMOR_BOOTS(VanillaPose.ARMOR_BOOTS, PlayerModelLayer.BOOTS),
	WEARING_ELYTRA(VanillaPose.WEARING_ELYTRA, PlayerModelLayer.ELYTRA),
	BOW_LEFT(VanillaPose.BOW_LEFT, ItemSlot.LEFT_HAND, DisplayItem.BOW, Type.HAND),
	BOW_RIGHT(VanillaPose.BOW_RIGHT, ItemSlot.RIGHT_HAND, DisplayItem.BOW, Type.HAND),
	CROSSBOW_LEFT(VanillaPose.CROSSBOW_LEFT, ItemSlot.LEFT_HAND, DisplayItem.CROSSBOW, Type.HAND),
	CROSSBOW_RIGHT(VanillaPose.CROSSBOW_RIGHT, ItemSlot.RIGHT_HAND, DisplayItem.CROSSBOW, Type.HAND),
	CROSSBOW_CH_LEFT(VanillaPose.CROSSBOW_CH_LEFT, ItemSlot.LEFT_HAND, DisplayItem.CROSSBOW, Type.HAND),
	CROSSBOW_CH_RIGHT(VanillaPose.CROSSBOW_CH_RIGHT, ItemSlot.RIGHT_HAND, DisplayItem.CROSSBOW, Type.HAND),
	TRIDENT_LEFT(VanillaPose.TRIDENT_LEFT, ItemSlot.LEFT_HAND, DisplayItem.TRIDENT, Type.HAND),
	TRIDENT_RIGHT(VanillaPose.TRIDENT_RIGHT, ItemSlot.RIGHT_HAND, DisplayItem.TRIDENT, Type.HAND),
	TRIDENT_SPIN(VanillaPose.TRIDENT_SPIN, null, null, Type.POSE),
	SPYGLASS_LEFT(VanillaPose.SPYGLASS_LEFT, ItemSlot.LEFT_HAND, DisplayItem.SPYGLASS, Type.HAND),
	SPYGLASS_RIGHT(VanillaPose.SPYGLASS_RIGHT, ItemSlot.RIGHT_HAND, DisplayItem.SPYGLASS, Type.HAND),
	HOLDING_LEFT(VanillaPose.HOLDING_LEFT, ItemSlot.LEFT_HAND, DisplayItem.BLOCK, Type.HAND),
	HOLDING_RIGHT(VanillaPose.HOLDING_RIGHT, ItemSlot.RIGHT_HAND, DisplayItem.BLOCK, Type.HAND),
	WEARING_SKULL(VanillaPose.WEARING_SKULL, ItemSlot.HEAD, DisplayItem.SKULL, Type.LAYERS),
	BLOCKING_LEFT(VanillaPose.BLOCKING_LEFT, ItemSlot.LEFT_HAND, DisplayItem.SHIELD, Type.HAND),
	BLOCKING_RIGHT(VanillaPose.BLOCKING_RIGHT, ItemSlot.RIGHT_HAND, DisplayItem.SHIELD, Type.HAND),
	PARROT_LEFT(VanillaPose.PARROT_LEFT, ItemSlot.LEFT_SHOULDER, null, Type.LAYERS),
	PARROT_RIGHT(VanillaPose.PARROT_RIGHT, ItemSlot.RIGHT_SHOULDER, null, Type.LAYERS),
	HURT(VanillaPose.HURT, null, null, Type.LAYERS),
	ON_FIRE(VanillaPose.ON_FIRE, null, null, Type.LAYERS),
	FREEZING(VanillaPose.FREEZING, null, null, Type.LAYERS),
	ON_LADDER(VanillaPose.ON_LADDER, null, null, Type.POSE),
	CLIMBING_ON_LADDER(VanillaPose.CLIMBING_ON_LADDER, null, null, Type.POSE),
	SPEAKING(VanillaPose.SPEAKING, null, null, Type.LAYERS),
	TOOT_HORN_LEFT(VanillaPose.TOOT_HORN_LEFT, ItemSlot.LEFT_HAND, DisplayItem.GOAT_HORN, Type.HAND),
	TOOT_HORN_RIGHT(VanillaPose.TOOT_HORN_RIGHT, ItemSlot.RIGHT_HAND, DisplayItem.GOAT_HORN, Type.HAND),
	IN_GUI(VanillaPose.IN_GUI, null, null, Type.LAYERS),
	FIRST_PERSON_MOD(VanillaPose.FIRST_PERSON_MOD, null, null, Type.LAYERS),
	VOICE_MUTED(VanillaPose.VOICE_MUTED, null, null, Type.LAYERS),
	VR_FIRST_PERSON(VanillaPose.VR_FIRST_PERSON, null, null, Type.LAYERS),
	VR_THIRD_PERSON_SITTING(VanillaPose.VR_THIRD_PERSON_SITTING, null, null, Type.LAYERS),
	VR_THIRD_PERSON_STANDING(VanillaPose.VR_THIRD_PERSON_STANDING, null, null, Type.LAYERS),
	FIRST_PERSON_HAND(VanillaPose.FIRST_PERSON_HAND, null, null, Type.LAYERS),
	HEALTH(VanillaPose.HEALTH, null, null, Type.LAYERS),
	HUNGER(VanillaPose.HUNGER, null, null, Type.LAYERS),
	AIR(VanillaPose.AIR, null, null, Type.LAYERS),
	IN_MENU(VanillaPose.IN_MENU, null, null, Type.LAYERS),
	;
	public final VanillaPose pose;
	public final ItemSlot slot;
	public final DisplayItem item;
	public final Type type;
	public final PlayerModelLayer layer;
	public final Slot layerSlot;

	public static final AnimationDisplayData[] VALUES = values();

	private AnimationDisplayData(VanillaPose pose, ItemSlot slot, DisplayItem item, Type type) {
		this.pose = pose;
		this.slot = slot;
		this.item = item;
		this.type = type;
		this.layer = null;
		this.layerSlot = Slot.get(slot);
	}

	private AnimationDisplayData(VanillaPose pose, PlayerModelLayer layer) {
		this.pose = pose;
		this.slot = null;
		this.item = null;
		this.type = Type.LAYERS;
		this.layer = layer;
		this.layerSlot = Slot.get(layer);
	}

	public static AnimationDisplayData getFor(VanillaPose pose) {
		for (AnimationDisplayData v : VALUES) {
			if(v.pose == pose)
				return v;
		}
		return null;
	}

	public static enum Type {
		GLOBAL(0xFFFFFF),
		CUSTOM(0x0000FF),
		POSE(0xFFFF00),
		POSE_SERVER(0x00FF00),
		LAYERS(0x00FFFF),
		HAND(0xFF0000),
		;
		public final int color;
		private Type(int color) {
			this.color = color;
		}
	}

	public static enum Slot {
		HEAD,
		BODY,
		LEGS,
		BOOTS,
		LEFT_HAND,
		RIGHT_HAND,
		PARROTS
		;

		public static Slot get(ItemSlot sl) {
			if(sl == null)return null;
			switch (sl) {
			case HEAD:
				return HEAD;
			case LEFT_HAND:
				return LEFT_HAND;
			case RIGHT_SHOULDER:
			case LEFT_SHOULDER:
				return PARROTS;
			case RIGHT_HAND:
				return RIGHT_HAND;
			default:
				return null;
			}
		}

		public static Slot get(PlayerModelLayer sl) {
			if(sl == null)return null;
			switch (sl) {
			case BODY:
				return BODY;
			case BOOTS:
				return BOOTS;
			case ELYTRA:
				return BODY;
			case HELMET:
				return HEAD;
			case LEGS:
				return LEGS;
			default:
				return null;
			}
		}
	}
}
