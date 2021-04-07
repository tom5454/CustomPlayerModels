package com.tom.cpm.shared.editor.gui;

import java.io.File;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ButtonIcon;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.InputPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpl.gui.elements.ProcessPopup;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.elements.Tree;
import com.tom.cpl.gui.util.HorizontalLayout;
import com.tom.cpl.gui.util.TabbedPanelManager;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorTexture;
import com.tom.cpm.shared.editor.Exporter;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.gui.popup.ColorButton;
import com.tom.cpm.shared.editor.gui.popup.ExportSkinPopup;
import com.tom.cpm.shared.editor.gui.popup.FileChooserGui;
import com.tom.cpm.shared.editor.gui.popup.SettingsPopup;
import com.tom.cpm.shared.editor.gui.popup.SkinsPopup;
import com.tom.cpm.shared.editor.template.EditorTemplate;
import com.tom.cpm.shared.editor.template.TemplateSettings;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.tree.TreeElement.ModelTree;
import com.tom.cpm.shared.model.SkinType;

public class EditorGui extends Frame {
	private static Editor toReopen;
	private TabbedPanelManager tabs;
	private HorizontalLayout topPanel;
	private Editor editor;
	private static boolean smallGuiWarning = true;

	public EditorGui(IGui gui) {
		super(gui);
		if(toReopen != null) {
			this.editor = toReopen;
			toReopen = null;
			ModConfig.getConfig().clearValue(ConfigKeys.REOPEN_PROJECT);
			ModConfig.getConfig().save();
		} else {
			this.editor = new Editor(this);
			String reopen = ModConfig.getConfig().getString(ConfigKeys.REOPEN_PROJECT, null);
			if(reopen != null) {
				ModConfig.getConfig().clearValue(ConfigKeys.REOPEN_PROJECT);
				ModConfig.getConfig().save();
				load(new File(reopen));
			} else {
				this.editor.loadDefaultPlayerModel();
			}
		}
		String old = ModConfig.getConfig().getString(ConfigKeys.SELECTED_MODEL_OLD, null);
		if(old != null) {
			ModConfig.getConfig().clearValue(ConfigKeys.SELECTED_MODEL_OLD);
			ModConfig.getConfig().setString(ConfigKeys.SELECTED_MODEL, old);
			ModConfig.getConfig().save();
		}
		gui.setCloseListener(c -> {
			checkUnsaved(() -> {
				editor.free();
				c.run();
			});
		});
	}

	private void checkUnsaved(Runnable r) {
		if(editor.dirty) {
			openPopup(new ConfirmPopup(this, gui.i18nFormat("label.cpm.unsaved"), r, null));
		} else r.run();
	}

	@Override
	public void initFrame(int width, int height) {
		editor.updaterReg.reset();

		tabs = new TabbedPanelManager(gui);
		tabs.setBounds(new Box(0, 20, width, height - 20));
		addElement(tabs);

		Panel topPanel = new Panel(gui);
		topPanel.setBounds(new Box(0, 0, width, 20));
		topPanel.setBackgroundColor(gui.getColors().menu_bar_background);
		addElement(topPanel);
		this.topPanel = new HorizontalLayout(topPanel);

		initFileMenu();
		initEditMenu();
		initEffectMenu();
		initDisplayMenu();

		this.topPanel.addX(2);

		initModelPanel(width, height);
		initTexturePanel(width, height);
		initAnimPanel(width, height);

		Label title = new Label(gui, "");
		editor.setNameDisplay.add(title::setText);
		title.setBounds(new Box(5, 8, 0, 0));
		this.topPanel.add(title);

		editor.updateGui();
		if(smallGuiWarning && (height < 420 || width < 500)) {
			openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.warning"), gui.i18nFormat("label.cpm.gui_scale_too_large")));
		}
		smallGuiWarning = false;
	}

	private void initModelPanel(int width, int height) {
		Panel mainPanel = new Panel(gui);
		mainPanel.setBounds(new Box(0, 0, width, height - 20));
		ScrollPanel sp = new ScrollPanel(gui);
		sp.setDisplay(new PosPanel(gui, this));
		sp.setBounds(new Box(0, 0, 170, height - 20));
		mainPanel.addElement(sp);
		topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.model"), mainPanel));

		mainPanel.addElement(new TreePanel(gui, this, width, height - 20));

		ViewportPanel view = new ViewportPanel(gui, editor);
		view.setBounds(new Box(170, 0, width - 170 - 150, height - 20));
		mainPanel.addElement(view);
		editor.displayViewport.add(view::setEnabled);
	}

