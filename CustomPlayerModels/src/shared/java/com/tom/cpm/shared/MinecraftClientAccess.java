package com.tom.cpm.shared;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.util.DynamicTexture.ITexture;
import com.tom.cpl.util.ImageIO.IImageIO;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.util.MojangSkinUploadAPI;

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

	default Player<?, ?> getCurrentClientPlayer() {
		Object v = getCurrentPlayerIDObject();
		if(v == null)return getClientPlayer();
		return getDefinitionLoader().loadPlayer(v);
	}

	Object getPlayerIDObject();
	Object getCurrentPlayerIDObject();

	SkinType getSkinType();
	void setEncodedGesture(int value);
	boolean isInGame();
	List<IKeybind> getKeybinds();
	File getGameDir();
	NetHandler<?, ?, ?, ?, ?> getNetHandler();
	void openGui(Function<IGui, Frame> creator);
	IImageIO getImageIO();
	MojangSkinUploadAPI getUploadAPI();
	void clearSkinCache();
	String getConnectedServer();
	List<Object> getPlayers();

	default Runnable openSingleplayer() {
		throw new UnsupportedOperationException();
	}

	public static enum ServerStatus {
		OFFLINE,
		UNAVAILABLE,
		SKIN_LAYERS_ONLY,
		INSTALLED
	}

	default void sendSkinUpdate() {
		getNetHandler().sendSkinData();
	}

	default void setModelScale(float scl) {
		getNetHandler().setScale(scl);
	}

	default ServerStatus getServerSideStatus() {
		return isInGame() ? getNetHandler().hasModClient() ? ServerStatus.INSTALLED : ServerStatus.SKIN_LAYERS_ONLY : ServerStatus.OFFLINE;
	}
}
