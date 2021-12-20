package com.tom.cpm.shared.gui;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.AnimationRegistry.Gesture;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinition.ModelLoadingState;
import com.tom.cpm.shared.editor.TestIngameManager;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.Log;

public class GestureGui extends Frame {
	private GestureButton hoveredBtn;
	private Panel panel;
	private int openEditor;

	public GestureGui(IGui gui) {
		super(gui);
		gui.setCloseListener(r -> {
			ModConfig.getCommonConfig().save();
			r.run();
		});
		openEditor = TestIngameManager.isTesting() ? 3 : 0;
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(hoveredBtn != null) {
			ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.KEYBINDS);
			String hoveredID = hoveredBtn.pose != null ? "p" + hoveredBtn.pose.getName() : "g" + hoveredBtn.gesture.name;
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
				for (String k : dup) {
					ce.setString(k, "");
				}
			}
			panel.getElements().forEach(e -> ((GestureButton)e).getKb());
		}
		super.keyPressed(event);
	}

	@Override
	public void tick() {
		if(openEditor > 0) {
			openEditor--;
			if(gui.isShiftDown())
				MinecraftClientAccess.get().openGui(EditorGui::new);
		}
	}

	@Override
	public void initFrame(int width, int height) {
		ModelDefinition def = MinecraftClientAccess.get().getCurrentClientPlayer().getModelDefinition0();
		Log.debug(def);
		ServerStatus status = MinecraftClientAccess.get().getServerSideStatus();
		if(status == ServerStatus.OFFLINE) {
			String str = "How did you get here?";
			Label lbl = new Label(gui, str);
			lbl.setBounds(new Box(width / 2 - gui.textWidth(str) / 2, height / 2 - 4, 0, 0));
			addElement(lbl);
			return;
		}
		int h;
		if(def != null && def.getResolveState() == ModelLoadingState.LOADED && status != ServerStatus.UNAVAILABLE) {
			int[] id = new int[] {0};
			panel = new Panel(gui);
			def.getAnimations().getGestures().forEach((nm, g) -> panel.addElement(new GestureButton(g, id[0]++)));
			def.getAnimations().getCustomPoses().forEach((nm, g) -> panel.addElement(new GestureButton(g, id[0]++)));
			if(id[0] == 0) {
				String str = gui.i18nFormat("label.cpm.nothing_here");
				Label lbl = new Label(gui, str);
				lbl.setBounds(new Box(width / 2 - gui.textWidth(str) / 2, height / 2 - 4, 0, 0));
				addElement(lbl);
				h = 10;
			} else {
				h = (id[0] / 4 + 1) * 40;
				panel.setBounds(new Box(width / 2 - 180, height / 2 - h / 2, 360, h));
				addElement(panel);
			}
		} else if(def != null && (def.getResolveState() == ModelLoadingState.ERRORRED || def.getResolveState() == ModelLoadingState.SAFETY_BLOCKED)) {
			String txt = "";
			switch (def.getResolveState()) {
			case ERRORRED:
				txt = gui.i18nFormat("label.cpm.errorLoadingModel", def.getError().toString());
				break;

			case SAFETY_BLOCKED:
				txt = gui.i18nFormat("label.cpm.safetyBlocked");
				break;

			default:
				break;
			}
			if(!txt.isEmpty()) {
				Label lbl = new Label(gui, txt);
				lbl.setBounds(new Box(width / 2 - gui.textWidth(txt) / 2, height / 2 - 4, 0, 0));
				addElement(lbl);
			}
			h = 10;
		} else if(status == ServerStatus.UNAVAILABLE) {
			String str = gui.i18nFormat("label.cpm.feature_unavailable");
			Label lbl = new Label(gui, str);
			lbl.setBounds(new Box(width / 2 - gui.textWidth(str) / 2, height / 2 - 4, 0, 0));
			addElement(lbl);
			h = 10;
		} else {
			String str = gui.i18nFormat("label.cpm.nothing_here");
			Label lbl = new Label(gui, str);
			lbl.setBounds(new Box(width / 2 - gui.textWidth(str) / 2, height / 2 - 4, 0, 0));
			addElement(lbl);
			h = 10;
		}

		Panel btnPanel = new Panel(gui);
		btnPanel.setBounds(new Box(width / 2 - 180, height / 2 - h / 2 - 30, 360, 20));
		addElement(btnPanel);

		if(status != ServerStatus.UNAVAILABLE) {
			Button btnRstG = new Button(gui, gui.i18nFormat("button.cpm.anim_reset_gesture"), () -> setGesture(null));
			btnRstG.setBounds(new Box(0, 0, 100, 20));
			btnPanel.addElement(btnRstG);

			Button btnRstP = new Button(gui, gui.i18nFormat("button.cpm.anim_reset_pose"), () -> setPose(null));
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
		addElement(btnPanel2);

		FlowLayout fl = new FlowLayout(btnPanel2, 0, 1);

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
		btnPanel3.setBounds(new Box(0, height - 40, 160, 40));
		addElement(btnPanel3);

		Button btnSkinMenu = new Button(gui, gui.i18nFormat("button.cpm.models"), () -> MinecraftClientAccess.get().openGui(ModelsGui::new));
		btnSkinMenu.setBounds(new Box(0, 20, 160, 20));
		btnPanel3.addElement(btnSkinMenu);
		if(MinecraftClientAccess.get().getServerSideStatus() != ServerStatus.INSTALLED) {
			btnSkinMenu.setEnabled(false);
			btnSkinMenu.setTooltip(new Tooltip(this, gui.i18nFormat("label.cpm.feature_unavailable")));
		}

		if(TestIngameManager.isTesting()) {
			Button btnOpenEditor = new Button(gui, gui.i18nFormat("button.cpm.open_editor"), () -> MinecraftClientAccess.get().openGui(EditorGui::new));
			btnOpenEditor.setBounds(new Box(0, 0, 160, 20));
			btnPanel3.addElement(btnOpenEditor);
		}
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		hoveredBtn = null;
		super.draw(mouseX, mouseY, partialTicks);
		int i = 0;
		for(IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
			if(kb.getName().startsWith("qa")) {
				i++;
				String k = kb.getBoundKey();
				if(k.isEmpty())k = gui.i18nFormat("label.cpm.key_unbound");
				gui.drawText(10, i * 10, gui.i18nFormat("label.cpm.quick_key_bound", i, k), 0xffffffff);
			}
		}

		ModelDefinition def = MinecraftClientAccess.get().getCurrentClientPlayer().getModelDefinition();
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

	private void setGesture(Gesture g) {
		ModelDefinition def = MinecraftClientAccess.get().getCurrentClientPlayer().getModelDefinition();
		if(def != null)MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().playGesture(def.getAnimations(), g);
	}

	private void setPose(CustomPose pose) {
		ModelDefinition def = MinecraftClientAccess.get().getCurrentClientPlayer().getModelDefinition();
		if(def != null)MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().setCustomPose(def.getAnimations(), pose);
	}

	private class GestureButton extends Button {
		private Gesture gesture;
		private CustomPose pose;
		private String kb;

		public GestureButton(String name, int id) {
			super(getGui(), name, null);
			setBounds(new Box((id % 4) * 90, (id / 4) * 40, 80, 30));
		}

		public GestureButton(Gesture g, int id) {
			this(g.name, id);
			setAction(() -> setGesture(g));
			this.gesture = g;
			getKb();
		}

		public GestureButton(CustomPose pose, int id) {
			this(pose.getName(), id);
			setAction(() -> setPose(pose));
			this.pose = pose;
			getKb();
		}

		public void getKb() {
			ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.KEYBINDS);
			this.kb = null;
			for(IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
				if(kb.getName().startsWith("qa")) {
					String c = ce.getString(kb.getName(), null);
					if(c != null) {
						if(pose != null && c.startsWith("p") && c.substring(1).equals(pose.getName())) {
							this.kb = kb.getName();
							break;
						}
						if(gesture != null && c.startsWith("g") && c.substring(1).equals(gesture.name)) {
							this.kb = kb.getName();
							break;
						}
					}
				}
			}
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
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
			gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, bgColor);
			gui.drawText(bounds.x + bounds.w / 2 - w / 2, bounds.y + bounds.h / 2 - 8, name, color);
			if(kb != null) {
				for(IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
					if(kb.getName().equals(this.kb)) {
						String k = kb.getBoundKey();
						if(k.isEmpty())k = gui.i18nFormat("label.cpm.key_unbound");
						gui.drawText(bounds.x + bounds.w / 2 - w / 2, bounds.y + bounds.h / 2 + 4, k, color);
						break;
					}
				}
			}
			if(event.isHovered(bounds)) {
				hoveredBtn = this;
			}
		}
	}
}
