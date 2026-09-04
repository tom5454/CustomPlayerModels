package com.tom.cpm.client;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDL_DialogFileCallbackI;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import net.minecraft.client.Minecraft;

import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.FileChooserPopup.NativeChooser;

public class SDLChooser implements NativeChooser {
	private FileChooserPopup fc;

	public SDLChooser(FileChooserPopup fc) {
		this.fc = fc;
	}

	@Override
	public CompletableFuture<File> openFuture() {
		CompletableFuture<File> future = new CompletableFuture<>();
		String path = fc.getCurrentDirectory().getAbsolutePath() + "/";
		long window = Minecraft.getInstance().getWindow().handle();
		SDL_DialogFileCallbackI callback = (_, filelist, _) -> {
			if (filelist == MemoryUtil.NULL) {
				future.complete(null);
				return;
			}
			long pathPtr = MemoryUtil.memGetAddress(filelist);
			if (pathPtr == MemoryUtil.NULL) {
				future.complete(null);
				return;
			}
			String pth = MemoryUtil.memUTF8(pathPtr);
			future.complete(new File(pth));
		};
		SDL_DialogFileFilter.Buffer filters = null;
		if (fc.getFilter() instanceof FileFilter) {
			FileFilter ff = (FileFilter) fc.getFilter();
			if (ff.isFolder()) {
				SDLDialog.SDL_ShowOpenFolderDialog(callback, 0, window, path, false);
				return future;
			} else if(ff.getExt() != null) {
				filters = SDL_DialogFileFilter.create(1);
				filters.get(0).name(MemoryStack.stackUTF8(fc.getSafeDescription())).pattern(MemoryStack.stackUTF8(ff.getExt()));
			}
		}

		if (fc.isSaveDialog()) {
			SDLDialog.SDL_ShowSaveFileDialog(callback, 0, window, filters, path);
		} else {
			SDLDialog.SDL_ShowOpenFileDialog(callback, 0, window, filters, path, false);
		}
		return future;
	}

	@Override
	public File open() {
		return null;
	}
}
