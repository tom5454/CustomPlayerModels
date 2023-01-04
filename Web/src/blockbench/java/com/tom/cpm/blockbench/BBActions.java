package com.tom.cpm.blockbench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.tom.cpm.blockbench.proxy.Action;
import com.tom.cpm.blockbench.proxy.Action.Toggle;
import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.proxy.Group;
import com.tom.cpm.blockbench.proxy.Interface;
import com.tom.cpm.blockbench.proxy.MenuBar;
import com.tom.cpm.blockbench.proxy.MenuBar.BarMenu;
import com.tom.cpm.blockbench.proxy.MenuBar.BarMenuInit;
import com.tom.cpm.blockbench.proxy.Outliner;
import com.tom.cpm.blockbench.proxy.Plugin;
import com.tom.cpm.blockbench.proxy.Plugin.Plugins;
import com.tom.cpm.blockbench.proxy.Undo;
import com.tom.cpm.blockbench.proxy.Undo.UndoData;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.editor.elements.RootGroups;
import com.tom.cpm.web.client.util.I18n;

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
			a.click = e -> EmbeddedEditor.open();
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

		{
			MenuBar.SubMenu m = new MenuBar.SubMenu();
			m.name = I18n.get("button.cpm.edit.parts");
			m.icon = "icon-player";
			m.id = "cpm_add_parts";
			List<Object> parts = new ArrayList<>();
			RootGroups.forEach(c -> {
				Action.ActionProperties a = new Action.ActionProperties();
				a.name = I18n.get("button.cpm.root_group." + c.name().toLowerCase(Locale.ROOT));
				a.icon = "view_in_ar";
				a.click = e -> {

				};
				Action act = new Action("cpm_add_" + c.name().toLowerCase(), a);
				PluginStart.cleanup.add(act::delete);
				parts.add(act);
			});
			{
				Action.ActionProperties a = new Action.ActionProperties();
				a.name = I18n.get("button.cpm.root_group.itemHoldPos");
				a.icon = "back_hand";
				a.click = e -> {

				};
				Action act = new Action("cpm_add_items", a);
				PluginStart.cleanup.add(act::delete);
				parts.add(act);
			}
			{
				Action.ActionProperties a = new Action.ActionProperties();
				a.name = I18n.get("button.cpm.root_group.parrots");
				a.icon = "fa-dove";
				a.click = e -> {

				};
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
					Undo.initEdit(UndoData.make(Outliner.selected[0]));
					((Cube)Outliner.selected[0]).glow = v;
					Undo.finishEdit(I18n.format("action.cpm.switch", I18n.get("label.cpm.glow")), UndoData.make(Outliner.selected[0]));
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
			a.condition.method = c -> Outliner.selected.length == 1 && Outliner.selected[0] instanceof Group;
			a.onChange = v -> {
				if(Outliner.selected.length == 1 && Outliner.selected[0] instanceof Group) {
					Undo.initEdit(UndoData.make(Outliner.selected[0]));
					((Group)Outliner.selected[0]).hidden = v;
					Undo.finishEdit(I18n.format("action.cpm.switch", I18n.get("label.cpm.hidden_effect")), UndoData.make(Outliner.selected[0]));
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
				EmbeddedEditor.setOpenListener(ed -> {
					new BlockbenchExport(ed).doExport().then(__ -> {
						ed.refreshCaches();
						return null;
					});
				});
				EmbeddedEditor.open();
			};
			Action act = new Action("cpm_view_in_embedded", a);
			PluginStart.cleanup.add(act::delete);
			cpmMenu.add(act);
		}

		BarMenuInit bmi = new BarMenuInit();
		bmi.condition = PluginStart.formatCPM();
		bmi.name = "CPM";
		new BarMenu("cpm", cpmMenu.toArray(), bmi);
		PluginStart.cleanup.add(() -> MenuBar.menus.delete("cpm"));

		/*Action openCPM;
		{
			Action.ActionProperties a = new Action.ActionProperties();
			a.icon = "fa-plus-circle";
			a.name = I18n.get("bb-button.newAnimation");
			a.category = "animation";
			a.condition = PluginStart.formatCPM();
			openCPM = new Action("cpm_animation_wizard", a);
			PluginStart.cleanup.add(openCPM::delete);
		}

		boolean add = true;
		for(BarItem bi : Toolbars.animations.children) {
			if(bi.id.equals("add_animation")) {
				PluginStart.cleanup.add(new FieldReplace<>(bi.condition, a -> a.method, (a, b) -> a.method = b, c -> Global.getFormat() != CPMCodec.format, (a, b) -> c -> (a.check(c) && b.check(c))));
			} else if(bi.id.equals("cpm_animation_wizard")) {
				add = false;
			}
		}
		if(add)
			Toolbars.animations.add(openCPM, "0");*/

		Interface.updateInterface();
	}
}
