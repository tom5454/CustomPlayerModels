package com.tom.cpm.blockbench.format;

import com.tom.cpm.blockbench.BBActions;
import com.tom.cpm.blockbench.BlockBenchFS;
import com.tom.cpm.blockbench.PluginStart;
import com.tom.cpm.blockbench.convert.OldPluginConvert;
import com.tom.cpm.blockbench.convert.ProjectConvert;
import com.tom.cpm.blockbench.ee.EmbeddedEditorHandler;
import com.tom.cpm.blockbench.proxy.Action;
import com.tom.cpm.blockbench.proxy.Action.Condition;
import com.tom.cpm.blockbench.proxy.Action.ConditionMethod;
import com.tom.cpm.blockbench.proxy.Animation.AnimatorChannel;
import com.tom.cpm.blockbench.proxy.Blockbench;
import com.tom.cpm.blockbench.proxy.Blockbench.WriteProperties;
import com.tom.cpm.blockbench.proxy.BoneAnimator;
import com.tom.cpm.blockbench.proxy.Codec;
import com.tom.cpm.blockbench.proxy.Codecs;
import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.blockbench.proxy.KeyframeDataPoint;
import com.tom.cpm.blockbench.proxy.ModelFormat;
import com.tom.cpm.blockbench.proxy.ModelFormat.FormatPage;
import com.tom.cpm.blockbench.proxy.Outliner;
import com.tom.cpm.blockbench.proxy.Project;
import com.tom.cpm.blockbench.proxy.Property;
import com.tom.cpm.blockbench.proxy.Property.Clazz;
import com.tom.cpm.blockbench.proxy.Property.Type;
import com.tom.cpm.blockbench.proxy.VueComponent;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.util.I18n;

import elemental2.core.JsObject;
import javaemul.internal.annotations.DoNotAutobox;
import jsinterop.base.Js;

public class CPMCodec {
	public static final String FORMAT_ID = "cpm";
	public static final String VISIBILITY = "cpm_visibility";
	public static final String COLOR = "cpm_color";

	public static Codec codec;
	public static ModelFormat format;
	private static boolean addedAnimators;

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
		ctr.animation_mode = true;
		ctr.animation_files = true;
		if(MinecraftObjectHolder.DEBUGGING) {
			//ctr.animation_controllers = true;
			//ctr.bone_binding_expression = true;
		}
		ctr.codec = codec;
		ctr.category = "minecraft";
		ctr.format_page = new FormatPage();
		ctr.format_page.component = new VueComponent();
		ctr.format_page.component.methods = Js.uncheckedCast(new JsObject());
		ctr.format_page.component.methods.set("create", ProjectGenerator::newProject);
		ctr.format_page.component.methods.set("openembed", EmbeddedEditorHandler::open);
		ctr.format_page.component.methods.set("open", ProjectConvert::open);
		ctr.format_page.component.template = "<div style=\"display:flex;flex-direction:column;height:100%\">"
				+ "<p class=\"format_description\">" + I18n.get("bb-label.cpmCodecDesc") + "</p>"
				+ "<p class=\"format_target\"><b>Target</b>:<span>Minecraft: Java Edition with Customizable Player Models mod</span></p>"
				+ "<h3 class=\"markdown\">" + Global.translate("mode.start.format.informations") + "</h3>"
				+ "<p class=\"markdown\">"
				+ "<ul><li>" + I18n.get("bb-label.cpmInfo.export") + "</li></ul>"
				+ "<ul><li>" + I18n.get("bb-label.cpmInfo.import") + "</li></ul>"
				+ "</p>"
				+ "<h3 class=\"markdown\">" + Global.translate("mode.start.format.resources") + "</h3>"
				+ "<p class=\"markdown\">"
				+ "<ul><li><a href=\"https://github.com/tom5454/CustomPlayerModels/wiki\">Wiki</a></li></ul>"
				+ "<ul><li><a href=\"https://discord.gg/mKyXdEsMZD\">Discord</a></li></ul>"
				+ "<ul><li><a href=\"https://www.curseforge.com/minecraft/mc-mods/custom-player-models\">CPM on CurseForge</a></li></ul>"
				+ "<ul><li><a href=\"https://modrinth.com/mod/custom-player-models\">CPM on Modrinth</a><br></li></ul>"
				+ "<ul><li><a href=\"https://github.com/tom5454/CustomPlayerModels/issues\">Bug tracker</a><br></li></ul>"
				+ "</p>"
				+ "<p><button @click=\"open\"><i class=\"material-icons\">folder_open</i> " + I18n.get("bb-button.openCPMProject") + " </button></p>"
				+ "<p><button @click=\"openembed\"><i class=\"material-icons\">launch</i> " + I18n.get("bb-button.openEmbeddedEditor") + " </button></p>"
				+ "<p class=\"markdown\"><p>Version: " + WebMC.platform + "</p>"
				+ "<div class=\"button_bar\"><button id=\"create_new_model_button\" style=\"margin-top: 20px;\" @click=\"create\"><i class=\"material-icons\">arrow_forward</i> Create New Model</button></div>"
				+ "</div>";

