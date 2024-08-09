package com.tom.cpm.shared.gui.gesture;

import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpm.shared.parts.anim.menu.CustomPoseGestureButtonData;

public class CustomPoseGestureButton extends AbstractGestureButton implements IGestureButton {

	public CustomPoseGestureButton(IGestureButtonContainer c, CustomPoseGestureButtonData data) {
		super(c, data, data.getName(), () -> { data.activate(); c.valueChanged(); });
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		super.draw(event, partialTicks);
		int color = gui.getColors().button_text_color;
		if (event.isHovered(bounds)) {
			color = gui.getColors().button_text_hover;
		}
		if (kb != null && kb.bound != null) {
			int w = gui.textWidth(kb.bound);
			gui.drawText(bounds.x + bounds.w / 2 - w / 2, bounds.y + bounds.h / 2 + 4, kb.bound, color);
		}
	}

	@Override
	protected boolean canHold() {
		// TODO Auto-generated method stub
		return super.canHold();
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
