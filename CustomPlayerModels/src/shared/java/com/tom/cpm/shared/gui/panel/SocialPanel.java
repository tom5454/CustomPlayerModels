package com.tom.cpm.shared.gui.panel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.gui.util.HorizontalLayout;
import com.tom.cpl.gui.util.TabbedPanelManager;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.config.SocialConfig;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.SocialPlayerPanel.SafetyPopup;
import com.tom.cpm.shared.skin.TextureProvider;

public class SocialPanel extends Panel {
	private final Frame frm;
	private TabbedPanelManager tabs;
	public HorizontalLayout topPanel;
	private UUID selUUID;
	private ScrollPanel scrollPlayer;
	private ViewportCamera cam;
	private ListPanel<PlayerInServer> playerList;
	private int listWidth, scrollWidth;
	private List<TextureProvider> vanillaSkins = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public SocialPanel(Frame frm, int width, int height, ViewportCamera cam, UUID selected, boolean isInServer) {
		super(frm.getGui());
		this.frm = frm;
		this.cam = cam;
		setBounds(new Box(0, 0, width, height));

		tabs = new TabbedPanelManager(gui);
		tabs.setBounds(new Box(0, 20, width, height - 20));
		addElement(tabs);

		Panel topPanel = new Panel(gui);
		topPanel.setBounds(new Box(0, 0, width, 20));
		topPanel.setBackgroundColor(gui.getColors().menu_bar_background);
		addElement(topPanel);
		this.topPanel = new HorizontalLayout(topPanel);

		if(isInServer) {
			Panel inServer = new Panel(gui);
			addTab("inServer", inServer, 0);

			List<Player<?>> loadedPlayers = MinecraftClientAccess.get().getDefinitionLoader().getPlayers();
			Set<PlayerInServer> pls = loadedPlayers.stream().map(PlayerInServer::new).collect(Collectors.toSet());
			MinecraftClientAccess.get().getPlayers().stream().map(PlayerInServer::new).forEach(pls::add);
			List<PlayerInServer> players = pls.stream().filter(PlayerInServer::isOtherPlayer).distinct().
					sorted(Comparator.comparing(PlayerInServer::toString)).
					collect(Collectors.toList());

			listWidth = width / 4;
			scrollWidth = width - listWidth;

			scrollPlayer = new ScrollPanel(gui);
			scrollPlayer.setBounds(new Box(listWidth, 0, scrollWidth, height - 20));
			scrollPlayer.setVisible(false);

			playerList = new ListPanel<>(gui, players, width, height - 20);
			playerList.setSelect(this::setPlayerPanel);
			playerList.setGetTooltip(PlayerInServer::getTooltip);
			playerList.setBounds(new Box(0, 0, width, height - 20));
			playerList.setWidth(width);
			playerList.setBackgroundColor(gui.getColors().popup_background & 0x80FFFFFF);
			inServer.addElement(playerList);

			if(selected != null) {
				selUUID = selected;
				players.stream().filter(p -> p.getUUID().equals(selUUID)).findFirst().ifPresent(this::setPlayerPanel);
			}

			inServer.addElement(scrollPlayer);
		}

		Panel friends = new Panel(gui);
		addTab("friends", friends, 0);
		initList(friends, ModConfig.getCommonConfig().getEntry(ConfigKeys.FRIEND_LIST));

		Panel blocked = new Panel(gui);
		addTab("blocked", blocked, 0);
		initList(blocked, ModConfig.getCommonConfig().getEntry(ConfigKeys.BLOCKED_LIST));
	}

	private void setPlayerPanel(PlayerInServer p) {
		if(p != null) {
			playerList.setWidth(listWidth);
			selUUID = p.getUUID();
			if(scrollPlayer.getDisplay() != null)((SocialPlayerPanel)scrollPlayer.getDisplay()).cleanup();
			scrollPlayer.setDisplay(new SocialPlayerPanel(frm, p.getPlayer(), cam, scrollWidth, bounds.h - 20, () -> setPlayerPanel(p)));
			MinecraftClientAccess.get().getNetHandler().requestPlayerData(selUUID);
		} else {
			selUUID = null;
			playerList.setWidth(bounds.w);
		}
		scrollPlayer.setVisible(p != null);
	}

	@SuppressWarnings("unchecked")
	private class PlayerInServer {
		private Object gp;
		private UUID uuid;
		private String unique;

		private PlayerInServer(Object gp) {
			this.gp = gp;
			uuid = MinecraftClientAccess.get().getDefinitionLoader().getGP_UUID(gp);
			if(uuid == null)uuid = UUID.randomUUID();
			unique = ModelDefinitionLoader.PLAYER_UNIQUE;
		}

		private PlayerInServer(Player<?> pl) {
			this(pl.getGameProfile());
			unique = pl.unique;
		}

		public Player<?> getPlayer() {
			return MinecraftClientAccess.get().getDefinitionLoader().loadPlayer(gp, unique);
		}

		public UUID getUUID() {
			return uuid;
		}

		private boolean isOtherPlayer() {
			return !getUUID().equals(MinecraftClientAccess.get().getCurrentClientPlayer().getUUID());
		}

		@Override
		public String toString() {
			String name = getName();
			if(name.length() > 16)name = gui.i18nFormat("label.cpm.shortName", name.substring(0, 15));
			return name;
		}

		private String getName() {
			String name = MinecraftClientAccess.get().getDefinitionLoader().getGP_Name(gp);
			if(name == null)return gui.i18nFormat("label.cpm.unknown");
			return name;
		}

