package com.tom.cpm.shared.loaders;

import java.io.IOException;
import java.net.URL;

public class GithubRepoResourceLoader extends HttpResourceLoader {

	@Override
	protected URL createURL(String path) throws IOException {
		return new URL("https://raw.githubusercontent.com/" + path);
	}

	@Override
	public Validator getValidator() {
		return new Validator(
				"GitHub Repo File",
				"github.com",
				"https\\:\\/\\/github\\.com\\/([a-zA-Z_0-9\\-]+)\\/([a-zA-Z_0-9\\-]+)\\/blob\\/([a-zA-Z_0-9\\-\\/]+)\\/([a-zA-Z_0-9\\-\\/\\.]+)\\#?[a-zA-Z_0-9\\-]*",
				"gh:$1/$2/$3/$4",
				"https://github.com/<username>/<repo>/blob/<branch>/<path>");
	}
}