		AnimatorChannel visCh = new AnimatorChannel();
		visCh.name = I18n.get("label.cpm.visible");
		visCh.mutable = true;
		visCh.transform = false;
		visCh.max_data_points = 1;

		AnimatorChannel colorCh = new AnimatorChannel();
		colorCh.name = I18n.get("label.cpm.recolor");
		colorCh.mutable = true;
		colorCh.transform = true;
		colorCh.max_data_points = 1;

		ctr.onActivation = () -> {
			if(addedAnimators)return;
			addedAnimators = true;
			BoneAnimator.prototype.channels.set(VISIBILITY, visCh);
			BoneAnimator.prototype.channels.set(COLOR, colorCh);
		};
		ctr.onDeactivation = () -> {
			if(!addedAnimators)return;
			addedAnimators = false;
			BoneAnimator.prototype.channels.delete(VISIBILITY);
			BoneAnimator.prototype.channels.delete(COLOR);
		};

		format = new ModelFormat(ctr);
		codec.format = format;
		PluginStart.cleanup.add(() -> Global.getFormats().delete(FORMAT_ID));

		createProperty(Clazz.CUBE, Type.BOOLEAN, "cpm_glow", "CPM Glow Effect", false, true);
		createProperty(Clazz.CUBE, Type.NUMBER, "cpm_recolor", "CPM Recolor Effect", -1, true);
		createProperty(Clazz.CUBE, Type.BOOLEAN, "cpm_extrude", "CPM Extrude Effect", false, true);

		createProperty(Clazz.GROUP, Type.BOOLEAN, "cpm_hidden", "CPM Hidden Effect", false, true);
		createProperty(Clazz.GROUP, Type.BOOLEAN, "cpm_dva", "CPM Disable Vanilla Animations Effect", false, true);
		createProperty(Clazz.GROUP, Type.STRING, "cpm_copy_transform", "CPM Copy Transform Effect", Js.undefined(), true);

		createProperty(Clazz.PROJECT, Type.BOOLEAN, "cpm_hideHeadIfSkull", "CPM Hide Head with Skull", true, false);
		createProperty(Clazz.PROJECT, Type.BOOLEAN, "cpm_removeBedOffset", "CPM Remove Bed Offset", false, false);
		createProperty(Clazz.PROJECT, Type.BOOLEAN, "cpm_invisGlow", "CPM Invisible Glow", false, false);

