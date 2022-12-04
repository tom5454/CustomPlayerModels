package com.tom.cpm.shared.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.ComboSlider;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.InputPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.NamedElement;
import com.tom.cpl.util.NamedElement.NameMapper;
import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.Gesture;
import com.tom.cpm.shared.animation.IManualGesture;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.definition.ModelDefinition;

public class PropertiesPopup extends PopupPanel {
	private ConfigEntry prop, current;
	private final ModelDefinition def;
	private List<Runnable> updateState;

	public PropertiesPopup(IGui gui, int h, ModelDefinition def) {
		super(gui);
		this.def = def;

		updateState = new ArrayList<>();
		List<Profile> ps;
		Profile sel;
		if(def.getAnimations().getProfileId() != null) {
			prop = ModConfig.getCommonConfig().getEntry(ConfigKeys.MODEL_PROPERTIES).getEntry(def.getAnimations().getProfileId());
			ps = prop.getEntry(ConfigKeys.MODEL_PROPERTIES_PROFILES).keySet().stream().map(Profile::new).collect(Collectors.toList());
			ps.add(new Profile(null));
			current = prop.getEntry(ConfigKeys.MODEL_PROPERTIES_VALUES);
			String s = prop.getString(ConfigKeys.MODEL_PROPERTIES_SELECTED, null);
			sel = s == null ? null : ps.stream().filter(p -> s.equals(p.name)).findFirst().orElse(null);
		} else {
			ps = Collections.emptyList();
			sel = null;
		}

		DropDownBox<Profile> profileBox = new DropDownBox<>(gui.getFrame(), ps);
		profileBox.setBounds(new Box(5, 5, 160, 20));
		if(sel != null)profileBox.setSelected(sel);
		addElement(profileBox);

		Button save = new Button(gui, gui.i18nFormat("button.cpm.file.save"), () -> {
			Profile p = profileBox.getSelected();
			if(p != null) {
				if(p.name == null) {
					new InputPopup(gui.getFrame(), gui.i18nFormat("label.cpm.name"), n -> {
						if (prop.getEntry(ConfigKeys.MODEL_PROPERTIES_PROFILES).hasEntry(n))
							ConfirmPopup.confirm(gui.getFrame(), gui.i18nFormat("label.cpm.overwriteProfile"), () -> saveProfile(n));
						else
							saveProfile(n);
					}, null).run();
				} else {
					ConfirmPopup.confirm(gui.getFrame(), gui.i18nFormat("label.cpm.overwriteProfile"), () -> saveProfile(p.name));
				}
			}
		});
		save.setBounds(new Box(5, 30, 50, 20));
		addElement(save);

		Button load = new Button(gui, gui.i18nFormat("button.cpm.file.load"), () -> {
			Profile p = profileBox.getSelected();
			if(p != null && p.name != null) {
				ConfirmPopup.confirm(gui.getFrame(), gui.i18nFormat("label.cpm.loadProfile", p.name), () -> loadProfile(p.name));
			}
		});
		load.setBounds(new Box(60, 30, 50, 20));
		addElement(load);

		Button del = new Button(gui, gui.i18nFormat("button.cpm.delete"), ConfirmPopup.confirmHandler(gui.getFrame(), gui.i18nFormat("label.cpm.confirmDel"), () -> {
			Profile p = profileBox.getSelected();
			if(p != null && p.name != null) {
				ps.remove(p);
				prop.getEntry(ConfigKeys.MODEL_PROPERTIES_PROFILES).clearValue(p.name);
				profileBox.setSelected(null);
			}
		}));
		del.setBounds(new Box(115, 30, 50, 20));
		addElement(del);

		ScrollPanel scp = new ScrollPanel(gui);
		scp.setBounds(new Box(5, 55, 160, h - 60));
		addElement(scp);

		Panel panel = new Panel(gui);
		FlowLayout layout = new FlowLayout(panel, 5, 1);
		scp.setDisplay(panel);
		panel.setBounds(new Box(0, 0, 160, 0));

		Map<String, Group> groups = new HashMap<>();
		def.getAnimations().getGestures().values().stream().
		filter(Gesture::isProperty).sorted(Comparator.comparingInt(IManualGesture::getOrder)).
		forEach(g -> {
			if(g.type == AnimationType.LAYER) {
				if(g.group != null) {
					groups.computeIfAbsent(g.group, k -> new Group(k, panel)).add(g);
					return;
				}
				Checkbox chbx = new Checkbox(gui, g.name);
				chbx.setBounds(new Box(5, 0, 150, 20));
				chbx.setAction(() -> {
					chbx.setSelected(!chbx.isSelected());
					setToggleGesture(g, chbx.isSelected());
				});
				updateState.add(() -> chbx.setSelected(MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getGestureValue(def.getAnimations(), g) != 0));
				panel.addElement(chbx);
			} else if(g.type == AnimationType.VALUE_LAYER) {
				ComboSlider s = new ComboSlider(gui, null, null, null);
				s.setBounds(new Box(5, 0, 150, 20));
				s.getSpinner().setDp(0);
				s.setAction(() -> {
					MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().setLayerValue(def.getAnimations(), g, s.getValue());
					current.setFloat(g.name, s.getValue());
				});
				updateState.add(() -> s.setValue(Byte.toUnsignedInt(MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getGestureValue(def.getAnimations(), g)) / 255f));
				panel.addElement(s);
			}
		});
		if(panel.getElements().isEmpty()) {
			panel.addElement(new Label(gui, gui.i18nFormat("label.cpm.nothing_here")).setBounds(new Box(5, 5, 100, 10)));
		} else {
			updateState.forEach(Runnable::run);
		}
		if(panel.getElements().isEmpty() || def.getAnimations().getProfileId() == null) {
			profileBox.setEnabled(false);
			save.setEnabled(false);
			load.setEnabled(false);
			del.setEnabled(false);
		}

		layout.reflow();
		setBounds(new Box(0, 0, 170, h));
	}

