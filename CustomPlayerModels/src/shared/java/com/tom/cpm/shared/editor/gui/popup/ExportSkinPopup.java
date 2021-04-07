package com.tom.cpm.shared.editor.gui.popup;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Predicate;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorTexture;
import com.tom.cpm.shared.editor.Exporter;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.util.ModelDescription;

public abstract class ExportSkinPopup extends PopupPanel {
	protected final EditorGui editorGui;
	protected Button ok, modeBtn;

	protected ExportSkinPopup(EditorGui e, int width, int height, ExportMode mode) {
		super(e.getGui());
		this.editorGui = e;

		modeBtn = new Button(gui, gui.i18nFormat("button.cpm.export.as_" + mode.name().toLowerCase()), () -> {
			close();
			for(int i = mode.ordinal() + 1;i<ExportMode.VALUES.length*2;i++) {
				ExportMode em = ExportMode.VALUES[i % ExportMode.VALUES.length];
				if(em.canDisplay.test(e.getEditor())) {
					e.openPopup(em.factory.apply(e));
					return;
				}
			}
		});
		modeBtn.setTooltip(new Tooltip(editorGui, gui.i18nFormat("tooltip.cpm.export.as_" + mode.name().toLowerCase())));
		modeBtn.setBounds(new Box(5, 5, 135, 20));
		addElement(modeBtn);

		ok = new Button(gui, gui.i18nFormat("button.cpm.file.export"), this::export);
		ok.setBounds(new Box(5, height - 25, 80, 20));
		addElement(ok);

		setBounds(new Box(0, 0, width, height));
	}

	protected void export() {
		if(Exporter.check(editorGui.getEditor(), editorGui, this::export)) {
			export0();
			close();
		}
	}

