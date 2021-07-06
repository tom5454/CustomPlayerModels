package com.tom.cpm.shared.editor.gui;

import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.editor.Exporter;

public class TestIngameManager {
	private static final String VANILLA_MODEL = "~~VANILLA~~";

	public static void checkConfig() {
		String old = ModConfig.getCommonConfig().getString(ConfigKeys.SELECTED_MODEL_OLD, null);
		if(old != null) {
			ModConfig.getCommonConfig().clearValue(ConfigKeys.SELECTED_MODEL_OLD);
			if(VANILLA_MODEL.equals(old))
				ModConfig.getCommonConfig().clearValue(ConfigKeys.SELECTED_MODEL);
			else
				ModConfig.getCommonConfig().setString(ConfigKeys.SELECTED_MODEL, old);
			ModConfig.getCommonConfig().save();
		}
	}

	public static boolean openTestIngame(EditorGui e) {
		if(e.getEditor().dirty) {
			e.openPopup(new MessagePopup(e, e.getGui().i18nFormat("label.cpm.error"), e.getGui().i18nFormat("label.cpm.must_save.test")));
			return false;
		}
		try {
			Runnable open;
			if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED) {
				open = () -> {
					MinecraftClientAccess.get().sendSkinUpdate();
					e.openPopup(new MessagePopup(e, e.getGui().i18nFormat("label.cpm.info"), e.getGui().i18nFormat("label.cpm.test_model_exported")));
				};
			} else if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.OFFLINE)
				open = MinecraftClientAccess.get().openSingleplayer();
			else
				throw new UnsupportedOperationException();
			if(!Exporter.exportTempModel(e.getEditor(), e))return false;
			String model = ModConfig.getCommonConfig().getString(ConfigKeys.SELECTED_MODEL, null);
			if(model != null) {
				ModConfig.getCommonConfig().setString(ConfigKeys.SELECTED_MODEL_OLD, model);
			} else {
				ModConfig.getCommonConfig().setString(ConfigKeys.SELECTED_MODEL_OLD, VANILLA_MODEL);
			}
			ModConfig.getCommonConfig().setString(ConfigKeys.SELECTED_MODEL, Exporter.TEMP_MODEL);
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
