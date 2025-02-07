package com.tom.cpm.shared.loaders;

import java.io.IOException;
import java.net.URL;

public class ModelsCDNResourceLoader extends HttpResourceLoader {

	@Override
	protected URL createURL(String path) throws IOException {
		return new URL("https://cdn-cpmmodels.tom5454.com/content/" + path);
	}
}