	protected abstract void export0();

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.exportSkin");
	}

	private static enum ExportMode {
		SKIN(Skin::new, ((Predicate<Editor>) ExportSkinPopup::isTemplate).negate()),
		B64(B64::new, ((Predicate<Editor>) ExportSkinPopup::isTemplate).negate()),
		TEMPLATE(Template::new, ExportSkinPopup::isTemplate),
		MODEL(Model::new, ((Predicate<Editor>) ExportSkinPopup::isTemplate).negate()),
		;

		public static final ExportMode[] VALUES = values();
		private Function<EditorGui, ExportSkinPopup> factory;
		private Predicate<Editor> canDisplay;

		private ExportMode(Function<EditorGui, ExportSkinPopup> factory, Predicate<Editor> canDisplay) {
			this.factory = factory;
			this.canDisplay = canDisplay;
		}
	}

	public static boolean isTemplate(Editor e) {
		return e.templateSettings != null;
	}

	private static class Skin extends ExportSkinPopup {
		private EditorTexture vanillaSkin;
		private Button setOut, changeVanillaSkin, encSettings, okDef;
		private Label expOutLbl, exportName, vanillaSkinLbl;
		private File selFile;
		private Checkbox forceLinkFile;
		private Link defLink;
		private boolean gist;

		protected Skin(EditorGui e) {
			super(e, 320, 190, ExportMode.SKIN);
			Editor editor = e.getEditor();

			vanillaSkin = new EditorTexture();
			vanillaSkin.setImage(editor.vanillaSkin);

			encSettings = new Button(gui, gui.i18nFormat("button.cpm.animEncSettings"), () -> e.openPopup(new AnimEncConfigPopup(gui, editor, vanillaSkin::markDirty)));
			encSettings.setBounds(new Box(5, 30, 135, 20));
			addElement(encSettings);

			changeVanillaSkin = new Button(gui, gui.i18nFormat("button.cpm.change_vanilla_skin"), () -> {
				FileChooserGui fc = new FileChooserGui(editor.frame);
				fc.setTitle(gui.i18nFormat("button.cpm.change_vanilla_skin"));
				fc.setFileDescText(gui.i18nFormat("label.cpm.file_png"));
				fc.setFilter((f, n) -> n.endsWith(".png") && !f.isDirectory());
				fc.setAccept(f -> {
					try {
						Image img = Image.loadFrom(f);
						editor.vanillaSkin = img;
						vanillaSkin.setImage(img);
						detectDef();
					} catch (IOException ex) {
						e.openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.img_load_failed", ex.getLocalizedMessage())));
					}
				});
				fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
				e.openPopup(fc);
			});
			changeVanillaSkin.setBounds(new Box(5, 55, 135, 20));
			addElement(changeVanillaSkin);

			vanillaSkinLbl = new Label(gui, gui.i18nFormat("label.cpm.vanilla_skin"));
			vanillaSkinLbl.setBounds(new Box(185, 5, 0, 0));
			addElement(vanillaSkinLbl);

			forceLinkFile = new Checkbox(gui, gui.i18nFormat("label.cpm.force_link_file"));
			addElement(forceLinkFile);
			forceLinkFile.setBounds(new Box(5, 80, 135, 20));
			forceLinkFile.setAction(() -> forceLinkFile.setSelected(!forceLinkFile.isSelected()));

			expOutLbl = new Label(gui, gui.i18nFormat("label.cpm.export_output"));
			expOutLbl.setBounds(new Box(5, 105, 0, 0));
			addElement(expOutLbl);

			exportName = new Label(gui, gui.i18nFormat("label.cpm.no_file"));
			exportName.setBounds(new Box(5, 125, 0, 0));
			addElement(exportName);

			setOut = new Button(gui, "...", () -> {
				FileChooserGui fc = new FileChooserGui(e);
				fc.setTitle(gui.i18nFormat("label.cpm.exportSkin"));
				fc.setFileDescText(gui.i18nFormat("label.cpm.file_png"));
				fc.setFilter((f, n) -> n.endsWith(".png") && !f.isDirectory());
				fc.setSaveDialog(true);
				fc.setExtAdder(n -> n + ".png");
				fc.setAccept(f -> {
					selFile = f;
					ok.setEnabled(true);
					exportName.setText(f.getName());
				});
				fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
				e.openPopup(fc);
			});
			setOut.setBounds(new Box(150, 120, 30, 20));
			addElement(setOut);

			okDef = new Button(gui, gui.i18nFormat("button.cpm.export_def"), () -> {
				if(defLink != null) {
					gist = true;
					close();
					export();
				}
			});
			okDef.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.export_def")));
			okDef.setBounds(new Box(90, 165, 80, 20));
			addElement(okDef);
			ok.setEnabled(false);
			detectDef();
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			super.draw(mouseX, mouseY, partialTicks);
			gui.drawBox(bounds.x + bounds.w - 136, bounds.y + 14, 130, 130, gui.getColors().color_picker_border);
			gui.drawBox(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0xffffffff);

			vanillaSkin.bind();
			gui.drawTexture(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0, 0, 1, 1);
		}

		@Override
		public void onClosed() {
			vanillaSkin.free();
		}

		private void detectDef() {
			ModelDefinition def = MinecraftClientAccess.get().getDefinitionLoader().loadModel(editorGui.getEditor().vanillaSkin, MinecraftClientAccess.get().getClientPlayer());
			if(def != null) {
				defLink = def.findDefLink();
			}
			okDef.setEnabled(defLink != null);
		}

		@Override
		protected void export0() {
			if(gist) {
				if(editorGui.getEditor().templateSettings != null) {
					editorGui.openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.templateExportAsSkin")));
					return;
				}
				Exporter.exportGistUpdate(editorGui.getEditor(), editorGui, gist -> editorGui.openPopup(new ExportStringResultPopup(editorGui, gui, "skin_update", gist)));
			} else {
				if(editorGui.getEditor().templateSettings != null) {
					editorGui.openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.templateExportAsSkin")));
					return;
				}
				Exporter.exportSkin(editorGui.getEditor(), editorGui, selFile, forceLinkFile.isSelected());
			}
		}
	}

	private static class Template extends ExportSkinPopup {
		private Label nameLbl, descLbl;
		private TextField nameField, descField;
		private EditorTexture icon;

		protected Template(EditorGui e) {
			super(e, 320, 190, ExportMode.TEMPLATE);

			Editor editor = e.getEditor();

			nameLbl = new Label(gui, gui.i18nFormat("label.cpm.name"));
			nameLbl.setBounds(new Box(5, 55, 130, 10));
			addElement(nameLbl);

			nameField = new TextField(gui);
			nameField.setBounds(new Box(5, 65, 130, 20));
			addElement(nameField);

			descLbl = new Label(gui, gui.i18nFormat("label.cpm.desc"));
			descLbl.setBounds(new Box(5, 90, 130, 10));
			addElement(descLbl);

			descField = new TextField(gui);
			descField.setBounds(new Box(5, 100, 130, 20));
			addElement(descField);

			icon = new EditorTexture();
			if(editor.description != null) {
				nameField.setText(editor.description.name);
				descField.setText(editor.description.desc);
				if(editor.description.icon != null) {
					icon.setImage(editor.description.icon);
				}
			}

			Button setIcon = new Button(gui, gui.i18nFormat("button.cpm.setIcon"), () -> openScreenshot(e, icon, this));
			setIcon.setBounds(new Box(5, 125, 100, 20));
			addElement(setIcon);
		}

		@Override
		protected void export0() {
			Editor editor = editorGui.getEditor();

			if(editor.templateSettings == null) {
				editorGui.openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.projectNotTemplate")));
				return;
			}

			if(editor.description == null) {
				editor.description = new ModelDescription();
			}
			editor.description.name = nameField.getText();
			editor.description.desc = descField.getText();
			editor.markDirty();

			Exporter.exportTemplate(editor, editorGui, editor.description,
					t -> editorGui.openPopup(new CreateGistPopup(editorGui, gui, "template_export", t,
							l -> editorGui.openPopup(new ExportStringResultPopup(editorGui, gui, "template", l.toString()))
							)));
		}

		@Override
		public void onClosed() {
			icon.free();
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			super.draw(mouseX, mouseY, partialTicks);
			gui.drawBox(bounds.x + bounds.w - 136, bounds.y + 14, 130, 130, gui.getColors().color_picker_border);
			gui.drawBox(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0xffffffff);

			icon.bind();
			gui.drawTexture(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0, 0, 1, 1);
		}
	}

	private static class B64 extends ExportSkinPopup {
		private Checkbox forceLinkFile;

		protected B64(EditorGui e) {
			super(e, 160, 150, ExportMode.B64);

			forceLinkFile = new Checkbox(gui, gui.i18nFormat("label.cpm.force_link_file"));
			addElement(forceLinkFile);
			forceLinkFile.setAction(() -> forceLinkFile.setSelected(!forceLinkFile.isSelected()));
			forceLinkFile.setBounds(new Box(5, 55, 135, 20));
		}

		@Override
		protected void export0() {
			if(editorGui.getEditor().templateSettings != null) {
				editorGui.openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.templateExportAsSkin")));
				return;
			}
			Exporter.exportB64(editorGui.getEditor(), editorGui, b64 -> editorGui.openPopup(new ExportStringResultPopup(editorGui, gui, "base64_model", b64)), forceLinkFile.isSelected());
		}

	}

	private static class Model extends ExportSkinPopup {
		private Label nameLbl, descLbl;
		private TextField nameField, descField;
		private EditorTexture icon;

		protected Model(EditorGui e) {
			super(e, 320, 260, ExportMode.MODEL);

			Editor editor = e.getEditor();

			nameLbl = new Label(gui, gui.i18nFormat("label.cpm.name"));
			nameLbl.setBounds(new Box(5, 55, 130, 10));
			addElement(nameLbl);

			nameField = new TextField(gui);
			nameField.setBounds(new Box(5, 65, 130, 20));
			addElement(nameField);

			descLbl = new Label(gui, gui.i18nFormat("label.cpm.desc"));
			descLbl.setBounds(new Box(5, 90, 130, 10));
			addElement(descLbl);

			descField = new TextField(gui);
			descField.setBounds(new Box(5, 100, 130, 20));
			addElement(descField);

			icon = new EditorTexture();
			if(editor.description != null) {
				nameField.setText(editor.description.name);
				descField.setText(editor.description.desc);
				if(editor.description.icon != null) {
					icon.setImage(editor.description.icon);
				}
			}

			Button setIcon = new Button(gui, gui.i18nFormat("button.cpm.setIcon"), () -> openScreenshot(e, icon, this));
			setIcon.setBounds(new Box(5, 125, 100, 20));
			addElement(setIcon);
		}

		@Override
		protected void export0() {
			Editor editor = editorGui.getEditor();

			if(editor.templateSettings != null) {
				editorGui.openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.templateExportAsSkin")));
				return;
			}

			if(editor.description == null) {
				editor.description = new ModelDescription();
			}
			editor.description.name = nameField.getText();
			editor.description.desc = descField.getText();
			editor.markDirty();

			File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
			modelsDir.mkdirs();
			String fileName = nameField.getText().replaceAll("[^a-zA-Z0-9\\.\\-]", "") + ".cpmmodel";
			File selFile = new File(modelsDir, fileName);
			if(selFile.exists()) {
				editorGui.openPopup(new ConfirmPopup(editorGui, gui.i18nFormat("label.cpm.overwrite"), gui.i18nFormat("label.cpm.overwrite"),
						() -> Exporter.exportModel(editor, editorGui, selFile, editor.description),
						null));
			} else {
				Exporter.exportModel(editor, editorGui, selFile, editor.description);
			}
		}

		@Override
		public void onClosed() {
			icon.free();
		}

		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			super.draw(mouseX, mouseY, partialTicks);
			gui.drawBox(bounds.x + bounds.w - 136, bounds.y + 14, 130, 130, gui.getColors().color_picker_border);
			gui.drawBox(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0xffffffff);

			icon.bind();
			gui.drawTexture(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0, 0, 1, 1);
		}
	}

	private static void openScreenshot(EditorGui e, EditorTexture icon, ExportSkinPopup popup) {
		popup.close();
		e.openPopup(new ScreenshotPopup(e, icon::setImage, () -> e.openPopup(popup)));
	}

	public static PopupPanel createPopup(EditorGui e) {
		for(ExportMode m : ExportMode.VALUES) {
			if(m.canDisplay.test(e.getEditor())) {
				return m.factory.apply(e);
			}
		}
		IGui gui = e.getGui();
		System.err.println("Project can't be exported in any format");
		return new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.unknownError"));
	}
}
