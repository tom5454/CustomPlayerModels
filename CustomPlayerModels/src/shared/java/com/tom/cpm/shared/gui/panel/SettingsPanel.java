package com.tom.cpm.shared.gui.panel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.config.ModConfigFile.ConfigEntryTemp;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ButtonIcon;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.InputPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Slider;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.gui.util.HorizontalLayout;
import com.tom.cpl.gui.util.TabbedPanelManager;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.Util;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey.KeyGroup;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.KeybindsPopup;

public class SettingsPanel extends Panel {
	private final Frame frm;
	private TabbedPanelManager tabs, safety;
	private HorizontalLayout topPanel, safetyPanel;
	private ConfigEntryTemp ce;
	private Button buttonPosMode;
	private Runnable save;

	public SettingsPanel(Frame frm, SettingsPanel panel, int width, int height, Runnable save) {
		this(frm, null, width, height, panel == null ? ModConfig.getCommonConfig().createTemp() : panel.ce, save);
		setBackgroundColor(gui.getColors().popup_background & 0x80FFFFFF);
	}

	public SettingsPanel(Frame frm, PopupPanel popup, int width, int height, Runnable save) {
		this(frm, popup, width, height, ModConfig.getCommonConfig().createTemp(), save);
	}

	private SettingsPanel(Frame frm, PopupPanel popup, int width, int height, ConfigEntryTemp ce, Runnable save) {
		super(frm.getGui());
		this.frm = frm;
		this.save = save;
		setBounds(new Box(0, 0, width, height));

		this.ce = ce;

		tabs = new TabbedPanelManager(gui);
		tabs.setBounds(new Box(0, 20, width, height - 50));
		addElement(tabs);

		Button saveBtn = new Button(gui, gui.i18nFormat("button.cpm.saveCfg"), this::saveConfig);
		addElement(saveBtn);
		saveBtn.setBounds(new Box(5, height - 25, 80, 20));

		Panel topPanel = new Panel(gui);
		topPanel.setBounds(new Box(0, 0, width, 20));
		topPanel.setBackgroundColor(gui.getColors().menu_bar_background);
		addElement(topPanel);
		this.topPanel = new HorizontalLayout(topPanel);

		{
			Panel general = new Panel(gui);
			addTabScroll("general", general, 5);

			FlowLayout layout = new FlowLayout(general, 4, 1);
			makeCheckbox(general, ConfigKeys.SHOW_LOADING_INFO, true);

			Button kbButton = new Button(gui, gui.i18nFormat("label.cpm.keybinds.title"), () -> frm.openPopup(new KeybindsPopup(frm, ce)));
			kbButton.setBounds(new Box(5, 0, 150, 20));
			general.addElement(kbButton);

			makeCheckbox(general, ConfigKeys.WIKI_OFFLINE_MODE, false);

			MinecraftClientAccess.get().populatePlatformSettings("general", general);

			layout.reflow();
		}

		{
			Panel editor = new Panel(gui);
			addTabScroll("editor", editor, 5);
			FlowLayout editorLayout = new FlowLayout(editor, 5, 1);

			makeCheckbox(editor, ConfigKeys.TITLE_SCREEN_BUTTON, true);

			if(gui.getMaxScale() != -1) {
				Button guiScale = new Button(gui, gui.i18nFormat("button.cpm.config.scale", getScale()), null);
				guiScale.setAction(() -> {
					int scale = ce.getInt(ConfigKeys.EDITOR_SCALE, -1) + 1;
					if(scale >= gui.getMaxScale()) {
						scale = gui.canScaleVanilla() ? -1 : 0;
					}
					ce.setInt(ConfigKeys.EDITOR_SCALE, scale);
					if(popup != null) {
						try {
							EditorGui.rescaleGui = false;
							popup.close();
							gui.setScale(scale);
							guiScale.setText(gui.i18nFormat("button.cpm.config.scale", getScale()));
							frm.openPopup(popup);
						} finally {
							EditorGui.rescaleGui = true;
						}
					}
				});
				guiScale.setBounds(new Box(5, 0, 250, 20));
				editor.addElement(guiScale);
			}

			makeCheckbox(editor, ConfigKeys.ADV_SCALING_SETTINGS, false);

			buttonPosMode = new Button(gui, "", null);
			buttonPosMode.setAction(() -> {
				ce.setBoolean(ConfigKeys.EDITOR_POSITION_MODE, !ce.getSetBoolean(ConfigKeys.EDITOR_POSITION_MODE, false));
				updatePosModeBtn();
			});
			buttonPosMode.setBounds(new Box(5, 0, 250, 20));
			//editor.addElement(buttonPosMode);
			updatePosModeBtn();

			addAlphaSlider(editor, ConfigKeys.EDITOR_GIZMO_ALPHA, 255);
			addAlphaSlider(editor, ConfigKeys.EDITOR_UV_AREA_ALPHA, 0xcc);
			addAlphaSlider(editor, ConfigKeys.EDITOR_UV_AREA_ALL_ALPHA, 0x55);

			makeCheckbox(editor, ConfigKeys.EDITOR_GIZMO_SCALE, true);
			addScaleSlider(editor, ConfigKeys.EDITOR_GIZMO_SIZE, 1, 0.1f, 0.2f, 2);
			addScaleSlider(editor, ConfigKeys.EDITOR_GIZMO_LENGTH, 1, 0.1f, 0.2f, 2);

			Slider autoSaveSlider = addScaleSlider(editor, ConfigKeys.EDITOR_AUTOSAVE_TIME, 5 * 60, 15f, -1f, 15 * 60);
			autoSaveSlider.setSteps(v -> {
				float val = v * (15 * 60 + 1) - 1;
				int rv = Math.round(val / 15) * 15;
				if(rv <= 0)rv = -1;
				return (rv + 1f) / (15 * 60 + 1);
			});

			MinecraftClientAccess.get().populatePlatformSettings("editor", editor);

			editorLayout.reflow();
		}

		Panel safetyTabPanel = new Panel(gui);
		addTab("safety", safetyTabPanel, 0);

		Panel safetyPanel = new Panel(gui);
		safetyPanel.setBounds(new Box(0, 0, width, 20));
		safetyPanel.setBackgroundColor(gui.getColors().menu_bar_background);
		safetyTabPanel.addElement(safetyPanel);
		this.safetyPanel = new HorizontalLayout(safetyPanel);

		safety = new TabbedPanelManager(gui);
		safety.setBounds(new Box(0, 20, width, height - 40));
		safetyTabPanel.addElement(safety);

		ConfigEntry spfs = ce.getEntry(ConfigKeys.SAFETY_PROFILES);
		SafetyHeaderPanel globalSafety = new SafetyHeaderPanel(frm, ce.getEntry(ConfigKeys.GLOBAL_SETTINGS), width,
				gui.i18nFormat("label.cpm.globalSafetyS"),
				createMap("safetyBtn", spfs), KeyGroup.GLOBAL, ce);
		ScrollPanel scpG = new ScrollPanel(gui);
		scpG.setDisplay(globalSafety);
		addSafetyTab("global", scpG, 5);

		Panel serverList = new Panel(gui);
		addSafetyTab("server", serverList, 5);
		String server = MinecraftClientAccess.get().getConnectedServer();
		buildListPanel(serverList, ce.getEntry(ConfigKeys.SERVER_SETTINGS),
				(server != null ? new Srv(server) : null),
				(k, v) -> new Srv(k, v.getString(ConfigKeys.NAME, Util.hideIp(k))),
				k -> k.ip,
				e -> new Tooltip(frm, gui.isShiftDown() && gui.isAltDown() ? gui.i18nFormat("tooltip.cpm.serverInfo", e.ip) : gui.i18nFormat("tooltip.cpm.displServerInfo")),
				createMap("nameBoxInit", true, "safetyBtn", spfs, "server", true), e -> KeyGroup.GLOBAL, true);

		SafetyHeaderPanel friends = new SafetyHeaderPanel(frm, ce.getEntry(ConfigKeys.FRIEND_SETTINGS), width,
				gui.i18nFormat("label.cpm.friendSafetyS"),
				Collections.emptyMap(), KeyGroup.FRIEND, ce);
		ScrollPanel scpF = new ScrollPanel(gui);
		scpF.setDisplay(friends);
		addSafetyTab("friends", scpF, 5);

		Panel player = new Panel(gui);
		addSafetyTab("player", player, 5);
		buildListPanel(player, ce.getEntry(ConfigKeys.PLAYER_SETTINGS),
				null,
				(k, v) -> new Pl(k, v.getString(ConfigKeys.NAME, gui.i18nFormat("label.cpm.unknown"))),
				k -> k.uuid,
				e -> new Tooltip(frm, gui.i18nFormat("tooltip.cpm.playerInfo", e.name, e.uuid)),
				Collections.emptyMap(), Pl::getKeyGroup, false);

		String v = gui.i18nFormat("label.cpm.runtimeVersion", PlatformFeature.getVersion());
		addElement(new Label(gui, v).setBounds(new Box(width - gui.textWidth(v) - 3, height - 11, 0, 0)));
	}

