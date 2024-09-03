package com.tom.cpm.shared.editor.gui.popup;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.InputPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.ButtonGroup;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.EmbeddedLocalizations;
import com.tom.cpl.util.Util;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorTexture;
import com.tom.cpm.shared.editor.Exporter;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.template.TemplateExporter;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.editor.util.ModelDescription.CopyProtection;
import com.tom.cpm.shared.gui.SelectSkinPopup;
import com.tom.cpm.shared.gui.SkinUploadPopup;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.parts.ModelPartDefinitionLink;
import com.tom.cpm.shared.parts.ModelPartLink;
import com.tom.cpm.shared.util.Log;

public abstract class ExportPopup extends PopupPanel {
	protected final EditorGui editorGui;
	protected Button ok, modeBtn;

	protected ExportPopup(EditorGui e, int width, int height, ExportMode mode) {
		super(e.getGui());
		this.editorGui = e;

		modeBtn = new Button(gui, gui.i18nFormat("button.cpm.export.as_" + mode.name().toLowerCase(Locale.ROOT)), () -> {
			close();
			for(int i = mode.ordinal() + 1;i<ExportMode.VALUES.length*2;i++) {
				ExportMode em = ExportMode.VALUES[i % ExportMode.VALUES.length];
				if(em.canDisplay.test(e.getEditor())) {
					e.openPopup(em.factory.apply(e));
					return;
				}
			}
		});
		modeBtn.setTooltip(new Tooltip(editorGui, gui.i18nFormat("tooltip.cpm.export.as_" + mode.name().toLowerCase(Locale.ROOT))));
		modeBtn.setBounds(new Box(5, 5, 170, 20));
		addElement(modeBtn);

		ok = new Button(gui, gui.i18nFormat("button.cpm.file.export"), this::export);
		ok.setBounds(new Box(5, height - 25, 80, 20));
		addElement(ok);

		setBounds(new Box(0, 0, width, height));
	}

