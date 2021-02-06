package com.tom.cpm.shared;

import java.awt.image.BufferedImage;
import java.util.List;

import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.config.ResourceLoader;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.gui.IKeybind;
import com.tom.cpm.shared.util.DynamicTexture.ITexture;

public interface MinecraftClientAccess extends ResourceLoader {
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
	int getSkinType();
	BufferedImage getVanillaSkin(int skinType);
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
