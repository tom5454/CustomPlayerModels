package com.tom.cpm.blockbench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.blockbench.convert.BlockbenchImport;
import com.tom.cpm.blockbench.convert.BlockbenchImport.UVMul;
import com.tom.cpm.blockbench.convert.ProjectConvert;
import com.tom.cpm.blockbench.ee.EmbeddedEditorHandler;
import com.tom.cpm.blockbench.format.AnimationWizard;
import com.tom.cpm.blockbench.format.CPMCodec;
import com.tom.cpm.blockbench.proxy.Action;
import com.tom.cpm.blockbench.proxy.Action.Toggle;
import com.tom.cpm.blockbench.proxy.Animation;
import com.tom.cpm.blockbench.proxy.Animation.GeneralAnimator;
import com.tom.cpm.blockbench.proxy.BoneAnimator;
import com.tom.cpm.blockbench.proxy.Canvas;
import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.proxy.Cube.CubeProperties;
import com.tom.cpm.blockbench.proxy.Group;
import com.tom.cpm.blockbench.proxy.Group.GroupProperties;
import com.tom.cpm.blockbench.proxy.Interface;
import com.tom.cpm.blockbench.proxy.Keyframe;
import com.tom.cpm.blockbench.proxy.KeyframeDataPoint;
import com.tom.cpm.blockbench.proxy.MenuBar;
import com.tom.cpm.blockbench.proxy.MenuBar.BarItem;
import com.tom.cpm.blockbench.proxy.MenuBar.BarMenu;
import com.tom.cpm.blockbench.proxy.MenuBar.BarMenuInit;
import com.tom.cpm.blockbench.proxy.Modes;
import com.tom.cpm.blockbench.proxy.Outliner;
import com.tom.cpm.blockbench.proxy.OutlinerElement;
import com.tom.cpm.blockbench.proxy.Plugin;
import com.tom.cpm.blockbench.proxy.Plugin.Plugins;
import com.tom.cpm.blockbench.proxy.Project;
import com.tom.cpm.blockbench.proxy.Texture;
import com.tom.cpm.blockbench.proxy.Texture.TextureProperties;
import com.tom.cpm.blockbench.proxy.Timeline;
import com.tom.cpm.blockbench.proxy.Toolbars;
import com.tom.cpm.blockbench.proxy.Undo;
import com.tom.cpm.blockbench.proxy.Undo.UndoData;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec2;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec3;
import com.tom.cpm.blockbench.proxy.jq.JQueryNode;
import com.tom.cpm.blockbench.proxy.jq.JQueryNode.SpectrumInit;
import com.tom.cpm.blockbench.util.ConditionUtil;
import com.tom.cpm.blockbench.util.PopupDialogs;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.editor.elements.RootGroups;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.PlayerPartValues;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.DirectPartValues;
import com.tom.cpm.shared.model.render.DirectParts;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.web.client.resources.Resources;
import com.tom.cpm.web.client.util.I18n;
import com.tom.ugwt.client.JsArrayE;

import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

public class BBActions {
	public static Toggle glowButton, hiddenButton;

