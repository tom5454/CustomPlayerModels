package com.tom.cpm.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.tom.cpl.function.ToFloatFunction;
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

}
