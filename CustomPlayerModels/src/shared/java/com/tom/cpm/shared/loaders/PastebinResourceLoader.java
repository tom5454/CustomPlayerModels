package com.tom.cpm.shared.loaders;

import java.io.IOException;
import java.net.URL;

public class PastebinResourceLoader extends HttpResourceLoader {

	@Override
	protected URL createURL(String path) throws IOException {
		return new URL("https://pastebin.com/raw/" + path);
	}

	@Override
	public Validator getValidator() {
		return new Validator(
				"Pastebin",
				"pastebin.com",
				"https\\:\\/\\/pastebin\\.com\\/([a-zA-Z_0-9\\-]+)",
				"pb:$1",
				"https://pastebin.com/<paste id>");
	}
}
