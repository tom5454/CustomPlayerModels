package com.tom.cpl.gui;

import java.util.function.Function;

import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.text.I18n;

public interface UI extends I18n {
	default void displayMessagePopup(String title, String text) {
		displayMessagePopup(title, text, i18nFormat("button.cpm.ok"));
	}

	void displayMessagePopup(String title, String text, String closeBtn);
	void executeLater(Runnable r);
	void displayPopup(Function<Frame, PopupPanel> factory);

	default void displayConfirm(String msg, Runnable ok, Runnable cancel) {
		displayConfirm(i18nFormat("label.cpm.confirm"), msg, ok, cancel);
	}

	default void displayConfirm(String title, String msg, Runnable ok, Runnable cancel) {
		displayConfirm(title, msg, ok, cancel, i18nFormat("button.cpm.ok"));
	}

	default void displayConfirm(String title, String msg, Runnable ok, Runnable cancel, String okTxt) {
		displayConfirm(title, msg, ok, cancel, okTxt, i18nFormat("button.cpm.cancel"));
	}

	void displayConfirm(String title, String msg, Runnable ok, Runnable cancel, String okTxt, String cancelTxt);

	void onGuiException(String msg, Throwable e, boolean fatal);
}
