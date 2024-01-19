package com.tom.cpm.web.client.entry;

import com.google.gwt.core.client.EntryPoint;

import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.web.client.CPMWebInterface;
import com.tom.cpm.web.client.CPMWebInterface.WebEntry;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.LocalStorageFS;
import com.tom.cpm.web.client.fbxtool.FBXEditorGui;
import com.tom.cpm.web.client.fbxtool.FBXToolGui;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.render.GuiImpl;
import com.tom.cpm.web.client.render.ViewerGui;

import elemental2.dom.DomGlobal;

public class DevEntry implements EntryPoint, WebEntry {

	@Override
	public void onModuleLoad() {
		FS.setImpl(new LocalStorageFS(DomGlobal.window));
		CPMWebInterface.init(this);
	}

	@Override
	public void doLaunch(GuiImpl gui) {
		String app = DomGlobal.window.location.pathname;
		if(app != null && app.endsWith("viewer.html") || "viewer".equals(Java.getQueryVariable("app"))) {
			DomGlobal.document.title = "CPM Web Viewer";
			gui.setGui(new ViewerGui(gui));
		} else if(app != null && app.endsWith("fbxeditor.html") || "fbxeditor".equals(Java.getQueryVariable("app"))) {
			DomGlobal.document.title = "CPM FBX Editor";
			gui.setGui(new FBXEditorGui(gui));
		} else if(app != null && app.endsWith("fbxtool.html") || "fbxtool".equals(Java.getQueryVariable("app"))) {
			DomGlobal.document.title = "CPM FBX Tool";
			gui.setGui(new FBXToolGui(gui));
		} else {
			DomGlobal.document.title = "CPM Web Editor";
			gui.setGui(new EditorGui(gui));
		}
	}
}
