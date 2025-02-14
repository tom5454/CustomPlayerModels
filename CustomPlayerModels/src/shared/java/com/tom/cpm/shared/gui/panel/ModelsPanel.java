package com.tom.cpm.shared.gui.panel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.AnimationHandler;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinition.ModelLoadingState;
import com.tom.cpm.shared.editor.Exporter;
import com.tom.cpm.shared.editor.TestIngameManager;
import com.tom.cpm.shared.gui.SelectSkinPopup;
import com.tom.cpm.shared.gui.SkinUploadPopup;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.ModelDisplayPanel.IModelDisplayPanel;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.IOHelper.ImageBlock;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.Log;

public class ModelsPanel extends Panel implements IModelDisplayPanel {
	private ViewportCamera camera;
	private Frame frm;
	private ScrollPanel list;
	private ModelDisplayPanel display;
	private ModelDefinition selectedDef;
	private String selected;
	private List<ListPanel> panels;
	private List<Consumer<Vec2i>> sizeSetters;
	private Button set, upload, simpleSkin;
	private boolean doRender = true;
	private AnimationHandler animHandler;
	private String selectedFolder = "";
	private Vec2i size = new Vec2i(0, 0);

	public ModelsPanel(Frame frm, ViewportCamera camera) {
		super(frm.getGui());
		this.frm = frm;
		this.camera = camera;
		sizeSetters = new ArrayList<>();
		animHandler = new AnimationHandler(this::getSelectedDefinition, AnimationMode.GUI);

		list = new ScrollPanel(gui);
		list.setBounds(new Box(0, 0, 0, 0));
		loadModelList();
		addElement(list);

		display = new ModelDisplayPanel(frm, this);
		display.setLoadingText(gui.i18nFormat("label.cpm.loading"));
		addElement(display);

		set = new Button(gui, gui.i18nFormat("button.cpm.applySkin"), this::applySelected);
		set.setEnabled(false);
		addElement(set);

		if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.OFFLINE) {
			upload = new Button(gui, gui.i18nFormat("button.cpm.uploadSkin"), this::uploadSelected);
			upload.setTooltip(new Tooltip(frm, gui.i18nFormat("tooltip.cpm.uploadSkin")));
			upload.setEnabled(false);
			addElement(upload);

			simpleSkin = new Button(gui, gui.i18nFormat("button.cpm.changeSkin"), () -> {
				SelectSkinPopup ssp = new SelectSkinPopup(frm, SkinType.DEFAULT, (type, img) -> {
					frm.openPopup(new ConfirmPopup(frm, gui.i18nFormat("label.cpm.export.upload"),
							gui.i18nFormat("label.cpm.export.upload.desc"), () -> upload(type, img), null));
				});
				ssp.setOnClosed(() -> doRender = true);
				doRender = false;
				frm.openPopup(ssp);
			});
			simpleSkin.setTooltip(new Tooltip(frm, gui.i18nFormat("tooltip.cpm.changeSkin")));
			addElement(simpleSkin);
		}

