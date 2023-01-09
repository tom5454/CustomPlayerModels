package com.tom.cpm.web.client.java.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.IFile;

public class File {
	private IFile impl;

	public File(File file, String name) {
		impl = FS.getImpl(file.impl, name);
	}

	public File(String name) {
		impl = FS.getImpl(name);
	}

	private File(IFile impl) {
		this.impl = impl;
	}

	public String getName() {
		return impl.getName();
	}

	public boolean exists() {
		return impl.exists();
	}

	public File getParentFile() {
		return new File(impl.getParentFile());
	}

	public void mkdirs() {
		impl.mkdirs();
	}

	public File[] listFiles(FilenameFilter filter) {
		String[] l = impl.list();
		List<File> ret = new ArrayList<>();
		for (int i = 0; i < l.length; i++) {
			File f = new File(l[i]);
			if(filter.accept(f.getParentFile(), f.getName())) {
				ret.add(f);
			}
		}
		return ret.toArray(new File[0]);
	}

	public String[] list(FilenameFilter filter) {
		String[] l = impl.list();
		List<String> ret = new ArrayList<>();
		for (int i = 0; i < l.length; i++) {
			File f = new File(l[i]);
			if(filter.accept(f.getParentFile(), f.getName())) {
				ret.add(f.getName());
			}
		}
		return ret.toArray(new String[0]);
	}

	public boolean isDirectory() {
		return impl.isDirectory();
	}

	public String getAbsolutePath() {
		return impl.getAbsolutePath();
	}

	public File getAbsoluteFile() {
		return new File(getAbsolutePath());
	}

	@Override
	public int hashCode() {
		return impl.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		File other = (File) obj;
		if (!impl.equals(other.impl)) return false;
		return true;
	}

	@Override
	public String toString() {
		return getAbsolutePath();
	}

	public boolean isHidden() {
		return impl.isHidden();
	}

	public File getCanonicalFile() throws IOException {
		return new File(impl.getCanonicalPath());
	}

	public File toPath() {
		return this;
	}
	/**Method from Path*/
	public int getNameCount() {
		return impl.isRoot() ? 0 : 1;
	}

	public boolean isFile() {
		return impl.isFile();
	}
}
