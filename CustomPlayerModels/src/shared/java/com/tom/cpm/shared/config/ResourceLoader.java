package com.tom.cpm.shared.config;

import java.io.IOException;

public interface ResourceLoader {
	byte[] loadResource(String path, ResourceEncoding enc) throws IOException;

	public static enum ResourceEncoding {
		NO_ENCODING,
		BASE64,
	}
}
