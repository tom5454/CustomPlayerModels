package com.tom.cpm.shared.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.InputPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.gui.gesture.GestureGuiButtons;
import com.tom.cpm.shared.gui.gesture.IGestureButtonContainer;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;

public class PropertiesPopup extends PopupPanel implements IGestureButtonContainer {
	private ConfigEntry prop, current;
	private final ModelDefinition def;
	private List<Runnable> updateState;
	private List<Profile> ps;
	private DropDownBox<Profile> profileBox;
	private Profile createNew;

	public PropertiesPopup(IGui gui, int h, ModelDefinition def) {
		super(gui);
		this.def = def;

		updateState = new ArrayList<>();
		Profile sel;
		if(def.getAnimations().getProfileId() != null) {
			prop = ModConfig.getCommonConfig().getEntry(ConfigKeys.MODEL_PROPERTIES).getEntry(def.getAnimations().getProfileId());
			ps = prop.getEntry(ConfigKeys.MODEL_PROPERTIES_PROFILES).keySet().stream().map(Profile::new).collect(Collectors.toList());
			ps.add(createNew = new Profile(false));
			ps.add(new Profile(true));
			current = prop.getEntry(ConfigKeys.MODEL_PROPERTIES_VALUES);
			String s = prop.getString(ConfigKeys.MODEL_PROPERTIES_SELECTED, null);
			sel = s == null ? null : ps.stream().filter(p -> s.equals(p.name)).findFirst().orElse(null);
		} else {
			ps = Collections.emptyList();
			sel = null;
		}

		profileBox = new DropDownBox<>(gui.getFrame(), ps);
		profileBox.setBounds(new Box(5, 5, 160, 20));
		if(sel != null)profileBox.setSelected(sel);
		addElement(profileBox);

		Button save = new Button(gui, gui.i18nFormat("button.cpm.file.save"), () -> {
			Profile p = profileBox.getSelected();
			if(p != null) {
				if(p.name == null) {
					new InputPopup(gui.getFrame(), gui.i18nFormat("label.cpm.name"), n -> {
						if (prop.getEntry(ConfigKeys.MODEL_PROPERTIES_PROFILES).hasEntry(n)) {
							ConfirmPopup.confirm(gui.getFrame(), gui.i18nFormat("label.cpm.overwriteProfile"), () -> saveProfile(n));
						} else {
							Profile pr = new Profile(n);
							saveProfile(n);
							ps.add(pr);
							profileBox.setSelected(pr);
						}
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
			if(p != null) {
				if (p.name != null) {
					ConfirmPopup.confirm(gui.getFrame(), gui.i18nFormat("label.cpm.loadProfile", p.name), () -> loadProfile(p.name));
				} else if (p.reset) {
					ConfirmPopup.confirm(gui.getFrame(), gui.i18nFormat("label.cpm.loadProfile", p.toString()), this::resetProfile);
				}
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

		def.getAnimations().getNamedActions().stream().
		filter(d -> d.isProperty() && d.canShow()).
		map(t -> GestureGuiButtons.make(this, t)).
		filter(e -> e != null).
		forEach(g -> {
			g.setBounds(new Box(5, 0, 150, 20));
			panel.addElement(g);
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
		def.getAnimations().getNamedActions().stream().
		filter(AbstractGestureButtonData::isProperty).
		forEach(g -> g.storeTo(ce));
		prop.setString(ConfigKeys.MODEL_PROPERTIES_SELECTED, name);
		ModConfig.getCommonConfig().save();
	}

	private void resetProfile() {
		current.clear();
		prop.clearValue(ConfigKeys.MODEL_PROPERTIES_SELECTED);
		def.getAnimations().getNamedActions().stream().
		filter(AbstractGestureButtonData::isProperty).
		forEach(g -> g.loadFrom(current));//Load default
		ModConfig.getCommonConfig().save();
		updateState.forEach(Runnable::run);
	}

	private void loadProfile(String name) {
		ConfigEntry ce = prop.getEntry(ConfigKeys.MODEL_PROPERTIES_PROFILES).getEntry(name);
		current.clear();
		prop.setString(ConfigKeys.MODEL_PROPERTIES_SELECTED, name);
		ModConfig.getCommonConfig().save();
		def.getAnimations().getNamedActions().stream().
		filter(AbstractGestureButtonData::isProperty).
		forEach(g -> g.loadFrom(ce));
		updateState.forEach(Runnable::run);
	}

	@Override
	public void onClosed() {
		def.getAnimations().getNamedActions().stream().
		filter(AbstractGestureButtonData::isProperty).
		forEach(g -> g.storeTo(current));
		ModConfig.getCommonConfig().save();
		super.onClosed();
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.modelProperties");
	}

	private class Profile implements Comparable<Profile> {
		private String name;
		private boolean reset;

		public Profile(boolean reset) {
			this.name = null;
			this.reset = reset;
		}

		public Profile(String name) {
			this.name = name;
			this.reset = false;
		}

		@Override
		public String toString() {
			return name != null ? name : (reset ? gui.i18nFormat("label.cpm.reset_profile") : gui.i18nFormat("label.cpm.new_profile"));
		}

		@Override
		public int compareTo(Profile o) {
			if(o.name == name)return 0;
			if(name == null && o.name != null)return -1;
			if(o.name == null && name != null)return 1;
			return name.compareTo(o.name);
		}
	}

	@Override
	public IGui gui() {
		return gui;
	}

	@Override
	public void updateKeybind(String keybind, String id, boolean mode) {
	}

	@Override
	public BoundKeyInfo getBoundKey(String id) {
		return null;
	}

	@Override
	public void valueChanged() {
		if (profileBox.getSelected() != null && profileBox.getSelected().reset) {
			profileBox.setSelected(createNew);
		}
	}

	@Override
	public boolean canBindKeys() {
		return false;
	}
}
