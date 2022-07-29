package com.tom.cpm.blockbench;

import com.tom.cpm.web.client.FS.IFS;

import elemental2.dom.File;
import elemental2.promise.Promise;

public class BlockBenchFS implements IFS {

	public BlockBenchFS() {
	}

	@Override
	public String[] list(String path) {
		return null;
	}

	@Override
	public boolean exists(String path) {
		return false;
	}

	@Override
	public boolean isDir(String path) {
		return false;
	}

	@Override
	public void mkdir(String path) {

	}

	@Override
	public Promise<String> getContent(String path) {
		return null;
	}

	@Override
	public boolean setContent(String path, String content) {
		return false;
	}

	@Override
	public void deleteFile(String path) {

	}

	@Override
	public Promise<Object> mount(File file) {
		return null;
	}

	@Override
	public boolean needFileManager() {
		return false;
	}
}
