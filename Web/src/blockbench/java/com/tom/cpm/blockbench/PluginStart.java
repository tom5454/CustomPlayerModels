package com.tom.cpm.blockbench;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpm.blockbench.proxy.Action;
import com.tom.cpm.blockbench.proxy.Action.Condition;
import com.tom.cpm.blockbench.proxy.Blockbench;
import com.tom.cpm.blockbench.proxy.Blockbench.CallbackEvent;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.blockbench.proxy.NodePreviewController;
import com.tom.cpm.blockbench.proxy.Plugin;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.LocalStorageFS;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.render.RenderSystem;
import com.tom.cpm.web.client.util.LoggingPrintStream;

import elemental2.dom.DomGlobal;

public class PluginStart implements EntryPoint {
	public static List<Runnable> cleanup = new ArrayList<>();

	@Override
	public void onModuleLoad() {
		System.setOut(new LoggingPrintStream("STDOUT", DomGlobal.console::info));
		System.setErr(new LoggingPrintStream("STDERR", DomGlobal.console::error));
		RenderSystem.preloaded(() -> {
			try {
				FS.setImpl(Global.isApp() ? new BlockBenchFS() : new LocalStorageFS(DomGlobal.window));
				new WebMC(new ModConfigFile(DomGlobal.window, true), true, true) {

					@Override
					protected String buildPlatformString() {
						return Java.getPlatform() + " BB " + Blockbench.version + (!Global.isApp() ? " (Web)" : "") + " CPM " + System.getProperty("cpm.version");
					}
				};
				DomGlobal.console.log("CPM Plugin loading " + WebMC.platform);
			} catch (Throwable e) {
				e.printStackTrace();
			}

			Plugin.PluginProperties prop = new Plugin.PluginProperties();
			prop.name = "Customizable Player Models Plugin";
			prop.author = "tom5454";
			prop.variant = "both";
			prop.version = System.getProperty("cpm.version");
			prop.tags = new String[] {"Minecraft: Java Edition", "Modded"};
			prop.icon = "icon-player";
			prop.onload = PluginStart::onLoad;
			prop.onunload = PluginStart::onUnload;
			Plugin.register(System.getProperty("cpm.pluginId"), prop);
		});
	}

	public static void onLoad() {
		DomGlobal.console.log("On Load");

		CPMCodec.init();
		ProjectGenerator.initDialog();
		ProjectConvert.initDialogs();
		BBActions.load();
	}

	public static Condition formatCPM() {
		Condition condition = new Action.Condition();
		condition.formats = new String[] {"cpm"};
		return condition;
	}

	public static void onUnload() {
		DomGlobal.console.log("On Unload");
		cleanup.forEach(r -> {
			try {
				r.run();
			} catch (Throwable e) {
			}
		});
	}

	public static void addEventListener(String id, CallbackEvent cb) {
		Blockbench.on(id, cb);
		cleanup.add(() -> Blockbench.removeListener(id, cb));
	}

	public static void addEventListener(NodePreviewController npc, String id, CallbackEvent cb) {
		npc.on(id, cb);
		cleanup.add(() -> npc.removeListener(id, cb));
	}
}
