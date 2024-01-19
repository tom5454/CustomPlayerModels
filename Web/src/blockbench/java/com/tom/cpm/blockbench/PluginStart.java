package com.tom.cpm.blockbench;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpm.blockbench.convert.ProjectConvert;
import com.tom.cpm.blockbench.format.CPMCodec;
import com.tom.cpm.blockbench.format.ProjectGenerator;
import com.tom.cpm.blockbench.proxy.Blockbench;
import com.tom.cpm.blockbench.proxy.Blockbench.CallbackEvent;
import com.tom.cpm.blockbench.proxy.Codec;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.blockbench.proxy.NodePreviewController;
import com.tom.cpm.blockbench.proxy.Plugin;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.LocalStorageFS;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.render.RenderSystem;
import com.tom.cpm.web.client.util.LoggingPrintStream;
import com.tom.ugwt.client.ExceptionUtil;

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

					@Override
					public String getMCVersion() {
						return "blockbench";
					}

					@Override
					public void populatePlatformSettings(String group, Panel panel) {
						super.populatePlatformSettings(group, panel);
						switch (group) {
						case "filePopup": {
							PopupMenu pp = (PopupMenu) panel;
							pp.addButton(panel.getGui().i18nFormat("bb-button.viewInBB"), ProjectConvert::viewInBB);
						}
						break;
						}
					}

					@Override
					public void openURL(String url) {
						Global.openExternal(url);
					}

					@Override
					public String getAppID() {
						return "Blockbench CPM Plugin";
					}

					@Override
					protected String getLangChangeDesc() {
						return "bb-label.language.reloadRequired.desc";
					}
				};
				DomGlobal.console.log("CPM Plugin loading " + WebMC.platform);
			} catch (Throwable e) {
				StringBuilder sb = new StringBuilder();
				sb.append("Error loading plugin:\n");
				sb.append(ExceptionUtil.getStackTrace(e));
				DomGlobal.console.error(sb.toString());
			}

			Plugin.PluginProperties prop = new Plugin.PluginProperties();
			prop.name = "Customizable Player Models Plugin";
			prop.author = "tom5454";
			prop.variant = "both";
			prop.description = "Customizable Player Models Project (.cpmproject) support for Blockbench.";
			prop.version = System.getProperty("cpm.version");
			prop.tags = new String[] {"Minecraft: Java Edition", "Modded"};
			prop.icon = "icon-player";
			prop.onload = PluginStart::onLoad;
			prop.onunload = PluginStart::onUnload;
			Plugin.register(System.getProperty("cpm.pluginId"), prop);
		});
	}

	public static void onLoad() {
		CPMCodec.init();
		ProjectGenerator.initDialog();
		BBActions.load();
	}

	public static void onUnload() {
		cleanup.forEach(r -> {
			try {
				r.run();
			} catch (Throwable e) {
			}
		});
		DomGlobal.console.log("Unloaded CPM Plugin");
	}

	public static void addEventListener(String id, CallbackEvent cb) {
		Blockbench.on(id, cb);
		cleanup.add(() -> Blockbench.removeListener(id, cb));
	}

	public static void addEventListener(NodePreviewController npc, String id, CallbackEvent cb) {
		npc.on(id, cb);
		cleanup.add(() -> npc.removeListener(id, cb));
	}

	public static void addEventListener(Codec c, String id, CallbackEvent cb) {
		c.on(id, cb);
		cleanup.add(() -> c.removeListener(id, cb));
	}
}
