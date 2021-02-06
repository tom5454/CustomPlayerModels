package com.tom.cpm.shared.config;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceLoader {
	InputStream loadResource(String path) throws IOException;
}
