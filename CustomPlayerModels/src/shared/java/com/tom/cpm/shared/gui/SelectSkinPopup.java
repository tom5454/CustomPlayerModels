package com.tom.cpm.shared.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.TestIngameManager;
import com.tom.cpm.shared.gui.panel.ModelDisplayPanel;
import com.tom.cpm.shared.gui.panel.ModelDisplayPanel.IModelDisplayPanel;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.LegacySkinConverter;

public class SelectSkinPopup extends PopupPanel implements IModelDisplayPanel {
	private ViewportCamera camera;
	private ScrollPanel list;
	private List<SkinPanel> panels;
	private List<Consumer<Vec2i>> sizeSetters;
	private TextureProvider selected;
	private TextureProvider selectedOpen;
	private ModelDisplayPanel display;
	private ModelDefinition selectedDef;
	private Button openSkin, set;
	private SkinType type;

	public SelectSkinPopup(Frame frm, SkinType typeIn, BiConsumer<SkinType, Image> accept) {
		super(frm.getGui());
		sizeSetters = new ArrayList<>();
		this.camera = new ViewportCamera();
		this.type = typeIn;

		list = new ScrollPanel(gui);
		Panel panel = new Panel(gui);
		sizeSetters.add(s -> panel.setBounds(new Box(0, 0, s.x, 40)));
		panels = new ArrayList<>();

		File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
		File[] fs = modelsDir.exists() ? modelsDir.listFiles((f, n) -> n.endsWith(".png")) : null;
		if(fs == null || fs.length == 0) {
			Label lbl = new Label(gui, gui.i18nFormat("label.cpm.no_skins"));
			lbl.setBounds(new Box(5, 25, 0, 0));
			panel.addElement(lbl);
		} else {
			Label lbl = new Label(gui, gui.i18nFormat("label.cpm.loading"));
			lbl.setBounds(new Box(5, 25, 0, 0));
			MinecraftClientAccess.get().getDefinitionLoader().execute(() -> {
				int y = 0;
				for (int i = 0; i < fs.length; i++) {
					if(fs[i].getName().equals(TestIngameManager.TEST_MODEL_NAME))continue;
					try {
						Image img = Image.loadFrom(fs[i]);
						if(img.getWidth() != 64 || (img.getHeight() != 64 && img.getHeight() != 32))continue;
						if(img.getHeight() == 32)img = LegacySkinConverter.processLegacySkin(img);
						SkinPanel p = new SkinPanel(gui, fs[i].getName(), img, y);
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

		openSkin = new Button(gui, gui.i18nFormat("button.cpm.openSkin"), () -> {
			FileChooserPopup fc = new FileChooserPopup(frm);
			fc.setTitle(gui.i18nFormat("button.cpm.openSkin"));
			fc.setFileDescText(gui.i18nFormat("label.cpm.file_png"));
			fc.setFilter(new FileFilter("png"));
			fc.setExtAdder(n -> n + ".png");
			fc.setAccept(f -> {
				try {
					Image imgIn = Image.loadFrom(f);
					if(imgIn.getWidth() != 64 || (imgIn.getHeight() != 64 && imgIn.getHeight() != 32)) {
						frm.openPopup(new MessagePopup(frm, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.vanillaSkinSize")));
						return;
					}
					final Image img = imgIn.getHeight() == 32 ? LegacySkinConverter.processLegacySkin(imgIn) : imgIn;
					frm.openPopup(new ConfirmPopup(frm, gui.i18nFormat("button.cpm.openSkin"), gui.i18nFormat("label.cpm.addSkinToFolder"), () -> {
						File fn = new File(modelsDir, f.getName());
						modelsDir.mkdirs();
						if(fn.exists()) {
							int ind = f.getName().lastIndexOf('.');
							String name = ind == -1 ? f.getName() : f.getName().substring(0, ind);
							fn = new File(modelsDir, name + "_" + (System.nanoTime() % 1000) + ".png");
						}
						try {
							img.storeTo(fn);
						} catch (IOException e) {
							gui.onGuiException("Failed to save image to models folder", e, false);
						}
						selectedOpen.setImage(img);
						selected = selectedOpen;
					}, () -> {
						selectedOpen.setImage(img);
						selected = selectedOpen;
					}));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			frm.openPopup(fc);
		});
		addElement(openSkin);

		list.setDisplay(panel);
		panel.setBackgroundColor(gui.getColors().panel_background & 0x00_ffffff | 0x80_000000);
		addElement(list);

		display = new ModelDisplayPanel(gui, this);
		display.setLoadingText(gui.i18nFormat("button.cpm.selectSkin"));
		addElement(display);

		set = new Button(gui, gui.i18nFormat("button.cpm.selectSkin"), () -> {
			if(selected != null && selected.getImage() != null) {
				accept.accept(type, selected.getImage());
				close();
			}
		});
		addElement(set);

		selectedOpen = new TextureProvider();

		setSize(400, 300);

		selectedDef = ModelDefinition.createVanilla(() -> selected, type);
	}

	public void setSize(int width, int height) {
		Vec2i s = new Vec2i(width / 2 - 30, height - 80);
		list.setBounds(new Box(20, 40, s.x, s.y));
		openSkin.setBounds(new Box(20, 20, s.x, 20));
		sizeSetters.forEach(c -> c.accept(s));
		int dispSize = Math.min(width / 2 - 40, height - 100);
		set.setBounds(new Box(width / 2 + (dispSize / 2) - 50, height / 2 + (dispSize / 2) + 10, 100, 20));
		display.setBounds(new Box(width / 2 + 10, height / 2 - (dispSize / 2), dispSize, dispSize));
		setBounds(new Box(0, 0, width, height));
	}

	private class SkinPanel extends Panel {
		private TextureProvider icon;
		private Button select;
		private int y;

		public SkinPanel(IGui gui, String fileName, Image img, int y) {
			super(gui);
			icon = new TextureProvider(img, new Vec2i(64, 64));
			this.y = y;

			Label lbl = new Label(gui, fileName);
			lbl.setBounds(new Box(68, 5, 100, 10));
			addElement(lbl);

			select = new Button(gui, gui.i18nFormat("button.cpm.select"), () -> {
				selected = icon;
			});
			addElement(select);
		}

		public int getHeight() {
			return 70;
		}

		public void setSize(int w) {
			select.setBounds(new Box(w - 50, 15, 40, 20));
			setBounds(new Box(0, y, w, getHeight()));
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			super.draw(event, partialTicks);

			if(icon != null) {
				icon.bind();
				gui.drawTexture(bounds.x + 1, bounds.y + 1, 64, 64, 8/64f, 8/64f, 16/64f, 16/64f);
				gui.drawTexture(bounds.x + 1, bounds.y + 1, 64, 64, 40/64f, 8/64f, 48/64f, 16/64f);
			}
		}

		private void onClosed() {
			if(icon != null)icon.free();
		}
	}

	@Override
	public void onClosed() {
		super.onClosed();
		panels.forEach(SkinPanel::onClosed);
		selectedOpen.free();
	}

	@Override
	public ModelDefinition getSelectedDefinition() {
		return selected != null ? selectedDef : null;
	}

	@Override
	public ViewportCamera getCamera() {
		return camera;
	}

	@Override
	public void preRender() {
	}

	@Override
	public boolean doRender() {
		return true;
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("button.cpm.selectSkin");
	}
}
