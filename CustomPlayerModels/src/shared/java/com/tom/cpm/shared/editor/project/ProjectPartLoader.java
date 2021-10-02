package com.tom.cpm.shared.editor.project;

import java.io.IOException;

import com.tom.cpm.shared.editor.Editor;

public interface ProjectPartLoader {
	String getId();
	int getVersion();
	void load(Editor editor, IProject project) throws IOException;
	void save(Editor editor, ProjectWriter project) throws IOException;

	default int getLoadOrder() {
		return 0;
	}
}
