package com.tom.cpm.shared.config;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;

public interface ResourceLoader {
	byte[] loadResource(String path, ResourceEncoding enc, ModelDefinition def) throws IOException;

	public static enum ResourceEncoding {
		NO_ENCODING,
		BASE64,
	}
}
