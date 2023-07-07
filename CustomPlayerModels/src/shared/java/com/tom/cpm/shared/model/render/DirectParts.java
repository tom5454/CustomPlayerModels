package com.tom.cpm.shared.model.render;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.SkinType;

public enum DirectParts implements DirectPartValues {
	CAPE(RootModelType.CAPE, new Vec3f(0, 0, 0.125F * 16f), new Vec3f(-6, 180, 0)),
	ELYTRA_LEFT(RootModelType.ELYTRA_LEFT, new Vec3f(5, 0, 2), new Vec3f((float) Math.toDegrees(0.2617994F), 0, (float) Math.toDegrees(-0.2617994F))),
	ELYTRA_RIGHT(RootModelType.ELYTRA_RIGHT, new Vec3f(-5, 0, 2), new Vec3f((float)Math.toDegrees(0.2617994F), 0, (float) Math.toDegrees(0.2617994F))),
	;
	public static final DirectParts[] VALUES = values();
	public final VanillaModelPart value;
	public final Vec3f pos, rot;
	private final PartValues val;

	private DirectParts(VanillaModelPart value, Vec3f pos, Vec3f rot) {
		this.value = value;
		this.pos = pos;
		this.rot = rot;
		this.val = value.getDefaultSize(SkinType.DEFAULT);
	}

	public static PartValues getPartOverrides(VanillaModelPart val, SkinType type) {
		for (int i = 0; i < VALUES.length; i++) {
			DirectParts g = VALUES[i];
			if(g.value == val)return g;
		}
		return val.getDefaultSize(type);
	}

	@Override
	public Vec3f getPos() {
		return pos;
	}

	@Override
	public Vec3f getOffset() {
		return val.getOffset();
	}

	@Override
	public Vec3f getSize() {
		return val.getSize();
	}

	@Override
	public Vec2i getUV() {
		return val.getUV();
	}

	@Override
	public boolean isMirror() {
		return val.isMirror();
	}

	@Override
	public float getMCScale() {
		return val.getMCScale();
	}

	@Override
	public Vec3f getRotation() {
		return rot;
	}
}