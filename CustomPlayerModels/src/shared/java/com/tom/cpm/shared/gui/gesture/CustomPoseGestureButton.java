package com.tom.cpm.shared.gui.gesture;

import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpm.shared.parts.anim.menu.CustomPoseGestureButtonData;

public class CustomPoseGestureButton extends AbstractGestureButton implements IGestureButton {

	public CustomPoseGestureButton(IGestureButtonContainer c, CustomPoseGestureButtonData data) {
		super(c, data, data.getName(), () -> { data.activate(); c.valueChanged(); });
	}

	@Override
	protected boolean canHold() {
		return ((CustomPoseGestureButtonData) data).gestureTimeout <= 0;
	}

	@Override
	public void updateKeybinds() {
		super.updateKeybinds();
		if (kb != null) {
			String kbMode = this.kb.mode != null ? gui.i18nFormat("label.cpm.gestureMode." + this.kb.mode) : gui.i18nFormat("label.cpm.key_unbound");
			String boundKey = kb.bound;
			if (boundKey == null)boundKey = gui.i18nFormat("label.cpm.key_unbound");
			setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.gestureButton.mode", name, boundKey, kbMode)));
		} else setTooltip(null);
	}
}
