package com.tom.cpm.shared.loaders;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.paste.PasteClient;

public class PasteResourceLoader extends HttpResourceLoader {
	private static boolean useFallback;

	@Override
	protected URL createURL(String path) throws IOException {
		return new URL((useFallback ? PasteClient.URL_CF : PasteClient.URL) + "/raw/" + path);
	}

	@Override
	public byte[] loadResource(String path, ResourceEncoding enc, ModelDefinition def) throws IOException {
		URL url = createURL(path);
		try {
			return loadResource(url, enc, def);
		} catch (SocketTimeoutException | ConnectException e) {
			if(useFallback)throw e;
			useFallback = true;
			url = createURL(path);
			try {
				return loadResource(url, enc, def);
			} catch (SocketTimeoutException e2) {
				IOException ex = new IOException("No connection", e);
				ex.addSuppressed(e2);
				throw ex;
			}
		}
	}
}
