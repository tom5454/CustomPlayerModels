package com.tom.cpm.shared.model;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;

public interface PartValues {
	Vec3f getPos();
	Vec3f getOffset();
	Vec3f getSize();
	Vec2i getUV();
	boolean isMirror();
	float getMCScale();
}
