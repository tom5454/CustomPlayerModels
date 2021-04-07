package com.tom.cpm.shared;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
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

	default Player<?, ?> getClientPlayer() {
		return getDefinitionLoader().loadPlayer(getPlayerIDObject());
	}

	Object getPlayerIDObject();
	SkinType getSkinType();
	Image getVanillaSkin(SkinType skinType);
	void setEncodedGesture(int value);
	boolean isInGame();
	List<IKeybind> getKeybinds();
	File getGameDir();
	ServerStatus getServerSideStatus();
	void sendSkinUpdate();
	void openGui(Function<IGui, Frame> creator);

	default Runnable openSingleplayer() {
		throw new UnsupportedOperationException();
	}

	public static enum ServerStatus {
		OFFLINE,
		UNAVAILABLE,
		SKIN_LAYERS_ONLY,
		INSTALLED
	}
}
