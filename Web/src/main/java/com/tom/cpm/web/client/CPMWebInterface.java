package com.tom.cpm.web.client;

import com.google.gwt.core.client.GWT;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpm.web.client.render.GuiImpl;
import com.tom.cpm.web.client.render.RenderSystem;
import com.tom.cpm.web.client.util.LoggingPrintStream;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CPMWebInterface {
	private static WebEntry entry;

	private static void start() {
		DomGlobal.console.log("CPM web starting");
		System.setOut(new LoggingPrintStream("STDOUT", DomGlobal.console::info));
		System.setErr(new LoggingPrintStream("STDERR", DomGlobal.console::error));
		GWT.setUncaughtExceptionHandler(e -> {
			GWT.log("Uncaught exception", e);
		});
		RenderSystem.preloaded(() -> {
			new WebMC(new ModConfigFile(DomGlobal.window, FS.hasImpl()), false, false);
			RenderSystem.init(DomGlobal.window, CPMWebInterface::init);
		});
	}

	private static EventHandler init() {
		HTMLElement el = Js.uncheckedCast(RenderSystem.getDocument().getElementById("loadingBar"));
		el.style.display = "none";
		GuiImpl gui = new GuiImpl();
		try {
			entry.doLaunch(gui);
		} catch (Throwable e) {
			gui.onGuiException("Error creating gui", e, true);
		}
		return gui;
	}

	public static interface WebEntry {
		void doLaunch(GuiImpl gui);
	}

	public static void init(WebEntry e) {
		entry = e;
		start();
	}
}
