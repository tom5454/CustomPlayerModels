package com.tom.cpm.blockbench;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpm.blockbench.proxy.Action;
import com.tom.cpm.blockbench.proxy.Blockbench;
import com.tom.cpm.blockbench.proxy.Blockbench.WriteProperties;
import com.tom.cpm.blockbench.proxy.Codec;
import com.tom.cpm.blockbench.proxy.FileSystem;
import com.tom.cpm.blockbench.proxy.MenuBar;
import com.tom.cpm.blockbench.proxy.ModelFormat;
import com.tom.cpm.blockbench.proxy.Plugin;
import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.render.RenderSystem;
import com.tom.cpm.web.client.util.LoggingPrintStream;

import elemental2.dom.DomGlobal;

public class PluginStart implements EntryPoint {
	public static Codec codec;
	public static ModelFormat format;
	public static List<Runnable> cleanup = new ArrayList<>();

	@Override
	public void onModuleLoad() {
		System.setOut(new LoggingPrintStream("STDOUT", DomGlobal.console::info));
		System.setErr(new LoggingPrintStream("STDERR", DomGlobal.console::error));
		RenderSystem.preloaded(() -> {
			try {
				FS.setImpl(new BlockBenchFS());
				new WebMC(new ModConfigFile(DomGlobal.window, FS.hasImpl()), true, true);
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
		Codec.CodecProperties prop = new Codec.CodecProperties();
		prop.name = "Customizable Player Models Project";
		prop.extension = "cpmproject";
		prop.remember = true;
		prop.load_filter = new Codec.LoadFilter();
		prop.load_filter.type = "text";
		prop.load_filter.extensions = new String[] {"cpmproject"};
		prop.export = ProjectConvert::export;
		prop.write = (content, path) -> {
			if(FileSystem.existsSync(path) && codec.overwrite) {
				codec.overwrite(content, path, codec::afterSave);
			} else {
				WriteProperties pr = new WriteProperties();
				pr.content = content;
				pr.savetype = "zip";
				Blockbench.writeFile(path, pr, codec::afterSave);
			}
		};
		codec = new Codec("cpmproject", prop);
		ModelFormat.FormatProperties ctr = new ModelFormat.FormatProperties();
		ctr.id = "cpm";
		ctr.icon = "icon-player";
		ctr.name = "Customizable Player Models model";
		ctr.description = "";
		ctr.bone_rig = true;
		ctr.box_uv = true;
		ctr.optional_box_uv = true;
		ctr.centered_grid = true;
		ctr.single_texture = false;
		ctr.rotate_cubes = true;
		ctr.codec = codec;
		format = new ModelFormat(ctr);
		codec.format = format;
		ProjectGenerator.initDialog();
		ProjectConvert.initDialogs();

		{
			Action.ActionProperties a = new Action.ActionProperties();
			a.name = "Open Customizable Player Models Project";
			a.description = "";
			a.icon = "icon-player";
			a.category = "file";
			a.click = ProjectConvert::open;
			Action importAct = new Action("import_cpmproject", a);
			MenuBar.addAction(importAct, "file.5");
			cleanup.add(importAct::delete);
		}

		{
			Action.ActionProperties a = new Action.ActionProperties();
			a.name = "Export Customizable Player Models Project";
			a.description = "";
			a.icon = "icon-player";
			a.category = "file";
			a.click = e -> codec.export();
			Action exportAct = new Action("export_cpmproject", a);
			MenuBar.addAction(exportAct, "file.export");
			cleanup.add(exportAct::delete);
		}

		{
			Action.ActionProperties a = new Action.ActionProperties();
			a.name = "Open Embedded CPM Editor";
			a.description = "";
			a.icon = "icon-player";
			a.category = "file";
			a.click = e -> EmbeddedEditor.open();
			Action openCPM = new Action("open_cpm", a);
			MenuBar.addAction(openCPM, "file");
			cleanup.add(openCPM::delete);
		}

		/*{
			Action.ActionProperties a = new Action.ActionProperties();
			a.name = "Open Embedded CPM Editor (Tab)";
			a.description = "";
			a.icon = "icon-player";
			a.category = "file";
			a.click = e -> EmbeddedEditor.openTab();
			Action openCPM = new Action("open_cpm_d", a);
			MenuBar.addAction(openCPM, "file");
			cleanup.add(openCPM::delete);
		}*/
	}

	public static void onUnload() {
		DomGlobal.console.log("On Unload");
		cleanup.forEach(Runnable::run);
	}
}
