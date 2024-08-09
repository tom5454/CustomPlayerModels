package com.tom.cpm.shared.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.config.ConfigChangeRequest;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinition.ModelLoadingState;
import com.tom.cpm.shared.definition.SafetyException.BlockReason;
import com.tom.cpm.shared.editor.TestIngameManager;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.gui.FirstPersonHandPosGui;
import com.tom.cpm.shared.gui.gesture.GestureGuiButtons;
import com.tom.cpm.shared.gui.gesture.IGestureButton;
import com.tom.cpm.shared.gui.gesture.IGestureButtonContainer;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.network.ServerCaps;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.Log;

public class GestureGui extends Frame implements IGestureButtonContainer {
	private Panel contentPanel;
	private int tickCounter;
	private ModelDefinition def;
	private ModelLoadingState state;
	private List<IGestureButton> buttons = new ArrayList<>();

	public GestureGui(IGui gui) {
		super(gui);
		gui.setCloseListener(r -> {
			ModConfig.getCommonConfig().save();
			r.run();
		});
		tickCounter = 3;
	}

	private ConfigEntry getEntryForModel(boolean make) {
		return AnimationEngine.getEntryForModel(def, make);
	}

	@Override
	public void updateKeybind(String keybind, String gesture, boolean mode) {
		ConfigEntry ce = getEntryForModel(true);
		for(int j = 1;j<=IKeybind.QUICK_ACCESS_KEYBINDS_COUNT;j++) {
			String c = ce.getString("qa_" + j, null);
			if(c != null && c.equals(gesture)) {
				ce.setString("qa_" + j, "");
			}
		}
		ce.setString(keybind, gesture);
		ce.setString(keybind + "_mode", mode ? "hold" : "press");

		this.buttons.forEach(IGestureButton::updateKeybinds);
	}

	@Override
	public void tick() {
		if(tickCounter > 0) {
			tickCounter--;
			if(TestIngameManager.isTesting() && gui.isShiftDown()) {
				MinecraftClientAccess.get().openGui(EditorGui::new);
				return;
			}
			if(TestIngameManager.isTesting() && gui.isCtrlDown()) {
				MinecraftClientAccess.get().openGui(FirstPersonHandPosGui::new);
				return;
			}

			List<ConfigChangeRequest<?, ?>> changes = MinecraftClientAccess.get().getNetHandler().getRecommendedSettingChanges();
			String server = MinecraftClientAccess.get().getConnectedServer();
			if(server != null && !changes.isEmpty()) {
				openPopup(new RecommendSafetySettingsPopup(gui, server, changes));
				tickCounter = 0;
			}
		}
	}

	@Override
	public void initFrame(int width, int height) {
		contentPanel = new Panel(gui);
		contentPanel.setBounds(new Box(0, 0, width, height));
		def = MinecraftClientAccess.get().getCurrentClientPlayer().getModelDefinition0();
		initContent(contentPanel);
		addElement(contentPanel);
	}

