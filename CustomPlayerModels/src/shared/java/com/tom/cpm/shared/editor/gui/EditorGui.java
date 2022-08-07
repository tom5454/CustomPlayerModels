package com.tom.cpm.shared.editor.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tom.cpl.config.ConfigEntry.ConfigEntryList;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.InputPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpl.gui.elements.ProcessPopup;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.ButtonGroup;
import com.tom.cpl.gui.util.ElementGroup;
import com.tom.cpl.gui.util.HorizontalLayout;
import com.tom.cpl.gui.util.TabFocusHandler;
import com.tom.cpl.gui.util.TabbedPanelManager;
import com.tom.cpl.math.Box;
import com.tom.cpl.text.KeybindText;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpl.util.Pair;
import com.tom.cpm.externals.org.apache.maven.artifact.versioning.ComparableVersion;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.editor.DisplayItem;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.Effect;
import com.tom.cpm.shared.editor.Generators;
import com.tom.cpm.shared.editor.RootGroups;
import com.tom.cpm.shared.editor.TestIngameManager;
import com.tom.cpm.shared.editor.gui.popup.ChangelogPopup;
import com.tom.cpm.shared.editor.gui.popup.ColorButton;
import com.tom.cpm.shared.editor.gui.popup.DescPopup;
import com.tom.cpm.shared.editor.gui.popup.ErrorLogPopup;
import com.tom.cpm.shared.editor.gui.popup.ExportPopup;
import com.tom.cpm.shared.editor.gui.popup.FirstStartPopup;
import com.tom.cpm.shared.editor.gui.popup.ModelsPopup;
import com.tom.cpm.shared.editor.gui.popup.SettingsPopup;
import com.tom.cpm.shared.editor.gui.popup.WikiBrowserPopup;
import com.tom.cpm.shared.editor.template.EditorTemplate;
import com.tom.cpm.shared.editor.template.TemplateSettings;
import com.tom.cpm.shared.gui.Keybinds;
import com.tom.cpm.shared.gui.KeybindsPopup;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.paste.PastePopup;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.PlayerModelLayer;

public class EditorGui extends Frame {
	public static boolean rescaleGui = true;
	private static List<File> recent = new ArrayList<>();
	private static Editor toReopen;
	private TabbedPanelManager tabs;
	private HorizontalLayout topPanel;
	private Editor editor;
	private static boolean smallGuiWarning = true;
	private static boolean notSupportedWarning = true;
	private static boolean showFirstStart;
	private static String showChangelog;
	private static boolean showNewVersionPopup;
	private Tooltip msgTooltip;
	private long tooltipTime;

	static {
		ConfigEntryList ce = ModConfig.getCommonConfig().getEntryList(ConfigKeys.EDITOR_RECENT_PROJECTS);
		for (int i = 0;i<ce.size();i++) {
			String o = String.valueOf(ce.get(i));
			File f = new File(o);
			if(f.exists()) {
				recent.add(f);
			}
		}
		String last = ModConfig.getCommonConfig().getString(ConfigKeys.EDITOR_LAST_VERSION, null);
		if(last == null)showFirstStart = true;
		else if(!MinecraftObjectHolder.DEBUGGING) {
			ComparableVersion l = new ComparableVersion(last);
			ComparableVersion c = new ComparableVersion(MinecraftCommonAccess.get().getModVersion());
			if(c.compareTo(l) > 0)showChangelog = last;
			else {
				showNewVersionPopup = MinecraftCommonAccess.get().getVersionCheck().isOutdated();
			}
		}
		ModConfig.getCommonConfig().setString(ConfigKeys.EDITOR_LAST_VERSION, MinecraftCommonAccess.get().getModVersion());
		ModConfig.getCommonConfig().save();
	}

	private static void flushRecent() {
		ConfigEntryList ce = ModConfig.getCommonConfig().getEntryList(ConfigKeys.EDITOR_RECENT_PROJECTS);
		ce.clear();
		recent.stream().filter(File::exists).map(File::getAbsolutePath).forEach(ce::add);
		ModConfig.getCommonConfig().save();
	}

