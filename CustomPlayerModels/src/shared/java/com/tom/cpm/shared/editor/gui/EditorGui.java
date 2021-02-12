package com.tom.cpm.shared.editor.gui;

import java.io.File;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.popup.ColorButton;
import com.tom.cpm.shared.editor.gui.popup.ExportSkinPopup;
import com.tom.cpm.shared.editor.gui.popup.FileChooserGui;
import com.tom.cpm.shared.gui.Frame;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.ButtonIcon;
import com.tom.cpm.shared.gui.elements.Checkbox;
import com.tom.cpm.shared.gui.elements.ConfirmPopup;
import com.tom.cpm.shared.gui.elements.Label;
import com.tom.cpm.shared.gui.elements.MessagePopup;
import com.tom.cpm.shared.gui.elements.Panel;
import com.tom.cpm.shared.gui.elements.PopupMenu;
import com.tom.cpm.shared.gui.elements.PopupPanel;
import com.tom.cpm.shared.gui.util.HorizontalLayout;
import com.tom.cpm.shared.gui.util.TabbedPanelManager;
import com.tom.cpm.shared.math.Box;

public class EditorGui extends Frame {
	private TabbedPanelManager tabs;
	private HorizontalLayout topPanel;
	private Editor editor;
	private static boolean smallGuiWarning = true;

	public EditorGui(IGui gui) {
		super(gui);
		this.editor = new Editor(this);
		this.editor.loadDefaultPlayerModel();

		gui.setCloseListener(c -> {
			checkUnsaved(() -> {
				editor.skinProvider.free();
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
		mainPanel.addElement(new PosPanel(gui, this, height - 20));
		topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.model"), mainPanel));

		mainPanel.addElement(new TreePanel(gui, this, width, height - 20));

		ViewportPanel view = new ViewportPanel(gui, editor);
		view.setBounds(new Box(145, 0, width - 145 - 150, height - 20));
		mainPanel.addElement(view);
	}

	private void initTexturePanel(int width, int height) {
		Panel textureEditor = new Panel(gui);
		textureEditor.setBounds(new Box(0, 0, width, height - 20));
		topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.texture"), textureEditor));

		TextureDisplayPanel tdp = new TextureDisplayPanel(gui, editor, height / 2);
		tdp.setBounds(new Box(width - height / 2, 0, height / 2, height / 2));
		textureEditor.addElement(tdp);

		ViewportPaintPanel viewT = new ViewportPaintPanel(gui, editor);
		viewT.setBounds(new Box(0, 0, width - height / 2, height - 20));
		textureEditor.addElement(viewT);
		editor.cursorPos = viewT::getHoveredTexPos;

		Panel p = new Panel(gui);
		int treeW = Math.min(150, height / 2);
		p.setBounds(new Box(width - treeW, height / 2, treeW, height / 2));
		ModelElementsTree tree = new ModelElementsTree(gui, editor);
		tree.setBounds(new Box(0, 0, treeW, height / 2 - 25));
		p.addElement(tree);
		p.setBackgroundColor(gui.getColors().panel_background);
		editor.updateTree.add(tree::updateTree);
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
		mainPanel.addElement(new AnimPanel(gui, this, height - 20));
		topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.animation"), mainPanel));

		mainPanel.addElement(new TreePanel(gui, this, width, height - 20));

		ViewportPanelAnim view = new ViewportPanelAnim(gui, editor);
		view.setBounds(new Box(145, 0, width - 145 - 150, height - 20));
		mainPanel.addElement(view);
	}

	private void initFileMenu() {
		PopupMenu pp = new PopupMenu(gui);
		topPanel.add(new Button(gui, gui.i18nFormat("button.cpm.file"), () -> pp.display(this, 0, 20)));

		pp.addButton(gui.i18nFormat("button.cpm.file.new"), () -> checkUnsaved(() -> {
			this.editor.loadDefaultPlayerModel();
			this.editor.updateGui();
		}));

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

		pp.addButton(gui.i18nFormat("button.cpm.file.export"), () -> openPopup(new ExportSkinPopup(gui, this)));

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
		pp.add(boxGlow);

		Checkbox boxReColor = new Checkbox(gui, gui.i18nFormat("label.cpm.recolor"));
		boxReColor.setAction(editor::switchReColorEffect);
		editor.setReColor.add(b -> {
			boxReColor.setEnabled(b != null);
			if(b != null)boxReColor.setSelected(b);
			else boxReColor.setSelected(false);
		});
		pp.add(boxReColor);

		ColorButton colorBtn = new ColorButton(gui, this, editor::setColor);
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

		Checkbox chxbxBase = new Checkbox(gui, gui.i18nFormat("label.cpm.drawBase"));
		pp.add(chxbxBase);
		chxbxBase.setSelected(editor.renderBase);
		chxbxBase.setAction(() -> {
			editor.renderBase = !chxbxBase.isSelected();
			chxbxBase.setSelected(editor.renderBase);
		});

		Checkbox chxbxTpose = new Checkbox(gui, gui.i18nFormat("label.cpm.player_tpose"));
		pp.add(chxbxTpose);
		chxbxTpose.setSelected(editor.playerTpose);
		chxbxTpose.setAction(() -> {
			editor.playerTpose = !chxbxTpose.isSelected();
			chxbxTpose.setSelected(editor.playerTpose);
		});
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
		editor.skinFile = file;
		editor.reloadSkin();
	}

	private void showError(String msg, String error) {
		PopupPanel pp = new PopupPanel(gui);
		pp.setBounds(new Box(0, 0, 100, 30));

		Label lbl = new Label(gui, gui.i18nFormat("label.cpm.error." + msg));
		lbl.setBounds(new Box(5, 5, 0, 0));
		pp.addElement(lbl);

		Label lbl2 = new Label(gui, error);
		lbl2.setBounds(new Box(5, 15, 0, 0));
		pp.addElement(lbl2);

		openPopup(pp);
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
		}
	}
}
