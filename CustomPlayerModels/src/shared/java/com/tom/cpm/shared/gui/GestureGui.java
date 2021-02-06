package com.tom.cpm.shared.gui;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.AnimationRegistry.Gesture;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.config.ConfigEntry;
import com.tom.cpm.shared.config.ConfigEntry.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.Checkbox;
import com.tom.cpm.shared.gui.elements.Label;
import com.tom.cpm.shared.gui.elements.Panel;
import com.tom.cpm.shared.math.Box;

public class GestureGui extends Frame {
	private GestureButton hoveredBtn;
	private Panel panel;

	public GestureGui(IGui gui) {
		super(gui);
		gui.setCloseListener(r -> {
			ModConfig.getConfig().save();
			r.run();
		});
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(hoveredBtn != null) {
			ConfigEntry ce = ModConfig.getConfig().getEntry("keybinds");
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
	public void initFrame(int width, int height) {
		ModelDefinition def = MinecraftClientAccess.get().getClientPlayer().getModelDefinition();
		if(MinecraftObjectHolder.DEBUGGING)System.out.println(def);
		ServerStatus status = MinecraftClientAccess.get().getServerSideStatus();
		if(status == ServerStatus.OFFLINE) {
			String str = "How did you get here?";
			Label lbl = new Label(gui, str);
			lbl.setBounds(new Box(width / 2 - gui.textWidth(str) / 2, height / 2 - 4, 0, 0));
			addElement(lbl);
			return;
		}
		int h;
		if(def != null && status != ServerStatus.UNAVAILABLE) {
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
		btnPanel2.setBounds(new Box(width - 162, height - 52, 160, 50));
		addElement(btnPanel2);

		Button btnCleanCache = new Button(gui, gui.i18nFormat("button.cpm.reload_models"), MinecraftClientAccess.get().getDefinitionLoader()::clearCache);
		btnCleanCache.setBounds(new Box(0, 30, 160, 20));
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
	}

	private void setGesture(Gesture g) {
		ModelDefinition def = MinecraftClientAccess.get().getClientPlayer().getModelDefinition();
		if(def != null)MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().playGesture(def.getAnimations(), g);
	}

	private void setPose(CustomPose pose) {
		ModelDefinition def = MinecraftClientAccess.get().getClientPlayer().getModelDefinition();
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
			ConfigEntry ce = ModConfig.getConfig().getEntry("keybinds");
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
		public void draw(int mouseX, int mouseY, float partialTicks) {
			int w = gui.textWidth(name);
			int bgColor = gui.getColors().button_fill;
			int color = gui.getColors().button_text_color;
			if(!enabled) {
				color = gui.getColors().button_text_disabled;
				bgColor = gui.getColors().button_disabled;
			} else if(bounds.isInBounds(mouseX, mouseY)) {
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
			if(bounds.isInBounds(mouseX, mouseY)) {
				hoveredBtn = this;
			}
		}
	}
}
