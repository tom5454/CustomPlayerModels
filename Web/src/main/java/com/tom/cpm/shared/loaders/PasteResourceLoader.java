package com.tom.cpm.shared.loaders;

import java.io.IOException;
import java.net.URL;

import com.tom.cpm.shared.paste.PasteClient;

public class PasteResourceLoader extends HttpResourceLoader {

	@Override
	protected URL createURL(String path) throws IOException {
		return new URL(PasteClient.URL + "/raw/" + path);
	}
}
