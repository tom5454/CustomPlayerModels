package com.tom.cpm.blockbench.format;

import com.tom.cpm.blockbench.BBActions;
import com.tom.cpm.blockbench.BlockBenchFS;
import com.tom.cpm.blockbench.EmbeddedEditor;
import com.tom.cpm.blockbench.PluginStart;
import com.tom.cpm.blockbench.convert.OldPluginConvert;
import com.tom.cpm.blockbench.convert.ProjectConvert;
import com.tom.cpm.blockbench.proxy.Action.Condition;
import com.tom.cpm.blockbench.proxy.Blockbench;
import com.tom.cpm.blockbench.proxy.Blockbench.WriteProperties;
import com.tom.cpm.blockbench.proxy.Codec;
import com.tom.cpm.blockbench.proxy.Codecs;
import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.blockbench.proxy.Group;
import com.tom.cpm.blockbench.proxy.ModelFormat;
import com.tom.cpm.blockbench.proxy.ModelFormat.FormatPage;
import com.tom.cpm.blockbench.proxy.ModelFormat.FormatPageContent;
import com.tom.cpm.blockbench.proxy.Outliner;
import com.tom.cpm.blockbench.proxy.Property;
import com.tom.cpm.blockbench.proxy.Property.Clazz;
import com.tom.cpm.blockbench.proxy.Property.Type;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.util.I18n;
import com.tom.ugwt.client.GlobalFunc;
import com.tom.ugwt.client.JsRunnable;

import jsinterop.base.Js;

public class CPMCodec {
	public static final String FORMAT_ID = "cpm";

	public static Codec codec;
	public static ModelFormat format;

