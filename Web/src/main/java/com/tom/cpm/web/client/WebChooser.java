package com.tom.cpm.web.client;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.FileChooserPopup.NativeChooser;
import com.tom.cpl.gui.elements.InputPopup;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.render.RenderSystem;

import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

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
		CompletableFuture<File> f = new CompletableFuture<>();
		if(fc.isSaveDialog()) {
			fc.getFrame().openPopup(new InputPopup(fc.getFrame(), "File name", "Enter file name", v -> f.complete(new File("/mnt/" + v)), null));
		} else {
			HTMLInputElement input = Js.uncheckedCast(RenderSystem.getDocument().createElement("input"));
			input.style.display = "none";
			input.type = "file";
			if(fc.getFilter() instanceof FileFilter) {
				FileFilter ff = (FileFilter) fc.getFilter();
				if(ff.getExt() != null) {
					input.accept = "." + ff.getExt();
				}
			}
			RenderSystem.getDocument().body.appendChild(input);
			input.onchange = e -> {
				if(input.files.getLength() > 0) {
					Java.promiseToCf(RenderSystem.mount(input.files.getAt(0)), f);
				} else {
					f.complete(null);
				}
				return null;
			};
			input.click();
		}
		return f;
	}

}
