package com.tom.cpm.shared.gui.gesture;

import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.network.ServerCaps;
import com.tom.cpm.shared.parts.anim.menu.BoolParameterToggleButtonData;

public class GestureToggleButton extends AbstractGestureButton implements IGestureButton {
	private BoolParameterToggleButtonData data;

	public GestureToggleButton(IGestureButtonContainer c, BoolParameterToggleButtonData data) {
		super(c, data, data.getName(), null);
		this.data = data;
		if(!MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES)) {
			setEnabled(false);
			setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("label.cpm.feature_unavailable")));
		} else {
			setAction(() -> { data.toggle(); c.valueChanged(); });
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		boolean on = data.getValue();
		int bw = bounds.w-2;
		int w = gui.textWidth(name);
		int bgColor = gui.getColors().button_fill;
		int color = gui.getColors().button_text_color;
		if(!enabled) {
			color = gui.getColors().button_text_disabled;
			bgColor = gui.getColors().button_disabled;
		} else if(event.isHovered(bounds)) {
			color = gui.getColors().button_text_hover;
			bgColor = gui.getColors().button_hover;
		}
		if(event.isHovered(bounds) && tooltip != null)
			tooltip.set();
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x + 1,          bounds.y + 1, bw / 2, bounds.h - 2, on ? 0xff00ff00 : bgColor);
		gui.drawBox(bounds.x + 1 + bw / 2, bounds.y + 1, bw / 2, bounds.h - 2, on ? bgColor : 0xffff0000);
		gui.drawText(bounds.x + bounds.w / 2 - w / 2, bounds.y + bounds.h / 2 - 4, name, color);
		if (kb != null && kb.bound != null) {
			w = gui.textWidth(kb.bound);
			gui.drawText(bounds.x + bounds.w / 2 - w / 2, bounds.y + bounds.h / 2 + 4, kb.bound, color);
		}
	}

	@Override
	public void updateKeybinds() {
		super.updateKeybinds();
		if (MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES)) {
			if (kb != null) {
				String kbMode = kb.mode != null ? gui.i18nFormat("label.cpm.gestureMode." + kb.mode) : gui.i18nFormat("label.cpm.key_unbound");
				String boundKey = kb.bound;
				if (boundKey == null)boundKey = gui.i18nFormat("label.cpm.key_unbound");
				setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.gestureButton.mode", name, boundKey, kbMode)));
			} else {
				setTooltip(null);
			}
		}
	}
}
