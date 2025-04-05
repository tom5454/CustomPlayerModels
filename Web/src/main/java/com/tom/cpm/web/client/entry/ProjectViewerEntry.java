package com.tom.cpm.web.client.entry;

import com.google.gwt.core.client.EntryPoint;

import com.tom.cpm.web.client.CPMWebInterface;
import com.tom.cpm.web.client.CPMWebInterface.WebEntry;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.LocalStorageFS;
import com.tom.cpm.web.client.render.GuiImpl;
import com.tom.cpm.web.client.render.ProjectViewerGui;

import elemental2.dom.DomGlobal;

public class ProjectViewerEntry implements EntryPoint, WebEntry {

	@Override
	public void onModuleLoad() {
		FS.setImpl(new LocalStorageFS(DomGlobal.window));
		CPMWebInterface.init(this);
	}

	@Override
	public void doLaunch(GuiImpl gui) {
		DomGlobal.document.title = "CPM Web Project Viewer (Beta) " + System.getProperty("cpm.version");
		gui.setGui(new ProjectViewerGui(gui));
	}
}