	public static void load() {
		List<Object> cpmMenu = new ArrayList<>();
		{
			Action.ActionProperties a = new Action.ActionProperties();
			a.name = I18n.get("bb-button.openCPMProject");
			a.description = "";
			a.icon = "icon-player";
			a.category = "file";
			a.click = e -> ProjectConvert.open();
			Action importAct = new Action("import_cpmproject", a);
			MenuBar.addAction(importAct, "file.5");
			PluginStart.cleanup.add(importAct::delete);
		}

		{
			Action.ActionProperties a = new Action.ActionProperties();
			a.name = I18n.get("bb-button.exportCPMProject");
			a.description = "";
			a.icon = "icon-player";
			a.category = "file";
			a.click = e -> CPMCodec.codec.export();
			Action exportAct = new Action("export_cpmproject", a);
			MenuBar.addAction(exportAct, "file.export");
			PluginStart.cleanup.add(exportAct::delete);
		}

		{
			Action.ActionProperties a = new Action.ActionProperties();
			a.name = I18n.get("bb-button.openEmbeddedEditor");
			a.description = "";
			a.icon = "icon-player";
			a.category = "file";
			a.click = e -> {
				EmbeddedEditorHandler.open().catch_(ex -> {
					PopupDialogs.displayError("Failed to open Embedded Editor:", ex);
					return null;
				});
			};

			Action openCPM = new Action("open_cpm", a);
			MenuBar.addAction(openCPM, "file");
			PluginStart.cleanup.add(openCPM::delete);
		}

		if(MinecraftObjectHolder.DEBUGGING) {
			Action.ActionProperties a = new Action.ActionProperties();
			a.icon = "refresh";
			a.name = "Reload CPM Plugin";
			a.click = e -> Arrays.stream(Plugins.all).filter(p -> p.id.equals(System.getProperty("cpm.pluginId"))).findFirst().ifPresent(Plugin::reload);
			Action reload = new Action("reload_cpm", a);
			MenuBar.addAction(reload, "file");
			PluginStart.cleanup.add(reload::delete);
		}

		{
			MenuBar.SubMenu m = new MenuBar.SubMenu();
			m.name = I18n.get("button.cpm.edit.parts");
			m.icon = "icon-player";
			m.id = "cpm_add_parts";
			List<Object> parts = new ArrayList<>();
			{
				Action.ActionProperties a = new Action.ActionProperties();
				a.name = I18n.get("button.cpm.tools.add_skin_layer2");
				a.icon = "fa-shirt";
				a.click = e -> add2ndLayer();
				Action act = new Action("cpm_add_2nd_layer", a);
				PluginStart.cleanup.add(act::delete);
				parts.add(act);
			}
			RootGroups.forEach(c -> {
				Action.ActionProperties a = new Action.ActionProperties();
				a.name = I18n.get("button.cpm.root_group." + c.name().toLowerCase(Locale.ROOT));
				a.icon = BBGroups.getGroup(c).icon;
				a.click = e -> addRoot(c);
				Action act = new Action("cpm_add_" + c.name().toLowerCase(), a);
				PluginStart.cleanup.add(act::delete);
				parts.add(act);
			});
			{
				Action.ActionProperties a = new Action.ActionProperties();
				a.name = I18n.get("button.cpm.root_group.itemHoldPos");
				a.icon = "back_hand";
				a.click = e -> addSlots(ItemSlot.SLOTS);
				Action act = new Action("cpm_add_items", a);
				PluginStart.cleanup.add(act::delete);
				parts.add(act);
			}
			{
				Action.ActionProperties a = new Action.ActionProperties();
				a.name = I18n.get("button.cpm.root_group.parrots");
				a.icon = "fa-dove";
				a.click = e -> addSlots(ItemSlot.PARROTS);
				Action act = new Action("cpm_add_parrots", a);
				PluginStart.cleanup.add(act::delete);
				parts.add(act);
			}
			m.children = parts.toArray();
			cpmMenu.add(m);
		}

		cpmMenu.add("_");

		{
			Action.ToggleProperties a = new Action.ToggleProperties();
			a.name = I18n.get("label.cpm.glow");
			a.description = "";
			a.condition = new Action.Condition();
			a.condition.method = c -> Outliner.selected.length == 1 && Outliner.selected[0] instanceof Cube;
			a.onChange = v -> {
				if(Outliner.selected.length == 1 && Outliner.selected[0] instanceof Cube) {
					Cube cube = (Cube) Outliner.selected[0];
					Undo.initEdit(UndoData.make(cube));
					cube.glow = v;
					//Cube.preview_controller.updateGeometry(cube);
					Undo.finishEdit(I18n.format("action.cpm.switch", I18n.get("label.cpm.glow")), UndoData.make(cube));
				}
			};
			glowButton = new Toggle("cpm_glow", a);
			cpmMenu.add(glowButton);
			PluginStart.cleanup.add(glowButton::delete);
		}

		{
			Action.ToggleProperties a = new Action.ToggleProperties();
			a.name = I18n.get("label.cpm.hidden_effect");
			a.description = "";
			a.condition = new Action.Condition();
			a.condition.method = c -> Project.selected_groups.length > 0;
			a.onChange = v -> {
				if(Project.selected_groups.length > 0) {
					Undo.initEdit(UndoData.make(Project.selected_groups.array()));
					Project.selected_groups.forEach(g -> g.hidden = v);
					Undo.finishEdit(I18n.format("action.cpm.switch", I18n.get("label.cpm.hidden_effect")), UndoData.make(Project.selected_groups.array()));
				}
			};
			hiddenButton = new Toggle("cpm_hidden", a);
			cpmMenu.add(hiddenButton);
			PluginStart.cleanup.add(hiddenButton::delete);
		}

		cpmMenu.add("_");

		{
			Action.ActionProperties a = new Action.ActionProperties();
			a.name = I18n.get("bb-button.viewInEmbeddedEditor");
			a.icon = "launch";
			a.click = e -> {
				ProjectConvert.convertToCPM().then(b -> b.arrayBuffer()).then(dt -> {
					return EmbeddedEditorHandler.open().then(ee -> ee.openProject(dt));
				}).catch_(ex -> {
					PopupDialogs.displayError(I18n.get("bb-label.error.export"), ex);
					return null;
				});
			};
			Action act = new Action("cpm_view_in_embedded", a);
			PluginStart.cleanup.add(act::delete);
			cpmMenu.add(act);
		}

		BarMenuInit bmi = new BarMenuInit();
		bmi.condition = CPMCodec.formatCPM();
		bmi.name = "CPM";
		new BarMenu("cpm", cpmMenu.toArray(), bmi);
		PluginStart.cleanup.add(() -> MenuBar.menus.delete("cpm"));

		Action openCPM;
		{
			Action.ActionProperties a = new Action.ActionProperties();
			a.icon = "fa-plus-circle";
			a.name = I18n.get("bb-button.newAnimation");
			a.category = "animation";
			a.condition = CPMCodec.formatCPM();
			a.click = e -> AnimationWizard.open(null, null);
			openCPM = new Action("cpm_animation_wizard", a);
			PluginStart.cleanup.add(openCPM::delete);
		}

		boolean add = true;
		for (int i = 0; i < Toolbars.animations.children.length; i++) {
			BarItem bi = Toolbars.animations.children[i];
			if(bi.id.equals("add_animation")) {
				ConditionUtil.and(bi.condition, a -> bi.condition = a, CPMCodec.notCPM());
			} else if(bi.id.equals("cpm_animation_wizard")) {
				add = false;
				Toolbars.animations.children[i] = openCPM;
			}
		}
		if(add)
			Toolbars.animations.add(openCPM, "0");

		PluginStart.cleanup.add(() -> {
			for (int i = 0; i < Toolbars.animations.children.length; i++) {
				BarItem bi = Toolbars.animations.children[i];
				if(bi.id.equals("cpm_animation_wizard")) {
					JsArrayE<BarItem> ar = Js.cast(Toolbars.animations.children);
					ar.splice(i, 1);
					break;
				}
			}
		});

		Animation.menu.structure.forEach(v -> {
			if(!(v instanceof String)) {
				Action ac = Js.uncheckedCast(v);
				if(ac.name != null && ac.name.equals("menu.animation.properties")) {
					ConditionUtil.and(ac.condition, a -> ac.condition = a, CPMCodec.notCPM());
				}
			}
		});
		{
			Action.ActionProperties a = new Action.ActionProperties();
			a.category = "animation";
			a.icon = "list";
			a.name = "menu.animation.properties";
			a.click = e -> AnimationWizard.open(Js.uncheckedCast(e), null);
			a.condition = CPMCodec.formatCPM();
			Animation.menu.structure.push(a);
			PluginStart.cleanup.add(() -> {
				Animation.menu.structure.remove(a);
			});
		}

		PluginStart.addEventListener("update_keyframe_selection", __ -> {
			JQueryNode v = JQueryNode.jq("#keyframe_bar_cpm_visible");
			if(v.length != 0) {
				JQueryNode cb = v.find("#keyframe_bar_cpm_visible_cb");
				if(cb.length == 0) {
					HTMLDivElement el = (HTMLDivElement) v.getAt(0);
					HTMLInputElement inputElement = (HTMLInputElement) el.querySelector("input[type=text]");
					HTMLInputElement checkboxElement = (HTMLInputElement) DomGlobal.document.createElement("input");
					checkboxElement.type = "checkbox";
					checkboxElement.checked = "true".equalsIgnoreCase(inputElement.value);
					checkboxElement.id = "keyframe_bar_cpm_visible_cb";
					checkboxElement.addEventListener("change", event -> {
						inputElement.value = Boolean.toString(checkboxElement.checked);
						inputElement.dispatchEvent(new Event("input"));
					});
					inputElement.style.display = "none";
					el.append(checkboxElement);
				} else {
					HTMLInputElement inputElement = (HTMLInputElement) v.find("input[type=text]").getAt(0);
					HTMLInputElement checkboxElement = (HTMLInputElement) cb.getAt(0);
					checkboxElement.checked = "true".equalsIgnoreCase(inputElement.value);
				}
			}
			v = JQueryNode.jq("#keyframe_bar_cpm_color_picker_place");
			if(v.length != 0) {
				KeyframeDataPoint kdp = Keyframe.selected.getAt(0).data_points.getAt(0);
				SpectrumInit si = new SpectrumInit();
				si.preferredFormat = "hex";
				si.showAlpha = false;
				si.showInput = true;
				si.color = String.format("#%02X%02X%02X", (int) kdp.x, (int) kdp.y, (int) kdp.z);
				si.change = si.hide = si.move = ci -> {
					String c = ci.toHexString();
					kdp.x = Integer.valueOf(c.substring(1, 3), 16);
					kdp.y = Integer.valueOf(c.substring(3, 5), 16);
					kdp.z = Integer.valueOf(c.substring(5, 7), 16);
				};
				v.find("input[type=text]").spectrum(si);

				JQueryNode.jq("#keyframe_bar_x > label").text("R");
				JQueryNode.jq("#keyframe_bar_y > label").text("G");
				JQueryNode.jq("#keyframe_bar_z > label").text("B");
			} else {
				JQueryNode.jq("#keyframe_bar_x > label").text("X");
				JQueryNode.jq("#keyframe_bar_y > label").text("Y");
				JQueryNode.jq("#keyframe_bar_z > label").text("Z");
			}
		});

		PluginStart.addEventListener("display_animation_frame", __ -> {
			if (Project.format == CPMCodec.format) {
				if (Modes.animate != null) {
					Map<String, BoneAnimator> anims = new HashMap<>();
					Arrays.stream(Animation.all).
					filter(a -> a.playing && !(a.loop.equals("once") && Timeline.time > a.length && a.length > 0)).
					sorted(Comparator.comparingInt(Animation::getPriority)).forEach(a -> {
						a.animators.forEach(k -> {
							GeneralAnimator ga = a.animators.get(k);
							if(ga instanceof BoneAnimator) {
								BoneAnimator ba = (BoneAnimator) ga;
								if(!ba.hasVisible())return;
								anims.put(k, ba);
							}
						});
					});

					for (int i = 0; i < Group.all.length; i++) {
						Group g = Group.all[i];
						if (g.parent == Group.ROOT)
							g.currentVisible = true;
						else
							g.currentVisible = Animation.interpolateVisible(anims.get(g.uuid), Timeline.time, !g.hidden);

						/*if (g.copyTransform != null && !g.copyTransform.isEmpty()) {//TODO
							JsonMap s = JsonUtil.fromJson(g.copyTransform);
							Group from = Group.uuids.get((String) s.get("uuid"));
							if (from != null) {
								Vec3f addPos = new Vec3f();
								Vec3f addRot = new Vec3f();
								if(from.parent == Group.ROOT) {
									VanillaModelPart part = from.parent.getRootType();
									if(part != null) {

									}
								}
								if(s.getBoolean("px", false))g.mesh.position.x = from.mesh.position.x + addPos.x;
								if(s.getBoolean("py", false))g.mesh.position.y = from.mesh.position.y - addPos.y;
								if(s.getBoolean("pz", false))g.mesh.position.z = from.mesh.position.z - addPos.z;
								if(s.getBoolean("rx", false))g.mesh.rotation.x = from.mesh.rotation.x + (float) Math.toRadians(addRot.x);
								if(s.getBoolean("ry", false))g.mesh.rotation.y = from.mesh.rotation.y + (float) Math.toRadians(addRot.y);
								if(s.getBoolean("rz", false))g.mesh.rotation.z = from.mesh.rotation.z - (float) Math.toRadians(addRot.z);
								if(s.getBoolean("sx", false))g.mesh.scale.x = from.mesh.scale.x;
								if(s.getBoolean("sy", false))g.mesh.scale.y = from.mesh.scale.y;
								if(s.getBoolean("sz", false))g.mesh.scale.z = from.mesh.scale.z;
								if(s.getBoolean("cv", false))g.currentVisible = from.currentVisible;
							}
						}*/
					}

					for (int i = 0; i < Cube.all.length; i++) {
						Cube c = Cube.all.getAt(i);
						if(!c.animatorInit) {
							c.animatorInit = true;
							c.defaultVisible = c.visibility;
						}
					}

					for (int i = 0; i < Group.all.length; i++) {
						Group g = Group.all[i];
						if(g.parent != Group.ROOT)continue;
						g.applyVisible(true);
					}
				} else {
					for (int i = 0; i < Group.all.length; i++) {
						Group g = Group.all[i];
						if(g.animatorInit) {
							g.animatorInit = false;
							g.visibility = g.defaultVisible;
						}
					}

					for (int i = 0; i < Cube.all.length; i++) {
						Cube c = Cube.all.getAt(i);
						if(c.animatorInit) {
							c.animatorInit = false;
							c.visibility = c.defaultVisible;
						}
					}
				}
				Canvas.updateVisibility();
			}
		});

		Interface.updateInterface();
	}