	public EditorGui(IGui gui) {
		super(gui);
		rescaleGui = true;
		if(toReopen != null) {
			this.editor = toReopen;
			this.editor.setGui(this);
			if(MinecraftClientAccess.get().getServerSideStatus() != ServerStatus.INSTALLED) {
				toReopen = null;
				ModConfig.getCommonConfig().clearValue(ConfigKeys.REOPEN_PROJECT);
				ModConfig.getCommonConfig().save();
			}
		} else {
			this.editor = new Editor();
			this.editor.setGui(this);
			String reopen = ModConfig.getCommonConfig().getString(ConfigKeys.REOPEN_PROJECT, null);
			if(reopen != null) {
				ModConfig.getCommonConfig().clearValue(ConfigKeys.REOPEN_PROJECT);
				ModConfig.getCommonConfig().save();
				load(new File(reopen));
			} else {
				this.editor.loadDefaultPlayerModel();
			}
		}
		TestIngameManager.checkConfig();
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
		int scale = ModConfig.getCommonConfig().getInt(ConfigKeys.EDITOR_SCALE, -1);
		if(scale != -1 && rescaleGui) {
			if(gui.getScale() != scale) {
				gui.setScale(scale);
				return;
			}
		}

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
			openPopup(new MessagePopup(this, gui.i18nFormat("label.cpm.warning"), gui.i18nFormat("label.cpm.gui_scale_too_large")));
		}
		smallGuiWarning = false;

		if(notSupportedWarning && !PlatformFeature.EDITOR_SUPPORTED.isSupported()) {
			openPopup(new MessagePopup(this, gui.i18nFormat("label.cpm.warning"), gui.i18nFormat("label.cpm.editor_not_supported")));
		}
		notSupportedWarning = false;

		if(showFirstStart) {
			openPopup(new FirstStartPopup(gui));
			showFirstStart = false;
		}

		if(showChangelog != null) {
			openPopup(new ChangelogPopup(gui, showChangelog));
			showChangelog = null;
		}

		if(showNewVersionPopup) {
			openPopup(new MessagePopup(this, gui.i18nFormat("label.cpm.changelog.newVersion.title"), gui.i18nFormat("label.cpm.changelog.newVersion.desc")));
			showNewVersionPopup = false;
		}

		editor.setInfoMsg.add(p -> {
			msgTooltip = new Tooltip(this, gui.wordWrap(p.getValue(), width));
			tooltipTime = System.currentTimeMillis() + p.getKey();
		});