	protected void export() {
		if(Exporter.check(editorGui.getEditor(), editorGui.getGui(), this::export)) {
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
		SKIN(Skin::new, ((Predicate<Editor>) ExportPopup::isTemplate).negate()),
		MODEL(Model::new, ((Predicate<Editor>) ExportPopup::isTemplate).negate()),
		B64(ExportPopup.B64::new, ((Predicate<Editor>) ExportPopup::isTemplate).negate()),
		TEMPLATE(Template::new, ExportPopup::isTemplate),
		;

		public static final ExportMode[] VALUES = values();
		private Function<EditorGui, ExportPopup> factory;
		private Predicate<Editor> canDisplay;

		private ExportMode(Function<EditorGui, ExportPopup> factory, Predicate<Editor> canDisplay) {
			this.factory = factory;
			this.canDisplay = canDisplay;
		}
	}

	public static boolean isTemplate(Editor e) {
		return e.templateSettings != null;
	}

	private static class Skin extends ExportPopup {
		private EditorTexture vanillaSkin;
		private Button okDef, okUpload, changeUUID;
		private File selFile;
		private Checkbox forceLinkFile, chbxClone, chbxUUIDLock;
		private Link defLink;
		private Tooltip vanillaSkinTooltip;
		private boolean gist, upload;
		private SkinType skinType;
		private ButtonGroup<SkinType, Checkbox> groupSkinType;

		protected Skin(EditorGui e) {
			super(e, 320, 280, ExportMode.SKIN);
			Editor editor = e.getEditor();
			skinType = editor.skinType;

			vanillaSkin = new EditorTexture();
			vanillaSkin.setImage(editor.vanillaSkin);

			Button encSettings = new Button(gui, gui.i18nFormat("button.cpm.animEncSettings"), () -> e.openPopup(new AnimEncConfigPopup(gui, editor, vanillaSkin::markDirty)));
			encSettings.setBounds(new Box(5, 30, 170, 20));
			addElement(encSettings);

			Button changeVanillaSkin = new Button(gui, gui.i18nFormat("button.cpm.change_vanilla_skin"), () -> {
				SelectSkinPopup ssp = new SelectSkinPopup(e, skinType, (type, img) -> {
					skinType = type;
					groupSkinType.accept(type);
					editor.vanillaSkin = img;
					vanillaSkin.setImage(img);
					detectLink();
				});
				ssp.setOnClosed(() -> editor.displayViewport.accept(true));
				e.openPopup(ssp);
				editor.displayViewport.accept(false);
			});
			changeVanillaSkin.setBounds(new Box(bounds.w - 135, bounds.y + 150, 128, 20));
			addElement(changeVanillaSkin);

			Label vanillaSkinLbl = new Label(gui, gui.i18nFormat("label.cpm.vanilla_skin"));
			vanillaSkinLbl.setBounds(new Box(185, 5, 0, 0));
			addElement(vanillaSkinLbl);

			forceLinkFile = new Checkbox(gui, gui.i18nFormat("label.cpm.force_link_file"));
			addElement(forceLinkFile);
			forceLinkFile.setBounds(new Box(5, 80, 135, 20));
			forceLinkFile.setAction(() -> forceLinkFile.setSelected(!forceLinkFile.isSelected()));
			forceLinkFile.setTooltip(new Tooltip(editorGui, gui.i18nFormat("tooltip.cpm.force_link_file")));

			Label expOutLbl = new Label(gui, gui.i18nFormat("label.cpm.export_output"));
			expOutLbl.setBounds(new Box(5, 160, 0, 0));
			addElement(expOutLbl);

			Label exportName = new Label(gui, gui.i18nFormat("label.cpm.no_file"));
			exportName.setBounds(new Box(5, 175, 0, 0));
			addElement(exportName);

			Button setOut = new Button(gui, "...", () -> {
				FileChooserPopup fc = new FileChooserPopup(e);
				fc.setTitle(EmbeddedLocalizations.exportSkin);
				fc.setFileDescText(EmbeddedLocalizations.filePng);
				fc.setFilter(new FileFilter("png"));
				fc.setSaveDialog(true);
				fc.setExtAdder(n -> n + ".png");
				fc.setAccept(f -> {
					selFile = f;
					ok.setEnabled(true);
					ok.setTooltip(null);
					exportName.setText(f.getName());
				});
				fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
				e.openPopup(fc);
			});
			setOut.setBounds(new Box(150, 170, 25, 20));
			addElement(setOut);

			chbxClone = new Checkbox(gui, gui.i18nFormat("label.cpm.cloneable"));
			chbxClone.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.cloneable")));
			chbxClone.setBounds(new Box(5, 105, 60, 20));
			addElement(chbxClone);

			Button btnEditDesc = new Button(gui, gui.i18nFormat("label.cpm.desc"), () -> e.openPopup(new DescPopup(e, false, this::updateDesc)));
			btnEditDesc.setBounds(new Box(90, 105, 85, 20));
			addElement(btnEditDesc);

			chbxUUIDLock = new Checkbox(gui, gui.i18nFormat("label.cpm.uuidlock"));
			chbxUUIDLock.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.uuidlock", MinecraftClientAccess.get().getClientPlayer().getUUID().toString())));
			chbxUUIDLock.setBounds(new Box(5, 130, 60, 20));
			addElement(chbxUUIDLock);

			changeUUID = new Button(gui, gui.i18nFormat("button.cpm.changeUUID"), new InputPopup(e, gui.i18nFormat("button.cpm.changeUUID"), gui.i18nFormat("label.cpm.enterNewUUID"), n -> {
				if(editor.description == null)editor.description = new ModelDescription();
				try {
					editor.description.uuid = Util.uuidFromString(n);
					chbxUUIDLock.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.uuidlockOw", editor.description.uuid.toString())));
				} catch (IllegalArgumentException ex) {
					e.openPopup(new MessagePopup(e, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.invalidUUID")));
				}
			}, null));
			changeUUID.setBounds(new Box(90, 130, 85, 20));
			addElement(changeUUID);

			chbxClone.setAction(() -> {
				if(!chbxClone.isSelected()) {
					chbxUUIDLock.setSelected(false);
					chbxClone.setSelected(true);
					changeUUID.setEnabled(false);
				} else {
					chbxClone.setSelected(false);
				}
			});

			chbxUUIDLock.setAction(() -> {
				changeUUID.setEnabled(!chbxUUIDLock.isSelected());
				if(!chbxUUIDLock.isSelected()) {
					chbxUUIDLock.setSelected(true);
					chbxClone.setSelected(false);
				} else {
					chbxUUIDLock.setSelected(false);
				}
			});

			okDef = new Button(gui, gui.i18nFormat("button.cpm.export_def"), () -> {
				if(defLink != null) {
					gist = true;
					close();
					export();
				}
			});
			okDef.setBounds(new Box(90, bounds.h - 25, 80, 20));
			addElement(okDef);

			okUpload = new Button(gui, gui.i18nFormat("button.cpm.export.skin.exportApply"), () -> {
				upload = true;
				close();
				export();
			});
			okUpload.setBounds(new Box(175, bounds.h - 25, 100, 20));
			if(MinecraftClientAccess.get().getServerSideStatus() != ServerStatus.OFFLINE) {
				okUpload.setEnabled(false);
				okUpload.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.export.skin.cantUploadIngame")));
			} else {
				okUpload.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.export.skin.exportApply")));
			}
			addElement(okUpload);

			groupSkinType = new ButtonGroup<>(Checkbox::setSelected, Checkbox::setAction, i -> skinType = i);
			for (int j = 0; j < SkinType.VANILLA_TYPES.length; j++) {
				SkinType s = SkinType.VANILLA_TYPES[j];
				Checkbox chbxSt = new Checkbox(gui, gui.i18nFormat("label.cpm.skin_type." + s.getName()));
				chbxSt.setBounds(new Box(bounds.w - 135, bounds.y + 175 + j * 25, 60, 20));
				addElement(chbxSt);
				groupSkinType.addElement(s, chbxSt);
			}
			groupSkinType.accept(skinType);

			ok.setEnabled(false);
			ok.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.export.skin.noFile")));
			detectLink();
			updateDesc();

			vanillaSkinTooltip = new Tooltip(e,  gui.i18nFormat("tooltip.cpm.export.skin.vanillaSkinInfo"));
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			super.draw(event, partialTicks);
			gui.drawBox(bounds.x + bounds.w - 136, bounds.y + 14, 130, 130, gui.getColors().color_picker_border);
			gui.drawBox(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0xffffffff);

			vanillaSkin.bind();
			gui.drawTexture(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0, 0, 1, 1);

			if(event.isHovered(new Box(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128)))vanillaSkinTooltip.set();
		}

		private void updateDesc() {
			Editor editor = editorGui.getEditor();
			chbxClone.setSelected(editor.description != null && editor.description.copyProtection == CopyProtection.CLONEABLE);
			chbxUUIDLock.setSelected(editor.description != null && editor.description.copyProtection == CopyProtection.UUID_LOCK);
			changeUUID.setEnabled(editor.description != null && editor.description.copyProtection == CopyProtection.UUID_LOCK);

			if(editor.description != null && editor.description.uuid != null)
				chbxUUIDLock.setTooltip(new Tooltip(editorGui, gui.i18nFormat("tooltip.cpm.uuidlockOw", editor.description.uuid.toString())));
		}

		@Override
		public void onClosed() {
			vanillaSkin.free();
		}

		private void detectLink() {
			ModelDefinition def = MinecraftClientAccess.get().getDefinitionLoader().loadModel(editorGui.getEditor().vanillaSkin, MinecraftClientAccess.get().getClientPlayer());
			boolean incompatibleDef = false;
			if (def != null) {
				ModelPartLink link = def.findDefLink();
				if (link != null) {
					defLink = link.getLink();
					if ((link instanceof ModelPartDefinitionLink) == ModConfig.getCommonConfig().getBoolean(ConfigKeys.EDITOR_EXPERIMENTAL_EXPORT, false)) {
						defLink = null;
						incompatibleDef = true;
					}
				}
			}
			okDef.setEnabled(defLink != null);
			if(defLink != null) {
				okDef.setTooltip(new Tooltip(editorGui, gui.i18nFormat("tooltip.cpm.export_def")));
			} else if (incompatibleDef) {
				okDef.setTooltip(new Tooltip(editorGui, gui.i18nFormat("tooltip.cpm.export_def.incompatible")));
			} else {
				okDef.setTooltip(new Tooltip(editorGui, gui.i18nFormat("tooltip.cpm.export_def.noLink")));
			}
		}

		@Override
		protected void export0() {
			Editor e = editorGui.getEditor();
			if(editorGui.getEditor().templateSettings != null) {
				editorGui.openPopup(new MessagePopup(editorGui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.templateExportAsSkin")));
				return;
			}
			if(e.description == null && (chbxClone.isSelected() || chbxUUIDLock.isSelected())) {
				e.description = new ModelDescription();
				e.markDirty();
			}
			if(e.description != null) {
				CopyProtection cp = chbxUUIDLock.isSelected() ? CopyProtection.UUID_LOCK : chbxClone.isSelected() ? CopyProtection.CLONEABLE : CopyProtection.NORMAL;
				if(e.description.copyProtection != cp) {
					e.description.copyProtection = cp;
					e.markDirty();
				}
			}
			if(e.skinType != skinType) {
				if(e.elements.stream().anyMatch(m -> !m.hidden)) {
					editorGui.openPopup(new ConfirmPopup(editorGui, gui.i18nFormat("label.cpm.confirmSkinTypeEdit", gui.i18nFormat("label.cpm.skin_type." + e.skinType.getName()), gui.i18nFormat("label.cpm.skin_type." + skinType.getName())), () -> {
						e.skinType = skinType;
						e.markDirty();
						export1();
					}, null));
					return;
				} else {
					e.skinType = skinType;
					e.markDirty();
				}
			}
			export1();
		}

		private void export1() {
			Editor e = editorGui.getEditor();
			if(gist) {
				Exporter.exportUpdate(e, gui, defLink);
			} else if(upload) {
				editorGui.openPopup(new ConfirmPopup(editorGui, gui.i18nFormat("label.cpm.export.upload"), gui.i18nFormat("label.cpm.export.upload.desc"), () -> {
					Exporter.exportSkin(e, gui, img -> new SkinUploadPopup(editorGui, e.skinType, img).start(), forceLinkFile.isSelected());
				}, null));
			} else {
				Exporter.exportSkin(e, gui, selFile, forceLinkFile.isSelected());
			}
		}
	}

	private static class Template extends ExportPopup {
		private TextField nameField, descField;
		private EditorTexture icon;

		protected Template(EditorGui e) {
			super(e, 320, 190, ExportMode.TEMPLATE);

			Editor editor = e.getEditor();

			Label nameLbl = new Label(gui, gui.i18nFormat("label.cpm.name"));
			nameLbl.setBounds(new Box(5, 55, 150, 10));
			addElement(nameLbl);

			nameField = new TextField(gui);
			nameField.setBounds(new Box(5, 65, 150, 20));
			addElement(nameField);

			Label descLbl = new Label(gui, gui.i18nFormat("label.cpm.desc"));
			descLbl.setBounds(new Box(5, 90, 150, 10));
			addElement(descLbl);

			descField = new TextField(gui);
			descField.setBounds(new Box(5, 100, 150, 20));
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
				editorGui.openPopup(new MessagePopup(editorGui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.projectNotTemplate")));
				return;
			}

			if(editor.description == null) {
				editor.description = new ModelDescription();
			}
			editor.description.name = nameField.getText();
			editor.description.desc = descField.getText();
			editor.markDirty();

			TemplateExporter.exportTemplate(editor, gui, editor.description,
					t -> editorGui.openPopup(new CreateGistPopup(editorGui, gui, "template_export", t,
							l -> editorGui.openPopup(new ExportStringResultPopup(editorGui, "template", l.toString()))
							)));
		}

		@Override
		public void onClosed() {
			icon.free();
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			super.draw(event, partialTicks);

			if(icon.getImage() != null) {
				gui.drawBox(bounds.x + bounds.w - 136, bounds.y + 14, 130, 130, gui.getColors().color_picker_border);
				gui.drawBox(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0xffffffff);

				icon.bind();
				gui.drawTexture(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0, 0, 1, 1);
			}
		}
	}

	private static class B64 extends ExportPopup {
		private Checkbox forceLinkFile, chbxClone;

		protected B64(EditorGui e) {
			super(e, 180, 150, ExportMode.B64);

			forceLinkFile = new Checkbox(gui, gui.i18nFormat("label.cpm.force_link_file"));
			addElement(forceLinkFile);
			forceLinkFile.setAction(() -> forceLinkFile.setSelected(!forceLinkFile.isSelected()));
			forceLinkFile.setBounds(new Box(5, 55, 135, 20));

			chbxClone = new Checkbox(gui, gui.i18nFormat("label.cpm.cloneable"));
			chbxClone.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.cloneable")));
			chbxClone.setBounds(new Box(5, 80, 60, 20));
			addElement(chbxClone);

			chbxClone.setAction(() -> chbxClone.setSelected(!chbxClone.isSelected()));

			Button btnEditDesc = new Button(gui, gui.i18nFormat("label.cpm.desc"), () -> e.openPopup(new DescPopup(e, false, this::updateDesc)));
			btnEditDesc.setBounds(new Box(90, 80, 85, 20));
			addElement(btnEditDesc);

			updateDesc();
		}

		private void updateDesc() {
			Editor editor = editorGui.getEditor();
			chbxClone.setSelected(editor.description != null && editor.description.copyProtection == CopyProtection.CLONEABLE);
		}

		@Override
		protected void export0() {
			if(editorGui.getEditor().templateSettings != null) {
				editorGui.openPopup(new MessagePopup(editorGui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.templateExportAsSkin")));
				return;
			}
			Editor e = editorGui.getEditor();
			if(e.description == null && chbxClone.isSelected()) {
				e.description = new ModelDescription();
				e.markDirty();
			}
			if(e.description != null) {
				CopyProtection cp = chbxClone.isSelected() ? CopyProtection.CLONEABLE : e.description.copyProtection;
				if(e.description.copyProtection != cp) {
					e.description.copyProtection = cp;
					e.markDirty();
				}
			}
			Exporter.exportB64(editorGui.getEditor(), gui, b64 -> editorGui.openPopup(new ExportStringResultPopup(editorGui, "base64_model", b64)), forceLinkFile.isSelected());
		}

	}

	private static class Model extends ExportPopup {
		private TextField nameField, descField;
		private EditorTexture icon;
		private Checkbox skinCompat, chbxClone, chbxUUIDLock;
		private Link defLink;
		private Button okDef;
		private boolean gist;

		protected Model(EditorGui e) {
			super(e, 320, 260, ExportMode.MODEL);

			Editor editor = e.getEditor();

			Label nameLbl = new Label(gui, gui.i18nFormat("label.cpm.name"));
			nameLbl.setBounds(new Box(5, 35, 150, 10));
			addElement(nameLbl);

			nameField = new TextField(gui);
			nameField.setBounds(new Box(5, 45, 150, 20));
			addElement(nameField);

			Label descLbl = new Label(gui, gui.i18nFormat("label.cpm.desc"));
			descLbl.setBounds(new Box(5, 70, 150, 10));
			addElement(descLbl);

			descField = new TextField(gui);
			descField.setBounds(new Box(5, 80, 150, 20));
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
			setIcon.setBounds(new Box(5, 105, 100, 20));
			addElement(setIcon);

			skinCompat = new Checkbox(gui, gui.i18nFormat("label.cpm.export.skinCompat"));
			skinCompat.setBounds(new Box(5, 130, 100, 20));
			skinCompat.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.export.skinCompat")));
			addElement(skinCompat);
			skinCompat.setAction(() -> skinCompat.setSelected(!skinCompat.isSelected()));

			chbxClone = new Checkbox(gui, gui.i18nFormat("label.cpm.cloneable"));
			chbxClone.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.cloneable")));
			chbxClone.setBounds(new Box(5, 155, 60, 20));
			chbxClone.setSelected(editor.description != null && editor.description.copyProtection == CopyProtection.CLONEABLE);
			addElement(chbxClone);

			chbxUUIDLock = new Checkbox(gui, gui.i18nFormat("label.cpm.uuidlock"));
			chbxUUIDLock.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.uuidlock", MinecraftClientAccess.get().getClientPlayer().getUUID().toString())));
			chbxUUIDLock.setBounds(new Box(5, 180, 60, 20));
			chbxUUIDLock.setSelected(editor.description != null && editor.description.copyProtection == CopyProtection.UUID_LOCK);
			addElement(chbxUUIDLock);

			Button changeUUID = new Button(gui, gui.i18nFormat("button.cpm.changeUUID"), new InputPopup(e, gui.i18nFormat("button.cpm.changeUUID"), gui.i18nFormat("label.cpm.enterNewUUID"), n -> {
				if(editor.description == null)editor.description = new ModelDescription();
				try {
					editor.description.uuid = Util.uuidFromString(n);
					chbxUUIDLock.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.uuidlockOw", editor.description.uuid.toString())));
				} catch (IllegalArgumentException ex) {
					e.openPopup(new MessagePopup(e, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.invalidUUID")));
				}
			}, null));
			changeUUID.setEnabled(editor.description != null && editor.description.copyProtection == CopyProtection.UUID_LOCK);
			changeUUID.setBounds(new Box(90, 180, 80, 20));
			addElement(changeUUID);

			if(editor.description != null && editor.description.uuid != null)
				chbxUUIDLock.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.uuidlockOw", editor.description.uuid.toString())));

			chbxClone.setAction(() -> {
				if(!chbxClone.isSelected()) {
					chbxUUIDLock.setSelected(false);
					chbxClone.setSelected(true);
					changeUUID.setEnabled(false);
				} else {
					chbxClone.setSelected(false);
				}
			});

			chbxUUIDLock.setAction(() -> {
				changeUUID.setEnabled(!chbxUUIDLock.isSelected());
				if(!chbxUUIDLock.isSelected()) {
					chbxUUIDLock.setSelected(true);
					chbxClone.setSelected(false);
				} else {
					chbxUUIDLock.setSelected(false);
				}
			});

			okDef = new Button(gui, gui.i18nFormat("button.cpm.export_def"), () -> {
				if(defLink != null) {
					gist = true;
					close();
					export();
				}
			});
			okDef.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.export_def.model")));
			okDef.setBounds(new Box(90, 235, 80, 20));
			addElement(okDef);

			updateLink();
		}

		@Override
		protected void export0() {
			Editor editor = editorGui.getEditor();

			if(editor.templateSettings != null) {
				editorGui.openPopup(new MessagePopup(editorGui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.templateExportAsSkin")));
				return;
			}
			boolean descChanged = false;
			if(editor.description == null) {
				editor.description = new ModelDescription();
				descChanged = true;
			}
			if(!descChanged && !editor.description.name.equals(nameField.getText()))descChanged = true;
			if(!descChanged && !editor.description.desc.equals(descField.getText()))descChanged = true;
			editor.description.name = nameField.getText();
			editor.description.desc = descField.getText();
			CopyProtection cp = chbxUUIDLock.isSelected() ? CopyProtection.UUID_LOCK : chbxClone.isSelected() ? CopyProtection.CLONEABLE : CopyProtection.NORMAL;
			if(editor.description.copyProtection != cp) {
				editor.description.copyProtection = cp;
				descChanged = true;
			}
			if(descChanged)editor.markDirty();

			if(gist) {
				Exporter.exportUpdate(editor, gui, defLink);
			} else {
				File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
				modelsDir.mkdirs();
				String fileName = nameField.getText().replaceAll("[^a-zA-Z0-9\\.\\-]", "") + ".cpmmodel";
				File selFile = new File(modelsDir, fileName);
				if(selFile.exists()) {
					editorGui.openPopup(new ConfirmPopup(editorGui, gui.i18nFormat("label.cpm.overwrite"), gui.i18nFormat("label.cpm.overwrite"),
							() -> Exporter.exportModel(editor, gui, selFile, editor.description, skinCompat.isSelected()),
							null));
				} else {
					Exporter.exportModel(editor, gui, selFile, editor.description, skinCompat.isSelected());
				}
			}
		}

		private void updateLink() {
			File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
			modelsDir.mkdirs();
			String fileName = nameField.getText().replaceAll("[^a-zA-Z0-9\\.\\-]", "") + ".cpmmodel";
			File selFile = new File(modelsDir, fileName);
			defLink = null;
			boolean incompatibleDef = false;
			if(selFile.exists()) {
				try {
					ModelFile mf = ModelFile.load(selFile);
					ModelDefinition def = MinecraftClientAccess.get().getDefinitionLoader().loadModel(mf.getDataBlock(), MinecraftClientAccess.get().getClientPlayer());
					if(def != null) {
						ModelPartLink link = def.findDefLink();
						if (link != null) {
							defLink = link.getLink();
							if ((link instanceof ModelPartDefinitionLink) == ModConfig.getCommonConfig().getBoolean(ConfigKeys.EDITOR_EXPERIMENTAL_EXPORT, false)) {
								defLink = null;
								incompatibleDef = true;
							}
						}
					}
				} catch (IOException e) {
					Log.error("Failed to load file", e);
				}
			}
			okDef.setEnabled(defLink != null);
			if(defLink != null) {
				okDef.setTooltip(new Tooltip(editorGui, gui.i18nFormat("tooltip.cpm.export_def.model")));
			} else if (incompatibleDef) {
				okDef.setTooltip(new Tooltip(editorGui, gui.i18nFormat("tooltip.cpm.export_def.incompatible")));
			} else {
				okDef.setTooltip(new Tooltip(editorGui, gui.i18nFormat("tooltip.cpm.export_def.noLink.model")));
			}
		}

		@Override
		public void onClosed() {
			icon.free();
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			super.draw(event, partialTicks);

			if(icon.getImage() != null) {
				gui.drawBox(bounds.x + bounds.w - 136, bounds.y + 14, 130, 130, gui.getColors().color_picker_border);
				gui.drawBox(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0xffffffff);

				icon.bind();
				gui.drawTexture(bounds.x + bounds.w - 135, bounds.y + 15, 128, 128, 0, 0, 1, 1);
			}
		}
	}

	private static void openScreenshot(EditorGui e, EditorTexture icon, ExportPopup popup) {
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
		Log.error("Project can't be exported in any format");
		return new MessagePopup(e, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.unknownError"));
	}
}
