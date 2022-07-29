package com.tom.cpm.shared.editor;

import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpm.shared.editor.gui.EditorGui;

public class TestIngameManager {
	public static final String TEST_MODEL_NAME = ".temp.cpmmodel";

	public static boolean isTesting() {
		return false;
	}

	public static void checkConfig() {
	}

	public static boolean openTestIngame(EditorGui e, boolean noPopup) {
		e.openPopup(new MessagePopup(e, e.getGui().i18nFormat("label.cpm.error"), e.getGui().i18nFormat("label.cpm.feature_unavailable")));
		return false;
	}
}
