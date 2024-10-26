package com.tom.cpm.client;

import java.io.File;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.FileChooserPopup.NativeChooser;

public class TinyFDChooser implements NativeChooser {
	private FileChooserPopup fc;

	public TinyFDChooser(FileChooserPopup fc) {
		this.fc = fc;
	}

	@Override
	public File open() {
		String path = fc.getCurrentDirectory().getAbsolutePath() + "/";
		if(fc.getFilter() instanceof FileFilter) {
			FileFilter ff = (FileFilter) fc.getFilter();
			if(ff.isFolder()) {
				String sel = TinyFileDialogs.tinyfd_selectFolderDialog(fc.getSafeTitle(), path);
				if(sel == null)return null;
				return new File(sel);
			} else if(ff.getExt() != null) {
				try (MemoryStack stack = MemoryStack.stackPush()) {
					PointerBuffer aFilterPatterns = stack.mallocPointer(1);

					aFilterPatterns.put(stack.UTF8("*." + ff.getExt()));

					aFilterPatterns.flip();

					String sel = fc.isSaveDialog() ?
							TinyFileDialogs.tinyfd_saveFileDialog(fc.getSafeTitle(), path, aFilterPatterns, fc.getSafeDescription()) :
								TinyFileDialogs.tinyfd_openFileDialog(fc.getSafeTitle(), path, aFilterPatterns, fc.getSafeDescription(), false);
					if(sel == null)return null;
					return new File(sel);
				}
			}
		}
		String sel = fc.isSaveDialog() ?
				TinyFileDialogs.tinyfd_saveFileDialog(fc.getSafeTitle(), path, null, fc.getSafeDescription()) :
					TinyFileDialogs.tinyfd_openFileDialog(fc.getSafeTitle(), path, null, fc.getSafeDescription(), false);
		if(sel == null)return null;
		return new File(sel);
	}
}