		private Tooltip getTooltip() {
			String type = "player";
			if(unique != null) {
				if(unique.equals(ModelDefinitionLoader.SKULL_UNIQUE) || unique.startsWith("skull_tex:"))type = "skull";
				else if(unique.startsWith("model:"))type = "model";
			}
			return new Tooltip(frm, gui.i18nFormat("tooltip.cpm.playerUUID", getName(), getUUID().toString(),
					gui.i18nFormat("label.cpm.modelLoadingType." + type)));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((unique == null) ? 0 : unique.hashCode());
			result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			PlayerInServer other = (PlayerInServer) obj;
			if (unique == null) {
				if (other.unique != null) return false;
			} else if (!unique.equals(other.unique)) return false;
			if (uuid == null) {
				if (other.uuid != null) return false;
			} else if (!uuid.equals(other.uuid)) return false;
			return true;
		}
	}

	private class Pl {
		private String uuid, name;

		public Pl(String uuid, ConfigEntry ce) {
			this.uuid = uuid;
			name = ce.getEntry(uuid).getString(ConfigKeys.NAME, uuid);
		}

		public String getName() {
			return name;
		}

		public String getUUID() {
			return uuid;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private void initList(Panel panel, ConfigEntry ce) {
		int w = panel.getBounds().w;
		int h = panel.getBounds().h;

		panel.setBackgroundColor(gui.getColors().popup_background & 0x80FFFFFF);
		Panel listPanel = new Panel(gui);
		listPanel.setBackgroundColor(gui.getColors().menu_bar_background);

		List<Pl> players = ce.keySet().stream().map(u -> new Pl(u, ce)).sorted(Comparator.comparing(Pl::getName)).collect(Collectors.toList());
		int lw = w / 4;
		int spw = w - lw - 10;

		ScrollPanel scpS = new ScrollPanel(gui);
		scpS.setBounds(new Box(lw + 10, 0, spw, h));
		scpS.setVisible(false);

		ListPanel<Pl> playerList = new ListPanel<>(gui, players, w, h);
		playerList.setSelect(p -> {
			if(p != null) {
				playerList.setWidth(lw);
				scpS.setDisplay(new PlayerInfoPanel(p, spw - 5, h));
			} else {
				playerList.setWidth(w - 10);
			}
			scpS.setVisible(p != null);
		});
		playerList.setBounds(new Box(0, 0, w, h));
		panel.addElement(playerList);
		panel.addElement(scpS);
	}

	private class PlayerInfoPanel extends Panel {

		public PlayerInfoPanel(Pl pl, int w, int h) {
			super(SocialPanel.this.getGui());
			setBounds(new Box(0, 0, w, h));
			FlowLayout layout = new FlowLayout(this, 5, 1);

			Panel padding = new Panel(gui);
			padding.setBounds(new Box(0, 0, w, 5));
			addElement(padding);

			addElement(new Label(gui, gui.i18nFormat("label.cpm.sSettingsFor", pl.getName())).setBounds(new Box(5, 0, w, 10)));

			Button friend = new Button(gui, gui.i18nFormat(SocialConfig.isFriend(pl.getUUID()) ? "button.cpm.removeFriend" : "button.cpm.addFriend"), null);
			friend.setBounds(new Box(5, 0, w - 5, 20));
			friend.setAction(() -> {
				if(SocialConfig.isFriend(pl.getUUID()))SocialConfig.removeFriend(pl.getUUID());
				else SocialConfig.addFriend(pl.getUUID(), pl.getName());
				friend.setText(gui.i18nFormat(SocialConfig.isFriend(pl.getUUID()) ? "button.cpm.removeFriend" : "button.cpm.addFriend"));
				MinecraftClientAccess.get().getDefinitionLoader().settingsChanged(UUID.fromString(pl.getUUID()));
				ModConfig.getCommonConfig().save();
			});
			addElement(friend);

			Button block = new Button(gui, gui.i18nFormat(SocialConfig.isBlocked(pl.getUUID()) ? "button.cpm.removeBlock" : "button.cpm.addBlock"), null);
			block.setBounds(new Box(5, 0, w - 5, 20));
			block.setAction(() -> {
				if(SocialConfig.isBlocked(pl.getUUID()))SocialConfig.removeBlock(pl.getUUID());
				else SocialConfig.blockPlayer(pl.getUUID(), pl.getName());
				block.setText(gui.i18nFormat(SocialConfig.isBlocked(pl.getUUID()) ? "button.cpm.removeBlock" : "button.cpm.addBlock"));
				MinecraftClientAccess.get().getDefinitionLoader().settingsChanged(UUID.fromString(pl.getUUID()));
				ModConfig.getCommonConfig().save();
			});
			addElement(block);

			Button playerSettings = new Button(gui, gui.i18nFormat("button.cpm.playerSettings"), () -> frm.openPopup(new SafetyPopup(this, pl.getUUID(), pl.getName(), () -> {}, frm)));
			playerSettings.setBounds(new Box(5, 0, w - 5, 20));
			addElement(playerSettings);

			layout.reflow();
		}

	}

	public void addTab(String name, Panel panel, int topPadding) {
		panel.setBounds(new Box(0, topPadding, bounds.w, bounds.h - 20 - topPadding));
		topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.social." + name), panel));
	}

	public UUID getSelectedUUID() {
		return selUUID;
	}

	public void cleanup() {
		if(scrollPlayer.getDisplay() != null)
			((SocialPlayerPanel)scrollPlayer.getDisplay()).cleanup();
		vanillaSkins.forEach(TextureProvider::free);
		vanillaSkins.clear();
	}
}
