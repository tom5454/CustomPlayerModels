package com.tom.cpm.shared.animation;

import com.tom.cpl.text.I18n;

public interface IPose {
	String getName(I18n gui, String display);

	default long getTime(AnimationState state, long time) {
		return time;
	}
}
