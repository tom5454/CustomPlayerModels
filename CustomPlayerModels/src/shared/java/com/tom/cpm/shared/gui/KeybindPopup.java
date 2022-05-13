package com.tom.cpm.shared.gui;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpm.shared.config.Keybind;

public class KeybindPopup extends MessagePopup {
	private Keybind kb;
	private ConfigEntry ce;
	private int modKeys;

	public KeybindPopup(Frame frame, ConfigEntry ce, Keybind kb) {
		super(frame, frame.getGui().i18nFormat("label.cpm.setKeybind.title"), frame.getGui().i18nFormat("label.cpm.setKeybind.desc"), frame.getGui().i18nFormat("button.cpm.cancel"));
		this.ce = ce;
		this.kb = kb;
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		super.draw(event, partialTicks);

		if(modKeys > 0) {
			if((modKeys & Keybind.SHIFT) != 0 && !gui.isShiftDown()) {
				kb.setKey(ce, new KeyboardEvent(gui.getKeyCodes().KEY_LEFT_SHIFT, 0, (char) 0, null), modKeys & ~Keybind.SHIFT);
				close();
				return;
			}
			if((modKeys & Keybind.CTRL) != 0 && !gui.isCtrlDown()) {
				kb.setKey(ce, new KeyboardEvent(gui.getKeyCodes().KEY_LEFT_CONTROL, 0, (char) 0, null), modKeys & ~Keybind.CTRL);
				close();
				return;
			}
			if((modKeys & Keybind.ALT) != 0 && !gui.isAltDown()) {
				kb.setKey(ce, new KeyboardEvent(gui.getKeyCodes().KEY_LEFT_ALT, 0, (char) 0, null), modKeys & ~Keybind.ALT);
				close();
				return;
			}
		}
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(event.keyCode == gui.getKeyCodes().KEY_LEFT_SHIFT || event.keyCode == gui.getKeyCodes().KEY_RIGHT_SHIFT) {
			modKeys |= Keybind.SHIFT;
			event.consume();
			return;
		}
		if(event.keyCode == gui.getKeyCodes().KEY_LEFT_CONTROL || event.keyCode == gui.getKeyCodes().KEY_RIGHT_CONTROL) {
			modKeys |= Keybind.CTRL;
			event.consume();
			return;
		}
		if(event.keyCode == gui.getKeyCodes().KEY_LEFT_ALT || event.keyCode == gui.getKeyCodes().KEY_RIGHT_ALT) {
			modKeys |= Keybind.ALT;
			event.consume();
			return;
		}
		if(event.keyCode == gui.getKeyCodes().KEY_ESCAPE)
			kb.unbindKey(ce);
		else
			kb.setKey(ce, event, modKeys);
		close();
		event.consume();
	}
}
