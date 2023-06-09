package com.tom.cpm.shared.model;

import com.tom.cpl.math.Vec3f;

public class PartPosition {
	protected Vec3f rPos = new Vec3f(), rRotation = new Vec3f(), rScale = new Vec3f();

	public void setRenderScale(Vec3f pos, Vec3f rotation, Vec3f scale) {
		rPos = pos;
		rRotation = rotation;
		rScale = scale;
	}

	public Vec3f getRPos() {
		return rPos;
	}

	public Vec3f getRRotation() {
		return rRotation;
	}

	public Vec3f getRScale() {
		return rScale;
	}

	public void setRPos(Vec3f rPos) {
		this.rPos = rPos;
	}

	public void setRRotation(Vec3f rRotation) {
		this.rRotation = rRotation;
	}

	public void setRotationDeg(Vec3f r) {
		this.rRotation = new Vec3f((float) Math.toRadians(r.x), (float) Math.toRadians(r.y), (float) Math.toRadians(r.z));
	}

	public void setRScale(Vec3f rScale) {
		this.rScale = rScale;
	}

	public boolean isChanged() {
		return rPos.x != 0 || rPos.y != 0 || rPos.z != 0 ||
				rRotation.x != 0 || rRotation.y != 0 || rRotation.z != 0 ||
				rScale.x != 0 || rScale.y != 0 || rScale.z != 0;
	}
}
