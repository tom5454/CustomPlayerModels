package com.tom.cpm.blockbench.util;

import com.tom.cpm.blockbench.proxy.electron.BrowserWindow;
import com.tom.cpm.blockbench.proxy.electron.Electron;
import com.tom.cpm.blockbench.proxy.electron.ElectronDialog.DialogProperties;

import elemental2.promise.Promise;

public class SystemFileChooser {

	public static Promise<String> showOpenDialog(DialogProperties dp) {
		return Electron.dialog.showOpenDialog(dp).then(d -> {
			return Promise.resolve(d.canceled ? null : d.filePaths[0]);
		});
	}

	public static Promise<String> showSaveDialog(DialogProperties dp) {
		return Electron.dialog.showSaveDialog(dp).then(d -> {
			return Promise.resolve(d.canceled ? null : d.filePath);
		});
	}

	public static Promise<String> showOpenDialog(BrowserWindow window, DialogProperties dp) {
		return Electron.dialog.showOpenDialog(window, dp).then(d -> {
			return Promise.resolve(d.canceled ? null : d.filePaths[0]);
		});
	}

	public static Promise<String> showSaveDialog(BrowserWindow window, DialogProperties dp) {
		return Electron.dialog.showSaveDialog(window, dp).then(d -> {
			return Promise.resolve(d.canceled ? null : d.filePath);
		});
	}
}