	private void addAlphaSlider(Panel panel, String key, int def) {
		Panel p = new Panel(gui);
		p.setBounds(new Box(0, 0, 320, 20));
		panel.addElement(p);

		Slider alphaSlider = new Slider(gui, formatAlphaSlider(key, def));
		alphaSlider.setSteps(1 / 255f);
		alphaSlider.setBounds(new Box(5, 0, 250, 20));
		alphaSlider.setAction(() -> {
			ce.setFloat(key, alphaSlider.getValue());
			alphaSlider.setText(formatAlphaSlider(key, def));
		});
		alphaSlider.setValue(ce.getSetFloat(key, def / 255f));
		alphaSlider.setTooltip(new Tooltip(frm, gui.i18nFormat("tooltip.cpm.config." + key)));
		p.addElement(alphaSlider);

		Button btnR = new Button(gui, gui.i18nFormat("button.cpm.settings.reset"), () -> {
			ce.setFloat(key, def / 255f);
			alphaSlider.setValue(def / 255f);
			alphaSlider.setText(formatAlphaSlider(key, def));
		});
		btnR.setBounds(new Box(260, 0, 60, 20));
		p.addElement(btnR);
	}

	private Slider addScaleSlider(Panel panel, String key, float def, float div, float min, float max) {
		Panel p = new Panel(gui);
		p.setBounds(new Box(0, 0, 320, 20));
		panel.addElement(p);

		Slider scaleSlider = new Slider(gui, formatScaleSlider(key, def));
		scaleSlider.setSteps(1 / ((max - min) / div));
		scaleSlider.setBounds(new Box(5, 0, 250, 20));
		scaleSlider.setAction(() -> {
			ce.setFloat(key, scaleSlider.getValue() * (max - min) + min);
			scaleSlider.setText(formatScaleSlider(key, def));
		});
		scaleSlider.setValue((ce.getSetFloat(key, def) - min) / (max - min));
		scaleSlider.setTooltip(new Tooltip(frm, gui.i18nFormat("tooltip.cpm.config." + key)));
		p.addElement(scaleSlider);

		Button btnR = new Button(gui, gui.i18nFormat("button.cpm.settings.reset"), () -> {
			ce.setFloat(key, def);
			scaleSlider.setValue((def - min) / (max - min));
			scaleSlider.setText(formatScaleSlider(key, def));
		});
		btnR.setBounds(new Box(260, 0, 60, 20));
		p.addElement(btnR);
		return scaleSlider;
	}

