package com.tom.cpm.api;

import java.util.function.BiConsumer;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.io.ModelFile;

public class CommonApi implements ICommonAPI {

	public void callInit(ICPMPlugin plugin) {
	}

	public CommonApi() {
	}

	public static class ApiBuilder {

		protected ApiBuilder(CPMApiManager api) {
			api.common = new CommonApi();
		}
	}

	@Override
	public <P> void setPlayerModel(Class<P> playerClass, P player, String b64, boolean forced, boolean persistent) {
	}

	@Override
	public <P> void setPlayerModel(Class<P> playerClass, P player, ModelFile model, boolean forced) {
	}

	@Override
	public <P> void resetPlayerModel(Class<P> playerClass, P player) {
	}

	@Override
	public <P> void playerJumped(Class<P> playerClass, P player) {
	}

	@Override
	public <P> void playAnimation(Class<P> playerClass, P player, String name, int value) {
	}

	@Override
	public <P> void playAnimation(Class<P> playerClass, P player, String name) {
	}

	@Override
	public <P> MessageSender<P> registerPluginMessage(Class<P> clazz, String messageId,
			BiConsumer<P, NBTTagCompound> handler) {
		return null;
	}

	public void handlePacket(String id, NBTTagCompound tag, Object player) {
	}

	@Override
	public <P> int getAnimationPlaying(Class<P> playerClass, P player, String name) {
		return -1;
	}
}