	private static Group findPart(VanillaModelPart p) {
		for(Group g : Group.all) {
			if(g.parent == Group.ROOT && g.name.equals(p.getName())) {
				return g;
			}
		}
		return null;
	}

	private static void addSlots(ItemSlot[] slots) {
		UndoData udt = new UndoData();
		udt.elements = new OutlinerElement[0];
		Undo.initEdit(udt);
		List<OutlinerElement> newParts = new ArrayList<>();

		for (ItemSlot itemSlot : slots) {
			GroupProperties grp = new GroupProperties();
			grp.name = I18n.format("label.cpm.elem.item." + itemSlot.name().toLowerCase(Locale.ROOT));
			Group gr = new Group(grp);

			gr.getData().setItemRenderer(itemSlot.name().toLowerCase()).flush();

			Group parent = null;
			switch (itemSlot) {
			case HEAD:
				parent = findPart(PlayerModelParts.HEAD);
				break;
			case LEFT_HAND:
				parent = findPart(PlayerModelParts.LEFT_ARM);
				break;
			case RIGHT_HAND:
				parent = findPart(PlayerModelParts.RIGHT_ARM);
				break;
			case RIGHT_SHOULDER:
			case LEFT_SHOULDER:
				parent = findPart(PlayerModelParts.BODY);
				break;
			default:
				break;
			}
			if(parent != null) {
				gr.origin = parent.origin;
				gr.init().addTo(parent);
			}
		}

		udt = new UndoData();
		udt.elements = newParts.toArray(new OutlinerElement[0]);
		Undo.finishEdit(I18n.format("action.cpm.add", I18n.format("action.cpm.itemHold")), udt);
	}