	public static void init() {
		Codec.CodecProperties prop = new Codec.CodecProperties();
		prop.name = "Customizable Player Models Project";
		prop.extension = "cpmproject";
		prop.remember = true;
		prop.load_filter = new Codec.LoadFilter();
		prop.load_filter.type = "text";
		prop.load_filter.extensions = new String[] {"cpmproject"};
		prop.export = ProjectConvert::export;
		prop.write = (content, path) -> {
			if(BlockBenchFS.fs.existsSync(path) && codec.overwrite) {
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
		ctr.id = FORMAT_ID;
		ctr.icon = "icon-player";
		ctr.name = "Customizable Player Models model";
		ctr.description = I18n.get("bb-label.cpmCodecDesc");
		ctr.target = "Minecraft: Java Edition with Customizable Player Models mod";
		ctr.bone_rig = true;
		ctr.box_uv = true;
		ctr.optional_box_uv = true;
		ctr.centered_grid = true;
		ctr.single_texture = false;
		ctr.rotate_cubes = true;
		ctr.uv_rotation = true;
		if(MinecraftObjectHolder.DEBUGGING) {
			ctr.animation_mode = true;
			ctr.animation_controllers = true;
			ctr.animation_files = true;
			ctr.bone_binding_expression = true;
		}
		ctr.codec = codec;
		ctr.category = "minecraft";
		GlobalFunc openEmbed = GlobalFunc.pushGlobalFunc(JsRunnable.class, EmbeddedEditor::open);
		GlobalFunc importCPM = GlobalFunc.pushGlobalFunc(JsRunnable.class, ProjectConvert::open);
		ctr.format_page = FormatPage.create(
				FormatPageContent.create("h3", Global.translate("mode.start.format.informations")),
				FormatPageContent.create("* " + I18n.get("bb-label.cpmInfo.export")),
				FormatPageContent.create("* " + I18n.get("bb-label.cpmInfo.import")),
				FormatPageContent.create("h3", Global.translate("mode.start.format.resources")),
				FormatPageContent.create("* [Wiki](https://github.com/tom5454/CustomPlayerModels/wiki)"),
				FormatPageContent.create("* [Discord](https://discord.gg/mKyXdEsMZD)"),
				FormatPageContent.create("* [CPM on CurseForge](https://www.curseforge.com/minecraft/mc-mods/custom-player-models)"),
				FormatPageContent.create("* [CPM on Modrinth](https://modrinth.com/mod/custom-player-models)<br>"),
				FormatPageContent.create("<button onclick='" + importCPM + "()'><i class=\"material-icons\">folder_open</i> " + I18n.get("bb-button.openCPMProject") + " </button>"),
				FormatPageContent.create("<button onclick='" + openEmbed + "()'><i class=\"material-icons\">launch</i> " + I18n.get("bb-button.openEmbeddedEditor") + " </button>"),
				FormatPageContent.create("Version: " + WebMC.platform)
				);
		PluginStart.cleanup.add(openEmbed);
		PluginStart.cleanup.add(importCPM);
		format = new ModelFormat(ctr);
		codec.format = format;
		PluginStart.cleanup.add(() -> Global.getFormats().delete(FORMAT_ID));

		createProperty(Clazz.CUBE, Type.BOOLEAN, "cpm_glow", "CPM Glow Effect", false, PluginStart.formatCPM());
		createProperty(Clazz.CUBE, Type.NUMBER, "cpm_recolor", "CPM Recolor Effect", -1, PluginStart.formatCPM());
		createProperty(Clazz.GROUP, Type.BOOLEAN, "cpm_hidden", "CPM Hidden Effect", false, PluginStart.formatCPM());
		createProperty(Clazz.CUBE, Type.BOOLEAN, "cpm_extrude", "CPM Extrude Effect", false, PluginStart.formatCPM());
		createProperty(Clazz.GROUP, Type.BOOLEAN, "cpm_dva", "CPM Disable Vanilla Animations Effect", false, PluginStart.formatCPM());
		createProperty(Clazz.GROUP, Type.STRING, "cpm_copy_transform", "CPM Copy Transform Effect", Js.undefined(), PluginStart.formatCPM());
		createProperty(Clazz.PROJECT, Type.BOOLEAN, "cpm_hideHeadIfSkull", "CPM Hide Head with Skull", true, PluginStart.formatCPM());
		createProperty(Clazz.PROJECT, Type.BOOLEAN, "cpm_removeBedOffset", "CPM Remove Bed Offset", false, PluginStart.formatCPM());

		createProperty(Clazz.GROUP, Type.STRING, "cpm_data", "CPM Data", Js.undefined(), PluginStart.formatCPM());
		createProperty(Clazz.CUBE, Type.STRING, "cpm_data", "CPM Data", Js.undefined(), PluginStart.formatCPM());
		createProperty(Clazz.PROJECT, Type.STRING, "cpm_data", "CPM Data", Js.undefined(), PluginStart.formatCPM());

		PluginStart.addEventListener("update_selection", dt -> {
			if(Global.getFormat() == CPMCodec.format) {
				if(Outliner.selected.length == 1 && Outliner.selected[0] instanceof Cube) {
					BBActions.glowButton.value = ((Cube)Outliner.selected[0]).glow;
					BBActions.glowButton.updateEnabledState();
				}
				if(Group.selected != null) {
					BBActions.hiddenButton.value = Group.selected.hidden;
					BBActions.hiddenButton.updateEnabledState();
				}
			}
		});
		PluginStart.addEventListener(Codecs.project, "parsed", __ -> OldPluginConvert.convert());

		/*PluginStart.addEventListener(Cube.preview_controller, "update_geometry", dt -> {
			OutlinerElement elem = NodePreviewEvent.getElement(dt);
			if(elem instanceof Cube) {
				Cube c = (Cube) elem;
				c.mesh.material = c.glow ? c.mesh.material.getGlow() : c.mesh.material.getNormal();
			}
		});*/
	}

	public static void createProperty(Clazz clz, Type type, String id, String label, Object def, Condition cond) {
		Property p = Property.createProperty(clz, type, id, label, def, cond);
		PluginStart.cleanup.add(p::delete);
	}
}
