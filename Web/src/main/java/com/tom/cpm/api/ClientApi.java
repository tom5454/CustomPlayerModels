package com.tom.cpm.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.tom.cpl.function.ToFloatFunction;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.editor.gui.EditorGui;

public class ClientApi implements IClientAPI {

	public ClientApi() {
	}

	public void callInit(ICPMPlugin plugin) {
	}

	@Override
	public <T> void registerVoice(Class<T> clazz, Function<T, Float> getVoiceLevel) {
	}

	public static class ApiBuilder {
		protected ApiBuilder(CPMApiManager api) {
			api.client = new ClientApi();
		}
	}

	public List<ToFloatFunction<Object>> getVoiceProviders() {
		return Collections.emptyList();
	}

	public List<Predicate<Object>> getVoiceMutedProviders() {
		return Collections.emptyList();
	}

	@Override
	public <HM, RL, RT, MBS, GP> PlayerRenderer<HM, RL, RT, MBS, GP> createPlayerRenderer(Class<HM> humanoidModelClass,
			Class<RL> resourceLocationClass, Class<RT> renderTypeClass, Class<MBS> multiBufferSourceClass, Class<GP> gameProfileClass) {
		return null;
	}

	@Override
	public LocalModel loadModel(String name, InputStream is) throws IOException {
		return null;
	}

	@Override
	public void registerEditorGenerator(String name, String tooltip, Consumer<EditorGui> func) {
	}

	@Override
	public void registerVoice(Function<UUID, Float> getVoiceLevel) {
	}

	@Override
	public <T> void registerVoiceMute(Class<T> clazz, Predicate<T> getMuted) {
	}

	@Override
	public void registerVoiceMute(Predicate<UUID> getMuted) {
	}

	@Override
	public boolean playAnimation(String name, int value) {
		return false;
	}

	@Override
	public boolean playAnimation(String name) {
		return false;
	}

	@Override
	public <T> MessageSender registerPluginMessage(Class<T> clazz, String messageId, BiConsumer<T, NBTTagCompound> handler, boolean broadcastToTracking) {
		return null;
	}

	@Override
	public MessageSender registerPluginMessage(String messageId, BiConsumer<UUID, NBTTagCompound> handler, boolean broadcastToTracking) {
		return null;
	}

	public void handlePacket(String id, NBTTagCompound tag, Object player) {
	}

	@Override
	public <P> MessageSender registerPluginStateMessage(Class<P> clazz, String messageId,
			BiConsumer<P, NBTTagCompound> handler) {
		return null;
	}

	@Override
	public MessageSender registerPluginStateMessage(String messageId, BiConsumer<UUID, NBTTagCompound> handler) {
		return null;
	}

	@Override
	public int getAnimationPlaying(String name) {
		return -1;
	}

	@Override
	public int getAnimationMaxValue(String name) {
		return -1;
	}
}
