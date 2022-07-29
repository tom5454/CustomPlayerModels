package com.tom.cpm.web.client.java.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.tom.cpm.web.client.FS;

public class File {
	private String[] path;

	public File(File file, String name) {
		List<String> l = new ArrayList<>();
		l.addAll(Arrays.asList(file.path));
		l.addAll(Arrays.asList(name.split("/")));
		l.removeIf(f -> f.isEmpty() || f.equals("."));
		path = l.toArray(new String[0]);
	}

	public File(String name) {
		if(name.startsWith("/"))name = name.substring(1);
		path = name.split("/");
	}

	private File(String[] path) {
		this.path = path;
	}

	public String getName() {
		return path.length == 0 ? "" : path[path.length - 1];
	}

	public boolean exists() {
		return FS.exists(getAbsolutePath());
	}

	public File getParentFile() {
		return path.length > 1 ? new File(Arrays.copyOf(path, path.length - 1)) : new File("/");
	}

	public void mkdirs() {
		if(path.length < 2)return;
		for (int i = 1; i <= path.length; i++) {
			FS.mkdir("/" + Arrays.stream(path, 0, i).collect(Collectors.joining("/")));
		}
	}

	public File[] listFiles(FilenameFilter filter) {
		String[] l = FS.list(getAbsolutePath());
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
		String[] l = FS.list(getAbsolutePath());
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
		return FS.isDir(getAbsolutePath());
	}

	public String getAbsolutePath() {
		List<String> l = new ArrayList<>();
		for(String pe : path) {
			if(pe.equals("..")) {
				if(!l.isEmpty())
					l.remove(l.size() - 1);
			} else {
				l.add(pe);
			}
		}
		return "/" + l.stream().collect(Collectors.joining("/"));
	}

	public File getAbsoluteFile() {
		return new File(getAbsolutePath());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(path);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		File other = (File) obj;
		if (!Arrays.equals(path, other.path)) return false;
		return true;
	}

	@Override
	public String toString() {
		return getAbsolutePath();
	}

	public boolean isHidden() {
		return false;
	}

	public File getCanonicalFile() throws IOException {
		return getAbsoluteFile();
	}

	public File toPath() {
		return this;
	}

	public int getNameCount() {
		return getAbsolutePath().equals("/") ? 0 : 1;
	}

	public boolean isFile() {
		return exists() && !isDirectory();
	}
}
