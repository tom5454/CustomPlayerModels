package com.tom.cpm.shared.model;

import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.skin.TextureType;

public enum TextureSheetType {
	SKIN(true, 512, TextureType.SKIN, new Vec2i(64, 64)),
	LIST_ICON(true, 32, null, new Vec2i(8, 8)),
	CAPE(true, 256, TextureType.CAPE, new Vec2i(64, 32)),
	ELYTRA(true, 256, TextureType.ELYTRA, new Vec2i(64, 32)),
	ARMOR1(false, 64, null, new Vec2i(64, 32)),
	ARMOR2(false, 64, null, new Vec2i(64, 32)),
	;
	public static final TextureSheetType[] VALUES = values();
	public final boolean editable;
	public final int texLimit;
	public final TextureType texType;
	private final Vec2i defSize;

	private TextureSheetType(boolean editable, int texLimit, TextureType type, Vec2i defSize) {
		this.editable = editable;
		this.texLimit = texLimit;
		this.texType = type;
		this.defSize = defSize;
	}

	public Vec2i getDefSize() {
		return new Vec2i(defSize);
	}
}
