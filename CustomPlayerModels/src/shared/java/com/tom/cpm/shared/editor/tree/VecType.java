package com.tom.cpm.shared.editor.tree;

public enum VecType {
	SIZE,
	OFFSET,
	ROTATION,
	POSITION,
	SCALE,
	TEXTURE,
	;
	public static final VecType[] MOUSE_EDITOR_TYPES = {SIZE, OFFSET, ROTATION, POSITION};
	public static final VecType[] MOUSE_EDITOR_ANIM_TYPES = {ROTATION, POSITION};
}