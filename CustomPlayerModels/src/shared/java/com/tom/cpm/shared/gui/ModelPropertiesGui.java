package com.tom.cpm.shared.gui;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinition.ModelLoadingState;

public class ModelPropertiesGui extends Frame {
	private PropertiesPopup popup;
	private boolean close;

	public ModelPropertiesGui(IGui gui) {
		super(gui);
	}

	@Override
	public void initFrame(int width, int height) {
		if (popup != null)return;
		ModelDefinition def = MinecraftClientAccess.get().getCurrentClientPlayer().getModelDefinition0();
		ServerStatus status = MinecraftClientAccess.get().getServerSideStatus();

		if (def == null || def.getResolveState() != ModelLoadingState.LOADED || status != ServerStatus.INSTALLED) {
			close = true;
			return;
		}

		popup = new PropertiesPopup(gui, height * 2 / 3, def);
		popup.setOnClosed(gui::closeGui);
		openPopup(popup);
	}

	@Override
	public void tick() {
		if (close) {
			gui.closeGui();
		}
	}
}