	private void saveProfile(String name) {
		ConfigEntry ce = prop.getEntry(ConfigKeys.MODEL_PROPERTIES_PROFILES).getEntry(name);
		ce.clear();
		current.keySet().stream().map(k -> Pair.of(k, current.getFloat(k, 0))).forEach(p -> ce.setFloat(p.getKey(), p.getValue()));
		prop.setString(ConfigKeys.MODEL_PROPERTIES_SELECTED, name);
		ModConfig.getCommonConfig().save();
	}

	private void loadProfile(String name) {
		ConfigEntry ce = prop.getEntry(ConfigKeys.MODEL_PROPERTIES_PROFILES).getEntry(name);
		current.clear();
		ce.keySet().stream().map(k -> Pair.of(k, ce.getFloat(k, 0))).forEach(p -> current.setFloat(p.getKey(), p.getValue()));
		prop.setString(ConfigKeys.MODEL_PROPERTIES_SELECTED, name);
		ModConfig.getCommonConfig().save();
		def.getAnimations().getGestures().values().stream().
		filter(Gesture::isProperty).sorted(Comparator.comparingInt(IManualGesture::getOrder)).
		forEach(g -> {
			MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().setLayerValue(def.getAnimations(), g, current.getFloat(g.name, g.defVal / 255f));
		});
		updateState.forEach(Runnable::run);
	}

	private void setToggleGesture(Gesture g, boolean v) {
		MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().setLayerValue(def.getAnimations(), g, v ? 1 : 0);
		current.setFloat(g.name, v ? 1 : 0);
	}

	@Override
	public void onClosed() {
		ModConfig.getCommonConfig().save();
		super.onClosed();
	}

	private class Group {
		private NameMapper<Gesture> groupGestures;
		private List<Gesture> list;
		private DropDownBox<NamedElement<Gesture>> box;

		public Group(String name, Panel panel) {
			list = new ArrayList<>();
			groupGestures = new NameMapper<>(list, g -> g == null ? gui.i18nFormat("label.cpm.layerNone") : g.name);
			box = new DropDownBox<>(gui.getFrame(), groupGestures.asList());
			groupGestures.setSetter(box::setSelected);

			Panel p = new Panel(gui);
			p.setBounds(new Box(5, 0, 150, 20));
			panel.addElement(p);

			p.addElement(new Label(gui, name).setBounds(new Box(0, 12, 70, 10)));
			p.addElement(box);
			box.setBounds(new Box(70, 0, 80, 20));
			box.setAction(() -> {
				Gesture s = box.getSelected().getElem();
				list.forEach(g -> setToggleGesture(g, g == s));
			});
			updateState.add(this::update);
		}

		public void add(Gesture g) {
			list.add(g);
		}

		public void update() {
			groupGestures.refreshValues();
			Gesture s = list.stream().filter(g -> MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getGestureValue(def.getAnimations(), g) != 0).findFirst().orElse(null);
			groupGestures.setValue(s);
		}
	}

	private class Profile implements Comparable<Profile> {
		private String name;

		public Profile(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name != null ? name : gui.i18nFormat("label.cpm.new_profile");
		}

		@Override
		public int compareTo(Profile o) {
			if(o.name == name)return 0;
			if(name == null && o.name != null)return -1;
			if(o.name == null && name != null)return 1;
			return name.compareTo(o.name);
		}
	}
}
