package com.tom.cpm.shared.editor.project;

import static com.tom.cpm.shared.MinecraftObjectHolder.gson;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.tom.cpl.util.ThrowingBiConsumer;
import com.tom.cpl.util.ThrowingRunnable;

public interface ProjectWriter {
	<T> T queueSave(String path, Supplier<T> creator, ThrowingBiConsumer<T, OutputStream, IOException> save);

	default JsonMap getJson(String path) {
		return queueSave(path, JsonMapImpl::new, (data, out) -> {
			try(OutputStreamWriter os = new OutputStreamWriter(out)) {
				gson.toJson(data.asMap(), os);
			}
		});
	}

	void clearFolder(String path);
	void delete(String string);

	<T> void putFile(String path, T val, ThrowingBiConsumer<T, OutputStream, IOException> save);
	void flush() throws IOException;

	public static class Impl implements ProjectWriter {
		private final ProjectFile project;
		private Map<String, Data<?>> files = new HashMap<>();
		private List<ThrowingRunnable<IOException>> saveActions = new ArrayList<>();

		public Impl(ProjectFile project) {
			this.project = project;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T queueSave(String path, Supplier<T> creator,
				ThrowingBiConsumer<T, OutputStream, IOException> save) {
			return (T) files.computeIfAbsent(path, k -> new Data<>(creator.get(), save)).get();
		}

		@Override
		public <T> void putFile(String path, T val, ThrowingBiConsumer<T, OutputStream, IOException> save) {
			files.put(path, new Data<>(val, save));
		}

		@Override
		public void flush() throws IOException {
			for (ThrowingRunnable<IOException> a : saveActions) {
				a.run();
			}
			for(Entry<String, Data<?>> e : files.entrySet()) {
				try (OutputStream os = project.setAsStream(e.getKey())){
					e.getValue().save(os);
				}
			}
		}

		private static class Data<T> {
			private final T value;
			private final ThrowingBiConsumer<T, OutputStream, IOException> save;

			public Data(T value, ThrowingBiConsumer<T, OutputStream, IOException> save) {
				this.value = value;
				this.save = save;
			}

			public void save(OutputStream os) throws IOException {
				save.accept(value, os);
			}

			public T get() {
				return value;
			}
		}

		@Override
		public void clearFolder(String path) {
			saveActions.add(() -> project.clearFolder(path));
		}

		@Override
		public void delete(String path) {
			saveActions.add(() -> project.delete(path));
		}
	}
}