	private void initContent(Panel p) {
		int width = p.getBounds().w;
		int height = p.getBounds().h;
		Log.debug(def);
		if(def != null)
			this.state = def.getResolveState();
		buttons.clear();
		ServerStatus status = MinecraftClientAccess.get().getServerSideStatus();
		if(status == ServerStatus.OFFLINE) {
			String str = "How did you get here?";
			Label lbl = new Label(gui, str);
			lbl.setBounds(new Box(width / 2 - gui.textWidth(str) / 2, height / 2 - 4, 0, 0));
			p.addElement(lbl);
			return;
		}
		List<String> keys = new ArrayList<>();
		{
			int i = 0;
			for(IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
				if(kb.getName().startsWith("qa")) {
					i++;
					String k = kb.getBoundKey();
					if(k.isEmpty())k = gui.i18nFormat("label.cpm.key_unbound");
					keys.add(gui.i18nFormat("label.cpm.quick_key_bound", i, k));
				}
			}
			i++;
			keys.add(gui.i18nFormat("label.cpm.quick_key_info"));
		}
		if((bounds.w - 360) / 2 > 100) {
			String tt = gui.i18nFormat("tooltip.cpm.gestureQuickAccess");
			Panel qk = new Panel(gui);
			int i = 0;
			int mw = 0;
			for (String k : keys) {
				i++;
				qk.addElement(new Label(gui, k).setBounds(new Box(0, i * 10, 0, 10)));
				mw = Math.max(gui.textWidth(k), mw);
			}
			i++;
			qk.setBounds(new Box(10, 0, 200, i * 10));

			Label lbl = new Label(gui, "");
			tt += "\\" + gui.i18nFormat("tooltip.cpm.gesture.valueReset", Keybinds.RESET_VALUE_LAYER.getSetKey(gui));
			lbl.setTooltip(new Tooltip(this, tt));
			qk.addElement(lbl.setBounds(new Box(0, 0, 200, i * 10)));
			p.addElement(qk);
		} else {
			StringBuilder tt = new StringBuilder();
			for (String key : keys) {
				tt.append(key);
				tt.append('\\');
			}

			tt.append(gui.i18nFormat("label.cpm.quick_key_info"));
			tt.append("\\\\");
			tt.append(gui.i18nFormat("tooltip.cpm.gestureQuickAccess"));
			tt.append("\\");
			tt.append(gui.i18nFormat("tooltip.cpm.gesture.valueReset", Keybinds.RESET_VALUE_LAYER.getSetKey(gui)));
			String text = gui.i18nFormat("label.cpm.quick_key_short");
			Label lbl = new Label(gui, text);
			lbl.setTooltip(new Tooltip(this, tt.toString()));
			p.addElement(lbl.setBounds(new Box(10, 10, gui.textWidth(text), 10)));
		}
		int h;
		if(def != null && this.state == ModelLoadingState.LOADED && status != ServerStatus.UNAVAILABLE) {
			List<GuiElement> buttons = def.getAnimations().getNamedActions().stream().
					filter(d -> !d.isProperty() && d.canShow()).map(t -> GestureGuiButtons.make(this, t)).
					filter(e -> e != null).collect(Collectors.toList());

			Panel panel = new Panel(gui);

			if (buttons.isEmpty()) {
				String str = gui.i18nFormat("label.cpm.nothing_here");
				Label lbl = new Label(gui, str);
				lbl.setBounds(new Box(width / 2 - gui.textWidth(str) / 2, height / 2 - 4, 0, 0));
				p.addElement(lbl);
				h = 10;
			} else {
				h = (buttons.size() / 4 + 1) * 40;

				for (int j = 0; j < buttons.size(); j++) {
					GuiElement b = buttons.get(j);
					panel.addElement(b);
					b.setBounds(new Box((j % 4) * 90, (j / 4) * 40, 80, 30));
					this.buttons.add((IGestureButton) b);//Enforced in GestureGuiButtons.make
				}

				if(h < height - 150) {
					panel.setBounds(new Box(width / 2 - 180, height / 2 - h / 2, 360, h));
					p.addElement(panel);
				} else {
					panel.setBounds(new Box(0, 0, 360, h));
					ScrollPanel scp = new ScrollPanel(gui);
					h = height - 120;
					scp.setBounds(new Box(width / 2 - 180, 60, 363, height - 150));
					scp.setDisplay(panel);
					p.addElement(scp);
				}

				this.buttons.forEach(IGestureButton::updateKeybinds);
			}
		} else if(def != null && (this.state == ModelLoadingState.ERRORRED || this.state == ModelLoadingState.SAFETY_BLOCKED)) {
			String txt = "";
			switch (this.state) {
			case ERRORRED:
				txt = gui.i18nFormat("label.cpm.errorLoadingModel", def.getError().toString());
				break;

			case SAFETY_BLOCKED:
				txt = gui.i18nFormat("label.cpm.safetyBlocked");
				break;

			default:
				break;
			}
			if(def.getBlockReason() == BlockReason.UUID_LOCK)txt = gui.i18nFormat("label.cpm.uuidBlocked");
			p.addElement(new Label(gui, txt).setBounds(new Box(width / 2 - gui.textWidth(txt) / 2, height / 2 - 4, 0, 0)));
			txt = gui.i18nFormat("label.cpm.checkErrorLog");
			p.addElement(new Label(gui, txt).setBounds(new Box(width / 2 - gui.textWidth(txt) / 2, height / 2 - 4 + 10, 0, 0)));
			h = 20;
		} else if(status == ServerStatus.UNAVAILABLE) {
			String str = gui.i18nFormat("label.cpm.feature_unavailable");
			Label lbl = new Label(gui, str);
			lbl.setBounds(new Box(width / 2 - gui.textWidth(str) / 2, height / 2 - 4, 0, 0));
			p.addElement(lbl);
			h = 10;
		} else {
			String str = gui.i18nFormat("label.cpm.nothing_here");
			Label lbl = new Label(gui, str);
			lbl.setBounds(new Box(width / 2 - gui.textWidth(str) / 2, height / 2 - 4, 0, 0));
			p.addElement(lbl);
			h = 10;
		}

		Panel btnPanel = new Panel(gui);
		btnPanel.setBounds(new Box(width / 2 - 180, height / 2 - h / 2 - 30, 360, 20));
		p.addElement(btnPanel);

		if(status != ServerStatus.UNAVAILABLE) {
			Button btnRstG = new Button(gui, gui.i18nFormat("button.cpm.anim_reset_gesture"), this::clearGesture);
			btnRstG.setBounds(new Box(0, 0, 100, 20));
			btnPanel.addElement(btnRstG);

			Button btnRstP = new Button(gui, gui.i18nFormat("button.cpm.anim_reset_pose"), this::clearPose);
			btnRstP.setBounds(new Box(110, 0, 100, 20));
			btnPanel.addElement(btnRstP);
		}

		Checkbox chbxNames = new Checkbox(gui, gui.i18nFormat("button.cpm.render_name"));
		chbxNames.setBounds(new Box(220, 0, 70, 20));
		chbxNames.setSelected(Player.isEnableNames());
		chbxNames.setAction(() -> {
			boolean v = !chbxNames.isSelected();
			chbxNames.setSelected(v);
			Player.setEnableNames(v);
		});
		btnPanel.addElement(chbxNames);

		Panel btnPanel2 = new Panel(gui);
		btnPanel2.setBounds(new Box(width - 162, 0, 160, 0));
		p.addElement(btnPanel2);

		FlowLayout fl = new FlowLayout(btnPanel2, 0, 1);

		Button btnModelProperties = new Button(gui, gui.i18nFormat("button.cpm.modelProperties"), () -> MinecraftClientAccess.get().openGui(ModelPropertiesGui::new));
		btnModelProperties.setBounds(new Box(0, 0, 160, 20));
		if(status != ServerStatus.INSTALLED || !MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES)) {
			btnModelProperties.setEnabled(false);
			btnModelProperties.setTooltip(new Tooltip(this, gui.i18nFormat("label.cpm.feature_unavailable")));
		}
		btnPanel2.addElement(btnModelProperties);

