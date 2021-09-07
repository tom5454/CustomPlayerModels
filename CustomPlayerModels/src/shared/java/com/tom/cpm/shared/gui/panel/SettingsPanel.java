package com.tom.cpm.shared.gui.panel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.config.ConfigEntry.ModConfigFile.ConfigEntryTemp;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ButtonIcon;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.InputPopup;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.gui.util.HorizontalLayout;
import com.tom.cpl.gui.util.TabbedPanelManager;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey.KeyGroup;
import com.tom.cpm.shared.editor.gui.EditorGui;

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
			addTab("general", general, 5);

			Checkbox chxbxTSBtn = new Checkbox(gui, gui.i18nFormat("label.cpm.config.titleScreenButton"));
			chxbxTSBtn.setSelected(ce.getSetBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true));
			chxbxTSBtn.setAction(() -> {
				boolean b = !ce.getBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, true);
				chxbxTSBtn.setSelected(b);
				ce.setBoolean(ConfigKeys.TITLE_SCREEN_BUTTON, b);
			});
			chxbxTSBtn.setBounds(new Box(5, 0, 200, 20));
			chxbxTSBtn.setTooltip(new Tooltip(frm, gui.i18nFormat("tooltip.cpm.config.titleScreenButton")));
			general.addElement(chxbxTSBtn);
		}

		{
			Panel editor = new Panel(gui);
			addTab("editor", editor, 5);
			FlowLayout editorLayout = new FlowLayout(editor, 5, 1);

			Button buttonRMB = new Button(gui, gui.i18nFormat("button.cpm.config.rotateButton", getRotateBtn()), null);
			buttonRMB.setAction(() -> {
				ce.setInt(ConfigKeys.EDITOR_ROTATE_MOUSE_BUTTON, ce.getSetInt(ConfigKeys.EDITOR_ROTATE_MOUSE_BUTTON, 2) == 2 ? 1 : 2);
				buttonRMB.setText(gui.i18nFormat("button.cpm.config.rotateButton", getRotateBtn()));
			});
			buttonRMB.setBounds(new Box(5, 0, 200, 20));
			editor.addElement(buttonRMB);

			if(gui.getMaxScale() != -1) {
				Button guiScale = new Button(gui, gui.i18nFormat("button.cpm.config.scale", getScale()), null);
				guiScale.setAction(() -> {
					int scale = ce.getInt(ConfigKeys.EDITOR_SCALE, -1) + 1;
					if(scale >= gui.getMaxScale()) {
						scale = -1;
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
				guiScale.setBounds(new Box(5, 0, 200, 20));
				editor.addElement(guiScale);
			}

			buttonPosMode = new Button(gui, "", null);
			buttonPosMode.setAction(() -> {
				ce.setBoolean(ConfigKeys.EDITOR_POSITION_MODE, !ce.getSetBoolean(ConfigKeys.EDITOR_POSITION_MODE, false));
				updatePosModeBtn();
			});
			buttonPosMode.setBounds(new Box(5, 0, 200, 20));
			//editor.addElement(buttonPosMode);
			updatePosModeBtn();

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
				(k, v) -> new Srv(k, v.getString(ConfigKeys.NAME, hideIp(k))),
				k -> k.ip,
				e -> new Tooltip(frm, gui.isShiftDown() && gui.isAltDown() ? gui.i18nFormat("tooltip.cpm.serverInfo", e.ip) : gui.i18nFormat("tooltip.cpm.displServerInfo")),
				createMap("nameBoxInit", true, "safetyBtn", spfs), e -> KeyGroup.GLOBAL, true);

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
				(k, v) -> new Pl(UUID.fromString(k), v.getString(ConfigKeys.NAME, gui.i18nFormat("label.cpm.unknown"))),
				k -> k.uuid.toString(),
				e -> new Tooltip(frm, gui.i18nFormat("tooltip.cpm.playerInfo", e.name, e.uuid.toString())),
				Collections.emptyMap(), Pl::getKeyGroup, false);
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
			this.name = hideIp(ip);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private class Pl {
		private UUID uuid;
		private String name;

		public Pl(UUID uuid, String name) {
			this.uuid = uuid;
			this.name = name;
		}

		public KeyGroup getKeyGroup() {
			return ce.getEntry(ConfigKeys.FRIEND_LIST).keySet().contains(uuid.toString()) ? KeyGroup.FRIEND : KeyGroup.GLOBAL;
		}

		@Override
		public String toString() {
			return name;
		}
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
		int scale = ce.getInt(ConfigKeys.EDITOR_SCALE, -1);
		return scale == -1 ? gui.i18nFormat("button.cpm.config.scale.vanilla") : scale == 0 ? gui.i18nFormat("button.cpm.config.scale.auto") : Integer.toString(scale);
	}

	private String getRotateBtn() {
		return gui.i18nFormat("button.cpm.config.rotateButton." + ce.getInt(ConfigKeys.EDITOR_ROTATE_MOUSE_BUTTON, 2));
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
						gui.i18nFormat("label.cpm.sSettingsFor", s.toString()), f, getKg.apply(s), ce));
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
				}
			}, null));
		});
		newBtn.setEnabled(enableAdd);
		newBtn.setBounds(new Box(5, panel.getBounds().h - 25, 18, 18));

		ButtonIcon delBtn = new ButtonIcon(gui, "editor", 14, 16, () -> {
			E elem = playerList.getSelected();
			if(elem != null) {
				frm.openPopup(new ConfirmPopup(frm, gui.i18nFormat("label.cpm.confirmDel"), () -> {
					entries.remove(elem);
					ce.clearValue(getKey.apply(elem));
					scpS.setVisible(false);
					playerList.setWidth(w);
				}, null));
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

	public static String hideIp(String ip) {
		ip = ip.replaceAll("[0-9]", "*");
		int cnt = 0;
		StringBuilder bb = new StringBuilder();
		for(int i = 0;i<ip.length();i++) {
			char c = ip.charAt(i);
			if(c == '.' || c == ':')cnt = -1;
			if(cnt++ > 1) {
				c = '*';
			}
			bb.append(c);
		}
		ip = bb.toString();
		return ip;
	}
}
