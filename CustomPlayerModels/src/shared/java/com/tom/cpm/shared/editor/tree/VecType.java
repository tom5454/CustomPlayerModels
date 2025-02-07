package com.tom.cpm.shared.editor.tree;

public enum VecType {
	SIZE,
	OFFSET,
	ROTATION,
	POSITION,
	MESH_SCALE,
	TEXTURE,
	PIVOT,
	;
	public static final VecType[] MOUSE_EDITOR_TYPES = {SIZE, OFFSET, ROTATION, POSITION, PIVOT};
	public static final VecType[] MOUSE_EDITOR_ANIM_TYPES = {ROTATION, POSITION};
}