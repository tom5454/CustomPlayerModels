package com.tom.cpm.shared.loaders;

import java.io.IOException;
import java.net.URL;

public class GithubRepoResourceLoader extends HttpResourceLoader {

	@Override
	protected URL createURL(String path) throws IOException {
		return new URL("https://raw.githubusercontent.com/" + path);
	}
}
