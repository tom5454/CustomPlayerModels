package com.tom.cpm.shared.loaders;

import java.io.IOException;
import java.net.URL;

public class GistResourceLoader extends HttpResourceLoader {

	@Override
	protected URL createURL(String path) throws IOException {
		return new URL("https://gist.githubusercontent.com/" + path + "/raw");
	}

	@Override
	public Validator getValidator() {
		return new Validator(
				"GitHub Gist",
				"gist.github.com",
				"https\\:\\/\\/gist\\.github\\.com\\/([a-zA-Z_0-9\\-]+)\\/(\\w+)\\#?[a-zA-Z_0-9\\-]*",
				"git:$1/$2",
				"https://gist.github.com/<name>/<gist id>");
	}
}