	private String formatAlphaSlider(String key, int def) {
		float v = ce.getFloat(key, def / 255f);
		return gui.i18nFormat("label.cpm.config." + key, (int) (v * 255));
	}

	private String formatScaleSlider(String key, float def) {
		float v = ce.getFloat(key, def);
		return gui.i18nFormat("label.cpm.config." + key, String.format("%.1f", v));
	}

	private void makeCheckbox(Panel panel, String key, boolean def) {
		Checkbox chxbx = new Checkbox(gui, gui.i18nFormat("label.cpm.config." + key));
		chxbx.setSelected(ce.getSetBoolean(key, def));
		chxbx.setAction(() -> {
			boolean b = !ce.getBoolean(key, def);
			chxbx.setSelected(b);
			ce.setBoolean(key, b);
		});
		chxbx.setBounds(new Box(5, 0, 200, 20));
		chxbx.setTooltip(new Tooltip(frm, gui.i18nFormat("tooltip.cpm.config." + key)));
		panel.addElement(chxbx);
	}

	private void saveConfig() {
		ce.saveConfig();
		MinecraftClientAccess.get().getDefinitionLoader().clearCache();
		save.run();
	}

	private static class Srv {
		private String ip;
		private String name;

		public Srv(String ip, String name) {
			this.ip = ip;
			this.name = name;
		}