		Button btnSafety = new Button(gui, gui.i18nFormat("button.cpm.safetySettings"), () -> MinecraftClientAccess.get().openGui(SettingsGui::safetySettings));
		btnSafety.setBounds(new Box(0, 0, 160, 20));
		btnPanel2.addElement(btnSafety);

		Button btnSocial = new Button(gui, gui.i18nFormat("button.cpm.socialMenu"), () -> MinecraftClientAccess.get().openGui(SocialGui::new));
		btnSocial.setBounds(new Box(0, 0, 160, 20));
		btnPanel2.addElement(btnSocial);

		Button btnCleanCache = new Button(gui, gui.i18nFormat("button.cpm.reload_models"), MinecraftClientAccess.get().getDefinitionLoader()::clearCache);
		btnCleanCache.setBounds(new Box(0, 0, 160, 20));
		btnPanel2.addElement(btnCleanCache);

		IKeybind rtkb = null;
		for(IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
			if(kb.getName().startsWith("renderToggle")) {
				rtkb = kb;
			}
		}

		String k = rtkb == null ? "?" : rtkb.getBoundKey();
		if(k.isEmpty())k = gui.i18nFormat("label.cpm.key_unbound");

		Checkbox chbxRender = new Checkbox(gui, gui.i18nFormat("button.cpm.render_models", k));
		chbxRender.setBounds(new Box(0, 0, 160, 20));
		chbxRender.setSelected(Player.isEnableRendering());
		chbxRender.setAction(() -> {
			boolean v = !chbxRender.isSelected();
			chbxRender.setSelected(v);
			Player.setEnableRendering(v);
		});
		btnPanel2.addElement(chbxRender);

