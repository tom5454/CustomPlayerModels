package com.tom.cpm.shared.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.ComboSlider;
import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Slider;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.NamedElement;
import com.tom.cpl.util.NamedElement.NameMapper;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.Gesture;
import com.tom.cpm.shared.animation.IManualGesture;
import com.tom.cpm.shared.config.ConfigChangeRequest;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinition.ModelLoadingState;
import com.tom.cpm.shared.editor.TestIngameManager;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.gui.FirstPersonHandPosGui;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.network.ServerCaps;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.Log;

public class GestureGui extends Frame {
	private GestureButton hoveredBtn;
	private Panel panel, contentPanel;
	private int tickCounter;
	private ModelDefinition def;
	private ModelLoadingState state;

	public GestureGui(IGui gui) {
		super(gui);
		gui.setCloseListener(r -> {
			ModConfig.getCommonConfig().save();
			r.run();
		});
		tickCounter = 3;
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(hoveredBtn != null) {
			ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.KEYBINDS);
			String hoveredID = hoveredBtn.gesture.getGestureId();
			String keybindPressed = null;
			List<String> dup = new ArrayList<>();
			for(IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
				if(kb.getName().startsWith("qa")) {
					if(kb.isPressed(event)) {
						keybindPressed = kb.getName();
					} else {
						String c = ce.getString(kb.getName(), null);
						if(c != null && c.equals(hoveredID)) {
							dup.add(kb.getName());
						}
					}
				}
			}
			if(keybindPressed != null) {
				ce.setString(keybindPressed, hoveredID);
				ce.setString(keybindPressed + "_mode", !hoveredBtn.layer && gui.isCtrlDown() ? "hold" : "press");
				for (String k : dup) {
					ce.setString(k, "");
				}
			}
			panel.getElements().forEach(e -> {
				if(e instanceof GestureButton)
					((GestureButton)e).getKb();
			});
		}
		super.keyPressed(event);
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
			String text = gui.i18nFormat("label.cpm.quick_key_short");
			Label lbl = new Label(gui, text);
			lbl.setTooltip(new Tooltip(this, tt.toString()));
			p.addElement(lbl.setBounds(new Box(10, 10, gui.textWidth(text), 10)));
		}
		int h;
		if(def != null && this.state == ModelLoadingState.LOADED && status != ServerStatus.UNAVAILABLE) {
			int[] id = new int[] {0};
			panel = new Panel(gui);
			Map<String, Group> groups = new HashMap<>();
			Stream.concat(def.getAnimations().getGestures().values().stream(), def.getAnimations().getCustomPoses().values().stream()).
			filter(g -> !g.isProperty() && !g.isCommand()).sorted(Comparator.comparingInt(IManualGesture::getOrder)).
			forEach(g -> {
				if (g instanceof Gesture && ((Gesture)g).group != null) {
					groups.computeIfAbsent(((Gesture)g).group, k -> new Group(k, panel, id[0]++)).add((Gesture) g);
				} else
					panel.addElement(new GestureButton(g, id[0]++));
			});
			groups.values().forEach(Group::update);
			if(id[0] == 0) {
				String str = gui.i18nFormat("label.cpm.nothing_here");
				Label lbl = new Label(gui, str);
				lbl.setBounds(new Box(width / 2 - gui.textWidth(str) / 2, height / 2 - 4, 0, 0));
				p.addElement(lbl);
				h = 10;
			} else {
				h = (id[0] / 4 + 1) * 40;

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

		Button btnModelProperties = new Button(gui, gui.i18nFormat("button.cpm.modelProperties"), () -> openPopup(new PropertiesPopup(gui, height * 2 / 3, def)));
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

	private class Group {
		private NameMapper<Gesture> groupGestures;
		private List<Gesture> list;
		private DropDownBox<NamedElement<Gesture>> box;

		public Group(String name, Panel panel, int id) {
			list = new ArrayList<>();
			list.add(null);
			groupGestures = new NameMapper<>(list, g -> g == null ? gui.i18nFormat("label.cpm.layerNone") : g.name);
			box = new DropDownBox<>(gui.getFrame(), groupGestures.asList());
			groupGestures.setSetter(box::setSelected);

			Panel p = new Panel(gui);
			p.setBounds(new Box((id % 4) * 90, (id / 4) * 40, 80, 30));
			panel.addElement(p);

			p.addElement(new Label(gui, name).setBounds(new Box(0, 0, 70, 10)));
			p.addElement(box);
			box.setBounds(new Box(0, 10, 80, 20));
			box.setAction(() -> {
				Gesture s = box.getSelected().getElem();
				list.forEach(g -> {
					MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().setLayerValue(def.getAnimations(), g, g == s ? 1 : 0);
				});
			});
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

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		hoveredBtn = null;
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

	private void clearGesture() {
		if(def != null)MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().playGesture(def.getAnimations(), null);
	}

	private void clearPose() {
		if(def != null)MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().setCustomPose(def.getAnimations(), null);
	}

	private void setManualGesture(IManualGesture g) {
		if(def != null)g.play(def.getAnimations());
	}

	private class GestureButton extends Panel {
		private IManualGesture gesture;
		private String kb, kbMode, name;
		private boolean layer, value;

		public GestureButton(int id) {
			super(GestureGui.this.gui);
			setBounds(new Box((id % 4) * 90, (id / 4) * 40, 80, 30));
		}

		public GestureButton(IManualGesture g, int id) {
			this(id);
			String nm = g.getName();
			layer = g.getType() == AnimationType.LAYER;
			value = g.getType() == AnimationType.VALUE_LAYER;
			if(!(layer || value)) {
				Button btn = new Button(gui, "", () -> setManualGesture(g));
				btn.setBounds(new Box(0, 0, bounds.w, bounds.h));
				addElement(btn);
			}
			this.name = nm;
			this.gesture = g;
			getKb();
			if(layer || value) {
				if(!MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES)) {
					setEnabled(false);
					Slider s = new Slider(gui, nm);
					s.setBounds(new Box(0, 0, bounds.w, bounds.h));
					s.setEnabled(false);
					s.setTooltip(new Tooltip(GestureGui.this, gui.i18nFormat("label.cpm.feature_unavailable")));
					addElement(s);
				} else if(value) {
					ComboSlider s = new ComboSlider(gui, a -> nm, a -> a * 100f, a -> a / 100f);
					s.getSpinner().setDp(0);
					s.setBounds(new Box(0, 0, bounds.w, bounds.h));
					s.setAction(() -> {
						if(def != null)
							MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().setLayerValue(def.getAnimations(), (Gesture) g, s.getValue());
					});
					if(def != null)
						s.setValue(Byte.toUnsignedInt(MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getGestureValue(def.getAnimations(), (Gesture) g)) / 255f);
					addElement(s);
				} else if(layer) {
					Button btn = new Button(gui, "", () -> setManualGesture(g));
					btn.setBounds(new Box(0, 0, bounds.w, bounds.h));
					addElement(btn);
				}
			}
		}

		public void getKb() {
			ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.KEYBINDS);
			this.kb = null;
			this.kbMode = null;
			for(IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
				if(kb.getName().startsWith("qa")) {
					String c = ce.getString(kb.getName(), null);
					if(c != null) {
						if(c.equals(gesture.getGestureId())) {
							this.kb = kb.getName();
							this.kbMode = ce.getString(kb.getName() + "_mode", "press");
							break;
						}
					}
				}
			}
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			if(value) {
				super.draw(event, partialTicks);
				return;
			}
			int w = gui.textWidth(name);
			int bgColor = gui.getColors().button_fill;
			int color = gui.getColors().button_text_color;
			if(!enabled) {
				color = gui.getColors().button_text_disabled;
				bgColor = gui.getColors().button_disabled;
			} else if(event.isHovered(bounds)) {
				color = gui.getColors().button_text_hover;
				bgColor = gui.getColors().button_hover;
			}
			gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
			if(layer) {
				if(def != null) {
					boolean on = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getGestureValue(def.getAnimations(), (Gesture) gesture) != 0;
					int bw = bounds.w-2;
					gui.drawBox(bounds.x + 1,          bounds.y + 1, bw / 2, bounds.h - 2, on ? 0xff00ff00 : bgColor);
					gui.drawBox(bounds.x + 1 + bw / 2, bounds.y + 1, bw / 2, bounds.h - 2, on ? bgColor : 0xffff0000);
				}
			} else {
				gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, bgColor);
			}
			gui.drawText(bounds.x + bounds.w / 2 - w / 2, bounds.y + bounds.h / 2 - 8, name, color);
			String boundKey = gui.i18nFormat("label.cpm.key_unbound");
			if(kb != null) {
				for(IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
					if(kb.getName().equals(this.kb)) {
						String k = kb.getBoundKey();
						if(k.isEmpty())k = gui.i18nFormat("label.cpm.key_unbound");
						boundKey = k;
						gui.drawText(bounds.x + bounds.w / 2 - w / 2, bounds.y + bounds.h / 2 + 4, k, color);
						break;
					}
				}
			}
			if(event.isHovered(bounds)) {
				if(!enabled) {
					new Tooltip(GestureGui.this, gui.i18nFormat("label.cpm.feature_unavailable")).set();
				} else {
					hoveredBtn = this;
					String kbMode = this.kbMode != null ? gui.i18nFormat("label.cpm.gestureMode." + this.kbMode) : gui.i18nFormat("label.cpm.key_unbound");
					if(layer)
						new Tooltip(GestureGui.this, gui.i18nFormat("tooltip.cpm.gestureButton", name, boundKey)).set();
					else
						new Tooltip(GestureGui.this, gui.i18nFormat("tooltip.cpm.gestureButton.mode", name, boundKey, kbMode)).set();
				}
			}
		}
	}
}