	private static void add2ndLayer() {
		UndoData udt = new UndoData();
		udt.elements = new OutlinerElement[0];
		Undo.initEdit(udt);
		List<OutlinerElement> newParts = new ArrayList<>();
		Texture tex = null;
		for(Texture t : Texture.all.array()) {
			if(t.name.equals("skin")) {
				tex = t;
				break;
			}
		}
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			if(p == PlayerModelParts.CUSTOM_PART)continue;
			Group gr = null;
			for(Group g : Group.all) {
				if(g.parent == Group.ROOT && g.name.equals(p.getName())) {
					gr = g;
					break;
				}
			}
			if(gr == null)continue;
			PlayerPartValues pv = PlayerPartValues.getFor(p, SkinType.DEFAULT);
			CubeProperties cp = new CubeProperties();
			cp.name = I18n.format("label.cpm.layer_" + pv.layer.getLowerName());
			cp.origin = gr.origin;
			cp.from = JsVec3.make(
					gr.origin.x - pv.getOffset().x - pv.getSize().x,
					gr.origin.y - pv.getOffset().y - pv.getSize().y,
					gr.origin.z + pv.getOffset().z);
			Cube cube = new Cube(cp);
			cp.to = JsVec3.make(cube.from.x + pv.getSize().x, cube.from.y + pv.getSize().y, cube.from.z + pv.getSize().z);
			cube.extend(cp);
			cube.inflate = 0.25F;
			cube.box_uv = true;
			cube.uv_offset = JsVec2.make(pv.u2, pv.v2);
			cube.addTo(gr).init();
			newParts.add(cube);
			cube.applyTexture(tex, true);
		}

