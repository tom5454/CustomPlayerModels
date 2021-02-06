package com.tom.cpm.shared.editor.gui.popup;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.Exporter;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.Checkbox;
import com.tom.cpm.shared.gui.elements.Label;
import com.tom.cpm.shared.gui.elements.MessagePopup;
import com.tom.cpm.shared.gui.elements.PopupPanel;
import com.tom.cpm.shared.math.Box;
import com.tom.cpm.shared.skin.SkinProvider;

public class ExportSkinPopup extends PopupPanel {
	private SkinProvider vanillaSkin;
	private EditorGui editorGui;

	public ExportSkinPopup(IGui gui, EditorGui e) {
		super(gui);

		this.editorGui = e;
		Editor editor = e.getEditor();

		vanillaSkin = new SkinProvider();
		vanillaSkin.setImage(editor.vanillaSkin);

		addElement(new Label(gui, gui.i18nFormat("label.cpm.vanilla_skin")).setBounds(new Box(185, 5, 0, 0)));

		Button encSettings = new Button(gui, gui.i18nFormat("button.cpm.animEncSettings"), () -> e.openPopup(new AnimEncConfigPopup(gui, editor, vanillaSkin::markDirty)));
		encSettings.setBounds(new Box(5, 5, 135, 20));
		addElement(encSettings);

		Button changeVanillaSkin = new Button(gui, gui.i18nFormat("button.cpm.change_vanilla_skin"), () -> {
			FileChooserGui fc = new FileChooserGui(editor.gui);
			fc.setTitle(gui.i18nFormat("button.cpm.change_vanilla_skin"));
			fc.setFileDescText(gui.i18nFormat("label.cpm.file_png"));
			fc.setFilter((f, n) -> n.endsWith(".png") && !f.isDirectory());
			fc.setAccept(f -> {
				try {
					BufferedImage img = ImageIO.read(f);
					editor.vanillaSkin = img;
					vanillaSkin.setImage(img);
				} catch (IOException ex) {
					e.openPopup(new MessagePopup(gui, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.img_load_failed", ex.getLocalizedMessage())));
				}
			});
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			e.openPopup(fc);
		});
		changeVanillaSkin.setBounds(new Box(5, 30, 135, 20));
		addElement(changeVanillaSkin);

		Checkbox forceLinkFile = new Checkbox(gui, gui.i18nFormat("label.cpm.force_link_file"));
		forceLinkFile.setBounds(new Box(5, 55, 135, 20));
		addElement(forceLinkFile);
		forceLinkFile.setAction(() -> forceLinkFile.setSelected(!forceLinkFile.isSelected()));

		Label exportName = new Label(gui, gui.i18nFormat("label.cpm.no_file"));
		exportName.setBounds(new Box(5, 85, 0, 0));
		addElement(exportName);

		File[] exportFile = new File[] {null};

		Button ok = new Button(gui, gui.i18nFormat("button.cpm.file.export"), () -> {
			if(exportFile[0] != null) {
				close();
				export(exportFile[0], forceLinkFile.isSelected());
			}
		});

		Button setOut = new Button(gui, "...", () -> {
			FileChooserGui fc = new FileChooserGui(e);
			fc.setTitle(gui.i18nFormat("label.cpm.exportSkin"));
			fc.setFileDescText(gui.i18nFormat("label.cpm.file_png"));
			fc.setFilter((f, n) -> n.endsWith(".png") && !f.isDirectory());
			fc.setSaveDialog(true);
			fc.setExtAdder(n -> n + ".png");
			fc.setAccept(f -> {
				exportFile[0] = f;
				ok.setEnabled(true);
				exportName.setText(f.getName());
			});
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			e.openPopup(fc);
		});
		setOut.setBounds(new Box(150, 80, 30, 20));
		addElement(setOut);

		ok.setEnabled(false);
		ok.setBounds(new Box(5, 165, 80, 20));
		addElement(ok);

		setBounds(new Box(0, 0, 320, 190));
	}

	private void export(File file, boolean force) {
		if(Exporter.check(editorGui.getEditor(), editorGui, () -> export(file, force)))
			Exporter.exportSkin(editorGui.getEditor(), editorGui, file, force);
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

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.exportSkin");
	}
}
