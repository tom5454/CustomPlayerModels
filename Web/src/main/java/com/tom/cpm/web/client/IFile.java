package com.tom.cpm.web.client;

import java.io.IOException;

public interface IFile {
	String getName();
	boolean exists();
	IFile getParentFile();
	void mkdirs();
	boolean isDirectory();
	String getAbsolutePath();
	boolean isHidden();
	String getCanonicalPath() throws IOException;
	boolean isFile();
	String[] list();
	boolean isRoot();
}