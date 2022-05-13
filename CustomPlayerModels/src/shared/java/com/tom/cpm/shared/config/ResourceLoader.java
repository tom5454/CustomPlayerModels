package com.tom.cpm.shared.config;

import java.io.IOException;

import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinition;

public interface ResourceLoader {
	byte[] loadResource(String path, ResourceEncoding enc, ModelDefinition def) throws IOException;

	default byte[] loadResource(Link link, ResourceEncoding enc, ModelDefinition def) throws IOException {
		return loadResource(link.getPath(), enc, def);
	}

	public static enum ResourceEncoding {
		NO_ENCODING,
		BASE64,
	}
}
