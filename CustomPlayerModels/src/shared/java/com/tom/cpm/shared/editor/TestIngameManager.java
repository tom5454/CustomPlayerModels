package com.tom.cpm.shared.editor;

import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.editor.gui.EditorGui;

public class TestIngameManager {
	private static final String VANILLA_MODEL = "~~VANILLA~~";
	public static final String TEST_MODEL_NAME = ".temp.cpmmodel";

	public static boolean isTesting() {
		return TEST_MODEL_NAME.equals(ModConfig.getCommonConfig().getString(ConfigKeys.SELECTED_MODEL, null));
	}

	public static void checkConfig() {
		String old = ModConfig.getCommonConfig().getString(ConfigKeys.SELECTED_MODEL_OLD, null);
		if(MinecraftClientAccess.get().getServerSideStatus() != ServerStatus.INSTALLED) {
			ModConfig.getCommonConfig().clearValue(ConfigKeys.SELECTED_MODEL_OLD);
			String model = ModConfig.getCommonConfig().getString(ConfigKeys.SELECTED_MODEL, null);
			if(TEST_MODEL_NAME.equals(model)) {
				if(old == null || VANILLA_MODEL.equals(old))
					ModConfig.getCommonConfig().clearValue(ConfigKeys.SELECTED_MODEL);
				else
					ModConfig.getCommonConfig().setString(ConfigKeys.SELECTED_MODEL, old);
			}
			ModConfig.getCommonConfig().save();
		}
	}

	public static boolean openTestIngame(EditorGui e, boolean noPopup) {
		if(e.getEditor().dirty || e.getEditor().file == null) {
			e.openPopup(new MessagePopup(e, e.getGui().i18nFormat("label.cpm.error"), e.getGui().i18nFormat("label.cpm.must_save.test")));
			return false;
		}
		try {
			Runnable open;
			if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED) {
				open = () -> {
					MinecraftClientAccess.get().sendSkinUpdate();
					if(!noPopup)
						e.openPopup(new MessagePopup(e, e.getGui().i18nFormat("label.cpm.info"), e.getGui().i18nFormat("label.cpm.test_model_exported")));
				};
			} else if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.OFFLINE) {
				open = MinecraftClientAccess.get().openSingleplayer();
			} else {
				e.openPopup(new MessagePopup(e, e.getGui().i18nFormat("label.cpm.error"), e.getGui().i18nFormat("label.cpm.feature_unavailable")));
				return false;
			}
			if(!Exporter.exportTempModel(e.getEditor(), e))return false;
			String model = ModConfig.getCommonConfig().getString(ConfigKeys.SELECTED_MODEL, null);
			if(!TEST_MODEL_NAME.equals(model)) {
				if(model != null) {
					ModConfig.getCommonConfig().setString(ConfigKeys.SELECTED_MODEL_OLD, model);
				} else {
					ModConfig.getCommonConfig().setString(ConfigKeys.SELECTED_MODEL_OLD, VANILLA_MODEL);
				}
				ModConfig.getCommonConfig().setString(ConfigKeys.SELECTED_MODEL, TestIngameManager.TEST_MODEL_NAME);
			}
			open.run();
			ModConfig.getCommonConfig().setString(ConfigKeys.REOPEN_PROJECT, e.getEditor().file.getAbsolutePath());
			ModConfig.getCommonConfig().save();
			return true;
		} catch (UnsupportedOperationException ex) {
			e.openPopup(new MessagePopup(e, e.getGui().i18nFormat("label.cpm.error"), e.getGui().i18nFormat("label.cpm.test_unsupported")));
		}
		return false;
	}
}