		MinecraftClientAccess.get().populatePlatformSettings("editor", this);
	}

	private void initModelPanel(int width, int height) {
		Panel mainPanel = new Panel(gui);
		mainPanel.setBounds(new Box(0, 0, width, height - 20));
		ScrollPanel sp = new ScrollPanel(gui);
		sp.setDisplay(new PosPanel(gui, this));
		sp.setBounds(new Box(0, 0, 170, height - 20));
		sp.setScrollBarSide(true);
		mainPanel.addElement(sp);
		topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.model"), mainPanel));

		mainPanel.addElement(new TreePanel(gui, this, width, height - 20, true));

		ViewportPanel view = new ViewportPanel(this, editor);
		view.setBounds(new Box(170, 0, width - 170 - 150, height - 20));
		mainPanel.addElement(view);
		editor.displayViewport.add(view::setEnabled);
	}

	private void initTexturePanel(int width, int height) {
		Panel textureEditor = new Panel(gui);
		textureEditor.setBounds(new Box(0, 0, width, height - 20));
		topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.texture"), textureEditor));

		ViewportPaintPanel viewT = new ViewportPaintPanel(this, editor);
		viewT.setBounds(new Box(0, 0, width - height / 2, height - 20));
		textureEditor.addElement(viewT);
		editor.cursorPos = viewT::getHoveredTexPos;
		editor.displayViewport.add(viewT::setEnabled);

		TextureEditorPanel tdp = new TextureEditorPanel(gui, editor, height / 2);
		tdp.setBounds(new Box(width - height / 2, 0, height / 2, height / 2));
		textureEditor.addElement(tdp);

		int treeW = Math.min(150, height / 2);
		TreePanel treePanel = new TreePanel(gui, this, treeW, height / 2 - 20, false);
		treePanel.setBounds(new Box(width - treeW, height / 2, treeW, height / 2));
		textureEditor.addElement(treePanel);

		textureEditor.addElement(new DrawToolsPanel(this, width - height / 2, height / 2, height / 2 - treeW, height / 2));

		Panel uvPanel = new Panel(gui);
		uvPanel.setBounds(new Box(0, height, 170, 0));
		uvPanel.setBackgroundColor(gui.getColors().panel_background);
		ElementGroup<ModeDisplayType, GuiElement> group = new ElementGroup<>(GuiElement::setVisible);
		editor.setModePanel.add(group);
		editor.setModePanel.add(mdt -> {
			uvPanel.setVisible(mdt != ModeDisplayType.NULL);
			if(mdt != ModeDisplayType.NULL) {
				int h = group.getFirst(mdt).getBounds().h + 5;
				uvPanel.setBounds(new Box(0, height - 20 - h, 175, h));
			}
		});
		TabFocusHandler tabHandler = new TabFocusHandler(gui);
		Panel panel = new Panel(gui);
		panel.setBounds(new Box(0, 5, 170, 100));
		for (ModeDisplayType mdt : ModeDisplayType.VALUES) {
			if(mdt != ModeDisplayType.NULL) {
				Panel p = mdt.factory.apply(this, editor, tabHandler);
				panel.addElement(p);
				group.addElement(mdt, p);
			}
		}
		uvPanel.addElement(panel);
		uvPanel.addElement(tabHandler);
		textureEditor.addElement(uvPanel);
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
		spSetup.setScrollBarSide(true);

		ScrollPanel spTest = new ScrollPanel(gui);
		spTest.setBounds(new Box(0, 0, 170, height - 40));
		spTest.setDisplay(new AnimTestPanel(gui, this));
		spTest.setScrollBarSide(true);

		buttons.add(animPanelTabs.createTab(gui.i18nFormat("tab.cpm.animation.setup"), spSetup));
		buttons.add(animPanelTabs.createTab(gui.i18nFormat("tab.cpm.animation.test"), spTest));

		animPanelTabs.setBounds(new Box(0, 20, 170, height - 40));
		mainPanel.addElement(animPanelTabs);
		mainPanel.addElement(buttonsPanel);

		topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.animation"), mainPanel));

		mainPanel.addElement(new TreePanel(gui, this, width, height - 20, false) {

			@Override
			public void draw(MouseEvent event, float partialTicks) {
				editor.applyAnim = true;
				super.draw(event, partialTicks);
				editor.applyAnim = false;
			}
		});

		ViewportPanelAnim view = new ViewportPanelAnim(this, editor);
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
		PopupMenu pp = new PopupMenu(gui, this);
		topPanel.add(new Button(gui, gui.i18nFormat("button.cpm.file"), () -> pp.display(0, 20)));

		PopupMenu newMenu = new PopupMenu(gui, this);
		PopupMenu newModelMenu = new PopupMenu(gui, this);

		newMenu.addMenuButton(gui.i18nFormat("button.cpm.new.model"), newModelMenu);

		for (SkinType type : SkinType.VANILLA_TYPES) {
			newModelMenu.addButton(gui.i18nFormat("label.cpm.skin_type." + type.getName()), () -> newModel(type));
		}

		Button newTempl = newMenu.addButton(gui.i18nFormat("button.cpm.new.template"), () -> checkUnsaved(new ConfirmPopup(this, gui.i18nFormat("label.cpm.warning"), gui.i18nFormat("label.cpm.warnTemplate"), () -> {
			this.editor.loadDefaultPlayerModel();
			editor.templateSettings = new TemplateSettings(editor);
			Generators.setupTemplateModel(editor);
			ETextures skin = editor.textures.get(TextureSheetType.SKIN);
			skin.setImage(new Image(64, 64));
			skin.markDirty();
			this.editor.updateGui();
		}, null)));
		newTempl.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.new.template")));

		pp.addMenuButton(gui.i18nFormat("button.cpm.file.new"), newMenu);

		pp.addButton(gui.i18nFormat("button.cpm.file.load"), () -> checkUnsaved(() -> {
			FileChooserPopup fc = new FileChooserPopup(this);
			fc.setTitle(gui.i18nFormat("label.cpm.loadFile"));
			fc.setFileDescText(gui.i18nFormat("label.cpm.file_project"));
			fc.setFilter(new FileFilter("cpmproject"));
			fc.setAccept(this::load);
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			openPopup(fc);
		}));

		pp.addButton(gui.i18nFormat("button.cpm.file.save"), this::save);

		pp.addButton(gui.i18nFormat("button.cpm.file.saveAs"), this::saveAs);

		PopupMenu importMenu = new PopupMenu(gui, this);

		importMenu.addButton(gui.i18nFormat("button.cpm.file.import.project"), () -> {
			FileChooserPopup fc = new FileChooserPopup(this);
			fc.setTitle(gui.i18nFormat("button.cpm.file.import"));
			fc.setFileDescText(gui.i18nFormat("label.cpm.file_project"));
			fc.setFilter(new FileFilter("cpmproject"));
			fc.setAccept(f -> {});
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			openPopup(fc);
		});

		//pp.addMenuButton(gui.i18nFormat("button.cpm.file.import"), importMenu);

		pp.addButton(gui.i18nFormat("button.cpm.file.export"), () -> openPopup(ExportPopup.createPopup(this)));

		pp.addButton(gui.i18nFormat("button.cpm.file.test"), () -> {
			if(TestIngameManager.openTestIngame(this, false))toReopen = editor;
		});

		pp.addMenuButton(gui.i18nFormat("button.cpm.openRecent"), () -> {
			PopupMenu menu = new PopupMenu(gui, this);
			for (int i = recent.size() - 1; i >= 0; i--) {
				File f = recent.get(i);
				if(f.exists()) {
					menu.addButton(f.getName(), () -> checkUnsaved(() -> load(f))).setTooltip(new Tooltip(this, f.getAbsolutePath().replace('\\', '/')));
				}
			}
			return menu;
		});

		pp.addButton(gui.i18nFormat("button.cpm.file.exit"), gui::close);

		MinecraftClientAccess.get().populatePlatformSettings("filePopup", pp);
	}

	private void save() {
		if(editor.file != null) {
			saveProject(editor.file);
		} else {
			saveAs();
		}
	}

	private void saveAs() {
		FileChooserPopup fc = new FileChooserPopup(this);
		fc.setTitle(gui.i18nFormat("label.cpm.saveFile"));
		fc.setFileDescText(gui.i18nFormat("label.cpm.file_project"));
		fc.setFilter(new FileFilter("cpmproject"));
		fc.setSaveDialog(true);
		fc.setExtAdder(n -> n + ".cpmproject");
		fc.setAccept(this::saveProject);
		fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
		openPopup(fc);
	}

	private void initEditMenu() {
		PopupMenu pp = new PopupMenu(gui, this);
		int x = topPanel.getX();
		topPanel.add(new Button(gui, gui.i18nFormat("button.cpm.edit"), () -> pp.display(x, 20)));

		Button undo = pp.addButton(gui.i18nFormat("button.cpm.edit.undo"), editor::undo);
		editor.setUndo.add(t -> {
			undo.setEnabled(t != null);
			undo.setText(t == null || t.isEmpty() ? gui.i18nFormat("button.cpm.edit.undo") : gui.i18nFormat("button.cpm.edit.undoAction", t));
		});

		Button redo = pp.addButton(gui.i18nFormat("button.cpm.edit.redo"), editor::redo);
		editor.setRedo.add(t -> {
			redo.setEnabled(t != null);
			redo.setText(t == null || t.isEmpty() ? gui.i18nFormat("button.cpm.edit.redo") : gui.i18nFormat("button.cpm.edit.redoAction", t));
		});

		PopupMenu tools = new PopupMenu(gui, this);
		pp.addMenuButton(gui.i18nFormat("button.cpm.edit.tools"), tools);

		Generators.generators.forEach(g -> {
			Button btn = tools.addButton(gui.i18nFormat(g.name), () -> g.func.accept(this));
			if(g.tooltip != null)btn.setTooltip(new Tooltip(this, g.tooltip.toString(gui)));
		});

		pp.addButton(gui.i18nFormat("button.cpm.edit.add_template"), new InputPopup(this, gui.i18nFormat("label.cpm.template_link_input"), gui.i18nFormat("label.cpm.template_link_input.desc"), link -> {
			new ProcessPopup<>(this, gui.i18nFormat("label.cpm.loading_template"), gui.i18nFormat("label.cpm.loading_template.desc"), () -> {
				return EditorTemplate.create(editor, link);
			}, t -> {
				editor.templates.add(t);
				editor.restitchTextures();
				editor.markDirty();
				editor.updateGui();
			}, e -> {
				if(e == null)return;
				Log.warn("Failed to download template", e);
				openPopup(new MessagePopup(this, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.template_load_error", e.getMessage())));
			}).start();
		}, null));

		pp.addButton(gui.i18nFormat("label.cpm.desc"), () -> openPopup(new DescPopup(this, true, null)));

		PopupMenu parts = new PopupMenu(gui, this);
		pp.addMenuButton(gui.i18nFormat("button.cpm.edit.parts"), parts);

		RootGroups.forEach(c -> {
			parts.addButton(gui.i18nFormat("button.cpm.root_group." + c.name().toLowerCase()), () -> editor.addRoot(c));
		});

		parts.addButton(gui.i18nFormat("button.cpm.root_group.itemHoldPos"), () -> Generators.addItemHoldPos(editor));

		parts.addButton(gui.i18nFormat("button.cpm.root_group.parrots"), () -> Generators.addParrots(editor));

		pp.add(new Label(gui, "=========").setBounds(new Box(5, 5, 0, 0)));

		pp.addButton(gui.i18nFormat("button.cpm.edit.settings"), () -> openPopup(new SettingsPopup(this)));

		pp.addButton(gui.i18nFormat("button.cpm.models"), () -> openPopup(new ModelsPopup(this)));

		//pp.addButton(gui.i18nFormat("button.cpm.edit.controls"), () -> openPopup(new MessagePopup(this, gui.i18nFormat("button.cpm.edit.controls"), gui.i18nFormat("label.cpm.controls.text"))));
		pp.addButton(gui.i18nFormat("button.cpm.edit.controls"), () -> openPopup(KeybindsPopup.create(this)));

		pp.addButton(gui.i18nFormat("tab.cpm.social.errorLog"), () -> openPopup(new ErrorLogPopup(this)));

		pp.addButton(gui.i18nFormat("button.cpm.edit.pastes"), () -> new PastePopup(this).open());

		pp.addButton(gui.i18nFormat("label.cpm.wiki.title"), () -> openPopup(new WikiBrowserPopup(gui)));

		pp.addButton(gui.i18nFormat("label.cpm.changelog.title"), () -> openPopup(new ChangelogPopup(gui, null)));
	}

	private void initEffectMenu() {
		PopupMenu pp = new PopupMenu(gui, this);
		int x = topPanel.getX();
		topPanel.add(new Button(gui, gui.i18nFormat("button.cpm.effect"), () -> pp.display(x, 20)));

		pp.add(new Label(gui, gui.i18nFormat("label.cpm.effect.cubeEffects")).setBounds(new Box(5, 5, 0, 0)));

		Checkbox boxGlow = pp.addCheckbox(gui.i18nFormat("label.cpm.glow"), () -> editor.switchEffect(Effect.GLOW));
		editor.setGlow.add(boxGlow::updateState);
		boxGlow.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.glow")));

		Checkbox boxSingleTex = pp.addCheckbox(gui.i18nFormat("label.cpm.singleTex"), () -> editor.switchEffect(Effect.SINGLE_TEX));
		editor.setSingleTex.add(boxSingleTex::updateState);
		boxSingleTex.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.singleTex")));

		Checkbox boxPfUV = pp.addCheckbox(gui.i18nFormat("label.cpm.perfaceUV"), () -> editor.switchEffect(Effect.PER_FACE_UV));
		editor.setPerFaceUV.add(boxPfUV::updateState);
		boxPfUV.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.perfaceUV")));

		Checkbox boxReColor = pp.addCheckbox(gui.i18nFormat("label.cpm.recolor"), () -> editor.switchEffect(Effect.RECOLOR));
		editor.setReColor.add(boxReColor::updateState);
		boxReColor.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.recolor")));

		ColorButton colorBtn = new ColorButton(gui, editor.frame, editor::setColor);
		editor.setPartColor.add(c -> {
			colorBtn.setEnabled(c != null);
			if(c != null)colorBtn.setColor(c);
			else colorBtn.setColor(0);
		});
		colorBtn.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.recolor.colorbtn")));
		pp.add(colorBtn);

		Checkbox boxHidden = pp.addCheckbox(gui.i18nFormat("label.cpm.hidden_effect"), () -> editor.switchEffect(Effect.HIDE));
		editor.setHiddenEffect.add(boxHidden::updateState);
		boxHidden.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.hidden_effect")));

		Checkbox boxExtrude = pp.addCheckbox(gui.i18nFormat("label.cpm.extrude_effect"), () -> editor.switchEffect(Effect.EXTRUDE));
		editor.setExtrudeEffect.add(boxExtrude::updateState);
		boxExtrude.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.extrude_effect")));

		Checkbox boxCopyTransform = pp.addCheckbox(gui.i18nFormat("label.cpm.copyTransform"), () -> editor.switchEffect(Effect.COPY_TRANSFORM));
		editor.setCopyTransformEffect.add(boxCopyTransform::updateState);
		boxCopyTransform.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.copyTransform")));

		Checkbox boxDisableVanilla = pp.addCheckbox(gui.i18nFormat("label.cpm.disableVanillaAnim"), () -> editor.switchEffect(Effect.DISABLE_VANILLA_ANIM));
		editor.setDisableVanillaEffect.add(boxDisableVanilla::updateState);
		boxDisableVanilla.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.disableVanillaAnim")));

		pp.add(new Label(gui, gui.i18nFormat("label.cpm.effect.modelEffects")).setBounds(new Box(5, 5, 0, 0)));

		Checkbox chxbxScale = pp.addCheckbox(gui.i18nFormat("label.cpm.effect.scaling"), () -> {
			editor.action("switch", "label.cpm.effect.scaling").
			updateValueOp(editor, editor.scalingElem.enabled, !editor.scalingElem.enabled, (a, b) -> a.scalingElem.enabled = b).execute();
			editor.updateGui();
		});
		editor.updateGui.add(() -> chxbxScale.setSelected(editor.scalingElem.enabled));
		chxbxScale.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.effect.scaling")));

		Checkbox hideHead = pp.addCheckbox(gui.i18nFormat("label.cpm.effect.hideHeadIfSkull"), () -> {
			editor.action("switch", "label.cpm.effect.hideHeadIfSkull").
			updateValueOp(editor, editor.hideHeadIfSkull, !editor.hideHeadIfSkull, (a, b) -> a.hideHeadIfSkull = b).execute();
			editor.updateGui();
		});
		editor.updateGui.add(() -> hideHead.setSelected(editor.hideHeadIfSkull));
		hideHead.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.effect.hideHeadIfSkull")));

		Checkbox removeArmorOffset = pp.addCheckbox(gui.i18nFormat("label.cpm.effect.removeArmorOffset"), () -> {
			editor.action("switch", "label.cpm.effect.removeArmorOffset").
			updateValueOp(editor, editor.removeArmorOffset, !editor.removeArmorOffset, (a, b) -> a.removeArmorOffset = b).execute();
			editor.updateGui();
		});
		editor.updateGui.add(() -> removeArmorOffset.setSelected(editor.removeArmorOffset));
		removeArmorOffset.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.effect.removeArmorOffset")));

		String fpText = gui.i18nFormat("tooltip.cpm.effect.setFpHandPos", new KeybindText("key.cpm.gestureMenu", "gestureMenu").toString(gui));
		Button fpHand = pp.addButton(gui.i18nFormat("button.cpm.effect.setFpHandPos"), new MessagePopup(this, gui.i18nFormat("label.cpm.info"), fpText));
		fpHand.setTooltip(new Tooltip(this, fpText));

		pp.add(new Label(gui, gui.i18nFormat("label.cpm.effect.textureEffects")).setBounds(new Box(5, 5, 0, 0)));

		Button btnAddAnimTex = pp.addButton(gui.i18nFormat("button.cpm.addAnimatedTex"), editor::addAnimTex);
		btnAddAnimTex.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.addAnimatedTex")));
		editor.setEnAddAnimTex.add(btnAddAnimTex::setEnabled);
	}

	private void initDisplayMenu() {
		PopupMenu pp = new PopupMenu(gui, this);
		int x = topPanel.getX();
		topPanel.add(new Button(gui, gui.i18nFormat("button.cpm.display"), () -> pp.display(x, 20)));

		editor.renderBase.makeCheckbox(pp, gui.i18nFormat("label.cpm.display.drawBase"));

		editor.playerTpose.makeCheckbox(pp, gui.i18nFormat("label.cpm.display.player_tpose"));

		editor.drawAllUVs.makeCheckbox(pp, gui.i18nFormat("label.cpm.display.allUVs"));

		Checkbox chxbxFilterDraw = editor.onlyDrawOnSelected.makeCheckbox(pp, gui.i18nFormat("label.cpm.display.onlyDrawOnSelected"));
		chxbxFilterDraw.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.display.onlyDrawOnSelected")));

		Checkbox chxbxVanillaAnims = editor.playVanillaAnims.makeCheckbox(pp, gui.i18nFormat("label.cpm.display.playVanillaAnims"));
		chxbxVanillaAnims.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.display.playVanillaAnims")));

		Checkbox chxbxAnimatedTex = editor.playAnimatedTex.makeCheckbox(pp, gui.i18nFormat("label.cpm.display.playAnimatedTex"));
		editor.playAnimatedTex.add(() -> {
			if(!editor.playAnimatedTex.get())
				editor.textures.values().forEach(ETextures::refreshTexture);
		});
		chxbxAnimatedTex.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.display.playAnimatedTex")));

		editor.drawBoundingBox.makeCheckbox(pp, gui.i18nFormat("label.cpm.display.drawBoundingBox"));

		Checkbox chxbxChat = pp.addCheckbox(gui.i18nFormat("label.cpm.display.displayChat"), b -> {
			editor.displayChat = !b.isSelected();
			b.setSelected(editor.displayChat);
		});
		chxbxChat.setSelected(editor.displayChat);

		Checkbox chxbxAdvScaling = pp.addCheckbox(gui.i18nFormat("label.cpm.display.advScaling"), b -> {
			editor.displayAdvScaling = !b.isSelected();
			b.setSelected(editor.displayAdvScaling);
			editor.updateGui();
		});
		chxbxAdvScaling.setSelected(editor.displayAdvScaling);

		Checkbox chxbxForceItemInAnim = editor.forceHeldItemInAnim.makeCheckbox(pp, gui.i18nFormat("label.cpm.display.forceItemInAnim"));
		editor.forceHeldItemInAnim.add(editor::updateGui);
		chxbxForceItemInAnim.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.display.forceItemInAnim")));

		Checkbox chxbxDisplayGizmo = editor.displayGizmo.makeCheckbox(pp, gui.i18nFormat("label.cpm.display.displayGizmo"));
		editor.displayGizmo.add(editor::updateGui);
		chxbxDisplayGizmo.setTooltip(new Tooltip(this, gui.i18nFormat("tooltip.cpm.display.displayGizmo", Keybinds.TOGGLE_GIZMO.getSetKey(gui))));

		pp.add(new Label(gui, gui.i18nFormat("label.cpm.display.items")).setBounds(new Box(5, 5, 0, 0)));

		PopupMenu heldItemRight = new PopupMenu(gui, this);
		pp.addMenuButton(gui.i18nFormat("button.cpm.display.heldItem.right"), heldItemRight);
		initHeldItemPopup(heldItemRight, ItemSlot.RIGHT_HAND);

		PopupMenu heldItemLeft = new PopupMenu(gui, this);
		pp.addMenuButton(gui.i18nFormat("button.cpm.display.heldItem.left"), heldItemLeft);
		initHeldItemPopup(heldItemLeft, ItemSlot.LEFT_HAND);

		PopupMenu heldItemHead = new PopupMenu(gui, this);
		pp.addMenuButton(gui.i18nFormat("button.cpm.display.heldItem.head"), heldItemHead);
		initHeldItemPopup(heldItemHead, ItemSlot.HEAD);

		pp.add(new Label(gui, gui.i18nFormat("label.cpm.display.layers")).setBounds(new Box(5, 5, 0, 0)));

		addLayerToggle(pp, PlayerModelLayer.CAPE);

		PopupMenu armor = new PopupMenu(gui, this);
		pp.addMenuButton(gui.i18nFormat("button.cpm.display.armor"), armor);
		for (PlayerModelLayer a : PlayerModelLayer.ARMOR) {
			addLayerToggle(armor, a);
		}
		armor.addButton(gui.i18nFormat("button.cpm.display.toggleArmor"), () -> {
			if(Arrays.stream(PlayerModelLayer.ARMOR).noneMatch(r -> editor.modelDisplayLayers.stream().anyMatch(l -> l == r))) {
				for (PlayerModelLayer a : PlayerModelLayer.ARMOR)editor.modelDisplayLayers.add(a);
			} else {
				for (PlayerModelLayer a : PlayerModelLayer.ARMOR)editor.modelDisplayLayers.remove(a);
			}
			editor.updateGui();
		});

		addLayerToggle(pp, PlayerModelLayer.ELYTRA);

		editor.drawParrots.makeCheckbox(pp, gui.i18nFormat("label.cpm.display.drawParrots"));
	}

	private void addLayerToggle(PopupMenu pp, PlayerModelLayer layer) {
		Checkbox chbx = pp.addCheckbox(gui.i18nFormat("label.cpm.display." + layer.name().toLowerCase()), c -> {
			if(!c.isSelected())editor.modelDisplayLayers.add(layer);
			else editor.modelDisplayLayers.remove(layer);
			editor.updateGui();
		});
		editor.updateGui.add(() -> chbx.setSelected(editor.modelDisplayLayers.contains(layer)));
	}

	private void initHeldItemPopup(PopupMenu pp, ItemSlot hand) {
		ButtonGroup<DisplayItem, Checkbox> group = new ButtonGroup<>(Checkbox::setSelected, Checkbox::setAction, i -> editor.handDisplay.put(hand, i));
		for(DisplayItem item : DisplayItem.VALUES) {
			if(hand == ItemSlot.HEAD && !item.canBeOnHead)continue;
			group.addElement(item, r -> pp.addCheckbox(gui.i18nFormat("button.cpm.heldItem." + item.name().toLowerCase()), r));
		}
		group.accept(editor.handDisplay.getOrDefault(hand, DisplayItem.NONE));
	}

	private void load(File file) {
		editor.load(file).handleAsync((v, e) -> {
			if(e != null) {
				Log.warn("Error loading project file", e);
				ErrorLog.addFormattedLog(LogLevel.ERROR, "label.cpm.error.load", e);
				showError("load", e.toString());
				editor.setInfoMsg.accept(Pair.of(0, ""));
				editor.loadDefaultPlayerModel();
				editor.updateGui();
			}
			if(recent.contains(file)) {
				recent.remove(file);
			}
			while(recent.size() > 10)recent.remove(0);
			recent.add(file);
			flushRecent();
			return null;
		}, gui::executeLater);
	}

	private void saveProject(File file) {
		editor.save(file).handleAsync((v, e) -> {
			if(e != null) {
				Log.warn("Error saving project file", e);
				ErrorLog.addFormattedLog(LogLevel.ERROR, "label.cpm.error.save", e);
				showError("save", e.toString());
				editor.setInfoMsg.accept(Pair.of(0, ""));
			}
			if(TestIngameManager.isTesting()) {
				if(TestIngameManager.openTestIngame(this, true)) {
					editor.setInfoMsg.accept(Pair.of(2000, gui.i18nFormat("tooltip.cpm.saveSuccess", file.getName()) + "\\" + gui.i18nFormat("label.cpm.test_model_exported")));
				}
			}
			if(recent.contains(file)) {
				recent.remove(file);
			}
			while(recent.size() > 10)recent.remove(0);
			recent.add(file);
			flushRecent();
			return null;
		}, gui::executeLater);
	}

	public void loadSkin(File file) {
		ETextures tex = editor.getTextureProvider();
		if(tex != null) {
			tex.file = file;
			editor.reloadSkin();
			editor.updateGui();
		}
	}

	private void showError(String msg, String error) {
		openPopup(new MessagePopup(this, gui.i18nFormat("label.cpm.error." + msg), error));
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
		getKeybindHandler().registerKeybind(Keybinds.UNDO, editor::undo);
		getKeybindHandler().registerKeybind(Keybinds.REDO, editor::redo);
		getKeybindHandler().registerKeybind(Keybinds.SAVE, this::save);
		getKeybindHandler().registerKeybind(Keybinds.TOGGLE_GIZMO, editor.displayGizmo::toggle);
		if(!event.isConsumed()) {
			if(event.keyCode == gui.getKeyCodes().KEY_F5) {
				editor.refreshCaches();
				event.consume();
			}
		}
		super.keyPressed(event);
	}

	public static int getRotateMouseButton() {
		return ModConfig.getCommonConfig().getSetInt(ConfigKeys.EDITOR_ROTATE_MOUSE_BUTTON, 2);
	}

	public static int getDragMouseButton() {
		return ModConfig.getCommonConfig().getSetInt(ConfigKeys.EDITOR_DRAG_MOUSE_BUTTON, -1);
	}

	public static int getSelectMouseButton() {
		return ModConfig.getCommonConfig().getSetInt(ConfigKeys.EDITOR_SELECT_MOUSE_BUTTON, 0);
	}

	public static int getMenuMouseButton() {
		return ModConfig.getCommonConfig().getSetInt(ConfigKeys.EDITOR_MENU_MOUSE_BUTTON, 1);
	}

	public static boolean doOpenEditor() {
		return toReopen != null || ModConfig.getCommonConfig().getString(ConfigKeys.REOPEN_PROJECT, null) != null;
	}

	@Override
	public void tick() {
		editor.tick();
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		if(tooltipTime > System.currentTimeMillis() && msgTooltip != null) {
			msgTooltip.set();
		}
		super.draw(event, partialTicks);
	}

	@Override
	public void logMessage(String msg) {
		editor.setInfoMsg.accept(Pair.of(3000, gui.i18nFormat("tooltip.cpm.errorTooltip", msg)));
	}

	@Override
	public boolean enableChat() {
		return editor.displayChat;
	}

	public static Editor getActiveTestingEditor() {
		return toReopen;
	}
}
