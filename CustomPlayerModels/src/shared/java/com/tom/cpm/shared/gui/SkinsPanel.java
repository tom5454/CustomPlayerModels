package com.tom.cpm.shared.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Exporter;
import com.tom.cpm.shared.gui.ViewportPanelBase.ViewportCamera;
import com.tom.cpm.shared.io.IOHelper.ImageBlock;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.skin.TextureProvider;

public class SkinsPanel extends Panel {
	public ViewportCamera camera;
	private ScrollPanel list;
	private SkinDisplayPanel display;
	private ModelDefinition selectedDef;
	private String selected;
	private List<SkinPanel> panels;
	private List<Consumer<Vec2i>> sizeSetters;
	private Button set;

	public SkinsPanel(IGui gui, ViewportCamera camera) {
		super(gui);
		this.camera = camera;
		sizeSetters = new ArrayList<>();

		list = new ScrollPanel(gui);
		Panel panel = new Panel(gui);

		File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
		File[] fs = modelsDir.exists() ? modelsDir.listFiles((f, n) -> n.endsWith(".cpmmodel")) : null;
		String model = ModConfig.getConfig().getString(ConfigKeys.SELECTED_MODEL, null);

		Button reset = new Button(gui, gui.i18nFormat(model == null ? "button.cpm.reset_skin.sel" : "button.cpm.reset_skin"), () -> {
			ModConfig.getConfig().clearValue(ConfigKeys.SELECTED_MODEL);
			ModConfig.getConfig().save();
			if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED) {
				MinecraftClientAccess.get().sendSkinUpdate();
			}
		});
		sizeSetters.add(s -> reset.setBounds(new Box(0, 0, s.x, 20)));
		panel.addElement(reset);
		sizeSetters.add(s -> panel.setBounds(new Box(0, 0, s.x, 40)));
		panels = new ArrayList<>();
		if(fs == null || fs.length == 0) {
			Label lbl = new Label(gui, gui.i18nFormat("label.cpm.no_skins"));
			lbl.setBounds(new Box(5, 25, 0, 0));
			panel.addElement(lbl);
		} else {
			Label lbl = new Label(gui, gui.i18nFormat("label.cpm.loading"));
			lbl.setBounds(new Box(5, 25, 0, 0));
			MinecraftClientAccess.get().getDefinitionLoader().execute(() -> {
				int y = 20;
				for (int i = 0; i < fs.length; i++) {
					if(fs[i].getName().equals(Exporter.TEMP_MODEL))continue;
					try {
						SkinPanel p = new SkinPanel(gui, ModelFile.load(fs[i]), model != null && fs[i].getName().equals(model), y);
						y += p.getHeight();
						panels.add(p);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				final int fy = y;
				MinecraftClientAccess.get().executeLater(() -> {
					panels.forEach(panel::addElement);
					if(panel.getBounds() != null) {
						int w = panel.getBounds().w;
						panels.forEach(p -> p.setSize(w));
						panel.setBounds(new Box(0, 0, w, fy));
					}
					sizeSetters.add(s -> {
						panel.setBounds(new Box(0, 0, s.x, fy));
						panels.forEach(p -> p.setSize(s.x));
					});
					panel.getElements().remove(lbl);
				});
			});
		}

		list.setDisplay(panel);
		panel.setBackgroundColor(gui.getColors().panel_background & 0x00_ffffff | 0x80_000000);
		addElement(list);

		display = new SkinDisplayPanel(gui, this);
		addElement(display);

		set = new Button(gui, gui.i18nFormat("button.cpm.applySkin"), this::applySelected);
		addElement(set);

		Player<?, ?> player = MinecraftClientAccess.get().getClientPlayer();
		selectedDef = player.getModelDefinition();
		if(player.forcedSkin) {
			Label lbl = new Label(gui, gui.i18nFormat("label.cpm.skinForced"));
			lbl.setColor(0xffff0000);
			lbl.setBounds(new Box(40, 30, 0, 0));
			addElement(lbl);
		}
	}

	public void setSize(int width, int height) {
		Vec2i s = new Vec2i(width / 2 - 30, height - 50);
		list.setBounds(new Box(20, 40, s.x, s.y));
		sizeSetters.forEach(c -> c.accept(s));
		int dispSize = Math.min(width / 2 - 40, height - 100);
		set.setBounds(new Box(width / 2 + (dispSize / 2) - 50, height / 2 + (dispSize / 2) + 10, 100, 20));
		display.setBounds(new Box(width / 2 + 10, height / 2 - (dispSize / 2), dispSize, dispSize));
		setBounds(new Box(0, 0, width, height));
	}

	private class SkinPanel extends Panel {
		private TextureProvider icon;
		private Button select;
		private int linesC, y;

		public SkinPanel(IGui gui, ModelFile file, boolean sel, int y) {
			super(gui);
			Label lbl = new Label(gui, file.getName());
			lbl.setBounds(new Box(68, 5, 100, 10));
			if(sel)lbl.setColor(gui.getColors().button_hover);
			addElement(lbl);

			String[] lines = file.getDesc().split("\\\\");
			for (int i = 0; i < lines.length; i++) {
				addElement(new Label(gui, lines[i]).setBounds(new Box(68, 20 + i * 10, 0, 0)));
			}
			linesC = lines.length;
			this.y = y;

			select = new Button(gui, gui.i18nFormat("button.cpm.select"), () -> {
				selected = file.getFileName();
				selectedDef = MinecraftClientAccess.get().getDefinitionLoader().loadModel(file.getDataBlock(), MinecraftClientAccess.get().getClientPlayer());
				file.registerLocalCache(MinecraftClientAccess.get().getDefinitionLoader());
			});
			addElement(select);

			if(file.getIcon() != null && file.getIcon().getWidth() > 0) {
				MinecraftClientAccess.get().getDefinitionLoader().execute(() -> {
					ImageBlock block = file.getIcon();
					try {
						block.doReadImage();
						if(block.getImage() != null)
							icon = new TextureProvider(block.getImage(), null);
					} catch (IOException e) {
					}
				});
			}
		}

		public void setSize(int w) {
			select.setBounds(new Box(w - 50, 2, 40, 20));
			setBounds(new Box(0, y, w, getHeight()));
		}

		public int getHeight() {
			return Math.max(64, linesC * 10 + 30);
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			super.draw(mouseX, mouseY, partialTicks);

			if(icon != null) {
				icon.bind();
				gui.drawTexture(bounds.x + 1, bounds.y + 1, 64, 64, 0, 0, 1, 1);
			}
		}

		private void onClosed() {
			if(icon != null)icon.free();
		}
	}

	public ModelDefinition getSelectedDefinition() {
		if(selectedDef != null) {
			if(selectedDef.getResolveState() == 0)selectedDef.startResolve();
			else if(selectedDef.getResolveState() == 2) {
				return selectedDef;
			}
		}
		return null;
	}

	public void preRender() {

	}

	private void applySelected() {
		ModConfig.getConfig().setString(ConfigKeys.SELECTED_MODEL, selected);
		ModConfig.getConfig().save();
		if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED) {
			MinecraftClientAccess.get().sendSkinUpdate();
		}
	}

	public void onClosed() {
		panels.forEach(SkinPanel::onClosed);
	}
}
