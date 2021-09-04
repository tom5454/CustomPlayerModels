package com.tom.cpm.shared.editor.project;

import static com.tom.cpm.shared.MinecraftObjectHolder.gson;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.tom.cpl.util.ThrowingConsumer;
import com.tom.cpl.util.ThrowingFunction;

public interface IProject {
	byte[] getEntry(String path);
	List<String> listEntires(String path);

	default InputStream getAsStream(String path) throws IOException {
		byte[] dt = getEntry(path);
		if(dt == null)throw new FileNotFoundException();
		return new ByteArrayInputStream(dt);
	}

	@SuppressWarnings("unchecked")
	default JsonMap toJson(InputStreamReader rd) {
		return new JsonMapImpl((Map<String, Object>) gson.fromJson(rd, Object.class));
	}

	default JsonMap getJson(String path) throws IOException {
		try(InputStreamReader rd = new InputStreamReader(getAsStream(path))) {
			return toJson(rd);
		}
	}

	default <E extends Throwable> void jsonIfExists(String path, ThrowingConsumer<JsonMap, E> e) throws IOException, E {
		byte[] ze = getEntry(path);
		if(ze != null) {
			try(InputStreamReader rd = new InputStreamReader(new ByteArrayInputStream(ze))) {
				e.accept(toJson(rd));
			}
		}
	}

	default <R, E extends Throwable> void ifExists(String path, ThrowingFunction<InputStream, R, E> loader, Consumer<R> e) throws E {
		byte[] ze = getEntry(path);
		if(ze != null)
			e.accept(loader.apply(new ByteArrayInputStream(ze)));
	}

	default <R, E extends Throwable> R getIfExists(String path, ThrowingFunction<InputStream, R, E> e) throws E {
		byte[] ze = getEntry(path);
		if(ze != null)
			return e.apply(new ByteArrayInputStream(ze));
		else
			return null;
	}
}
