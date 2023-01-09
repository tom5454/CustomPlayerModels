package com.tom.cpm.web.client;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.NativeChooser;

public class WebChooser implements NativeChooser {
	private FileChooserPopup fc;

	public WebChooser(FileChooserPopup fc) {
		this.fc = fc;
	}

	@Override
	public File open() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompletableFuture<File> openFuture() {
		return FS.openFileChooser(fc);
	}
}
