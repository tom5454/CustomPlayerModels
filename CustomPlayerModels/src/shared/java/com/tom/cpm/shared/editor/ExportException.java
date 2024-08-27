package com.tom.cpm.shared.editor;

import com.tom.cpl.text.I18n;
import com.tom.cpl.text.IText;

public class ExportException extends RuntimeException {
	private static final long serialVersionUID = 3255847899314886673L;
	private IText message;

	public ExportException(IText message, Throwable cause) {
		super("", cause);
		this.message = message;
	}

	public ExportException(IText message) {
		super("");
		this.message = message;
	}

	public IText getFormattedMessage() {
		return message;
	}

	@Override
	@Deprecated
	public String getMessage() {
		return super.getMessage();
	}

	public String toString(I18n gui) {
		return gui.i18nFormat("label.cpm.export_error", message.toString(gui));
	}
}