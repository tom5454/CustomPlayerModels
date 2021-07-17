package com.tom.cpm.shared.gui;

import java.io.IOException;
import java.util.List;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.ProcessPopup;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.MojangSkinUploadAPI;

public class SkinUploadPopup extends ProcessPopup<Void> {
	private SkinType type;
	private Image img;
	private List<GuiElement> labelsOld;

	public SkinUploadPopup(Frame frm, SkinType type, Image img) {
		super(frm);
		this.type = type;
		this.img = img;
		title = gui.i18nFormat("label.cpm.uploading");
		finished = v -> {};
		error = thr -> {
			Log.error("Unchecked exception while uploading skin", thr);
			frm.openPopup(new MessagePopup(frame, gui.i18nFormat("label.cpm.error"), gui.i18nFormat("label.cpm.skinUpload.fail", gui.i18nFormat("error.cpm.unknownError"))));
		};
		function = this::process;
		reSetupLabels("");
	}

	private Void process() {
		setupLabelsExec(gui.i18nFormat("label.cpm.uploading.connecting"));
		MojangSkinUploadAPI api = MinecraftClientAccess.get().getUploadAPI();
		if(!api.checkAuth()) {
			if(cancelled.get())return null;
			gui.executeLater(new ConfirmPopup(frame, title, gui.i18nFormat("label.cpm.uploading.authFail"), this::save, null, gui.i18nFormat("button.cpm.saveSkinFile")));
			return null;
		}
		if(cancelled.get())return null;
		setupLabelsExec(gui.i18nFormat("label.cpm.uploading.skin"));
		try {
			api.uploadSkin(type, img);
		} catch (Exception e) {
			if(cancelled.get())return null;
			Log.warn("Failed to upload skin", e);
			gui.executeLater(new ConfirmPopup(frame, title, gui.i18nFormat("label.cpm.skinUpload.fail", e.getMessage()), this::save, null, gui.i18nFormat("button.cpm.saveSkinFile")));
			return null;
		}
		gui.executeLater(new MessagePopup(frame, gui.i18nFormat("label.cpm.export_success"), gui.i18nFormat("label.cpm.skinUpload.success")));
		gui.executeLater(MinecraftClientAccess.get()::clearSkinCache);
		return null;
	}

	private void setupLabelsExec(String text) {
		gui.executeLater(() -> reSetupLabels(text));
	}

	private void reSetupLabels(String text) {
		if(labelsOld != null)labelsOld.forEach(this::remove);
		labelsOld = setupLabels(text);
	}

	private void save() {
		FileChooserPopup fc = new FileChooserPopup(frame);
		fc.setTitle(gui.i18nFormat("label.cpm.exportSkin"));
		fc.setFileDescText(gui.i18nFormat("label.cpm.file_png"));
		fc.setFilter(new FileFilter("png"));
		fc.setSaveDialog(true);
		fc.setExtAdder(n -> n + ".png");
		fc.setAccept(f -> {
			try {
				img.storeTo(f);
			} catch (IOException e) {
				Log.error("Failed to save image", e);
				frame.openPopup(new MessagePopup(frame, frame.getGui().i18nFormat("label.cpm.error"), frame.getGui().i18nFormat("error.cpm.img_save_failed", e.getLocalizedMessage())));
			}
		});
		fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
		frame.openPopup(fc);
	}
}
