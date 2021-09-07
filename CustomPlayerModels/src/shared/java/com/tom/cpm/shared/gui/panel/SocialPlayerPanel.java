package com.tom.cpm.shared.gui.panel;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.config.ConfigEntry.ModConfigFile.ConfigEntryTemp;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.InputPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationHandler;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey.KeyGroup;
import com.tom.cpm.shared.config.SocialConfig;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.ModelDisplayPanel.IModelDisplayPanel;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.skin.TextureType;

public class SocialPlayerPanel extends Panel implements IModelDisplayPanel {
	private Frame frm;
	private ViewportCamera cam;
	private Player<?, ?> player;
	private CompletableFuture<ModelDefinition> def;
	private AnimationHandler animHandler;
	private TextureProvider vanillaSkin;
	private FlowLayout layout;
	private Panel buttonsPanel;
	private Button cloneBtn;
	private boolean cloneLoaded;

	public SocialPlayerPanel(Frame frm, Player<?, ?> player, ViewportCamera cam, int w, int h, Runnable reload) {
		super(frm.getGui());
		this.frm = frm;
		Box parentBox = new Box(0, 0, w, h);
		setBounds(parentBox);
		this.player = player;
		this.cam = cam;
		animHandler = new AnimationHandler(this::getSelectedDefinition);

		String uuid = player.getUUID().toString();
		int s = Math.min(w, h) - 30;
		if(!SocialConfig.isBlocked(uuid) && !gui.isShiftDown()) {
			ModelDisplayPanel modelPanel = new ModelDisplayPanel(gui, this) {
				@Override
				public void draw(MouseEvent event, float partialTicks) {
					gui.drawBox(parentBox.x, parentBox.y, parentBox.w, parentBox.h, gui.getColors().popup_background & 0x80FFFFFF);
					if(def == null) {
						ModelDefinition d = player.getModelDefinition0();
						if(d != null) {
							switch (d.getResolveState()) {
							case CLEANED_UP:
								break;
							case ERRORRED:
								setLoadingText(gui.i18nFormat("label.cpm.errorLoadingModel", d.getError().toString()));
								break;
							case LOADED:
								break;
							case NEW:
							case RESOLVING:
								setLoadingText(gui.i18nFormat("label.cpm.loading"));
								break;
							case SAFETY_BLOCKED:
								setLoadingText(gui.i18nFormat("label.cpm.safetyBlocked"));
								break;
							default:
								break;
							}
						}
					}
					super.draw(event, partialTicks);
				}
			};
			modelPanel.setLoadingText(gui.i18nFormat("label.cpm.loading"));
			modelPanel.setBackgroundColor(0);
			modelPanel.setBounds(new Box(w - s - 5, 5, s, s));
			addElement(modelPanel);

			ModelDefinition def = player.getModelDefinition0();
			if(def == null) {
				this.def = player.getTextures().load().thenCompose(_v -> {
					return player.getTextures().getTexture(TextureType.SKIN).
							thenApply(i -> {
								if(i == null)i = player.getSkinType().getSkinTexture();
								vanillaSkin = new TextureProvider(i, new Vec2i(64, 64));
								return ModelDefinition.createVanilla(() -> vanillaSkin, player.getSkinType());
							});
				});
			}
		}

		String plname = player.getName();
		addElement(new Label(gui, plname != null ? plname : gui.i18nFormat("label.cpm.unknown")).setBounds(new Box(5, 5, 100, 10)));

		int bpw = w - s - 10;
		buttonsPanel = new Panel(gui);
		buttonsPanel.setBounds(new Box(0, 15, bpw, h));
		addElement(buttonsPanel);
		layout = new FlowLayout(buttonsPanel, 5, 1);

		Button friend = new Button(gui, gui.i18nFormat(SocialConfig.isFriend(uuid) ? "button.cpm.removeFriend" : "button.cpm.addFriend"), null);
		friend.setBounds(new Box(5, 0, bpw - 5, 20));
		friend.setAction(() -> {
			if(SocialConfig.isFriend(uuid))SocialConfig.removeFriend(uuid);
			else SocialConfig.addFriend(uuid, player.getName());
			friend.setText(gui.i18nFormat(SocialConfig.isFriend(uuid) ? "button.cpm.removeFriend" : "button.cpm.addFriend"));
			MinecraftClientAccess.get().getDefinitionLoader().settingsChanged(player.getUUID());
			reload.run();
			ModConfig.getCommonConfig().save();
		});
		buttonsPanel.addElement(friend);

		Button block = new Button(gui, gui.i18nFormat(SocialConfig.isBlocked(uuid) ? "button.cpm.removeBlock" : "button.cpm.addBlock"), null);
		block.setBounds(new Box(5, 0, bpw - 5, 20));
		block.setAction(() -> {
			if(SocialConfig.isBlocked(uuid))SocialConfig.removeBlock(uuid);
			else SocialConfig.blockPlayer(uuid, player.getName());
			block.setText(gui.i18nFormat(SocialConfig.isBlocked(uuid) ? "button.cpm.removeBlock" : "button.cpm.addBlock"));
			MinecraftClientAccess.get().getDefinitionLoader().settingsChanged(player.getUUID());
			reload.run();
			ModConfig.getCommonConfig().save();
		});
		buttonsPanel.addElement(block);

		Button playerSettings = new Button(gui, gui.i18nFormat("button.cpm.playerSettings"), () -> frm.openPopup(new SafetyPopup(this, player.getUUID().toString(), player.getName(), reload)));
		playerSettings.setBounds(new Box(5, 0, bpw - 5, 20));
		buttonsPanel.addElement(playerSettings);

		cloneBtn = new Button(gui, gui.i18nFormat("button.cpm.clone"), () -> {
			ModelDefinition d = player.getModelDefinition();
			if(d != null && d.cloneable != null) {
				String name = d.cloneable.name;
				if(name == null) {
					frm.openPopup(new InputPopup(frm, gui.i18nFormat("label.cpm.modelName"), this::clone, null));
				} else {
					clone(name);
				}
			}
		});
		cloneBtn.setBounds(new Box(5, 0, bpw - 5, 20));
		buttonsPanel.addElement(cloneBtn);
		cloneBtn.setVisible(false);

		layout.reflow();
	}

