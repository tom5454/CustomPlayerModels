package com.tom.cpm.client;

import java.io.File;

import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.NativeChooser;

public class ErrorChooser implements NativeChooser {

	public ErrorChooser(FileChooserPopup fc) {
	}

	@Override
	public File open() {
		throw new UnsupportedOperationException("System chooser is not available on this Minecraft version");
	}

}