	private void initTexturePanel(int width, int height) {
		Panel textureEditor = new Panel(gui);
		textureEditor.setBounds(new Box(0, 0, width, height - 20));
		topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.texture"), textureEditor));

		ViewportPaintPanel viewT = new ViewportPaintPanel(gui, editor);
		viewT.setBounds(new Box(0, 0, width - height / 2, height - 20));
		textureEditor.addElement(viewT);
		editor.cursorPos = viewT::getHoveredTexPos;
		editor.displayViewport.add(viewT::setEnabled);

		TextureDisplayPanel tdp = new TextureDisplayPanel(gui, editor, height / 2);
		tdp.setBounds(new Box(width - height / 2, 0, height / 2, height / 2));
		textureEditor.addElement(tdp);

		Panel p = new Panel(gui);
		int treeW = Math.min(150, height / 2);
		p.setBounds(new Box(width - treeW, height / 2, treeW, height / 2));
		Tree<TreeElement> tree = new Tree<>(this, new ModelTree(editor));
		tree.setBounds(new Box(0, 0, treeW, height / 2 - 25));
		p.addElement(tree);
		p.setBackgroundColor(gui.getColors().panel_background);
		editor.updateGui.add(tree::updateTree);
		textureEditor.addElement(p);

		textureEditor.addElement(new DrawToolsPanel(this, width - height / 2, height / 2, height / 2 - treeW, height / 2));

		ButtonIcon visBtn = new ButtonIcon(gui, "editor", 42, 16, editor::switchVis);
		visBtn.setBounds(new Box(5, height / 2 - 44, 18, 18));
		p.addElement(visBtn);
		editor.setVis.add(b -> {
			if(b == null) {
				visBtn.setEnabled(false);
				visBtn.setU(42);
			} else {
				visBtn.setEnabled(true);
				visBtn.setU(b ? 42 : 28);
			}
		});
	}

	private void initAnimPanel(int width, int height) {
		Panel mainPanel = new Panel(gui);
		mainPanel.setBounds(new Box(0, 0, width, height - 20));

		Panel buttonsPanel = new Panel(gui);
		buttonsPanel.setBounds(new Box(0, 0, 170, 20));
		buttonsPanel.setBackgroundColor(gui.getColors().menu_bar_background);

		HorizontalLayout buttons = new HorizontalLayout(buttonsPanel);
		TabbedPanelManager animPanelTabs = new TabbedPanelManager(gui);

		ScrollPanel spSetup = new ScrollPanel(gui);
		spSetup.setBounds(new Box(0, 0, 170, height - 40));
		spSetup.setDisplay(new AnimPanel(gui, this));

		ScrollPanel spTest = new ScrollPanel(gui);
		spTest.setBounds(new Box(0, 0, 170, height - 40));
		spTest.setDisplay(new AnimTestPanel(gui, this));

		buttons.add(animPanelTabs.createTab(gui.i18nFormat("tab.cpm.animation.setup"), spSetup));
		buttons.add(animPanelTabs.createTab(gui.i18nFormat("tab.cpm.animation.test"), spTest));

		animPanelTabs.setBounds(new Box(0, 20, 170, height - 40));
		mainPanel.addElement(animPanelTabs);
		mainPanel.addElement(buttonsPanel);

		topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.animation"), mainPanel));

		mainPanel.addElement(new TreePanel(gui, this, width, height - 20) {

			@Override
			public void draw(int mouseX, int mouseY, float partialTicks) {
				editor.applyAnim = true;
				super.draw(mouseX, mouseY, partialTicks);
				editor.applyAnim = false;
			}
		});

		ViewportPanelAnim view = new ViewportPanelAnim(gui, editor);
		view.setBounds(new Box(170, 0, width - 170 - 150, height - 20));
		mainPanel.addElement(view);
		editor.displayViewport.add(view::setEnabled);
	}

	private void newModel(SkinType type) {
		checkUnsaved(() -> {
			this.editor.loadDefaultPlayerModel();
			this.editor.customSkinType = true;
			this.editor.skinType = type;
			this.editor.updateGui();
		});
	}

