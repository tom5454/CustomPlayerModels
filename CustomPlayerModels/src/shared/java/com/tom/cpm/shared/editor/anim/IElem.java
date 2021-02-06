package com.tom.cpm.shared.editor.anim;

import com.tom.cpm.shared.math.Vec3f;

public interface IElem {
	Vec3f getPosition();
	Vec3f getRotation();
	Vec3f getColor();
	boolean isVisible();

	default float part(int i) {
		switch (i) {
		case 0: return getPosition().x;
		case 1: return getPosition().y;
		case 2: return getPosition().z;
		case 3: return getRotation().x;
		case 4: return getRotation().y;
		case 5: return getRotation().z;
		case 6: return getColor().x;
		case 7: return getColor().y;
		case 8: return getColor().z;
		default:
			break;
		}
		return 0;
	}
}