		udt = new UndoData();
		udt.elements = newParts.toArray(new OutlinerElement[0]);
		Undo.finishEdit(I18n.format("button.cpm.tools.add_skin_layer2"), udt);
	}

	private static void addRoot(RootGroups c) {
		UndoData udt = new UndoData();
		udt.elements = new OutlinerElement[0];
		udt.textures = new Texture[0];
		Undo.initEdit(udt);
		List<Texture> newTex = new ArrayList<>();
		List<OutlinerElement> newParts = new ArrayList<>();
		for(RootModelType r : c.types) {
			if(Arrays.stream(Group.all).anyMatch(g -> g.parent == Group.ROOT && g.name.equals(r.getName())))continue;
			GroupProperties grp = new GroupProperties();
			grp.name = r.getName();
			PartValues pv = DirectParts.getPartOverrides(r, SkinType.DEFAULT);
			grp.origin = JsVec3.make(-pv.getPos().x, 24 - pv.getPos().y, pv.getPos().z);
			if(pv instanceof DirectPartValues) {
				DirectPartValues bp = (DirectPartValues) pv;
				grp.rotation = JsVec3.make(-bp.getRotation().x, -bp.getRotation().y, bp.getRotation().z);
			}
			Group gr = new Group(grp).init();
			gr.isOpen = true;
			CubeProperties cp = new CubeProperties();
			cp.name = r.getName();
			cp.origin = gr.origin;
			cp.from = JsVec3.make(
					gr.origin.x - pv.getOffset().x - pv.getSize().x,
					gr.origin.y - pv.getOffset().y - pv.getSize().y,
					gr.origin.z + pv.getOffset().z);
			TextureSheetType tst = c.getTexSheet(r);
			Cube cube = new Cube(cp);
			cube.mirror_uv = pv.isMirror();
			cp = new CubeProperties();
			cp.to = JsVec3.make(cube.from.x + pv.getSize().x, cube.from.y + pv.getSize().y, cube.from.z + pv.getSize().z);
			cube.extend(cp);
			cube.inflate = pv.getMCScale();
			Texture tex = null;
			for(Texture t : Texture.all.array()) {
				if(t.name.equals(tst.name().toLowerCase())) {
					tex = t;
					break;
				}
			}
			if(tex == null) {
				TextureProperties txp = new TextureProperties();
				txp.mode = "bitmap";
				txp.name = tst.name().toLowerCase();
				tex = new Texture(txp);
				tex.fromDataURL("data:image/png;base64," + Resources.getResource("assets/cpm/textures/template/" + tst.name().toLowerCase(Locale.ROOT) + ".png"));
				tex.add(false);
				newTex.add(tex);
			}
			makeCubeTex(cube, pv.getUV(), tst == TextureSheetType.SKIN ? new Vec2i(Project.texture_width, Project.texture_height) : tst.getDefSize());
			cube.addTo(gr).init();
			newParts.add(cube);
			cube.applyTexture(tex, true);
		}
		udt = new UndoData();
		udt.elements = newParts.toArray(new OutlinerElement[0]);
		udt.textures = newTex.toArray(new Texture[0]);
		Undo.finishEdit(I18n.format("action.cpm.add", I18n.format("action.cpm.root")), udt);
	}

	private static void makeCubeTex(Cube cube, Vec2i uv, Vec2i sheet) {
		cube.uv_offset = JsVec2.make(uv);
		if(sheet.x != Project.texture_width || sheet.y != Project.texture_height) {
			cube.box_uv = false;
			UVMul m = new UVMul();
			m.x = Project.texture_width / (float) sheet.x;
			m.y = Project.texture_height / (float) sheet.y;
			BlockbenchImport.boxToPFUV(cube, 1, 1, 1, 1, m);
		} else {
			cube.box_uv = true;
		}
	}

	public static enum BBGroups {
		CAPE(RootGroups.CAPE, "icon-player"),
		ELYTRA(RootGroups.ELYTRA, "fa-feather-alt"),
		ARMOR(RootGroups.ARMOR, "icon-armor_stand_small"),
		;
		public static final BBGroups[] VALUES = values();
		public final RootGroups group;
		public final String icon;

		private BBGroups(RootGroups group, String icon) {
			this.group = group;
			this.icon = icon;
		}

		public static BBGroups getGroup(RootGroups group) {
			for (int i = 0; i < VALUES.length; i++) {
				BBGroups g = VALUES[i];
				if(g.group == group)return g;
			}
			return null;
		}
	}
}
