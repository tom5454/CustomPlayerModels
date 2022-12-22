package com.tom.cpm.shared.loaders;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.tom.cpm.shared.config.ResourceLoader;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.web.client.render.ViewerGui;
import com.tom.cpm.web.client.util.AsyncResourceException;
import com.tom.cpm.web.client.util.CPMApi;

public abstract class HttpResourceLoader implements ResourceLoader {
	private static Map<String, CompletableFuture<byte[]>> data = new HashMap<>();

	protected abstract URL createURL(String path) throws IOException;

	@Override
	public byte[] loadResource(String path, ResourceEncoding enc, ModelDefinition def) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] loadResource(Link link, ResourceEncoding enc, ModelDefinition def) throws IOException {
		String path = link.toString();
		CompletableFuture<byte[]> d = data.get(path);
		if(d != null) {
			return d.getNow(null);
		}
		CompletableFuture<byte[]> cf = new CompletableFuture<>();
		ViewerGui.addBgLoad(cf);
		data.put(path, cf);
		CPMApi.fetch("link", path).then(v -> {
			String dt = (String) v.get("data");
			if(enc == ResourceEncoding.NO_ENCODING)
				cf.complete(dt.getBytes());
			else if(enc == ResourceEncoding.BASE64)
				cf.complete(Base64.getDecoder().decode(dt));
			else
				cf.completeExceptionally(new IOException("Unsupported file encoding"));
			return null;
		}).catch_(e -> {
			cf.completeExceptionally(new IOException("Failed to load linked resource: " + String.valueOf(e)));
			return null;
		});
		throw new AsyncResourceException();
	}
}
