package com.tom.cpm.shared.model;

import com.tom.cpl.math.Vec3f;

public class ScaleData {
	public static final ScaleData NULL = new ScaleData(1);
	private float scale, eyeH, hitboxW, hitboxH;
	private Vec3f rPos = new Vec3f(), rRotation = new Vec3f(), rScale = new Vec3f();

	public ScaleData(float scale) {
		this.scale = scale;
	}

	public float getScale() {
		if(scale > 10)return 0;
		else if(scale < 0.05f)return 0;
		return scale;
	}

	public float getEyeHScale() {
		if(eyeH > 10)return 0;
		else if(eyeH < 0.05f)return 0;
		return eyeH;
	}

	public float getWidthScale() {
		if(hitboxW > 10)return 0;
		else if(hitboxW < 0.05f)return 0;
		return hitboxW;
	}

	public float getHeightScale() {
		if(hitboxH > 10)return 0;
		else if(hitboxH < 0.05f)return 0;
		return hitboxH;
	}

	public void setScale(float eyeHeight, float hitboxW, float hitboxH) {
		this.eyeH = eyeHeight;
		this.hitboxW = hitboxW;
		this.hitboxH = hitboxH;
	}

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
}
