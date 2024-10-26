package com.tom.cpm;

import net.minecraft.network.chat.MutableComponent;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;

import com.tom.cpm.client.GuiImpl;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.gui.SettingsGui;
import com.tom.cpm.shared.util.IVersionCheck;

public class ModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> new GuiImpl(SettingsGui::new, screen);
	}

	@Override
	public UpdateChecker getUpdateChecker() {
		return () -> {
			IVersionCheck vc = MinecraftCommonAccess.get().getVersionCheck();
			return new UpdateInfo() {

				@Override
				public boolean isUpdateAvailable() {
					return vc.isOutdated();
				}

				@Override
				public UpdateChannel getUpdateChannel() {
					return UpdateChannel.RELEASE;
				}

				@Override
				public String getDownloadLink() {
					return "https://modrinth.com/plugin/custom-player-models";
				}

				@Override
				public MutableComponent getUpdateMessage() {
					return null;
				}
			};
		};
	}
}
