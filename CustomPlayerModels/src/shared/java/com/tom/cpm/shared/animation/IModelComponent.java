package com.tom.cpm.shared.animation;

import com.tom.cpl.math.Vec3f;

public interface IModelComponent {
	void setPosition(boolean add, float x, float y, float z);
	void setRotation(boolean add, float x, float y, float z);
	Vec3f getPosition();
	Vec3f getRotation();
	Vec3f getRenderScale();
	boolean isVisible();
	void setVisible(boolean v);
	void setColor(float x, float y, float z);
	void setRenderScale(boolean add, float x, float y, float z);
	void reset();
	int getRGB();
}
