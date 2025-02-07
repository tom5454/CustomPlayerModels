package com.tom.cpm.blockbench;

import java.util.function.Function;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.UI;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpm.blockbench.proxy.Dialog;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.web.client.util.I18n;

public class BBUI implements UI {

	@Override
	public String i18nFormat(String key, Object... obj) {
		return String.format(I18n.get(key), obj);
	}

	@Override
	public void displayMessagePopup(String title, String text, String closeBtn) {
		Dialog.DialogProperties dctr = new Dialog.DialogProperties();
		dctr.id = "ui_msg";
		dctr.title = title;
		dctr.lines = new String[] {text.replace("\\", "<br>")};
		dctr.singleButton = true;
		dctr.buttons = new String[] {closeBtn};
		new Dialog(dctr).show();
	}

	@Override
	public void executeLater(Runnable r) {
		MinecraftClientAccess.get().executeOnGameThread(() -> {
			try {
				r.run();
			} catch (Throwable e) {
				Log.error("Exception while executing task", e);
				ErrorLog.addLog(LogLevel.ERROR, "Exception while executing task", e);
			}
		});
	}

	@Override
	public void displayPopup(Function<Frame, PopupPanel> factory) {

	}

	@Override
	public void displayConfirm(String title, String msg, Runnable ok, Runnable cancel, String okTxt, String cancelTxt) {
		Dialog.DialogProperties dctr = new Dialog.DialogProperties();
		dctr.id = "ui_confirm";
		dctr.title = title;
		dctr.lines = new String[] {msg.replace("\\", "<br>")};
		dctr.buttons = new String[] {okTxt, cancelTxt};
		dctr.onConfirm = e -> {
			ok.run();
			return true;
		};
		dctr.onCancel = () -> {
			if (cancel != null)
				cancel.run();
			return true;
		};
		new Dialog(dctr).show();
	}

	@Override
	public void onGuiException(String msg, Throwable e, boolean fatal) {
		Log.error(msg, e);
		ErrorLog.addLog(LogLevel.ERROR, msg, e);
	}
}
