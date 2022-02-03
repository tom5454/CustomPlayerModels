package com.tom.cpl.util;

public enum ItemSlot {
	LEFT_HAND,
	RIGHT_HAND,
	HEAD,
	ANY_SLOT,
	TOOL_SLOT,
	LEFT_SHOULDER,
	RIGHT_SHOULDER
	;
	public static final ItemSlot[] VALUES = values();
	public static final ItemSlot[] SLOTS = {LEFT_HAND, RIGHT_HAND, HEAD, LEFT_SHOULDER, RIGHT_SHOULDER};
}