		Player<?> player = MinecraftClientAccess.get().getCurrentClientPlayer();
		player.getDefinitionFuture().thenAccept(d -> {
			if(selectedDef == null)selectedDef = d;
			animHandler.clear();
		});
		if(player.forcedSkin) {
			Label lbl = new Label(gui, gui.i18nFormat("label.cpm.skinForced"));
			lbl.setColor(0xffff0000);
			lbl.setBounds(new Box(40, 30, 0, 0));
			addElement(lbl);
		}
	}

	private void loadModelList() {
		sizeSetters.clear();
		Panel panel = new Panel(gui);

		File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models/" + selectedFolder);
		File[] fs = modelsDir.exists() ? modelsDir.listFiles((f, n) -> n.endsWith(".cpmmodel") || new File(f, n).isDirectory()) : null;
		String model = ModConfig.getCommonConfig().getString(ConfigKeys.SELECTED_MODEL, null);

		if(!selectedFolder.isEmpty()) {
			Button moveUp = new Button(gui, ".. (" + selectedFolder + ")", () -> {
				if(selectedFolder.contains("/")) {
					int i = selectedFolder.lastIndexOf('/');
					selectedFolder = selectedFolder.substring(0, i);
				} else selectedFolder = "";
				loadModelList();
			});
			sizeSetters.add(s -> moveUp.setBounds(new Box(0, 0, s.x, 20)));
			panel.addElement(moveUp);
		} else {
			Button reset = new Button(gui, gui.i18nFormat(model == null ? "button.cpm.reset_skin.sel" : "button.cpm.reset_skin"), () -> {
				ModConfig.getCommonConfig().clearValue(ConfigKeys.SELECTED_MODEL);
				ModConfig.getCommonConfig().save();
				if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED) {
					MinecraftClientAccess.get().sendSkinUpdate();
				}
				loadModelList();
			});
			sizeSetters.add(s -> reset.setBounds(new Box(0, 0, s.x, 20)));
			panel.addElement(reset);
		}
		sizeSetters.add(s -> panel.setBounds(new Box(0, 0, s.x, 40)));
		panels = new ArrayList<>();
		if(fs == null || fs.length == 0) {
			Label lbl = new Label(gui, gui.i18nFormat("label.cpm.no_models"));
			lbl.setBounds(new Box(5, 25, 0, 0));
			panel.addElement(lbl);
		} else {
			Label lbl = new Label(gui, gui.i18nFormat("label.cpm.loading"));
			lbl.setBounds(new Box(5, 25, 0, 0));
			MinecraftClientAccess.get().getDefinitionLoader().execute(() -> {
				int y = 20;
				for (int i = 0; i < fs.length; i++) {
					if(fs[i].getName().equals(TestIngameManager.TEST_MODEL_NAME) || fs[i].getName().equals("autosaves"))continue;
					try {
						ListPanel p;
						if(fs[i].isDirectory()) {
							p = new FolderPanel(gui, fs[i], y);
						} else {
							p = new ModelPanel(gui, ModelFile.load(fs[i]), model != null && getFullPath(fs[i].getName()).equals(model), y);
						}
						y += p.getHeight();
						panels.add(p);
					} catch (Exception e) {
						Log.warn("Error loading model: " + fs[i].getName(), e);
					}
				}
				final int fy = y;
				MinecraftClientAccess.get().executeOnGameThread(() -> {
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

		sizeSetters.forEach(c -> c.accept(size));
		list.setScrollY(0);
	}

	private String getFullPath(String name) {
		return selectedFolder.isEmpty() ? name : selectedFolder + "/" + name;
	}

	public void setSize(int width, int height) {
		Vec2i s = new Vec2i(width / 2 - 30, height - 50);
		list.setBounds(new Box(20, 40, s.x, s.y));
		this.size = s;
		sizeSetters.forEach(c -> c.accept(s));
		int dispSize = Math.min(width / 2 - 40, height - 100);
		if(upload != null) {
			set.setBounds(new Box(width / 2 + (dispSize / 2) - 102, height / 2 + (dispSize / 2) + 10, 100, 20));
			upload.setBounds(new Box(width / 2 + (dispSize / 2) + 2, height / 2 + (dispSize / 2) + 10, 100, 20));
			simpleSkin.setBounds(new Box(width / 2 + (dispSize / 2) - 50, 5, 100, 20));
		} else {
			set.setBounds(new Box(width / 2 + (dispSize / 2) - 50, height / 2 + (dispSize / 2) + 10, 100, 20));
		}
		display.setBounds(new Box(width / 2 + 10, height / 2 - (dispSize / 2), dispSize, dispSize));
		setBounds(new Box(0, 0, width, height));
	}

	private abstract class ListPanel extends Panel {

		public ListPanel(IGui gui) {
			super(gui);
		}

		protected abstract void setSize(int w);
		protected abstract int getHeight();
		protected abstract void onClosed();
	}

	private class ModelPanel extends ListPanel {
		private TextureProvider icon;
		private Button select;
		private int linesC, y;

		public ModelPanel(IGui gui, ModelFile file, boolean sel, int y) {
			super(gui);
			Label lbl = new Label(gui, file.getName());
			lbl.setBounds(new Box(70, 5, 100, 10));
			if(sel)lbl.setColor(gui.getColors().button_hover);
			addElement(lbl);

			String[] lines = file.getDesc().split("\\\\");
			for (int i = 0; i < lines.length; i++) {
				addElement(new Label(gui, lines[i]).setBounds(new Box(70, 20 + i * 10, 0, 0)));
			}
			linesC = lines.length;
			this.y = y;

			select = new Button(gui, gui.i18nFormat("button.cpm.select"), () -> {
				selected = getFullPath(file.getFileName());
				selectedDef = MinecraftClientAccess.get().getDefinitionLoader().loadModel(file.getDataBlock(), MinecraftClientAccess.get().getClientPlayer());
				file.registerLocalCache(MinecraftClientAccess.get().getDefinitionLoader());
				set.setEnabled(true);
				if(upload != null)
					upload.setEnabled(true);
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

		@Override
		public void setSize(int w) {
			select.setBounds(new Box(w - 50, 2, 40, 20));
			setBounds(new Box(0, y, w, getHeight()));
		}

		@Override
		public int getHeight() {
			return Math.max(68, linesC * 10 + 30);
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			super.draw(event, partialTicks);

			gui.drawBox(bounds.x + 1, bounds.y + 1, 66, 66, gui.getColors().popup_background);
			if(icon != null) {
				icon.bind();
				gui.drawTexture(bounds.x + 2, bounds.y + 2, 64, 64, 0, 0, 1, 1);
			} else {
				gui.drawTexture(bounds.x + 2, bounds.y + 2, 64, 64, 64, 0, "models_menu");
			}
		}

		@Override
		protected void onClosed() {
			if(icon != null)icon.free();
		}
	}

	private class FolderPanel extends ListPanel {
		private TextureProvider icon;
		private Button select;
		private int linesC, y;

		public FolderPanel(IGui gui, File folder, int y) throws IOException {
			super(gui);

			Label lbl = new Label(gui, folder.getName());
			lbl.setBounds(new Box(70, 5, 100, 10));
			addElement(lbl);

			File descFile = new File(folder, "description.txt");
			if(descFile.exists()) {
				int i = 0;
				try(BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(descFile), StandardCharsets.UTF_8))) {
					addElement(new Label(gui, rd.readLine()).setBounds(new Box(70, 20 + (i++) * 10, 0, 0)));
				}
				linesC = i;
			}
			this.y = y;

			select = new Button(gui, gui.i18nFormat("button.cpm.models.openFolder"), () -> {
				selectedFolder = getFullPath(folder.getName());
				loadModelList();
			});
			addElement(select);

			File iconFile = new File(folder, "icon.png");
			if(iconFile.exists()) {
				MinecraftClientAccess.get().getDefinitionLoader().execute(() -> {
					try (FileInputStream fin = new FileInputStream(iconFile)) {
						icon = new TextureProvider(ImageIO.read(fin), null);
					} catch (IOException e) {
					}
				});
			}
		}

		@Override
		public int getHeight() {
			return Math.max(68, linesC * 10 + 30);
		}

		@Override
		protected void setSize(int w) {
			select.setBounds(new Box(w - 50, 2, 40, 20));
			setBounds(new Box(0, y, w, getHeight()));
		}

		@Override
		protected void onClosed() {
			if(icon != null)icon.free();
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			super.draw(event, partialTicks);

			gui.drawBox(bounds.x + 1, bounds.y + 1, 66, 66, gui.getColors().popup_background);
			if(icon != null) {
				icon.bind();
				gui.drawTexture(bounds.x + 2, bounds.y + 2, 64, 64, 0, 0, 1, 1);
				gui.drawTexture(bounds.x + 50, bounds.y + 50, 16, 16, 128, 0, "models_menu");
			} else {
				gui.drawTexture(bounds.x + 2, bounds.y + 2, 64, 64, 0, 0, "models_menu");
			}
		}
	}

	@Override
	public ModelDefinition getSelectedDefinition() {
		if(selectedDef != null) {
			if(selectedDef.getResolveState() == ModelLoadingState.NEW)selectedDef.startResolve();
			else if(selectedDef.getResolveState() == ModelLoadingState.LOADED) {
				return selectedDef;
			}
		}
		return null;
	}

	@Override
	public void preRender() {
		if(getSelectedDefinition() != null) {
			MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().handleGuiAnimation(animHandler, getSelectedDefinition());
		}
	}

	private void applySelected() {
		ModConfig.getCommonConfig().setString(ConfigKeys.SELECTED_MODEL, selected);
		ModConfig.getCommonConfig().save();
		if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED) {
			MinecraftClientAccess.get().sendSkinUpdate();
		}
		loadModelList();
	}

	private void uploadSelected() {
		if(selected != null) {
			File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
			File modelF = new File(modelsDir, selected);
			ModelFile file;
			try {
				file = ModelFile.load(modelF);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			if(!file.convertable()) {
				frm.openPopup(new MessagePopup(frm, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.skinUpload.fail", gui.i18nFormat("label.cpm.modelNotSkinCompatible"))));
				return;
			}
			SelectSkinPopup ssp = new SelectSkinPopup(frm, SkinType.DEFAULT, (type, img) -> {
				frm.openPopup(new ConfirmPopup(frm, gui.i18nFormat("label.cpm.export.upload"), gui.i18nFormat("label.cpm.export.upload.desc"), () -> {
					Exporter.convert(file, img, type, out -> upload(type, out), () -> {
						frm.openPopup(new MessagePopup(frm, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.skinUpload.fail", gui.i18nFormat("label.cpm.convertFail"))));
					});
				}, null));
			});
			ssp.setOnClosed(() -> doRender = true);
			doRender = false;
			frm.openPopup(ssp);
		}
	}

	private void upload(SkinType type, Image img) {
		new SkinUploadPopup(frm, type, img).start();
	}

	public void onClosed() {
		panels.forEach(ListPanel::onClosed);
	}

	@Override
	public ViewportCamera getCamera() {
		return camera;
	}

	@Override
	public boolean doRender() {
		return doRender;
	}

	public void filesDropped(List<File> files) {
		File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models/" + selectedFolder);
		modelsDir.mkdirs();
		for (int i = 0; i < files.size(); i++) {
			File model = files.get(i);
			if(model.getName().endsWith(".cpmmodel")) {
				File m = new File(modelsDir, model.getName());
				Random r = new Random();
				String name = model.getName();
				name = name.substring(0, name.length() - 9);
				while(m.exists()) {
					m = new File(modelsDir, name + "_" + Integer.toHexString(r.nextInt()) + ".cpmmodel");
				}
				try(FileInputStream in = new FileInputStream(model);FileOutputStream out = new FileOutputStream(m)) {
					IOHelper.copy(in, out);
				} catch (IOException e) {
				}
			}
		}
		loadModelList();
	}
}
