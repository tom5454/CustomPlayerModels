package com.tom.cpm.shared.editor.gui.popup;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
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

public class ExportSkinPopup extends PopupPanel {
	private EditorTexture vanillaSkin;
	private EditorGui editorGui;
	private File selFile;
	private Checkbox forceLinkFile;
	private ExportMode mode = ExportMode.SKIN;
	private Button ok, modeBtn, setOut, changeVanillaSkin, encSettings, okDef;
	private Label expOutLbl, exportName, vanillaSkinLbl, nameLbl, descLbl;
	private TextField nameField, descField;
	private Link defLink;

	public ExportSkinPopup(IGui gui, EditorGui e) {
		super(gui);

		this.editorGui = e;
		Editor editor = e.getEditor();

		vanillaSkin = new EditorTexture();
		vanillaSkin.setImage(editor.vanillaSkin);

		vanillaSkinLbl = new Label(gui, gui.i18nFormat("label.cpm.vanilla_skin"));
		vanillaSkinLbl.setBounds(new Box(185, 5, 0, 0));
		addElement(vanillaSkinLbl);

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
					setExportType();
				} catch (IOException ex) {
					e.openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.img_load_failed", ex.getLocalizedMessage())));
				}
			});
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			e.openPopup(fc);
		});
		changeVanillaSkin.setBounds(new Box(5, 55, 135, 20));
		addElement(changeVanillaSkin);

		forceLinkFile = new Checkbox(gui, gui.i18nFormat("label.cpm.force_link_file"));
		addElement(forceLinkFile);
		forceLinkFile.setAction(() -> forceLinkFile.setSelected(!forceLinkFile.isSelected()));

		expOutLbl = new Label(gui, gui.i18nFormat("label.cpm.export_output"));
		expOutLbl.setBounds(new Box(5, 105, 0, 0));
		addElement(expOutLbl);

		exportName = new Label(gui, gui.i18nFormat("label.cpm.no_file"));
		exportName.setBounds(new Box(5, 125, 0, 0));
		addElement(exportName);

		nameLbl = new Label(gui, gui.i18nFormat("label.cpm.name"));
		addElement(nameLbl);

		nameField = new TextField(gui);
		addElement(nameField);

		descLbl = new Label(gui, gui.i18nFormat("label.cpm.desc"));
		addElement(descLbl);

		descField = new TextField(gui);
		addElement(descField);

		setOut = new Button(gui, "...", () -> {
			FileChooserGui fc = new FileChooserGui(e);
			fc.setTitle(gui.i18nFormat("label.cpm.exportSkin"));
			fc.setFileDescText(gui.i18nFormat("label.cpm.file_png"));
			fc.setFilter((f, n) -> n.endsWith(".png") && !f.isDirectory());
			fc.setSaveDialog(true);
			fc.setExtAdder(n -> n + ".png");
			fc.setAccept(f -> {
				selFile = f;
				updateOkEn();
				exportName.setText(f.getName());
			});
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			e.openPopup(fc);
		});
		setOut.setBounds(new Box(150, 120, 30, 20));
		addElement(setOut);

		modeBtn = new Button(gui, "", () -> {
			mode = ExportMode.VALUES[(mode.ordinal() + 1) % ExportMode.VALUES.length];
			setExportType();
		});
		modeBtn.setBounds(new Box(5, 5, 135, 20));
		addElement(modeBtn);

		ok = new Button(gui, gui.i18nFormat("button.cpm.file.export"), () -> {
			if(okEn()) {
				close();
				export();
			}
		});

		ok.setEnabled(false);
		addElement(ok);

		okDef = new Button(gui, gui.i18nFormat("button.cpm.export_def"), () -> {
			if(mode == ExportMode.SKIN && defLink != null) {
				mode = ExportMode.GIST_UPDATE;
				close();
				export();
			}
		});
		okDef.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.export_def")));
		addElement(okDef);

		detectDef();
		setExportType();
	}

	private void detectDef() {
		ModelDefinition def = MinecraftClientAccess.get().getDefinitionLoader().loadModel(editorGui.getEditor().vanillaSkin, MinecraftClientAccess.get().getClientPlayer());
		if(def != null) {
			defLink = def.findDefLink();
		}
	}

	private void setExportType() {
		boolean vis = layer != null;
		if(vis)close();
		modeBtn.setText(gui.i18nFormat("button.cpm.export.as_" + mode.name().toLowerCase()));
		modeBtn.setTooltip(new Tooltip(editorGui, gui.i18nFormat("tooltip.cpm.export.as_" + mode.name().toLowerCase())));
		updateOkEn();
		setOut.setVisible(mode.hasFile);
		exportName.setVisible(mode.hasFile);
		expOutLbl.setVisible(mode.hasFile);
		vanillaSkinLbl.setVisible(mode == ExportMode.SKIN);
		changeVanillaSkin.setVisible(mode == ExportMode.SKIN);
		if(mode.forceLinkY == -1) {
			forceLinkFile.setVisible(false);
		} else {
			forceLinkFile.setBounds(new Box(5, mode.forceLinkY, 135, 20));
			forceLinkFile.setVisible(true);
		}
		boolean en = mode.nameDescY != -1;
		nameLbl.setVisible(en);
		nameField.setVisible(en);
		descLbl.setVisible(en);
		descField.setVisible(en);
		if(en) {
			nameLbl.setBounds(new Box(5, mode.nameDescY, 130, 10));
			nameField.setBounds(new Box(5, mode.nameDescY + 10, 130, 20));
			descLbl.setBounds(new Box(5, mode.nameDescY + 35, 130, 10));
			descField.setBounds(new Box(5, mode.nameDescY + 45, 130, 20));
		}
		okDef.setVisible(mode == ExportMode.SKIN && defLink != null && editorGui.getEditor().templateSettings == null);
		ok.setBounds(new Box(5, mode.h - 25, 80, 20));
		okDef.setBounds(new Box(90, mode.h - 25, 80, 20));
		setBounds(new Box(0, 0, mode.w, mode.h));
		if(layer != null)editorGui.openPopup(this);
	}

	private void updateOkEn() {
		ok.setEnabled(okEn());
	}

	private boolean okEn() {
		return !mode.hasFile || selFile != null;
	}

	private void export() {
		if(Exporter.check(editorGui.getEditor(), editorGui, this::export)) {
			switch (mode) {
			case SKIN:
				if(editorGui.getEditor().templateSettings != null) {
					editorGui.openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.templateExportAsSkin")));
					return;
				}
				Exporter.exportSkin(editorGui.getEditor(), editorGui, selFile, forceLinkFile.isSelected());
				break;
				/*case B64:
				Exporter.exportSkin(editorGui.getEditor(), editorGui, b64 -> editorGui.openPopup(new ExportStringResultPopup(editorGui, gui, "base64_model", b64)), forceLinkFile.isSelected());
				break;
			case MODEL:
				editorGui.openPopup(new MessagePopup(gui, "Info", "This feature is not implemented yet."));
				break;*/
			case TEMPLATE:
				if(editorGui.getEditor().templateSettings == null) {
					editorGui.openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.projectNotTemplate")));
					return;
				}
				Exporter.exportTemplate(editorGui.getEditor(), editorGui, nameField.getText(), descField.getText(),
						t -> editorGui.openPopup(new CreateGistPopup(editorGui, gui, "template_export", t,
								l -> editorGui.openPopup(new ExportStringResultPopup(editorGui, gui, "template", l.toString()))
								)));
				break;

			case GIST_UPDATE:
				if(editorGui.getEditor().templateSettings != null) {
					editorGui.openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.templateExportAsSkin")));
					return;
				}
				Exporter.exportSkin(editorGui.getEditor(), editorGui, gist -> editorGui.openPopup(new ExportStringResultPopup(editorGui, gui, "skin_update", gist)));
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		super.draw(mouseX, mouseY, partialTicks);

		if(mode == ExportMode.SKIN) {
			gui.drawBox(bounds.x + bounds.w - 136, bounds.y + 14, 130, 130, gui.getColors().color_picker_border);
			gui.drawBox(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0xffffffff);

			vanillaSkin.bind();
			gui.drawTexture(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0, 0, 1, 1);
		}
	}

	@Override
	public void onClosed() {
		vanillaSkin.free();
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.exportSkin");
	}

	private static enum ExportMode {
		SKIN(true, 320, 190, 80, -1),
		//B64(false, 160, 150, 55, -1),
		TEMPLATE(false, 160, 190, -1, 55),
		//MODEL(true, 185, 260, 55, 145),
		GIST_UPDATE,
		;

		private ExportMode() {
			this(false, false, 0, 0, 0, 0);
		}

		private ExportMode(boolean hasFile, int w, int h, int forceLinkY, int nameDescY) {
			this(hasFile, true, w, h, forceLinkY, nameDescY);
		}

		private ExportMode(boolean hasFile, boolean doDisplay, int w, int h, int forceLinkY, int nameDescY) {
			this.hasFile = hasFile;
			this.doDisplay = doDisplay;
			this.w = w;
			this.h = h;
			this.forceLinkY = forceLinkY;
			this.nameDescY = nameDescY;
		}
		public static final ExportMode[] VALUES = Arrays.stream(values()).filter(v -> v.doDisplay).toArray(ExportMode[]::new);
		public boolean hasFile, doDisplay;
		public int w, h, forceLinkY, nameDescY;
	}
}
