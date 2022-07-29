package com.tom.cpm.web.client.entry;

import com.google.gwt.core.client.EntryPoint;

import com.tom.cpm.web.client.CPMWebInterface;
import com.tom.cpm.web.client.CPMWebInterface.WebEntry;
import com.tom.cpm.web.client.render.GuiImpl;
import com.tom.cpm.web.client.render.ViewerGui;

import elemental2.dom.DomGlobal;

public class ViewerEntry implements EntryPoint, WebEntry {

	@Override
	public void onModuleLoad() {
		CPMWebInterface.init(this);
	}

	@Override
	public void doLaunch(GuiImpl gui) {
		DomGlobal.document.title = "CPM Web Viewer (Beta) " + System.getProperty("cpm.version");
		gui.setGui(new ViewerGui(gui));
	}
}
