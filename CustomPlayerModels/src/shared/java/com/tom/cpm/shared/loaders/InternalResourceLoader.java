package com.tom.cpm.shared.loaders;

import java.io.IOException;
import java.io.InputStream;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.ResourceLoader;

public class InternalResourceLoader implements ResourceLoader {

	@Override
	public InputStream loadResource(String path) throws IOException {
		return MinecraftClientAccess.get().loadResource("builtin/" + path + ".bin");
	}

}
