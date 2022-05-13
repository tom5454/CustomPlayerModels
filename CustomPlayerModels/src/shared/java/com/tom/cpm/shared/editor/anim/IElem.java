package com.tom.cpm.shared.editor.anim;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.animation.InterpolatorChannel;

public interface IElem {
	Vec3f getPosition();
	Vec3f getRotation();
	Vec3f getColor();
	Vec3f getScale();
	boolean isVisible();

	default float part(InterpolatorChannel i) {
		switch (i) {
		case POS_X: return getPosition().x;
		case POS_Y: return getPosition().y;
		case POS_Z: return getPosition().z;
		case ROT_X: return (float) Math.toRadians(getRotation().x);
		case ROT_Y: return (float) Math.toRadians(getRotation().y);
		case ROT_Z: return (float) Math.toRadians(getRotation().z);
		case COLOR_R: return getColor().x;
		case COLOR_G: return getColor().y;
		case COLOR_B: return getColor().z;
		case SCALE_X: return getScale().x;
		case SCALE_Y: return getScale().y;
		case SCALE_Z: return getScale().z;
		default:
			break;
		}
		return 0;
	}
}
