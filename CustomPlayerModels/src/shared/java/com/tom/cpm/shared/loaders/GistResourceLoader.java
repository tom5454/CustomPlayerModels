package com.tom.cpm.shared.loaders;

import java.io.IOException;
import java.net.URL;

public class GistResourceLoader extends HttpResourceLoader {

	@Override
	protected URL createURL(String path, ResourceEncoding enc) throws IOException {
		return new URL("https://gist.githubusercontent.com/" + path + "/raw");
	}
}
