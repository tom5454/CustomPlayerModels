package com.tom.cpm.shared;

import java.util.List;

import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.util.DynamicTexture.ITexture;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.SkinType;

public interface MinecraftClientAccess {
	IPlayerRenderManager getPlayerRenderManager();
	ModelDefinitionLoader getDefinitionLoader();
	ITexture createTexture();
	void executeLater(Runnable r);

	public static MinecraftClientAccess get() {
		return MinecraftObjectHolder.clientObject;
	}

	default Player getClientPlayer() {
		return getDefinitionLoader().loadPlayer(getPlayerIDObject());
	}

	Object getPlayerIDObject();
	SkinType getSkinType();
	Image getVanillaSkin(SkinType skinType);
	void setEncodedGesture(int value);
	boolean isInGame();
	List<IKeybind> getKeybinds();

	ServerStatus getServerSideStatus();

	public static enum ServerStatus {
		OFFLINE,
		UNAVAILABLE,
		SKIN_LAYERS_ONLY,
		INSTALLED
	}
}
