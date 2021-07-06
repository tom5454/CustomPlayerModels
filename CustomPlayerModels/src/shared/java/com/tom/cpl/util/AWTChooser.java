package com.tom.cpl.util;

import java.io.File;

import javax.swing.JFileChooser;

import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.FileChooserPopup.NativeChooser;

public class AWTChooser implements NativeChooser {
	private FileChooserPopup fc;

	public AWTChooser(FileChooserPopup fc) {
		this.fc = fc;
	}

	@Override
	public File open() {
		JFileChooser jfc = new JFileChooser(fc.getCurrentDirectory());
		jfc.setDialogTitle(fc.getTitle());
		if(fc.getFilter() instanceof FileFilter) {
			FileFilter ff = (FileFilter) fc.getFilter();
			if(ff.isFolder()) {
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}
		}
		jfc.setFileFilter(new javax.swing.filechooser.FileFilter() {

			@Override
			public String getDescription() {
				return fc.getDesc();
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || fc.getFilter().test(f, f.getName());
			}
		});

		if((fc.isSaveDialog() ? jfc.showSaveDialog(null) : jfc.showOpenDialog(null)) == JFileChooser.APPROVE_OPTION) {
			return jfc.getSelectedFile();
		}
		return null;
	}

}