	@SuppressWarnings("unchecked")
	private void clone(String name) {
		MinecraftClientAccess.get().getDefinitionLoader().cloneModel(player, name).thenAcceptAsync(_b -> {
			if((boolean) _b) {
				frm.openPopup(new MessagePopup(frm, gui.i18nFormat("label.cpm.success"), gui.i18nFormat("label.cpm.cloningSuccess", name)));
			} else {
				frm.openPopup(new MessagePopup(frm, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.cloningFailed")));
			}
		}, gui::executeLater);
	}

	@Override
	public ModelDefinition getSelectedDefinition() {
		ModelDefinition d = player.getModelDefinition();
		if(d != null) {
			if(!cloneLoaded) {
				cloneLoaded = true;
				cloneBtn.setVisible(d.cloneable != null);
				layout.reflow();
			}
			return d;
		}
		if(def != null)return def.getNow(null);
		return null;
	}

	@Override
	public ViewportCamera getCamera() {
		return cam;
	}

	@Override
	public void preRender() {
		if(getSelectedDefinition() != null) {
			MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().handleGuiAnimation(animHandler, getSelectedDefinition());
		}
	}

	@Override
	public boolean doRender() {
		return true;
	}

	public static class SafetyPopup extends PopupPanel {
		private String uuid, name;

		protected SafetyPopup(Panel parent, String uuid, String name, Runnable reload) {
			super(parent.getGui());
			Box b = parent.getBounds();
			setBounds(new Box(0, 0, b.w - 30, b.h - 20));
			this.uuid = uuid;
			this.name = name;

			ConfigEntryTemp config = ModConfig.getCommonConfig().createTemp();
			ConfigEntry ce = config.getEntry(ConfigKeys.PLAYER_SETTINGS).getEntry(uuid);
			if(ce.keySet().isEmpty() && name != null)ce.setString(ConfigKeys.NAME, name);

			ScrollPanel scpS = new ScrollPanel(gui);
			scpS.setBounds(new Box(5, 5, bounds.w - 10, bounds.h - 30));
			addElement(scpS);

			SafetyPanel sp = new SafetyPanel(gui, ce, bounds.w - 10, getKeyGroup(), uuid, config);
			scpS.setDisplay(sp);

			Button saveBtn = new Button(gui, gui.i18nFormat("button.cpm.saveCfg"), () -> {
				config.saveConfig();
				MinecraftClientAccess.get().getDefinitionLoader().settingsChanged(UUID.fromString(uuid));
				reload.run();
				close();
			});
			addElement(saveBtn);
			saveBtn.setBounds(new Box(5, bounds.h - 25, 80, 20));
		}

		public KeyGroup getKeyGroup() {
			return SocialConfig.isFriend(uuid) ? KeyGroup.FRIEND : KeyGroup.GLOBAL;
		}

		@Override
		public String getTitle() {
			return gui.i18nFormat("label.cpm.sSettingsFor", name == null ? gui.i18nFormat("label.cpm.unknown") : name);
		}
	}

	public void cleanup() {
		if(vanillaSkin != null)vanillaSkin.free();
	}
}
