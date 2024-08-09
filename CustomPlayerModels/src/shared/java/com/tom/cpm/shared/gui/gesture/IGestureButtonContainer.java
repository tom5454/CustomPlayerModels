package com.tom.cpm.shared.gui.gesture;

import com.tom.cpl.gui.IGui;

public interface IGestureButtonContainer {
	IGui gui();
	void updateKeybind(String keybind, String id, boolean mode);
	BoundKeyInfo getBoundKey(String id);
	void valueChanged();

	public static class BoundKeyInfo {
		public final String key;
		public final String mode;
		public final String bound;

		public BoundKeyInfo(String key, String mode, String bound) {
			this.key = key;
			this.mode = mode;
			this.bound = bound;
		}
	}
}