		public Srv(String ip) {
			this.ip = ip;
			this.name = Util.hideIp(ip);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private class Pl {
		private String uuid;
		private String name;

		public Pl(String uuid, String name) {
			this.uuid = uuid;
			this.name = name;
		}

		public KeyGroup getKeyGroup() {
			return ce.getEntry(ConfigKeys.FRIEND_LIST).keySet().contains(uuid) ? KeyGroup.FRIEND : KeyGroup.GLOBAL;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public void addTabScroll(String name, Panel panel, int topPadding) {
		ScrollPanel scp = new ScrollPanel(gui);
		scp.setBounds(new Box(0, 0, bounds.w, bounds.h - 50));
		panel.setBounds(new Box(0, 0, bounds.w, 0));
		scp.setDisplay(panel);

		Panel p = new Panel(gui);
		p.addElement(scp);
		addTab(name, p, topPadding);
	}

	public void addTab(String name, Panel panel, int topPadding) {
		panel.setBounds(new Box(0, topPadding, bounds.w, bounds.h - 50));
		topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.settings." + name), panel));
	}

	public void addSafetyTab(String name, Panel panel, int topPadding) {
		panel.setBounds(new Box(0, topPadding, bounds.w, bounds.h - 70));
		safetyPanel.add(safety.createTab(gui.i18nFormat("tab.cpm.settings.safety." + name), panel));
	}

	private String getScale() {
		int scale = ce.getInt(ConfigKeys.EDITOR_SCALE, gui.canScaleVanilla() ? -1 : 0);
		return scale == -1 ? gui.i18nFormat("button.cpm.config.scale.vanilla") : scale == 0 ? gui.i18nFormat("button.cpm.config.scale.auto") : Integer.toString(scale);
	}

	private void updatePosModeBtn() {
		String mode = ce.getBoolean(ConfigKeys.EDITOR_POSITION_MODE, false) ? "absolute" : "relative";
		buttonPosMode.setText(gui.i18nFormat("button.cpm.config.posMode", gui.i18nFormat("button.cpm.config.posMode." + mode)));
		buttonPosMode.setTooltip(new Tooltip(frm, gui.i18nFormat("tooltip.cpm.config.posMode." + mode)));
	}

	private <E> void buildListPanel(Panel panel, ConfigEntry ce, E current, BiFunction<String, ConfigEntry, E> func, Function<E, String> getKey, Function<E, Tooltip> tooltip, Map<String, Object> fieldsIn, Function<E, KeyGroup> getKg, boolean enableAdd) {
		int w = panel.getBounds().w;
		int lw = w / 4;
		int spw = w - lw - 10;

		ScrollPanel scpS = new ScrollPanel(gui);
		scpS.setBounds(new Box(lw + 10, 5, spw, panel.getBounds().h - 10));
		scpS.setVisible(false);

		List<E> entries = ce.keySet().stream().map(k -> func.apply(k, ce.getEntry(k))).collect(Collectors.toList());
		if(current != null && !ce.hasEntry(getKey.apply(current)))entries.add(current);

		ListPanel<E> playerList = new ListPanel<>(gui, entries, w, panel.getBounds().h - 30);
		playerList.setGetTooltip(tooltip);
		Consumer<E> selEvt = s -> {
			if(s != null) {
				playerList.setWidth(lw);
				Map<String, Object> f = new HashMap<>(fieldsIn);
				if(fieldsIn.containsKey("nameBoxInit"))f.put("nameBox", s.toString());
				scpS.setDisplay(new SafetyHeaderPanel(frm, ce.getEntry(getKey.apply(s)), spw,
						gui.i18nFormat("label.cpm.sSettingsFor", s.toString()), f, getKg.apply(s), this.ce));
			} else {
				playerList.setWidth(w);
			}
			scpS.setVisible(s != null);
		};
		playerList.setSelect(selEvt);
		playerList.setBounds(new Box(0, 0, w, panel.getBounds().h - 30));
		playerList.setWidth(w);

		if(current != null) {
			playerList.setSelected(current);
			selEvt.accept(current);
		}

		ButtonIcon newBtn = new ButtonIcon(gui, "editor", 0, 16, () -> {
			frm.openPopup(new InputPopup(frm, gui.i18nFormat("label.cpm.enterServerIP"), key -> {
				if(!ce.keySet().contains(key)) {
					ConfigEntry ne = ce.getEntry(key);
					E newE = func.apply(key, ne);
					entries.add(newE);
					playerList.setSelected(newE);
					selEvt.accept(newE);
					playerList.refreshList();
				}
			}, null));
		});
		newBtn.setEnabled(enableAdd);
		newBtn.setBounds(new Box(5, panel.getBounds().h - 25, 18, 18));

		ButtonIcon delBtn = new ButtonIcon(gui, "editor", 14, 16, () -> {
			E elem = playerList.getSelected();
			if(elem != null) {
				ConfirmPopup.confirm(frm, gui.i18nFormat("label.cpm.confirmDel"), () -> {
					entries.remove(elem);
					ce.clearValue(getKey.apply(elem));
					scpS.setVisible(false);
					playerList.setWidth(w);
					playerList.refreshList();
				});
			}
		});
		delBtn.setBounds(new Box(30, panel.getBounds().h - 25, 18, 18));

		panel.addElement(playerList);
		panel.addElement(newBtn);
		panel.addElement(delBtn);

		panel.addElement(scpS);
	}

	private static Map<String, Object> createMap(Object... dataIn) {
		Map<String, Object> data = new HashMap<>();
		for(int i = 0;i<dataIn.length;i += 2) {
			String key = (String) dataIn[i];
			Object v = dataIn[i + 1];
			data.put(key, v);
		}
		return data;
	}

	public boolean isChanged() {
		return ce.isDirty();
	}

	public void setOpenTab(int tabID) {
		((Button)topPanel.getPanel().getElements().get(tabID)).getAction().run();
	}
}