		createProperty(Clazz.ANIMATION, Type.STRING, "cpm_type", "CPM Animation Type", "custom_pose", true);
		createProperty(Clazz.ANIMATION, Type.BOOLEAN, "cpm_additive", "CPM Additive", true, false);
		createProperty(Clazz.ANIMATION, Type.BOOLEAN, "cpm_layerCtrl", "CPM Layer Controlled", true, false);
		createProperty(Clazz.ANIMATION, Type.BOOLEAN, "cpm_commandCtrl", "CPM Command Controlled", false, false);
		createProperty(Clazz.ANIMATION, Type.NUMBER, "cpm_priority", "CPM Animation Priority", 0, false);
		createProperty(Clazz.ANIMATION, Type.NUMBER, "cpm_order", "CPM Animation Button Order", 0, false);
		createProperty(Clazz.ANIMATION, Type.BOOLEAN, "cpm_isProperty", "CPM Is Property", false, false);
		createProperty(Clazz.ANIMATION, Type.STRING, "cpm_group", "CPM Animation Type", "", false);
		createProperty(Clazz.ANIMATION, Type.NUMBER, "cpm_layerDefault", "CPM Layer Default Value", 0, false);

		createProperty(Clazz.GROUP, Type.STRING, "cpm_data", "CPM Data", Js.undefined(), true);
		createProperty(Clazz.CUBE, Type.STRING, "cpm_data", "CPM Data", Js.undefined(), true);
		createProperty(Clazz.PROJECT, Type.STRING, "cpm_data", "CPM Data", Js.undefined(), true);

		PluginStart.cleanup.add(() -> BoneAnimator.prototype.channels.delete(VISIBILITY));
		addTranslatedEntry("timeline." + VISIBILITY, "CPM " + I18n.get("label.cpm.visible"));

		PluginStart.cleanup.add(() -> BoneAnimator.prototype.channels.delete(COLOR));
		addTranslatedEntry("timeline." + COLOR, "CPM " + I18n.get("label.cpm.recolor"));

		if (Project.format != null && Project.format.id == FORMAT_ID) {
			addedAnimators = true;
			BoneAnimator.prototype.channels.set(VISIBILITY, visCh);
			BoneAnimator.prototype.channels.set(COLOR, colorCh);
		}

		createProperty(Clazz.KEYFRAME_DATA, Type.BOOLEAN, "cpm_visible", I18n.get("label.cpm.visible"), true, false).condition.method = a -> {
			KeyframeDataPoint point = Js.uncheckedCast(a);
			return point.keyframe.channel == VISIBILITY;
		};

		createProperty(Clazz.KEYFRAME_DATA, Type.BOOLEAN, "cpm_color_picker_place", "Color Picker", false, false).condition.method = a -> {
			KeyframeDataPoint point = Js.uncheckedCast(a);
			return point.keyframe.channel == COLOR;
		};

		PluginStart.addEventListener("update_selection", dt -> {
			if(Global.getFormat() == CPMCodec.format) {
				if(Outliner.selected.length == 1 && Outliner.selected[0] instanceof Cube) {
					BBActions.glowButton.value = ((Cube)Outliner.selected[0]).glow;
					BBActions.glowButton.updateEnabledState();
				}
				if(Project.selected_groups.length > 0) {
					BBActions.hiddenButton.value = Project.selected_groups.getAt(0).hidden;
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

	public static Property createProperty(Clazz clz, Type type, String id, String label, @DoNotAutobox Object def, boolean hidden) {
		Property p = Property.createProperty(clz, type, id, label, def, formatCPM(), hidden);
		PluginStart.cleanup.add(p::delete);
		return p;
	}

	public static void addTranslatedEntry(String key, String value) {
		Global.getLang().set(key, value);
		PluginStart.cleanup.add(() -> Global.getLang().delete(key));
	}

	public static Condition formatCPM() {
		Condition condition = new Action.Condition();
		condition.formats = new String[] {FORMAT_ID};
		return condition;
	}

	public static ConditionMethod notCPM() {
		return c -> !FORMAT_ID.equals(Global.getFormat().id);
	}
}