		fl.reflow();
		Box bp2 = btnPanel2.getBounds();
		btnPanel2.setBounds(new Box(bp2.x, height - bp2.h - 2, bp2.w, bp2.h));

		Panel btnPanel3 = new Panel(gui);
		btnPanel3.setBounds(new Box(0, height - 60, 160, 60));
		p.addElement(btnPanel3);

		Button btnSkinMenu = new Button(gui, gui.i18nFormat("button.cpm.models"), () -> MinecraftClientAccess.get().openGui(ModelsGui::new));
		btnSkinMenu.setBounds(new Box(0, 40, 160, 20));
		btnPanel3.addElement(btnSkinMenu);
		if(status != ServerStatus.INSTALLED) {
			btnSkinMenu.setEnabled(false);
			btnSkinMenu.setTooltip(new Tooltip(this, gui.i18nFormat("label.cpm.feature_unavailable")));
		}

		if(TestIngameManager.isTesting()) {
			Button btnOpenEditor = new Button(gui, gui.i18nFormat("button.cpm.open_editor"), () -> MinecraftClientAccess.get().openGui(EditorGui::new));
			btnOpenEditor.setBounds(new Box(0, 20, 160, 20));
			btnPanel3.addElement(btnOpenEditor);

			Button btnOpenFP = new Button(gui, gui.i18nFormat("button.cpm.effect.setFpHandPos"), () -> MinecraftClientAccess.get().openGui(FirstPersonHandPosGui::new));
			btnOpenFP.setBounds(new Box(0, 0, 160, 20));
			btnPanel3.addElement(btnOpenFP);
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		super.draw(event, partialTicks);

		ModelDefinition def = MinecraftClientAccess.get().getCurrentClientPlayer().getModelDefinition0();
		if(def != this.def || (def != null && def.getResolveState() != state)) {
			contentPanel.getElements().clear();
			this.def = def;
			initContent(contentPanel);
		}

		if(MinecraftObjectHolder.DEBUGGING && gui.isAltDown() && def != null) {
			TextureProvider skin = def.getTexture(TextureSheetType.SKIN, true);
			if(skin != null && skin.texture != null) {
				skin.bind();
				int size = Math.min(bounds.w, bounds.h);
				gui.drawText(514, 2, "Stitched: " + def.isStitchedTexture(), 0xffffffff);
				gui.drawBox(0, 0, size, size, 0xffaaaaaa);
				gui.drawTexture(0, 0, size, size, 0, 0, 1, 1);
			}
		}
	}

	@Override
	public IGui gui() {
		return gui;
	}

	private void clearGesture() {
		if(def != null)MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().clearGesture(def);
	}

	private void clearPose() {
		if(def != null)MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().clearCustomPose(def);
	}

	@Override
	public BoundKeyInfo getBoundKey(String id) {
		ConfigEntry ce = getEntryForModel(false);
		for (IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
			if (kb.getName().startsWith("qa")) {
				String c = ce.getString(kb.getName(), null);
				if (c != null && c.equals(id)) {
					String kbn = kb.getName();
					String kbMode = ce.getString(kb.getName() + "_mode", "press");
					String k = kb.getBoundKey();
					if(k.isEmpty())k = null;
					return new BoundKeyInfo(kbn, kbMode, k);
				}
			}
		}
		return null;
	}

	@Override
	public void valueChanged() {
	}
}
