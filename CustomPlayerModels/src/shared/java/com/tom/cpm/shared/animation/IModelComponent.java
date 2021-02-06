package com.tom.cpm.shared.animation;

import com.tom.cpm.shared.math.Vec3f;

public interface IModelComponent {
	void setPosition(boolean add, float x, float y, float z);
	void setRotation(boolean add, float x, float y, float z);
	Vec3f getPosition();
	Vec3f getRotation();
	boolean isVisible();
	void setVisible(boolean v);
	void setColor(float x, float y, float z);
	void reset();
	int getRGB();
}