	private void initFileMenu() {
		PopupMenu pp = new PopupMenu(gui);
		topPanel.add(new Button(gui, gui.i18nFormat("button.cpm.file"), () -> pp.display(this, 0, 20)));

		PopupMenu newMenu = new PopupMenu(gui);
		PopupMenu newModelMenu = new PopupMenu(gui);
		int x = topPanel.getX();

		newMenu.addButton(gui.i18nFormat("button.cpm.new.model"), () -> newModelMenu.display(this, x, 20));

		for (SkinType type : SkinType.VALUES) {
			if(type == SkinType.UNKNOWN)continue;
			newModelMenu.addButton(gui.i18nFormat("label.cpm.skin_type." + type.getName()), () -> newModel(type));
		}

		Button newTempl = newMenu.addButton(gui.i18nFormat("button.cpm.new.template"), () -> checkUnsaved(() -> {
			this.editor.loadDefaultPlayerModel();
			editor.templateSettings = new TemplateSettings(editor);
			editor.setupTemplateModel();
			editor.skinProvider.setImage(new Image(64, 64));
			editor.skinProvider.texture.markDirty();
			this.editor.updateGui();
		}));
		newTempl.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.new.template")));

		pp.addButton(gui.i18nFormat("button.cpm.file.new"), () -> newMenu.display(this, x, 20));

		pp.addButton(gui.i18nFormat("button.cpm.file.load"), () -> checkUnsaved(() -> {
			FileChooserGui fc = new FileChooserGui(this);
			fc.setTitle(gui.i18nFormat("label.cpm.loadFile"));
			fc.setFileDescText(gui.i18nFormat("label.cpm.file_project"));
			fc.setFilter((f, n) -> n.endsWith(".cpmproject") && !f.isDirectory());
			fc.setAccept(this::load);
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			openPopup(fc);
		}));

		pp.addButton(gui.i18nFormat("button.cpm.file.save"), () -> {
			if(editor.file != null) {
				save(editor.file);
			} else {
				FileChooserGui fc = new FileChooserGui(this);
				fc.setTitle(gui.i18nFormat("label.cpm.saveFile"));
				fc.setFileDescText(gui.i18nFormat("label.cpm.file_project"));
				fc.setFilter((f, n) -> n.endsWith(".cpmproject") && !f.isDirectory());
				fc.setSaveDialog(true);
				fc.setExtAdder(n -> n + ".cpmproject");
				fc.setFileName(editor.file != null ? editor.file.getName() : gui.i18nFormat("label.cpm.new_project"));
				fc.setAccept(this::save);
				fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
				openPopup(fc);
			}
		});

		pp.addButton(gui.i18nFormat("button.cpm.file.saveAs"), () -> {
			FileChooserGui fc = new FileChooserGui(this);
			fc.setTitle(gui.i18nFormat("label.cpm.saveFile"));
			fc.setFileDescText(gui.i18nFormat("label.cpm.file_project"));
			fc.setFilter((f, n) -> n.endsWith(".cpmproject") && !f.isDirectory());
			fc.setSaveDialog(true);
			fc.setExtAdder(n -> n + ".cpmproject");
			fc.setAccept(this::save);
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			openPopup(fc);
		});

		pp.addButton(gui.i18nFormat("button.cpm.file.export"), () -> openPopup(ExportSkinPopup.createPopup(this)));

		pp.addButton(gui.i18nFormat("button.cpm.file.test"), () -> {
			if(editor.dirty) {
				openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.must_save.test")));
				return;
			}
			try {
				Runnable open;
				if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED) {
					open = () -> {
						MinecraftClientAccess.get().sendSkinUpdate();
						openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.info"), gui.i18nFormat("label.cpm.test_model_exported")));
					};
				} else if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.OFFLINE)
					open = MinecraftClientAccess.get().openSingleplayer();
				else
					throw new UnsupportedOperationException();
				Exporter.exportTempModel(editor, this);
				String model = ModConfig.getConfig().getString(ConfigKeys.SELECTED_MODEL, null);
				if(model != null) {
					ModConfig.getConfig().setString(ConfigKeys.SELECTED_MODEL_OLD, model);
				} else {
					ModConfig.getConfig().clearValue(ConfigKeys.SELECTED_MODEL_OLD);
				}
				ModConfig.getConfig().setString(ConfigKeys.SELECTED_MODEL, Exporter.TEMP_MODEL);
				open.run();
				ModConfig.getConfig().setString(ConfigKeys.REOPEN_PROJECT, editor.file.getAbsolutePath());
				ModConfig.getConfig().save();
				toReopen = editor;
			} catch (UnsupportedOperationException e) {
				openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.test_unsupported")));
			}
		});

		pp.addButton(gui.i18nFormat("button.cpm.file.exit"), gui::close);
	}

	private void initEditMenu() {
		PopupMenu pp = new PopupMenu(gui);
		int x = topPanel.getX();
		topPanel.add(new Button(gui, gui.i18nFormat("button.cpm.edit"), () -> pp.display(this, x, 20)));

		Button undo = pp.addButton(gui.i18nFormat("button.cpm.edit.undo"), editor::undo);
		editor.setUndoEn.add(undo::setEnabled);

		Button redo = pp.addButton(gui.i18nFormat("button.cpm.edit.redo"), editor::redo);
		editor.setRedoEn.add(redo::setEnabled);

		pp.add(new Label(gui, "=========").setBounds(new Box(5, 5, 0, 0)));

		Button convertToCustom = pp.addButton(gui.i18nFormat("button.cpm.edit.convert_model_custom"), editor::convertModel);
		convertToCustom.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.edit.convert_model_custom")));

		pp.addButton(gui.i18nFormat("button.cpm.edit.add_skin_layer2"), editor::addSkinLayer);

		pp.addButton(gui.i18nFormat("button.cpm.edit.add_template"), new InputPopup(this, gui.i18nFormat("label.cpm.template_link_input"), gui.i18nFormat("label.cpm.template_link_input.desc"), link -> {
			new ProcessPopup<>(this, gui.i18nFormat("label.cpm.loading_template"), gui.i18nFormat("label.cpm.loading_template.desc"), () -> {
				return EditorTemplate.create(editor, link);
			}, t -> {
				editor.templates.add(t);
				editor.restitchTexture();
				editor.markDirty();
				editor.updateGui();
			}, e -> {
				if(e == null)return;
				e.printStackTrace();
				openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.template_load_error", e.getMessage())));
			}).start();
		}, null));

		pp.addButton(gui.i18nFormat("button.cpm.edit.convert2template"), () -> {
			if (editor.templateSettings == null) {
				if (editor.dirty) {
					openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.info"), gui.i18nFormat("label.cpm.must_save")));
				} else {
					if(editor.file == null)
						setupTemplate();
					else
						openPopup(new ConfirmPopup(this, gui.i18nFormat("label.cpm.warning"), gui.i18nFormat("label.cpm.warn_c2t"), this::setupTemplate, null));
				}
			}
		});

		pp.addButton(gui.i18nFormat("button.cpm.edit.settings"), () -> openPopup(new SettingsPopup(this)));

		pp.addButton(gui.i18nFormat("button.cpm.models"), () -> openPopup(new SkinsPopup(this)));

		pp.addButton(gui.i18nFormat("button.cpm.edit.controls"), () -> openPopup(new MessagePopup(gui, gui.i18nFormat("button.cpm.edit.controls"), gui.i18nFormat("label.cpm.controls.text"))));
	}

	private void setupTemplate() {
		editor.templateSettings = new TemplateSettings(editor);
		editor.setupTemplateModel();
		editor.markDirty();
		editor.updateGui();
	}

	private void initEffectMenu() {
		PopupMenu pp = new PopupMenu(gui);
		int x = topPanel.getX();
		topPanel.add(new Button(gui, gui.i18nFormat("button.cpm.effect"), () -> pp.display(this, x, 20)));

		Checkbox boxGlow = new Checkbox(gui, gui.i18nFormat("label.cpm.glow"));
		boxGlow.setAction(editor::switchGlow);
		editor.setGlow.add(b -> {
			boxGlow.setEnabled(b != null);
			if(b != null)boxGlow.setSelected(b);
			else boxGlow.setSelected(false);
		});
		boxGlow.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.glow")));
		pp.add(boxGlow);

		Checkbox boxReColor = new Checkbox(gui, gui.i18nFormat("label.cpm.recolor"));
		boxReColor.setAction(editor::switchReColorEffect);
		editor.setReColor.add(b -> {
			boxReColor.setEnabled(b != null);
			if(b != null)boxReColor.setSelected(b);
			else boxReColor.setSelected(false);
		});
		pp.add(boxReColor);

		ColorButton colorBtn = new ColorButton(gui, editor.frame, editor::setColor);
		editor.setPartColor.add(c -> {
			colorBtn.setEnabled(c != null);
			if(c != null)colorBtn.setColor(c);
			else colorBtn.setColor(0);
		});
		pp.add(colorBtn);

		Checkbox boxHidden = new Checkbox(gui, gui.i18nFormat("label.cpm.hidden_effect"));
		boxHidden.setAction(editor::switchHide);
		editor.setHiddenEffect.add(b -> {
			boxHidden.setEnabled(b != null);
			if(b != null)boxHidden.setSelected(b);
			else boxHidden.setSelected(false);
		});
		pp.add(boxHidden);
	}

	private void initDisplayMenu() {
		PopupMenu pp = new PopupMenu(gui);
		int x = topPanel.getX();
		topPanel.add(new Button(gui, gui.i18nFormat("button.cpm.display"), () -> pp.display(this, x, 20)));

		Checkbox chxbxBase = new Checkbox(gui, gui.i18nFormat("label.cpm.display.drawBase"));
		pp.add(chxbxBase);
		chxbxBase.setSelected(editor.renderBase);
		chxbxBase.setAction(() -> {
			editor.renderBase = !chxbxBase.isSelected();
			chxbxBase.setSelected(editor.renderBase);
		});

		Checkbox chxbxTpose = new Checkbox(gui, gui.i18nFormat("label.cpm.display.player_tpose"));
		pp.add(chxbxTpose);
		chxbxTpose.setSelected(editor.playerTpose);
		chxbxTpose.setAction(() -> {
			editor.playerTpose = !chxbxTpose.isSelected();
			chxbxTpose.setSelected(editor.playerTpose);
		});

		Checkbox chxbxAllUVs = new Checkbox(gui, gui.i18nFormat("label.cpm.display.allUVs"));
		pp.add(chxbxAllUVs);
		chxbxAllUVs.setSelected(editor.drawAllUVs);
		chxbxAllUVs.setAction(() -> {
			editor.drawAllUVs = !chxbxAllUVs.isSelected();
			chxbxAllUVs.setSelected(editor.drawAllUVs);
		});

		Checkbox chxbxFilterDraw = new Checkbox(gui, gui.i18nFormat("label.cpm.display.onlyDrawOnSelected"));
		pp.add(chxbxFilterDraw);
		chxbxFilterDraw.setSelected(editor.onlyDrawOnSelected);
		chxbxFilterDraw.setAction(() -> {
			editor.onlyDrawOnSelected = !chxbxFilterDraw.isSelected();
			chxbxFilterDraw.setSelected(editor.onlyDrawOnSelected);
		});
		chxbxFilterDraw.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.display.onlyDrawOnSelected")));
	}

	private void load(File file) {
		try {
			editor.load(file);
		} catch (Exception e) {
			e.printStackTrace();
			showError("load", e.toString());
			editor.loadDefaultPlayerModel();
		}
	}

	private void save(File file) {
		try {
			editor.save(file);
		} catch (Exception e) {
			showError("save", e.toString());
		}
	}

	public void loadSkin(File file) {
		EditorTexture tex = editor.getTextureProvider();
		if(tex != null) {
			tex.file = file;
			editor.reloadSkin();
		}
	}

	private void showError(String msg, String error) {
		openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error." + msg), error));
	}

	public Editor getEditor() {
		return editor;
	}

	@Override
	public Box getMinBounds() {
		return new Box(0, 0, 500, 420);
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		super.keyPressed(event);
		if(!event.isConsumed()) {
			if(gui.isCtrlDown()) {
				if(event.matches("z")) {
					editor.undo();
					event.consume();
				}
				if(event.matches("y")) {
					editor.redo();
					event.consume();
				}
			}
			if(event.keyCode == gui.getKeyCodes().KEY_F5) {
				editor.restitchTexture();
				editor.animations.forEach(EditorAnim::clearCache);
			}
		}
	}

	public static int getRotateMouseButton() {
		return ModConfig.getConfig().getSetInt(ConfigKeys.EDITOR_ROTATE_MOUSE_BUTTON, 2);
	}

	public static boolean doOpenEditor() {
		return toReopen != null || ModConfig.getConfig().getString(ConfigKeys.REOPEN_PROJECT, null) != null;
	}
}
